aggregateAnalysisName = "AggregateTableByAverage"
typejson = ["item"]
payload = {'q': "AggregateTableByAverage",'types': ["item"]}
print payload
# find the analysis
#analysisresp = requests.get('https://data.kitware.com:443/api/v1/resource/search?q=%22lisle%22&mode=text&types=%5B%22user%22%5D&level=0&limit=10&offset=0')
analysisresp = requests.get(girderlocation+'/api/v1/resource/search', data=payload);
# can't remember if the .json() call is necessary or not.... 
#aggregateAnalysis = analysisresp.json()["item"][0]['_id'];
print analysisresp.text
