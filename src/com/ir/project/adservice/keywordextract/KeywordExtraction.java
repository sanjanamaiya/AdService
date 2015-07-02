package com.ir.project.adservice.keywordextract;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import LBJ2.nlp.Word;
import LBJ2.nlp.WordSplitter;
import LBJ2.nlp.SentenceSplitter;
import LBJ2.nlp.seg.PlainToTokenParser;
import LBJ2.parse.Parser;
import com.ir.project.adservice.keywordextract.rake.RakeAlgorithm;
import edu.ehu.galan.rake.model.Document;
import edu.ehu.galan.rake.model.Term;
import edu.ehu.galan.rake.model.Token;
import edu.illinois.cs.cogcomp.lbj.chunk.Chunker;
import edu.illinois.cs.cogcomp.lbj.pos.POSTagger;

public class KeywordExtraction 
{
	/*private String textFileLocation = ""; 
	public KeywordExtraction(String fileName)
	{
		textFileLocation = fileName;
	}*/
	
	public List<Term> extractKeywords(String textFileLocation)
	{
		List<LinkedList<Token>> tokenizedSentenceList = new ArrayList<LinkedList<Token>>();
		List<String> sentenceList = new ArrayList<String>();
		POSTagger tagger = new POSTagger();
		Chunker chunker = new Chunker();
		boolean first = true;
		Parser parser = new PlainToTokenParser(new WordSplitter(new SentenceSplitter(textFileLocation)));
		String sentence = "";
		LinkedList<Token> tokenList = null;
		for (Word word = (LBJ2.nlp.seg.Token) parser.next(); word != null; word = (Word) parser
				.next()) {
			String chunked = chunker.discreteValue(word);
			tagger.discreteValue(word);
			if (first) {
				tokenList = new LinkedList<>();
				tokenizedSentenceList.add(tokenList);
				first = false;
			}
			tokenList.add(new Token(word.form, word.partOfSpeech, null, chunked));
			sentence = sentence + " " + (word.form);
			if (word.next == null) {
				sentenceList.add(sentence);
				first = true;
				sentence = "";
			}
		}
		parser.reset();
	     
		List<String> keywords = new ArrayList<String>();
		Document doc=new Document(textFileLocation, textFileLocation);
	    doc.setSentenceList(sentenceList);
	    doc.List(tokenizedSentenceList); 
	    RakeAlgorithm ex = new RakeAlgorithm();
	    ex.loadStopWordsList("D:\\downloads_new\\data\\SmartStopListEn");
	    ex.loadPunctStopWord("D:\\downloads_new\\data\\RakePunctDefaultStopList");
	    //PlainTextDocumentReaderLBJEn parser = new PlainTextDocumentReaderLBJEn();
	    //parser.readSource("testCorpus/textAstronomy");
	    //Document doc = new Document("full_path", "name");
	    ex.init(doc, null);
	    ex.runAlgorithm();
	    return doc.getTermList();
	}
	
	public static void main (String[] args)
	{
		KeywordExtraction testExtractor = new KeywordExtraction();
		List<Term> terms = testExtractor.extractKeywords("D:\\downloads_new\\data\\data_for_keywords\\test_rake.txt"); 
		for (Term term : terms) 
		{
			System.out.println(term.toString());
		}
	}

}
