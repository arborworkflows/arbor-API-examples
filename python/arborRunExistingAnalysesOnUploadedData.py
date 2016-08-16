#
# Find data that is already loaded in Arbor and run

import requests
import json


arborURL = "http://52.204.46.78"
arborAPIlocation = arborURL+"/girder/api/v1/"
print 'using arbor API URL: ',arborAPIlocation

collectionName = 'api-testing'

# Find the Analyses folder within the api-testing collection
resp = requests.get(arborAPIlocation + 'resource/lookup',
                    params={'path': '/collection/' + collectionName + '/Analyses'})
folderId = resp.json()['_id']

# loop through the 'Analyses' folder to find all the contained items in it
dataitemsresp = requests.get(arborAPIlocation+'item?folderId='+folderId)
itemNames = []
for item in dataitemsresp.json():
    itemNames.append(item['name'])
print "analyses found inside the collection: ",itemNames

# We now know that 'AggregateTableByAverage' is one of the objects stored in this collection, lets try to retrieve it

# find this particular item by name inside the Data folder
itemName = 'Aggregate Table By Average'
dataitemsresp = requests.get(arborAPIlocation+'item?folderId='+folderId)
for item in dataitemsresp.json():
        itemId = '0'
        if (item['name'] == itemName):
                itemId = item['_id']
                #print "found itemId:",itemId
                break

# return a pointer to the analysis we want to call.  We looked it up by collection
try:
        analysisID = '0'
        analysisresp = requests.get(arborAPIlocation+'item/'+itemId)
        if '_id' in analysisresp.json():
            analysisID = analysisresp.json()['_id']
            print 'found analysis! Here is the ID:',analysisID
except ValueError:
        # response and bson are both undefined
        response['error'] = "Search for analysis unsuccessful"
        print bson.json_util.dumps(response)

# *********** found analysis; now lets retrieve the input dataset already loaded in Arbor

resp = requests.get(arborAPIlocation + 'resource/lookup',
                    params={'path': '/collection/' + collectionName + '/Data'})
folderId = resp.json()['_id']
print "found folderID:",folderId


# loop through the 'Analyses' folder to find all the contained items in it
dataitemsresp = requests.get(arborAPIlocation+'item?folderId='+folderId)
itemNames = []
for item in dataitemsresp.json():
    itemNames.append(item['name'])
print itemNames

# We now know the name of the dataset to download, lets try to retrieve it

# find this particular item by name inside the Data folder
itemName = 'anolisDataAppended.csv'
resp = requests.get(arborAPIlocation + 'resource/lookup',
                    params={'path': '/collection/' + collectionName + '/Data/' + itemName})
itemId = resp.json()['_id']
print "found dataset itemId:",itemId


# we can download the dataset as a CSV, since it was loaded that way
# or we can convert it to table:rows.

tableResponse = requests.get(arborAPIlocation+'item/'+itemId+'/flow/table/csv/rows')
datatable = tableResponse.json()["data"]


# *********** now try to execute this analysis by passing it the "flow" parameter along with a parameter description block

username = 'api-testing'
password = 'api-testing'
usertoken = requests.get(arborAPIlocation+'user/authentication', auth=(username,password))
#print usertoken.text

authenticationHeader = {'Girder-Token': usertoken.json()['authToken']['token']}

# build a JSON spec that will pass the Anolis character data and a column selector (we are using
# the 'island' column name )

task = {
    "inputs" : {
        "table":  {"type": "table",  "format": "rows",   "data": datatable},
        "column": {"type": "string", "format": "text",   "data": "island"}
    },
    "outputs": {
    "output":  {"type": "table",  "format": "rows"}
    }
}

#print ''
#print 'Input Parameters:'
#print inputs

print task

resultPromise = requests.post(arborAPIlocation+'item/'+analysisID+'/flow',data=json.dumps(task), headers=authenticationHeader)
jobId = resultPromise.json()['_id']
print resultPromise.json()

# Assuming the job has finished, retrieve the output
print requests.get(arborAPIlocation + 'item/' + analysisID + '/flow/' + jobId + '/result', headers=authenticationHeader).json()
