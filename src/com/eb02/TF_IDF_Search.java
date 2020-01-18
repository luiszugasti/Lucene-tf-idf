package com.eb02;

// Standard Imports
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;

// Lucene Imports
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

/** TF-IDF: Term Frequency/Inverted Document Frequency
 *
 * Classically a search method that returns high quality search hits.
 * Core to the Lucene Framework.
 *
 * ATTENTION: The code returns hits, **Assuming** That you are running the code within the document corpus folder.
 * This way, we don't have to go through removing the path to the file you are accessing (much easier if for some
 * reason you have this in a nested folder sequence). Inspect the run configuration.
 *
 * */
public class TF_IDF_Search {

    private TF_IDF_Search() {}

    public static void main(String[] args) throws Exception {

        // Usage string for command line interfaces, as well as input directives.
        // Sample string:
        // java com.eb02.TF_IDF_Search -index your_index_location -queries file_with_queries -run_id your_run_id_here
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

        for(int i = 0;i < args.length;i++) {
            if ("-index".equals(args[i])) {
                index = args[i+1];
                i++;
            } else if ("-queries".equals(args[i])) {
                queries = args[i+1];
                i++;
            } else if ("-run_id".equals(args[i])) {
                run_id = args[i+1];
                i++;
            }
        }

        // Simple error checking

        System.out.println("Review this information very carefully.\n\n +" +
                "index location: " + index +
                ", queries: " + queries +
                ", run_id: " + run_id);


        // Open a new IndexReader object, using index.
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));

        // Open a new IndexSearcher object.
        // As a baseline, this works - Why?
        // IndexSearcher implements TF-IDF internally.

        IndexSearcher searcher = new IndexSearcher(reader);

        Analyzer analyzer = new StandardAnalyzer();

        BufferedReader queryReader = null;
        if (queries != null) {
            // Open the path to our queries
            queryReader = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
        }

        QueryParser parser = new QueryParser(field, analyzer);
        while (true) {

            String line = queryReader.readLine();

            // EOF
            if (line == null) {
                break;
            }

            // Processing the inputs
            // ^regex - Finds regex that must match at the beginning of the line.
            // \d - Any digit, short for [0-9]
            // + - Occurs one or more times, is short for {1,} Example: X+- Finds one or several letter X
            // For simplistic manners, I can just do this twice. OR Just split at the first instance of ":"

            String[] parts = line.split(":");

            // Assign the first part to qN, second part to stringOfQueries
            QueryString test = new QueryString(parts[0], parts[1]);

            // trim the stringOfQueries of any whitespace.
            test.stringOfQueries = test.stringOfQueries.trim();

            // If we have an instance where there's no query provided...
            if (test.stringOfQueries.length() == 0) {
                break;
            }

            // Build a Lucene query.
            Query query = parser.parse(test.stringOfQueries);

            HelperMethods.doPagingSearch(searcher, query, test.qN, run_id);
        }
        reader.close();
    }
}