
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
itemName = 'geospiza_tree.phy'

# get a listings of the collections at this Arbor instance
collectionRresponse = requests.get(arborAPIlocation+'collection')
#print resp.json()

# find a particular collection from the list of all collections
for coll in collectionRresponse.json():
    if (coll['name'] == collectionName):
        collectionId = coll['_id']
        print "found the collection we were looking for: collectionID:",collectionId
        break
            
folderResponse = requests.get(arborAPIlocation+'folder?parentType=collection&parentId='+collectionId)
#print datafolderresp.json()

# Datasets are stored inside a Data folder inside of each Arbor collection, so we want to get 
# a pointer to the Data folder

for folder in folderResponse.json():
    if (folder['name'] == 'Data'):
        folderId = folder['_id']
        print "found the Data folder inside the collection: folderID:",collectionId
        break
        
# loop through the 'Data' folder to find all the contained items in it.  All datasets (trees, characters, etc)
# can be intermixed inside a collection.  The basic loop will return all datasets

dataitemsresp = requests.get(arborAPIlocation+'item?folderId='+folderId)
itemNames = []
for item in dataitemsresp.json():
    itemNames.append(item['name'])
print "Here are the items in the Data folder:",itemNames

# We know  'anolis.phy' is one of the objects stored in this collection, lets try to retrieve it as a Newick file
# find this particular item by name inside the Data folder
print 'looking up the item ID for a particular item....'
dataitemsresp = requests.get(arborAPIlocation+'item?folderId='+folderId)
for item in dataitemsresp.json():
        if (item['name'] == itemName):
                itemId = item['_id']
                print "found itemId:",itemId
                break

# request the tree content in its json.nested for
try:
        treeresp = requests.get(arborAPIlocation+'item/'+itemId+'/flow/tree/nested/nested');
        print 'found a tree we were looking for by name.  Here is the downloaded tree contents:'
        print treeresp.json()['data']
except ValueError:
        response['error'] = "Search for tree unsuccessful"
        print bson.json_util.dumps(response)