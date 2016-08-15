
# example interaction with a public Arbor instance using the Authenticated API to access datasets and analyses
# hosted in a public Arbor instance

# use HTTP layer package
import requests

# setup the basic URLs to access Arbor.  There is a storage and execution engine (called Girder) that handles
# the dataset and analysis storage inside Arbor.  Use the API for this storage engine:

arborURL = "http://52.204.46.78"
arborAPIlocation = arborURL+"/girder/api/v1/"

# replace this with an actual username and password in order to accesss private data

username = 'api-testing'
password = 'api-testing'


# Request to authenticate with Arbor and print out the token received.
# This should be passed along to later calls to access private collections.
# The authentication is not needed for access to public collections

usertoken = requests.get(arborAPIlocation+'user/authentication', auth=(username,password))
print usertoken.json()




