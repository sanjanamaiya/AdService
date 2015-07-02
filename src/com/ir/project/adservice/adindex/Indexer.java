package com.ir.project.adservice.adindex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ir.project.adservice.jsonFiles.ProductInfo;

public class Indexer
{
	public void indexFile(String fileLocation, String indexCreationLoc)
	{
		int count = 0;
		File sourcefile = new File(fileLocation);
		if (sourcefile.exists())
		{
			BufferedReader reader = null;
			try
			{
				ProductInfo product = null;
				ObjectMapper objectMapper = new ObjectMapper();
				StringBuilder document = new StringBuilder();
				reader = new BufferedReader(new FileReader(sourcefile));
				String content = reader.readLine();
				//int sectionCount = 0;
				while (content != null)
				{
					try
					{
					product = objectMapper.readValue(content, ProductInfo.class);
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
					
					LuceneIndex.getInstance(indexCreationLoc).indexFile(document.toString().toLowerCase(), product.getAsin());
					count ++;
					content = reader.readLine();
					}
					catch (Exception e)
					{
						System.out.println("Exception : " + e);
						
					}
				}
				
				System.out.println("Checking tokens...");
			}
			
			catch (JsonParseException | JsonMappingException e1)
			{
				
				System.out.println("Exception : " + e1);
			}
			catch (IOException e)
			{
				//LOGGER.warning("Exception thrown while reading file: " + fileLocation + ", Exception: " + e);
				System.out.println("Exception : " + e);
			}
			catch (Exception e3)
			{
				System.out.println("Exception : " + e3);
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
				LuceneIndex.getInstance(indexCreationLoc).closeWriter();
				System.out.println("Count processed: " + count);
			}
		}
	}
}
