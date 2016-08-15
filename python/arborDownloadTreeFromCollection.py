
# example interaction with a public Arbor instance using the Authenticated API to access datasets and analyses
# hosted in a public Arbor instance

# use HTTP layer package
import requests
import json

# setup the basic URLs to access Arbor.  There is a storage and execution engine (called Girder) that handles
# the dataset and analysis storage inside Arbor.  Use the API for this storage engine:

arborURL = "http://52.204.46.78"
arborAPIlocation = arborURL+"/girder/api/v1/"
collectionName = "api-testing"
itemName = 'anolis.phy'


# find a particular collection from the list of all collections
for coll in resp.json():
    if (coll['name'] == collectionName):
        collectionId = coll['_id']
        print "found collectionID:",collectionId
        break
            
datafolderresp = requests.get(arborAPIlocation+'folder?parentType=collection&parentId='+collectionId,headers=girderheader)
#print datafolderresp.json()

# find Data folder inside named collection
for folder in datafolderresp.json():
    if (folder['name'] == 'Data'):
        folderId = folder['_id']
        print "found folderID:",collectionId
        break
        
# loop through the 'Data' folder to find all the contained items in it
dataitemsresp = requests.get(arborAPIlocation+'item?folderId='+folderId)
itemNames = []
for item in dataitemsresp.json():
    itemNames.append(item['name'])
print itemNames

# We now know that 'anolis.phy' is one of the objects stored in this collection, lets try to retrieve it as a Newick file

# find this particular item by name inside the Data folder

dataitemsresp = requests.get(arborAPIlocation+'item?folderId='+folderId)
for item in dataitemsresp.json():
        if (item['name'] == itemName):
                itemId = item['_id']
                print "found itemId:",itemId
                break

# request the tree content in its json.nested for
try:
        treeresp = requests.get(arborAPIlocation+'item/'+itemId+'/flow/tree/nested/nested');
        print 'found tree! Here is the downloaded contents:'
        print treeresp.json()['data']
except ValueError:
        response['error'] = "Search for tree unsuccessful"
        print bson.json_util.dumps(response)