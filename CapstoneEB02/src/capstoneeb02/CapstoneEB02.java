/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capstoneeb02;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
/**
 *
 * @author kingtahir
 */
public class CapstoneEB02 {
    private static ArrayList<String> queryList = new ArrayList<String>();
    private static ArrayList<String> queryNumList = new ArrayList<String>();
    static String outputStr = "";
	
   String indexDir = "/Users/kingtahir/Documents/topic_result";
   String dataDir = "/Users/kingtahir/Downloads/clueweb09PoolFilesTest";
   //String dataDir = "/Users/kingtahir/Documents/Hello2";
   public static String queryPath = "/Users/kingtahir/Documents/topics.txt";
   Indexer indexer;
   Searcher searcher;

   public static void main(String[] args) throws IOException {
      CapstoneEB02 tester;
      readFile(queryPath);
      int listSize = queryList.size();
      BufferedWriter output = null;
      try {
         tester = new CapstoneEB02();
         tester.createIndex();
         
         for(int i = 0; i < listSize; i++){
             tester.search(queryList.get(i), queryNumList.get(i));
         }
         
      } catch (IOException e) {
         e.printStackTrace();
      } catch (ParseException e) {
         e.printStackTrace();
      }
      
      try {
            File resultFile = new File("/Users/kingtahir/Documents/result_TF_IDF.test");
            output = new BufferedWriter(new FileWriter(resultFile));
            output.write(outputStr);
        } catch (IOException e){
            e.printStackTrace();
        }finally{
          if(output != null ){
              output.close();
          }
        }
   }

   private void createIndex() throws IOException {
      indexer = new Indexer(indexDir);
      int numIndexed;
      long startTime = System.currentTimeMillis();	
      numIndexed = indexer.createIndex(dataDir);
      
      long endTime = System.currentTimeMillis();
      indexer.close();
      System.out.println(numIndexed+" File indexed, time taken: "
         +(endTime-startTime)+" ms");		
   }

   private void search(String searchQuery, String queryNumber) throws IOException, ParseException {
      searcher = new Searcher(indexDir);
      long startTime = System.currentTimeMillis();
      TopDocs hits = searcher.search(searchQuery);
      long endTime = System.currentTimeMillis();
      int rank = 1;
   
      //System.out.println(hits.totalHits + " documents found. Time :" + (endTime - startTime));
      //System.out.println("Query #" + queryNumber + " topic name: " + searchQuery + " " + hits.totalHits + " documents found. Time :" + (endTime - startTime));

      for(ScoreDoc scoreDoc : hits.scoreDocs) {
          if(rank > 200)
             break;
          
          else{
              
              Document doc = searcher.getDocument(scoreDoc);
              outputStr = outputStr + queryNumber + " Q0 " + doc.get(LuceneConstants.FILE_NAME) + " " + rank + " " + scoreDoc.score + " Default\n";
              rank++;
          }
          
      }
      searcher.close();
   }

    private static void readFile(String queryPath) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(queryPath));
    try {
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();

        while (line != null) {
            queryNumTopic(line);
            sb.append(line);
            sb.append("\n");
            line = br.readLine();
        }
       // return sb.toString();
    } finally {
        br.close();
    }
  }
    
    private static void queryNumTopic(String query){
        char[] queryCharArray = query.toCharArray();
        String queryN = "";
        String queryTopic = "";
        int charPosition = 0;
        
        for(char c : queryCharArray){
            if(Character.isDigit(c) && charPosition < 3){
                queryN = queryN + c;
                charPosition++;
            }
            else if(c == ':'){
                charPosition++;
                continue;
            }
            else{
                queryTopic = queryTopic + c;
                charPosition++;
            }
        }
        queryList.add(queryTopic);
        queryNumList.add(queryN);
    }

}
