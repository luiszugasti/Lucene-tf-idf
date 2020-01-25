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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
//import java.util.Comparator;
//import java.util.Date;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

public class SDMSearch {
	
		public SDMSearch() {}


	/** Simple command-line based search demo. */
	  public static void main(String[] args) throws Exception {
	    String usage =
	      "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/4_1_0/demo/ for details.";
	    if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
	      System.out.println(usage);
	      System.exit(0);
	    }
	    Double lamda_ug = 0.00;
	    Double lamda_ubg = 0.00;
	    Double lamda_obg = 0.00;
	    String index = "index_test"; //Index directory name.
	    String field = "contents";
	    String queries = null;
	    int repeat = 0;
	    boolean raw = false;
	    String queryString = null;
	    int hitsPerPage = 1000;
	    //List of stop-words
	    String[] words = {"i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now"};
	    Double [] lamda_values = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
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
	    FileReader fr = new FileReader("C:\\Users\\Naffan\\Desktop\\Ryerson\\Capstone\\topics.txt"); //Filepath for topics list.
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
	    String fname = "SDMSearch-test.test"; //Filename to write to.
	    BufferedWriter out = new BufferedWriter(new FileWriter(fname, true),32768);
	    Random r = new Random();
	   
	    //Retrieve random values for lamda
	    lamda_ug = Double.valueOf(new DecimalFormat("#.####").format(r.nextDouble()));
	    lamda_obg = Double.valueOf(new DecimalFormat("#.####").format(r.nextDouble() * (1.0 - lamda_ug)));
	    lamda_ubg = Double.valueOf(new DecimalFormat("#.####").format(r.nextDouble() * (1.0 - lamda_ug - lamda_obg)));
	    
	    System.out.println("Lamda UNIGRAM: " + lamda_ug);
	    System.out.println("Lamda ORDERED BIGRAM: " + lamda_obg);
	    System.out.println("Lamda UNORDERED BIGRAM: " + lamda_ubg);
	    
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
	
	      //ORDERED BIGRAMS
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
	      
	      //UNORDERED BIGRAMS
	      ArrayList<String> unorder_bg = new ArrayList<String>();
	      String[] terms = line.split("\\W+");
	      if(terms.length > 1) {
	      for(int i = 0; i < terms.length-1; i++) {
	    	  for(int j = i+1; j < i+2; j++) {
	    		  unorder_bg.add(terms[i] + " " + terms[j]);
	    		  unorder_bg.add(terms[j] + " " + terms[i]);

	    	  }
	      }
	      } else
	    	  unorder_bg.add(terms[0]);
	      
	      //UNIGRAMS
	      String[] term = line.split("\\W+");
	      
	      List<TopDocs> unigrams = new ArrayList<TopDocs>(); //List of results for each UNIGRAM group.
	      List<TopDocs> ordered_bigrams = new ArrayList<TopDocs>(); //List of results for each ORDERED BIGRAM group.
	      List<TopDocs> unordered_bigrams = new ArrayList<TopDocs>(); //List of results for each UNORDERED BIGRAM group.
	      
	      //Iterate through the number of ORDERED BIGRAMS and add to ordered_bigrams list. 
	      for (int x = 0; x < token.size(); x++) {
	      	Query query = parser.createPhraseQuery("contents", token.get(x));
	      	//System.out.println(query.toString()); 
	      	TopDocs results = searcher.search(query, 100);
	      	ordered_bigrams.add(results); //Add results to the list
	      	int numTotalHits = Math.toIntExact(results.totalHits.value);
	      	if(numTotalHits > 0) {
	      		results.scoreDocs = searcher.search(query, numTotalHits).scoreDocs;
	      	}  
	      }
	      
	      //Iterate through the number of UNORDERED BIGRAMS and add to unordered_bigrams list. 
	      for (int x = 0; x < unorder_bg.size(); x++) {
	        	Query query = parser.createPhraseQuery("contents", unorder_bg.get(x));
	        	//System.out.println(query.toString()); 
	        	TopDocs results = searcher.search(query, 100);
	        	unordered_bigrams.add(results); //Add results to the list
	        	int numTotalHits = Math.toIntExact(results.totalHits.value);
	        	if(numTotalHits > 0) {
	        		results.scoreDocs = searcher.search(query, numTotalHits).scoreDocs;
	        	}  
	        }
	      //Iterate through the number of UNIGRAMS and add to unigrams list.
	      for (int x = 0; x < term.length; x++) {
	        	Query query = parser.createPhraseQuery("contents", term[x]);
	        	//System.out.println(query.toString()); 
	        	TopDocs results = searcher.search(query, 100);
	        	//ScoreDoc[] hits = results.scoreDocs;
	        	unigrams.add(results); //Add results to the list
	        	int numTotalHits = Math.toIntExact(results.totalHits.value);
	        	if(numTotalHits > 0) {
	        		results.scoreDocs = searcher.search(query, numTotalHits).scoreDocs;
	        	}  
	        }
	      List<Docs> bg_list = new ArrayList<Docs>(); //List of collected ORDERED BIGRAMS results
	      List<Docs> ubg_list = new ArrayList<Docs>(); //List of collected UNORDERED BIGRAMS results
	      List<Docs> ug_list = new ArrayList<Docs>(); //List of collected UNIGRAM results

