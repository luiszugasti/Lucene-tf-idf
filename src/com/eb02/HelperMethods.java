package com.eb02;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** HelperMethods:
 *
 * Core network of commonly used methods.
 * */
public class HelperMethods {

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
//        File resultsOutput = new File("relevance_judgements.txt");
//        FileWriter fr = new FileWriter(resultsOutput, true);
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
                System.out.print((runCount + " " + "Q0 " + " " +  parts[0] + " " + (i+1) + " " + score + " " + runName + "\n"));
            } else {
                System.out.println((i+1) + ". " + "No path for this document");
            }
        }
//        fr.close();
    }


}
