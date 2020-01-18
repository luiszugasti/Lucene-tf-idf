/**
 * qString is a helper class that allows us to split a string into the query number, and then the query
 * number.
 *
 * It leads to a data structure that looks like:
 *         qN | stringOfQueries
 *         1  |    .  .  .
 *
 *         where qN is the query number, while stringOfQueries is the complete query string.
 */

package com.eb02;

public class QueryString {
    String qN;
    String stringOfQueries;

    public QueryString(String qN, String stringOfQueries){
        this.qN = qN;
        this.stringOfQueries = stringOfQueries;
    }
}