	      //ORDERED BIGRAMS: Collect list of documents and their respective scores, move to Docs object (since Lucene syntax is a pain)
	      for(TopDocs tp: ordered_bigrams) {
	        int TotalHits = Math.toIntExact(tp.totalHits.value);
	        for(int a = 0; a < TotalHits; a++) {
	        	Document doc_tp = searcher.doc(tp.scoreDocs[a].doc);
	        	Double score = (tp.scoreDocs[a].score)/1.0;
	        	Docs bg = new Docs(doc_tp.get("path"), score);
	        	bg_list.add(bg); }
	    	  }
	      
	      //UNORDERED BIGRAMS: Collect list of documents and their respective scores, move to Docs object (since Lucene syntax is a pain)  
	    	for(TopDocs tp: unordered_bigrams) {
	    		int TotalHits = Math.toIntExact(tp.totalHits.value);
	        	for(int a = 0; a < TotalHits; a++) {
	        		Document doc_tp = searcher.doc(tp.scoreDocs[a].doc);
	        		Double score = (tp.scoreDocs[a].score)/1.0;
	        		Docs ubg = new Docs(doc_tp.get("path"), score);
	        		ubg_list.add(ubg); }
	    	  }
		    
	    	//UNIGRAMS: Collect list of documents and their respective scores, move to Docs object (since Lucene syntax is a pain)  
	    	for(TopDocs tp: unigrams) {
	        	  int TotalHits = Math.toIntExact(tp.totalHits.value);
	        	  for(int a = 0; a < TotalHits; a++) {
	        		  Document doc_tp = searcher.doc(tp.scoreDocs[a].doc);
	        		  Double score = (tp.scoreDocs[a].score)/1.0;
	        		  Docs ug = new Docs(doc_tp.get("path"), score);
	        		  ug_list.add(ug); }
	    	  }
			     
	    HashMap<String, Double> agg_OBG_list = new HashMap<String, Double>(); //ORDERED BIGRAM HashMap
	    HashMap<String, Double> agg_UBG_list = new HashMap<String, Double>(); //UNORDERED BIGRAM HashMap
	    HashMap<String, Double> agg_UG_list = new HashMap<String, Double>(); //UNIGRAM HashMap

	    //AGGREGATE results per document basis for ORDERED BIGRAM HashMap
	    for(int j = 0; j < bg_list.size(); j++) {
	  	  String path = bg_list.get(j).getDocument();
	  	  Double final_score = bg_list.stream().filter(o -> o.getDocument() == path).mapToDouble(o -> o.getScore()).sum(); 
	  	  final_score = final_score/lamda_obg; //LAMDA is a random value from 0.0 to 1.0
	  	  File f = new File(path);
	  	  agg_OBG_list.put(f.getName(), final_score);		  	
	    }
	    
	    //AGGREGATE results per document basis for UNORDERED BIGRAM HashMap
	    for(int j = 0; j < ubg_list.size(); j++) {
	    	  String path = ubg_list.get(j).getDocument();
	    	  Double final_score = ubg_list.stream().filter(o -> o.getDocument() == path).mapToDouble(o -> o.getScore()).sum(); 
		  	  final_score = final_score/lamda_ubg; //LAMDA is a random value from 0.0 to 1.0
	    	  File f = new File(path);
	    	  agg_UBG_list.put(f.getName(), final_score);		  	
	      }
	    
	    //AGGREGATE results per document basis for UNIGRAM HashMap
	    for(int j = 0; j < ug_list.size(); j++) {
	    	  String path = ug_list.get(j).getDocument();
	    	  Double final_score = ug_list.stream().filter(o -> o.getDocument() == path).mapToDouble(o -> o.getScore()).sum(); 
		  	  final_score = final_score/lamda_ug; //LAMDA is a random value from 0.0 to 1.0
	    	  File f = new File(path);
	    	  agg_UG_list.put(f.getName(), final_score);		  	
	      }
	    //MERGE HashMap results from each SDM implementation (ORDERED BIGRAM w/ UNORDERED BIGRAM)
	    agg_OBG_list.forEach((k, v) -> agg_UBG_list.merge(k, v, Double::sum));
	    
	  //MERGE HashMap results from each SDM implementation (UNORDERED BIGRAM w/ UNIGRAM)
	    agg_UBG_list.forEach((k, v) -> agg_UG_list.merge(k, v, Double::sum));
	    
	    //SORT the HashMap for TREC results
	    HashMap<String, Double> srt_list = agg_UG_list.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
	            LinkedHashMap::new));
	    
		//PRINT & WRITE the results from final merged SDM HashMap	
		int count = 0; 
		for(String d: srt_list.keySet()) { 
			String str = "";
			if (srt_list.get(d) > 0.0 && count < 200) { //Print the result for debugging.
				str = topic.indexOf(topic.get(e))+1 + " " + "Q0" + " " + d + " " + (++count) + " " + srt_list.get(d) + " " + "Default"; 
				System.out.println(str); 
				appendToFile(out,str); //Write to a file.
			}

		}

	    }
	   
	    reader.close();
	    out.close();
	    cmdexe();
	  }
	  
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
	  
	  //Opens cmd.exe and runs command for TREC Results.
	  public static void cmdexe() throws IOException {
		  ProcessBuilder builder = new ProcessBuilder(
		            "cmd.exe", "/c", "cd \"C:\\Users\\Naffan\\eclipse-workspace\\LuceneDemo\" && dir"); //Insert the TREC command here!
		        builder.redirectErrorStream(true);
		        Process p = builder.start();
		        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		        String line;
		        while (true) {
		            line = r.readLine();
		            if (line == null) { break; }
		            System.out.println(line);
		        }
	  }
}

