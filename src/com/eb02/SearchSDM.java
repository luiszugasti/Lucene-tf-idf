/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eb02;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
//import java.util.Comparator;
//import java.util.Date;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
//import org.apache.lucene.search.ScoreDoc;
//import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import static java.util.stream.Collectors.*;
//import static java.util.Map.Entry.*;

/** Simple command-line based search demo. */
public class SearchSDM {

  private SearchSDM() {}

  /** Simple command-line based search demo. */
  public static void main(String[] args) throws Exception {
    String usage =
      "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/4_1_0/demo/ for details.";
    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
      System.out.println(usage);
      System.exit(0);
    }

    String index = "index";
    String field = "contents";
    String queries = null;
    int repeat = 0;
    boolean raw = false;
    String queryString = null;
    int hitsPerPage = 100;
    //List of stop-words
    String[] words = {"i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now"};
    for(int i = 0;i < args.length;i++) {
      if ("-index".equals(args[i])) {
        index = args[i+1];
        i++;
      } else if ("-field".equals(args[i])) {
        field = args[i+1];
        i++;
      } else if ("-queries".equals(args[i])) {
        queries = args[i+1];
        i++;
      } else if ("-query".equals(args[i])) {
        queryString = args[i+1];
        i++;
      } else if ("-repeat".equals(args[i])) {
        repeat = Integer.parseInt(args[i+1]);
        i++;
      } else if ("-raw".equals(args[i])) {
        raw = true;
      } else if ("-paging".equals(args[i])) {
        hitsPerPage = Integer.parseInt(args[i+1]);
        if (hitsPerPage <= 0) {
          System.err.println("There must be at least 1 hit per page.");
          System.exit(1);
        }
        i++;
      }
    }
    
    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
    IndexSearcher searcher = new IndexSearcher(reader);
    Analyzer analyzer = new StandardAnalyzer();

