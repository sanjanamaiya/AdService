package com.ir.project.adservice.adindex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler.Builder;
import com.ir.project.adservice.common.Constants;
import com.ir.project.adservice.jsonFiles.ProductInfo;
import com.ir.project.adservice.keywordextract.KeywordExtraction;
import com.ir.project.adservice.search.LuceneSearch;

import edu.ehu.galan.rake.model.Term;


public class AdIndexerMain {

	private static final Logger LOGGER = Logger.getLogger(AdIndexerMain.class.getName());
	public static void main(String[] args) 
	{
		Handler fileHandler  = null;
		try
		{
			fileHandler  = new FileHandler("./indexer_output.log");
			LOGGER.addHandler(fileHandler);
			fileHandler.setLevel(Level.ALL);
			LOGGER.setLevel(Level.ALL);
		}
		catch (IOException e)
		{
			LOGGER.log(Level.WARNING, "Error initializing file handler for logger");
		}
		
		LOGGER.log(Level.FINE, "Building index for Ads");
		boolean indexSuccess = false;

		
		// Aside
		/*try {
			testJson();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		long startTime = System.currentTimeMillis();
		String amazonProductsFileLocation = null;
		String outputFileLocation = null;
        
		if (args.length > 0 && args[0].equals("--help"))
		{
			System.out.println("Usage: java -jar AdIndexer.jar <location of ads file> <output location> <shouldIndex=true|false>");
			System.out.println("For example: ");
			System.out.println("java -jar AdIndexer.jar C:\\test\\metadata.json C:\\test\\output shouldIndex=true");
			return;
		}
		if (args.length != 3)
		{
		    System.out.println("Needs three arguments: AdFileLocation, OutputLocation, shouldIndex=true|false");
		    System.out.println("Usage: java -jar AdIndexer.jar <location of ads file> <output location> <shouldIndex=true|false>");
			System.out.println("For example: ");
			System.out.println("java -jar AdIndexer.jar C:\\test\\metadata.json C:\\test\\output shouldIndex=true");
		    return;
		}
		
		amazonProductsFileLocation = args[0]; //"D:\\downloads_new\\data\\metadata.json";//;
		outputFileLocation  = args[1];
		
		boolean shouldIndex = false;
		String shouldIndexString = args[2];
		if (shouldIndexString.contains("shouldIndex") && shouldIndexString.substring(12).equalsIgnoreCase("true"))
		{
			shouldIndex = true;
		}
		
		if (shouldIndex)
		{
			if (!amazonProductsFileLocation.isEmpty()
					&& !outputFileLocation.isEmpty()) 
			{
				indexSuccess = indexInputFile(amazonProductsFileLocation, outputFileLocation + File.separator + "index");
			} 
			else 
			{
				System.out.println("Unable to locate the ad file, exiting");
				LOGGER.log(Level.WARNING, "Unable to locate the ad file, exiting");
				return;
			}
		}
		
		
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Running time for reading and indexing ads: " + totalTime);
		
		LuceneSearch search = null;
		try 
		{
			search = new LuceneSearch(outputFileLocation + File.separator + "index");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return;
		}
		TopDocs topDocsForArticles = null;
		TopDocs topDocsForLabeledKeywords = null;
		
		KeywordExtraction keywordExtractor = new KeywordExtraction();
		String dataDir = outputFileLocation + File.separator + "test_e2e";
		String keywordsDir = outputFileLocation + File.separator +  "test_e2e_keywords";
		File topFolder = new File(dataDir);
		File[] listOfFolders = topFolder.listFiles();  // folders business, entertainment etc
		for (File file : listOfFolders) 
		{
			if (file.getAbsolutePath().endsWith(".key"))
				continue;
			
			List<Term> terms = keywordExtractor.extractKeywords(file.getAbsolutePath());
			String keywords = createKeywordList(terms);
			if (keywords.isEmpty())
			{
				// no ads suggested
				continue;
			}
			
			String labeledKeywords = getKeywords(keywordsDir + File.separator + file.getName().substring(0, file.getName().length() - 4) + ".key");
			evaluateKeywords(labeledKeywords, keywords);
			//Writekeywords(file.getAbsolutePath(), keywords);
			try 
			{
				topDocsForArticles = search.search(keywords);
			} 
			catch (IOException | ParseException e) 
			{
				e.printStackTrace();
			}

			if (topDocsForArticles != null)
			{
				StringBuilder resultsForKeywords = new StringBuilder();
				boolean atLeastOneAd = false;
				for (int i = 0; i < 3; i++) 
				{
					ScoreDoc[] hits = topDocsForArticles.scoreDocs;
					System.out.println("-----------------------------------------------------");
					System.out.println("keywords: " + keywords);
					System.out.println("-----------------------------------------------------");
					Document doc = null;
					try 
					{
						doc = search.getDocument(hits[i].doc);
						System.out.println(doc.get(Constants.DOCID) + doc.get(Constants.CONTENTS) + " " + hits[i].score);
						
						if (hits[i].score > 0.5)
						{
							atLeastOneAd = true;
							String adString = doc.get(Constants.CONTENTS);
							resultsForKeywords.append(",\"" + adString.replace("\"", "\"\"") + "\"");
						}
						else if (atLeastOneAd)
						{
							resultsForKeywords.append(",\" No Ad Displayed \"");
						}
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
				if (atLeastOneAd)
				{
					WriteResultToFile(resultsForKeywords.toString(), file, false);
				}
			}
			
			try 
			{
				topDocsForLabeledKeywords = search.search(labeledKeywords);
			} 
			catch (IOException | ParseException e) 
			{
				e.printStackTrace();
			}
			
			if (topDocsForLabeledKeywords != null)
			{
				StringBuilder results = new StringBuilder();
				boolean atLeastOneAd = false;
				for (int i = 0; i < 3; i++) 
				{
					ScoreDoc[] hits = topDocsForLabeledKeywords.scoreDocs;
					System.out.println("-----------------------------------------------------");
					System.out.println("labeled keywords: " + labeledKeywords);
					System.out.println("-----------------------------------------------------");
					Document doc = null;
					try 
					{
						doc = search.getDocument(hits[i].doc);
						System.out.println(doc.get(Constants.DOCID) + doc.get(Constants.CONTENTS) + " " + hits[i].score);
						
						if (hits[i].score > 0.5)
						{
							atLeastOneAd = true;
							String adString = doc.get(Constants.CONTENTS);
							results.append(",\"" + adString.replace("\"", "\"\"") + "\"");
						}
						else if (atLeastOneAd)
						{
							results.append(",\" No Ad Displayed \"");
						}
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
				if (atLeastOneAd)
				{
					WriteResultToFile(results.toString(), file, true);
				}
			}
		}
		
		/// Testing
		
		/*
		//KeywordExtraction keywordExtractor = new KeywordExtraction();
		String dataDir = "D:\\downloads_new\\bbc-fulltext\\test_e2e";
		File topFolder = new File(dataDir);
		File[] listOfFolders = topFolder.listFiles();  // folders business, entertainment etc
		for (File file : listOfFolders) 
		{
			if (file.getAbsolutePath().endsWith(".txt") || file.getAbsolutePath().endsWith(".result"))
				continue;
			File keyWordFile = new File(file.getAbsolutePath() + ".key");
			if (keyWordFile.exists())
			{
				continue;
			}
			//List<Term> terms = keywordExtractor.extractKeywords(file.getAbsolutePath());
			//String keywords = terms.get(0).getTerm() + " " + terms.get(1).getTerm();
			//evaluateKeywords(file.getAbsolutePath(), keywords);
			//Writekeywords(file.getAbsolutePath(), keywords);
			
			String keywords = getKeywords(file);
			try 
			{
				topDocs = search.search(keywords);
			} 
			catch (IOException | ParseException e) 
			{
				e.printStackTrace();
			}

			if (topDocs != null)
			{
				ScoreDoc[] hits = topDocs.scoreDocs;
				StringBuilder results = new StringBuilder();
				// System.out.println("Number of hits : " + hits.length);
				for (int i = 0; i < 5; i++) 
				{
					Document doc = null;
					try 
					{
						doc = search.getDocument(hits[i].doc);
						System.out.println(doc.get(Constants.DOCID) + doc.get(Constants.CONTENTS));
						results.append(doc.get(Constants.DOCID) + " " + doc.get(Constants.CONTENTS) + "\n");
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
				
				String resultsFileName = file.getAbsolutePath().endsWith(".key") ? file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4) + ".result" :
					file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 6) + ".result";
						
				WriteResultsToFile(resultsFileName, results.toString(), keywords, file.getAbsolutePath());
			}
		}*/
		
		float precision = (float) totalRelevantKeywordsRetrieved/totalKeywordsRetrieved;
		float recall = (float) totalRelevantKeywordsRetrieved/totalRelevantKeywords;
		float f1Measure = (2 * precision * recall)/ (precision + recall);
		System.out.println("Precision of keywords: " + precision);
		System.out.println("Precision of keywords: " + recall);
		System.out.println("f1 measure : " + f1Measure);
	}
	

