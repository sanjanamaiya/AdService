package com.ir.project.adservice.adindex;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.ir.project.adservice.common.Constants;

public class LuceneIndex
{
	private IndexWriter writer;
	private static LuceneIndex indexer = null;
	//private Directory index = null;
	
	protected LuceneIndex(String indexLocation) throws IOException
	{
		//Directory index = FSDirectory.open(new File("D:\\downloads_new\\adindex").toPath());
		Directory index = FSDirectory.open(new File(indexLocation).toPath());
		//index = new RAMDirectory();
		StandardAnalyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setInfoStream(System.out);
		try 
		{
			writer = new IndexWriter(index, config);
		} 
		catch (IOException e) 
		{
			// Log here
			System.out.println("Exception initializing lucene index: " + e);
		}
	}
	
	public static LuceneIndex getInstance(String indexLocation)
	{
		if (indexer == null)
		{
			try {
				indexer = new LuceneIndex(indexLocation);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		return indexer;
	}
	
	public void indexFile(String fileContents, String docId) throws IOException 
	{
		//System.out.println("Indexing docId: " + docId);
		Document document = getDocument(fileContents, docId);
		writer.addDocument(document);
	}
	
	public void indexFile(File file, String docId) throws IOException 
	{
		//System.out.println("Indexing docId: " + docId);
		Document document = new Document();
		// index file contents
		TextField contentField = new TextField(Constants.CONTENTS,
				new FileReader(file));
		// index file MEDLINE Id
		TextField docIdField = new TextField(Constants.DOCID, docId,
				Field.Store.YES);
		document.add(contentField);
		document.add(docIdField);
		writer.addDocument(document);
	}
	
	private Document getDocument(String fileContents, String docId) throws IOException 
	{
		Document document = new Document();

		FieldType type = new FieldType();
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		type.setStoreTermVectorPositions(true);
		type.setStored(true);
		type.setStoreTermVectors(true);
		Field contentField = new Field(Constants.CONTENTS, fileContents, type);
		Field docIdField = new Field(Constants.DOCID, docId, type);
		
		/*doc.add(field);
		// index file contents
		TextField contentField = new TextField(IndexContants.CONTENTS, fileContents, Field.Store.YES);
		// index file MEDLINE Id
		TextField docIdField = new TextField(IndexContants.DOCID, docId, Field.Store.YES);*/
		
		document.add(contentField);
		document.add(docIdField);

		return document;
	}
	
	public void closeWriter()
	{
		if (writer != null)
		{
			try {
				writer.close();
			} catch (IOException e) {
				
				// TODO log
			}
		}
	}

}
