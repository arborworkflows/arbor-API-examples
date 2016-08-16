import json
import requests


arborURL = "http://52.204.46.78"
arborAPIlocation = arborURL+"/girder/api/v1/"
print 'using arbor API URL: ',arborAPIlocation

# look for a particular analysis by name
analysisName = "Aggregate table by average"


typejson = ["item"]
payload = {'q': analysisName,'types': json.dumps(["item"])}
#print payload

# find the analysis
#analysisresp = requests.get('https://data.kitware.com:443/api/v1/resource/search?q=%22lisle%22&mode=text&types=%5B%22user%22%5D&level=0&limit=10&offset=0')

analysisResponse = requests.get(arborAPIlocation+'resource/search', params=payload)

# can't remember if the .json() call is necessary or not....
#aggregateAnalysis = analysisresp.json()["item"][0]['_id'];

print analysisResponse.json()