	private static void WriteResultToFile(String ads, File articleFile, boolean isLabeledData) 
	{
		String folderName = new File(articleFile.getParent()).getParent() + File.separator + "results_e2e";
		File folder = new File(folderName);
		if (!folder.exists())
		{
			folder.mkdir();
		}
		
		String resultsFileName = folderName + File.separator + ((isLabeledData) ? "results_e2e_labeled.csv" : "results_e2e_test.csv");
		BufferedWriter writer = null;
		BufferedReader reader = null;
		try
		{
			writer = new BufferedWriter(new FileWriter(resultsFileName, true));
			reader = new BufferedReader(new FileReader(articleFile));
			String line = null;
			StringBuffer buffer = new StringBuffer();
			while ((line = reader.readLine()) != null)
			{
				buffer.append(line + "\n");
			}
			
			String modifiedString = buffer.toString().replace("\n", "<br>\n");
			modifiedString = "\"" + modifiedString.replace("\"", "\"\"") + "\""; 
			writer.write(modifiedString);
			
			writer.write(ads + "\n");
		}
		catch(Exception e)
		{
			
		}
		finally
		{
			if (writer != null)
			{
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (reader != null)
			{
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}

	
	public static String createKeywordList(List<Term> terms)
	{
		StringBuilder keywords = new StringBuilder();
		List<String> keywordList = new ArrayList<String>();
		for (Term term : terms) 
		{
			if (keywordList.size() > 10)
			{
				break;
			}
			String rankedTerms = term.getTerm();
			String[] termList = rankedTerms.split("\\s+");
			for (String string : termList) 
			{
				if (string.isEmpty())
					continue;
				if (!keywordList.contains(string.trim().toLowerCase()))
				{
					String keywordCand = string.toLowerCase().trim();
					if (Constants.stopWords.contains(keywordCand))
					{
						return "";
					}
					keywordList.add(keywordCand);
					keywords.append(" " + keywordCand);
				}
			}
		}
		
		return keywords.toString();
	}
	
	private static void WriteResultsToFile(String resutsfile, String results, String keywords, String keywordFileName) 
	{
		BufferedWriter writer = null;
		try
		{
			writer = new BufferedWriter(new FileWriter(resutsfile, true));
			writer.write(keywordFileName);
			writer.write("\n");
			writer.write(keywords);
			writer.write("\n");
			writer.write(results);
		}
		catch (Throwable e)
		{
			
		}
		finally
		{
			if (writer != null)
			{
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}


	private static String getKeywords(String keywordFileName) 
	{
		String keywords = "";
		File file = new File(keywordFileName);
		if (file.exists())
		{
			BufferedReader reader = null;
			try
			{
				reader = new BufferedReader(new FileReader(file));
				if (reader != null)
				{
					keywords = reader.readLine();
				}
			}
			catch(Exception e)
			{
				System.out.println("exception writing keywords to file: " + e);
			}
			
			finally
			{
				if (reader != null)
				{
					try {
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		
		}
		
		keywords = keywords.replace(",", " ");
		keywords = keywords.toLowerCase();
		return keywords;
	}


	private static void Writekeywords(String articleFile, String keywords) 
	{
		String keyWordFileName = articleFile.substring(0, articleFile.length() - 4) + ".mykey";
		BufferedWriter writer = null;
		try
		{
			writer = new BufferedWriter(new FileWriter(keyWordFileName));
			writer.write(keywords);
		}
		catch (Throwable e)
		{
			
		}
		finally
		{
			if (writer != null)
			{
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}


	private static int totalKeywordsRetrieved = 0;
	private static int totalRelevantKeywords = 0;
	private static int totalRelevantKeywordsRetrieved = 0;
	private static void evaluateKeywords(String labeledKeywords, String keywords) 
	{
		
		String[] labeledList = labeledKeywords.split("\\s+");
		String[] retrievedList = keywords.split(" ");
		
		totalRelevantKeywords = totalRelevantKeywords + labeledList.length;
		totalKeywordsRetrieved = totalKeywordsRetrieved + retrievedList.length;
		for (String realString : labeledList) 
		{
			realString = realString.trim();
			for (String retrievedString : retrievedList) 
			{
				retrievedString = retrievedString.trim();
				if (retrievedString.equalsIgnoreCase(realString))
				{
					totalRelevantKeywordsRetrieved ++;
				}
			}
		}
		
		/*String keyWordFileName = articleFile.substring(0, articleFile.length() - 4) + ".key";
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(keyWordFileName));
			if (reader != null)
			{
				String labeledKeywords = reader.readLine();
				
			}
		}
		catch(Exception e)
		{
			System.out.println("exception writing keywords to file: " + e);
		}
		
		finally
		{
			if (reader != null)
			{
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}*/
		
	}


	private static boolean indexInputFile(String adFileLocation, String indexOutputLocation) 
	{
		boolean indexSuccess = false;;
		try
		{
			File docFile = new File(adFileLocation);
			if (!docFile.exists())
			{
				System.out.println("The specified file " + adFileLocation + " does not exist, exiting");
			}
			else
			{
				Indexer parser = new Indexer();
				parser.indexFile(adFileLocation, indexOutputLocation);
				indexSuccess = true;
			}
		}
		catch(Throwable e)
		{
			System.out.println("Error building the index: " + e);
			LOGGER.warning("Error building the index: " + e);
		}
		return indexSuccess;
	}
	
	private static void testJson() throws IOException 
	{
		byte[] jsonData = Files.readAllBytes(Paths.get("D:\\downloads_new\\data\\jsontest.txt"));
		ObjectMapper objectMapper = new ObjectMapper();
		try 
		{
			ProductInfo product = objectMapper.readValue(jsonData, ProductInfo.class);
			StringBuilder document = new StringBuilder();
			document = new StringBuilder();
			document.append(product.getTitle());
			document.append(" ");
			if (product.getBrand() != null)
			{
				document.append(product.getBrand());
				document.append(" ");
			}
			
			if (product.getCategories() != null)
			{
				for (String[] catArray : product.getCategories()) 
				{
					for (String cat : catArray) 
					{
						document.append(" " + cat);
					}
				}
			}
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
