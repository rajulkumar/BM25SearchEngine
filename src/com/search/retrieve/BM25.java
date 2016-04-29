package com.search.retrieve;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;

public class BM25 {
	
	static HashMap<String,Integer> docTokenCount=new HashMap<String, Integer>();
	static HashMap<String,Map<String,Integer>> invIndex=new HashMap<String, Map<String,Integer>>();
	
	static HashMap<String, Double> bm25ScoreForQuery=new HashMap<String, Double>();
	static List<Entry<String,Double>> bm25RankForQuery=null;
	
	//static double docLength=0.0;
	static double avdl=0.0;
	
	static final double k1=1.2;
	static final double b=0.75;
	static final double k2=100;
	
	static Properties index=new Properties();
	
	static ArrayList<String> queryList=new ArrayList<String>();
	
	static String indexFile=null;
	static String queryFile=null;
	static int rankLength=0;
	static String rankFile=null;
	
		
	public static void main(String args[]) throws Exception
	{
		System.out.println("********Begining retrieval********");
		indexFile=args[0];
		String queryFile=args[1];
		rankLength=Integer.parseInt(args[2]);
		rankFile=args[3];
		System.out.println("*********Fetching token counts********");
		updateDocTokenCount();
		
		
		System.out.println("doc avg len::"+avdl);
		System.out.println("*********Reading queries********");
		readQueries(queryFile);
		
		System.out.println("********Ranking begins*******");
		processBM25Rank();
		System.out.println("*****End of ranking*****");
		
	}
	
	private static void loadIndexes(String fileName,String query) throws Exception
	{
		FileInputStream inputStream=null;
		String line=null;
		BufferedReader br=null;
		Map<String,Integer> indexLine=null;
		String[] postingList=null;
		String[] postingTerms=null;
		
		String[] terms=query.split(" ");
		try {
		    inputStream = new FileInputStream(fileName);
		    br=new BufferedReader(new InputStreamReader(inputStream));
		    
		    while (null!=(line=br.readLine())) {
		    	for(String term:terms)
		    	{
		    		String[] invLine=line.split("=");
		    		//System.out.println("terms::"+term+"::"+line);
		    		if(invLine[0].equalsIgnoreCase(term))
		    		{
		    			postingList= invLine[1].split("#");
		    			indexLine=new HashMap<String, Integer>();
						for(String posting:postingList)
						{
							postingTerms=posting.split(":");
							indexLine.put(postingTerms[0], Integer.parseInt(postingTerms[1]));
						}
						invIndex.put(invLine[0], indexLine);
		    		}
		    	}
		    }
		    
		} finally {
			if (br != null) {
		        br.close();
		    }
		    if (inputStream != null) {
		        inputStream.close();
		    }
		    
		}
	}
	
	private static void sysoForIndex()
	{
		for(Entry<String,Map<String, Integer>> posting : invIndex.entrySet())
		{
			System.out.println("word::"+posting.getKey());
			for(Entry<String,Integer> termFreq: posting.getValue().entrySet())
			{
				System.out.println("docID::"+termFreq.getKey()+"::"+termFreq.getValue());
			}
		}
	}
	
	private static void sysoUDTC()
	{
		for(Entry<String, Integer> entry:docTokenCount.entrySet())
		{
			System.out.println("dtc:"+entry.getKey()+"::"+entry.getValue());
		}
	}
	
	private static void sysoQL()
	{
		for(String query:queryList)
		{
			System.out.println("q:"+query);
		}
	}
	
	private static void loadIndex(String fileName) throws Exception
	{
		index.load(new FileInputStream(new File(fileName)));
		
		System.out.println("index vals::"+index.getProperty("ca581202"));
		//System.out.println("doc::"+index.getProperty("docID_2"));
		
		
		System.out.println("all_docs::"+index.getProperty("all_docs"));
	}
	
	private static void updateDocTokenCount() throws Exception
	{
		String[] docToken=null;
		String docTokens=null;
		FileInputStream fs=null;
		BufferedReader br=null;
		try
		{
			fs=new FileInputStream(new File(indexFile));
			br=new BufferedReader(new InputStreamReader(fs));
			if(null!=(docTokens=br.readLine()))
			{
		
		String[] docsTokenCnt=docTokens.split("=")[1].split("#");
		double docLength=0.0;
		for(String docTokenCnt:docsTokenCnt)
		{
			docToken=docTokenCnt.split(":");
			docTokenCount.put(docToken[0], Integer.parseInt(docToken[1]));
			docLength=docLength+Long.parseLong(docToken[1]);
		}
		docTokenCount.remove("docID_null");
		avdl=docLength/(docTokenCount.size());
			}
		}
		finally
		{
			if (br != null) {
		        br.close();
		    }
		    if (fs != null) {
		        fs.close();
		    }
		}
	}
	
