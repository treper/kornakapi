#API Specification

request recommendations for a user:

`recommend?recommender=<>&userID=<>&howMany=<>&label=<>`

request recommendations for an anonymous user:

`recommend?recommender=<>&itemIDs=<>&howMany=<>&label=<>`

add a single preference:

`setPreference?userID=<>&itemID=<>&value=<>`

add a preferences in batch via POSTing a file:

`batchSetPreferences?batchSize=<>`

manually train a recommender:

`train?recommender=<>`

add an item to a labeled candidate set:

`addCandidate?label=<>&itemID=<>`

add items to labeled candidate sets in batch via POSTing a file:

`batchAddCandidates?batchSize=<>`

remove an item from a labeled candidate set:

`deleteCandidate?label=<>&itemID=<>`

remove items from labeled candidate sets in batch via POSTing a file:

`batchDeleteCandidates?batchSize=<>`