package com.search.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Indexer {
	
	static HashMap<String, Map<String,Integer>> invertedIndex=new HashMap<String, Map<String,Integer>>();
	static HashMap<String, Integer> posting=new HashMap<String, Integer>();
	static HashMap<String, Integer> termCountPerDoc=new HashMap<String, Integer>();
	
	public static void main(String args[]) throws Exception
	{
		// read file
		// create inverted index
		System.out.println("::::::::::Building Index::::::::::");
		buildInvertedIndex(args[0]);
		
		System.out.println("::::::::::Index built:::::::::");
		//write index to file
		writeDocTermsToFile(args[1]);
		System.out.println("::::::::::term counts::::::::::");
		writeToFile(args[1]);
		
		System.out.println("::::::::::indexing done::::::::::");
		
	}
	
	private static void sysoForIndex()
	{
		for(Entry<String,Map<String, Integer>> posting : invertedIndex.entrySet())
		{
			System.out.println("word::"+posting.getKey());
			for(Entry<String,Integer> termFreq: posting.getValue().entrySet())
			{
				System.out.println("docID::"+termFreq.getKey()+"::"+termFreq.getValue());
			}
		}
	}
	
	private static void sysoForTermCount()
	{
		for(Entry<String,Integer> counts: termCountPerDoc.entrySet())
		{
			System.out.println("docID::"+counts.getKey()+"::"+counts.getValue());
		}
	}

	private static void buildInvertedIndex(String fileName) throws Exception
	{
		// read doc file
		FileInputStream fs= null;
		BufferedReader br=null;
		String line=null;
		String docID=null;
		int termCount=0;
		
		try
		{
			fs=new FileInputStream(new File(fileName));
			br=new BufferedReader(new InputStreamReader(fs));
			
			while(null!=(line=br.readLine()))
			{
				String[] tokens=line.split(" ");
				if(tokens[0].equals("#") && tokens.length<4)
				{
					termCountPerDoc.put(docID, termCount);
					
					docID=tokens[1];
					termCount=0;
				}
				else
				{
					for(String token:tokens)
					{
						
						if(!checkTokenForNumber(token))
						{
							addTokenToIndex(token, docID);
							termCount++;
						}
					}
				}
			}
			termCountPerDoc.put(docID, termCount);
			termCountPerDoc.remove(null);
		}
		finally
		{
			if(null!=br)
				br.close();
			if(null!=fs)
				fs.close();
		}
		
		
	}
	
	private static boolean checkTokenForNumber(String token)
	{
		Pattern pat=Pattern.compile("[0-9]+");
		Matcher mat=pat.matcher(token);
		boolean match=mat.matches();
		//System.out.println("mathch::"+match);
		return match;
	}
	
	private static void addTokenToIndex(String token, String docID)
	{
		Map<String, Integer> invertedList=null;
		int termFreq=-1;
		
			invertedList=invertedIndex.get(token);
			if(null!=invertedList)
			{
				if(null!=(invertedList.get(docID)))
				{
					termFreq=(Integer)invertedList.get(docID);
					invertedList.replace(docID, termFreq+1);
				}
				else
				{
					invertedList.put(docID, 1);
				}
			}
			else
			{
				Map newInvList= new HashMap<String, Integer>();
				newInvList.put(docID, 1);
				invertedIndex.put(token, newInvList);
			}
		
	}
	
	private static void writeToFile(String fileName) throws Exception
	{
		StringBuffer buff=new StringBuffer();
		StringBuffer postingBuff=null;
		
		FileWriter fw=null;
		
		for(Entry<String,Map<String, Integer>> posting : invertedIndex.entrySet())
		{
			postingBuff=new StringBuffer();
			for(Entry<String,Integer> termFreq: posting.getValue().entrySet())
			{
				if(!(postingBuff.length()==0))
					postingBuff.append("#");
				
				postingBuff.append(termFreq.getKey()+":"+termFreq.getValue());
				
			}
			buff.append(posting.getKey()+"="+postingBuff.toString());
			buff.append(System.getProperty("line.separator"));
		}
		
		try
		{
		 fw= new FileWriter(new File(fileName),true);
		 fw.write(buff.toString());
		}
		finally
		{
			fw.close();
		}
	}
	
	private static void writeDocTermsToFile(String fileName) throws Exception
	{
		FileWriter fw=null;
		StringBuffer tokenBuff= new StringBuffer();
		for(Entry<String,Integer> tokens: termCountPerDoc.entrySet())
		{
			if(tokenBuff.length()==0)
				tokenBuff.append("docID_"+tokens.getKey()+":"+tokens.getValue());
			else
				tokenBuff.append("#docID_"+tokens.getKey()+":"+tokens.getValue());
			//tokenBuff.append(System.getProperty("line.separator"));
		}
		tokenBuff.append(System.getProperty("line.separator"));
		
		
		try
		{
			fw=new FileWriter(new File(fileName));
			fw.write("~all_docs="+tokenBuff.toString());
		}
		finally
		{
			fw.close();
		}
	}
	

}