	private static void readQueries(String fileName) throws Exception
	{
		FileInputStream fs=null;
		BufferedReader br=null;
		String line=null;
		try
		{
			fs=new FileInputStream(new File(fileName));
			br=new BufferedReader(new InputStreamReader(fs));
			
			while(null!=(line=br.readLine()))
			{
				queryList.add(line);
			}
		}
		finally
		{
			br.close();
			fs.close();
		}
		
	}
	
	private static void processBM25Rank() throws Exception
	{
		String query = null;
		for(int i=0;i<queryList.size();i++)
		{
			query=queryList.get(i);
			System.out.println("query::"+query);
			System.out.println("********Loading index for::"+query+"********");
			loadIndexes(indexFile, query);
			
			System.out.println("********Calculating scores for::"+query+"********");
			calcBM25Score(query);
			
			System.out.println("********Ranking docs********");
			rankDocsOnBM25score();
			
			System.out.println("********Writing to file::"+rankFile+"********");
			writeRankToFile(i+1);
			
			
		}
	}
	
	private static void updateIndexForQueryTerms(String query)
	{
		Map<String,Integer> indexLine=null;
		String[] postingList=null;
		String[] postingTerms=null;
		
		String[] queryTerms=query.split(" ");
		
		for(String term:queryTerms)
		{
			if(null!=index.getProperty(term))
			{
				indexLine=new HashMap<String, Integer>();
				postingList= index.getProperty(term).split("#");
				for(String posting:postingList)
				{
					postingTerms=posting.split(":");
					indexLine.put(postingTerms[0], Integer.parseInt(postingTerms[1]));
				}
				invIndex.put(term, indexLine);
			}
		}
	}
	
	private static void calcBM25Score(String query)
	{
		double K=0.0;
		double docScore=0.0;
		double totalScore=0.0;
		int f=0;
		int n=0;
		int qf=0;
		String[] queryTerms=query.split(" ");
		int N=docTokenCount.size();
		String docID=null;
		
				
		for(Entry<String,Integer> doc:docTokenCount.entrySet())
		{
			docID=doc.getKey().substring(6);
			K=calcK(doc.getKey());
			totalScore=0.0;
			for(String term:queryTerms)
			{
				if(null!=invIndex.get(term).get(docID))
				{
					
					f=invIndex.get(term).get(docID);
					
				}
				else
				{
					f=0;
				}
				n=invIndex.get(term).entrySet().size();
				qf=qf(term,queryTerms);
				
				docScore= (Math.log((N-n+0.5)/(n+0.5)))*(((k1+1)*f)/(K+f))*(((k2+1)*qf)/(k2+qf));
				
				totalScore=totalScore+docScore;
				
			}
			bm25ScoreForQuery.put(docID,totalScore);
		}
	}
	
	private static int qf(String term, String[] queryTerms)
	{
		int qf=0;
		for(String qterm:queryTerms)
		{
			if(qterm.equalsIgnoreCase(term))
				qf++;
		}
		return qf;
	}
	
	private static double calcK(String doc)
	{
		int dl=docTokenCount.get(doc);
		double k=k1*((1-b)+ b*(dl/avdl));
		
		return k;
	}
	
	private static void rankDocsOnBM25score()
	{
		//sysoBM25Scr();
		bm25RankForQuery=new ArrayList<Map.Entry<String,Double>>(bm25ScoreForQuery.entrySet());

			Collections.sort(bm25RankForQuery, new Comparator<Entry<String, Double>>() {
				public int compare(Map.Entry<String, Double> o1,
	                                           Map.Entry<String, Double> o2) {
					if (o1.getValue()>=o2.getValue())
						return -1;
					else
						return 1;
							
				}
			});
			
	}
	
	private static void writeRankToFile(int queryID) throws Exception
	{
		BufferedWriter bw=null;
		StringBuffer buff=new StringBuffer();
		
		for(int count=1;count<=rankLength;count++)
		{
			Entry<String,Double> rank=bm25RankForQuery.get(count-1);
			//query_id Q0 doc_id rank BM25_score system_name
			buff.append(queryID+" Q0 "+rank.getKey()+" "+count+" "+rank.getValue()+" "+"IndexNRank");
			buff.append(System.getProperty("line.separator"));
			
		}
		
		buff.append(System.getProperty("line.separator"));
		buff.append(System.getProperty("line.separator"));
		
		try
		{
			bw=new BufferedWriter(new FileWriter(new File(rankFile),true));
			bw.write(buff.toString());
		}
		finally
		{
			bw.close();
		}
	}
	
	private static void sysoBM25Scr()
	{
		System.out.println("***BM25 scores***");
		for(Entry<String,Double> doc:bm25ScoreForQuery.entrySet())
		{
			System.out.println("doc::"+doc.getKey()+"::"+doc.getValue());
		}
	}

}
