package com.ir.project.adservice.common;

import java.util.Arrays;
import java.util.List;

public class Constants 
{
	public static final String CONTENTS = "contents";
	public static final String DOCID = "docid";
	public static final int MAX_SEARCH = 50;
	
	public final static List<String> stopWords = Arrays.asList(
			"kill", "killing", "assassination", "racism", "sex",
			"sexism", "violence", "rape", "hitler", "nazi", "nazis"
			);
}