    BufferedReader in = null;
    if (queries != null) {
      in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
    } else {
      in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    }
    QueryParser parser = new QueryParser(field, analyzer);
    //Extract topic list from file
    List<String> topic = new ArrayList<String>();
    FileReader fr = new FileReader("C:\\Users\\Naffan\\Desktop\\Ryerson\\Capstone\\topics.txt");
    StringBuffer sb = new StringBuffer();
    while(fr.ready()) {
    	char c = (char) fr.read();
    	if(c == '\n') {
    		topic.add(sb.toString());
    		sb = new StringBuffer();
    	}
    	else {
    		sb.append(c);
    	}
    }
    fr.close();
    if(sb.length() > 0) {
    	topic.add(sb.toString());
    }
    //Get rid of numbered value and keep only the query terms.
    for (int j = 0; j < topic.size(); j++) {
    	topic.set(topic.indexOf(topic.get(j)), topic.get(j).split("\\:")[1]);
    }
    //Write results to this file
    String fname = "SDM-results.test";
    BufferedWriter out = new BufferedWriter(new FileWriter(fname, true),32768);
    //Iterate over the topic list
    for(int e = 0; e < topic.size(); e++){
    	System.out.println(topic.get(e)); //Print the topic

     
      String line = topic.get(e);

      if (line == null || line.length() == -1) {
        break;
      }

      line = line.trim();
      if (line.length() == 0) {
        break;
      }
     // if(line.split("\\w+").length > 1) { //If query topic is more than one word
    //Get the ordered bigrams from topic
      List<String> token = new ArrayList<String>();
      TokenStream tokenizer = analyzer.tokenStream(null, new StringReader(line));

      //StopFilter stopWords = new StopFilter(tokenizer, StopFilter.makeStopSet(words, true));
      ShingleFilter shingle = new ShingleFilter(tokenizer, 2);

      shingle.setOutputUnigrams(false);
      shingle.setOutputUnigramsIfNoShingles(true);
      //shingle.setFillerToken("_");
      CharTermAttribute charTermAttribute = shingle.addAttribute(CharTermAttribute.class);
      tokenizer.reset();
      
      while(shingle.incrementToken()) {
    	  token.add(charTermAttribute.toString());
    	  //System.out.println(charTermAttribute.toString()); //Print the bigrams for topic.
      }
      shingle.end();
      shingle.close();
      List<TopDocs> bigrams = new ArrayList<TopDocs>(); //List of results for each bigram group.
      //Iterate through the number of bigrams and add to bigrams list.
      for (int x = 0; x < token.size(); x++) {
      	Query query = parser.createPhraseQuery("contents", token.get(x));
      	System.out.println(query.toString()); //Print searched bigrams
      	TopDocs results = searcher.search(query, hitsPerPage);
      	//ScoreDoc[] hits = results.scoreDocs;
      	bigrams.add(results); //Add results to the list
      	int numTotalHits = Math.toIntExact(results.totalHits.value);
      	if(numTotalHits > 0) {
      		results.scoreDocs = searcher.search(query, numTotalHits).scoreDocs;
      	}  
      }
      //List<TopDocs> bg_1 = new ArrayList<TopDocs>(); 
      List<OBGDocs> bg_list = new ArrayList<OBGDocs>(); //Final list of collected bigram results (didn't use yet) 
      
/*      if(bigrams.size() > 1) {
      //First bigram results copied to this list
      bg_1.add(bigrams.get(0));
      //Iterate through the first bigram group
      for(TopDocs tp_1: bg_1) {
    	  int TotalHits = Math.toIntExact(tp_1.totalHits.value);
    	  for(int a = 0; a < TotalHits; a++) {
    		  Document doc_tp = searcher.doc(tp_1.scoreDocs[a].doc);
    		  //Iterate through the other bigram groups
    		  for(int i = 1; i < bigrams.size(); i++) {
    			  int numTotalHits = Math.toIntExact(bigrams.get(i).totalHits.value);
    			  for (int b = 0; b < numTotalHits; b++) {
    				  Document doc_list = searcher.doc(bigrams.get(i).scoreDocs[b].doc);
    				  //If the documents are the same between the bigram groups
    				  if(doc_tp.get("path").equals(doc_list.get("path"))) {
    						//bg_list.add(tp_1);
    					  	Double score = (tp_1.scoreDocs[a].score + bigrams.get(i).scoreDocs[b].score)/1.0; //Add the scores and divide by three
    					  	OBGDocs bg = new OBGDocs(doc_tp.get("path"), score);
    					  	bg_list.add(bg);
    					  	//bg_list.get(a).scoreDocs[a].score = score;
    					  	//Document doc = searcher.doc(bg_list.get(a).scoreDocs[a].doc);
    				  }
      			              
    			  }
    		  }
    	  }
      	}
      }*/
      //else {
    	  for(TopDocs tp: bigrams) {
        	  int TotalHits = Math.toIntExact(tp.totalHits.value);
        	  for(int a = 0; a < TotalHits; a++) {
        		  Document doc_tp = searcher.doc(tp.scoreDocs[a].doc);
        		  Double score = (tp.scoreDocs[a].score)/1.0;
        		  OBGDocs bg = new OBGDocs(doc_tp.get("path"), score);
        		  bg_list.add(bg); }
    	  }
	  //}
		     
    HashMap<String, Double> final_list = new HashMap<String, Double>();
    
    for(int j = 0; j < bg_list.size(); j++) {
  	  String path = bg_list.get(j).getDocument();
  	  Double final_score = bg_list.stream().filter(o -> o.getDocument() == path).mapToDouble(o -> o.getScore()).sum(); 
  	  final_score = final_score/3.0;
  	  File f = new File(path);
  	  final_list.put(f.getName(), final_score);		  	
    }
    HashMap<String, Double> srt_list = final_list.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
            LinkedHashMap::new));
    
    int count = 0;
    for(String d: srt_list.keySet()) {	
    	if (srt_list.get(d) > 0.0 && count < 200) {
	  		//Print the result for debugging (not writing to file for now).
			System.out.println(topic.indexOf(topic.get(e))+1 + " " + "Q0" + " " + d + " " + (++count) + " " + srt_list.get(d) + " " + "Default");
	  		}
    	
    	}
       
 // }
			
			/*
			 * else { Query query = parser.parse(line); TopDocs results =
			 * searcher.search(query, hitsPerPage); ScoreDoc[] hits = results.scoreDocs;
			 * 
			 * int numTotalHits = Math.toIntExact(results.totalHits.value);
			 * 
			 * if(numTotalHits > 0) { hits = searcher.search(query, numTotalHits).scoreDocs;
			 * }
			 * 
			 * for (int i = 0; i < numTotalHits; i++) { Document doc =
			 * searcher.doc(hits[i].doc); String path = doc.get("path"); Double score =
			 * hits[i].score/1.0; File f = new File(path); if (score > 0.0 && i < 200) {
			 * System.out.println(topic.indexOf(topic.get(e))+1 + " " + "Q0" + " " +
			 * f.getName() + " " + (++i) + " " + score/3 + " " + "Default"); } } }
			 */
			 

    }
    reader.close();
    out.close();
  }

  /**
   * This demonstrates a typical paging search scenario, where the search engine presents 
   * pages of size n to the user. The user can then go to the next page if interested in
   * the next hits.
   * 
   * When the query is executed for the first time, then only enough results are collected
   * to fill 5 result pages. If the user wants to page beyond this limit, then the query
   * is executed another time and all hits are collected.
   * 
   */
	/*
	 * public static void doPagingSearch(BufferedReader in, BufferedWriter out,
	 * IndexSearcher searcher, Query query, int hitsPerPage, boolean raw, boolean
	 * interactive, int topic_index) throws IOException {
	 * 
	 * 
	 * }
	 */
  
  public static void appendToFile(BufferedWriter out, String str) {
	  try {
		  out.write(str);
		  out.newLine();
		  out.flush();
	  }
	  catch (IOException e) {
		  System.out.println("exception occurred" + e);
	  }
  }
}