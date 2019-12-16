/*
 * Created by Luis Zugasti
 * SDM Search based off of the TF-IDF similarity measure.
 */
package com.eb02;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

// import org.apache.lucene.search.similarities.TFIDFSimilarity;
// import org.apache.lucene.search.*;

/** Simple command-line based search demo. */
public class SDM_Search {

    private SDM_Search() {}

    public static void main(String[] args) throws Exception {
        String usage =
                "Usage:\tjava com.eb02.SearchV2 [-index dir] [-queries file] [-run_id]\n\nCheck out our github for more details.";
        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);
            System.exit(0);
        }

        String index = "index";
        String field = "contents";
        String queries = null;
        String run_id = null;

        // List creation. Data structure that looks like:
        // qN | stringOfQueries
        //  1 |    .  .  .
        //
        // And so on...
        class qString {
            String qN;
            String stringOfQueries;

            public qString(String qN, String stringOfQueries){
                this.qN = qN;
                this.stringOfQueries = stringOfQueries;
            }
        }

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
            } else if ("-run_id".equals(args[i])) {
                run_id = args[i+1];
                i++;
            }
        }

        if(index == "index"){
            System.out.println(usage + "<< You missed to place the index");
            System.exit(0);
        }
        if(queries == null){
            System.out.println(usage + "<< You missed to place the queries");
            System.exit(0);
        }

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index))); //Open a new IndexReader object
        IndexSearcher searcher = new IndexSearcher(reader); // As a baseline, this works - Why?
                                                            // IndexSearcher implements TF-IDF internally.
                                                            // Thus, this completes the requirement of getting TF-IDF.
                                                            // For now.
        Analyzer analyzer = new StandardAnalyzer();

        BufferedReader queryReader = null;
        if (queries != null) {
            queryReader = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8); // Open the path to our queries
        }

        QueryParser parser = new QueryParser(field, analyzer);
        while (true) {

            String line = queryReader.readLine(); // Next: Catch the errors

            if (line == null) {
                break;
            }

            // Processing the inputs
            // ^regex - Finds regex that must match at the beginning of the line.
            // \d - Any digit, short for [0-9]
            // + - Occurs one or more times, is short for {1,} Example: X+- Finds one or several letter X
            // For simplistic manners, I can just do this twice. OR Just split at the first instance of :
            String[] parts = line.split(":");

            // Assign the first part to qN, second part to stringOfQueries
            qString test = new qString(parts[0], parts[1]);
            //trim the stringOfQueries
            test.stringOfQueries = test.stringOfQueries.trim();
            if (test.stringOfQueries.length() == 0) {
                break;
            }

            Query query = parser.parse(test.stringOfQueries);
            // System.out.println("Searching for: " + query.toString(field));

            doPagingSearch(searcher, query, test.qN, run_id);
        }
        reader.close();
    }

    /**
     * This returns a String with the relevance judgements for a single query.
     *
     * @param searcher: searcher Object to compute our search
     * @param query: actual name of the query for the procedure to search
     * @param runCount: number of the current query
     * @param runName: name of the current run
     */

    public static void doPagingSearch(IndexSearcher searcher, Query query, String runCount, String runName) throws IOException {

        // Collect AT MOST, the top 1000 hits - roughly the same as the worst case scenario for relevance_judgements_for_CW09
        TopDocs results = searcher.search(query, 1);
        // Put the results in "hits" variable
        ScoreDoc[] hits = results.scoreDocs;

        // For outputting the results to file.
        File resultsOutput = new File("relevance_judgements.txt");
        FileWriter fr = new FileWriter(resultsOutput, true);
        int numTotalHits = Math.toIntExact(results.totalHits.value);
        // This will get ALL the relevant documents possible.
        if (numTotalHits > 0){
            hits = searcher.search(query, numTotalHits).scoreDocs;
        }
        int start = 0;
        int end = numTotalHits;

        for (int i = start; i < end; i++) {

            Document doc = searcher.doc(hits[i].doc);
            String path = doc.get("path");
            Float score = hits[i].score;
            // Simple. Trim after clueweb09PoolFilesTest.
            String[] parts = path.split("/");

            if (path != null) {
                //fr.write(runCount + " " + "Q0 " + " " +  parts[1] + " " + (i+1) + " " + "4999" + " " + runName + "\n");
                // Since the above is NOT working, when running this file, pipe the output to a file called "relevance_judgements1.txt"
                System.out.print((runCount + " " + "Q0 " + " " +  parts[1] + " " + (i+1) + " " + score + " " + runName + "\n"));
            } else {
                System.out.println((i+1) + ". " + "No path for this document");
            }
        }
    fr.close();
    }
}