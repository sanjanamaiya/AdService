# AdService
Suggest ads based on keyword extraction

Usage: java -jar AdIndexer.jar <location of ads file> <output location> <shouldIndex=true|false>

For example,
java -jar AdIndexer.jar C:\\test\\metadata.json C:\\test\\output shouldIndex=true

The metadata.json file consists of the Amazon product ads . This file is several GBs.
The output file location should contain two folders, test_e2e and test_e2e_keywords, which contain the BBC news articles, and the labelled keywords for the articles respectively. Contact smaiya@ucsc.edu to obtain all the data to run the program.

If shouldIndex=true, then the index for the ads is created in a folder "index" under the output location. This parameter should be set to "true" only the first time the program is run, and for subsequent runs, "shouldIndex=false" is used. Once the index is created, the search uses the indexed files to perform the retrieval.

In the output folder, a folder called results_e2e will be created, in which the results of the test will be written in 2 csv files. results_e2e_test.csv is the file which contains the end-to-end results from the ad system. results_e2e_labeled.csv contains the results of ad retrieval using the labelled keywords from news articles
