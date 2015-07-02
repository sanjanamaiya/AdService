package com.ir.project.adservice.search;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.ir.project.adservice.common.Constants;

public class LuceneSearch 
{
	IndexSearcher indexSearcher;
	QueryParser queryParser;
	IndexReader reader;

	public LuceneSearch(String indexLocation) throws IOException 
	{
		Directory indexDirectory = FSDirectory
				.open(new File(indexLocation).toPath());
		reader = DirectoryReader.open(indexDirectory);
		indexSearcher = new IndexSearcher(reader);
		queryParser = new QueryParser(Constants.CONTENTS, new StandardAnalyzer());
	}

	public TopDocs search(String searchQuery) throws IOException, ParseException
	{
		Query query = queryParser.parse(QueryParser.escape(searchQuery));
		
		TopDocs topDocs = indexSearcher.search(query, Constants.MAX_SEARCH);
		ScoreDoc[] hits = topDocs.scoreDocs;
		for (int i = 0; i< 3; i ++)
		{
			Document doc = getDocument(hits[i].doc);
			System.out.println(doc.get(Constants.DOCID) + doc.get(Constants.CONTENTS) + " " + hits[i].score);
			System.out.println(indexSearcher.explain(query, hits[i].doc));
		}
		//return indexSearcher.search(query, Constants.MAX_SEARCH);
		return topDocs;
	}

	public TopDocs search(Query query) throws IOException
	{
		return indexSearcher.search(query, Constants.MAX_SEARCH);
	}
	public Document getDocument(ScoreDoc scoreDoc)
			throws CorruptIndexException, IOException 
	{
		return indexSearcher.doc(scoreDoc.doc);
	}
	
	public Document getDocument(int docId) throws IOException 
	{
		return indexSearcher.doc(docId);
	}

	public void close() throws IOException 
	{
		if (reader != null)
		{
			reader.close();
		}
	}

}
