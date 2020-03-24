package com.eb02;

import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.Similarity.SimScorer;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;

/** I have copy pasted the BM25Similarity algorithm, code for code within this class.
 *  I will modify this until it resembles SDM similarity to the fullest.
 */
public class SDMSimilarity extends Similarity {
    private final float k1;
    private final float b;
    protected boolean discountOverlaps;
    private static final float[] LENGTH_TABLE = new float[256];

    public SDMSimilarity(float k1, float b) {
        this.discountOverlaps = true;
        if (Float.isFinite(k1) && k1 >= 0.0F) {
            if (!Float.isNaN(b) && b >= 0.0F && b <= 1.0F) {
                this.k1 = k1;
                this.b = b;
            } else {
                throw new IllegalArgumentException("illegal b value: " + b + ", must be between 0 and 1");
            }
        } else {
            throw new IllegalArgumentException("illegal k1 value: " + k1 + ", must be a non-negative finite value");
        }
    }

    public SDMSimilarity() {
        this(1.2F, 0.75F);
    }

    protected float idf(long docFreq, long docCount) {
        return (float)Math.log(1.0D + ((double)(docCount - docFreq) + 0.5D) / ((double)docFreq + 0.5D));
    }

//    protected float scorePayload(int doc, int start, int end, BytesRef payload) {
//        return 1.0F;
//    }

    protected float avgFieldLength(CollectionStatistics collectionStats) {
        return (float)((double)collectionStats.sumTotalTermFreq() / (double)collectionStats.docCount());
    }

    public final long computeNorm(FieldInvertState state) {
        int numTerms;
        if (state.getIndexOptions() == IndexOptions.DOCS && state.getIndexCreatedVersionMajor() >= 8) {
            numTerms = state.getUniqueTermCount();
        } else if (this.discountOverlaps) {
            numTerms = state.getLength() - state.getNumOverlap();
        } else {
            numTerms = state.getLength();
        }

        return (long)SmallFloat.intToByte4(numTerms);
    }

    public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats) {
        // CollectionStatistics holds statistics across all documents for scoring purposes
        // TermStatistics holds statistics for a specific term accross all documents for scoring purposes.
        long df = termStats.docFreq();
        long docCount = collectionStats.docCount();
        float idf = this.idf(df, docCount);
        return Explanation.match(idf,
                "idf, computed as log(1 + (N - n + 0.5) / (n + 0.5)) from:",
                new Explanation[]{Explanation.match(df, "n, number of documents containing term",
                        new Explanation[0]),
                        Explanation.match(docCount, "N, total number of documents with field",
                                new Explanation[0])});
    }

    public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics[] termStats) {
        double idf = 0.0D;
        List<Explanation> details = new ArrayList();
        TermStatistics[] var6 = termStats;
        int var7 = termStats.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            TermStatistics stat = var6[var8];
            Explanation idfExplain = this.idfExplain(collectionStats, stat);
            details.add(idfExplain);
            idf += (double)idfExplain.getValue().floatValue();
        }

        return Explanation.match((float)idf, "idf, sum of:", details);
    }

    public final SimScorer scorer(float boost, CollectionStatistics collectionStats, TermStatistics... termStats) {
        Explanation idf = termStats.length == 1 ? this.idfExplain(collectionStats, termStats[0]) : this.idfExplain(collectionStats, termStats);
        float avgdl = this.avgFieldLength(collectionStats);
        float[] cache = new float[256];

        for(int i = 0; i < cache.length; ++i) {
            cache[i] = this.k1 * (1.0F - this.b + this.b * LENGTH_TABLE[i] / avgdl);
        }

        return new SDMSimilarity.SDMScorer(boost, this.k1, this.b, idf, avgdl, cache);
    }

    public String toString() {
        return "SDM(k1=" + this.k1 + ",b=" + this.b + ")";
    }

    public final float getK1() {
        return this.k1;
    }

    public final float getB() {
        return this.b;
    }

    static {
        for(int i = 0; i < 256; ++i) {
            LENGTH_TABLE[i] = (float)SmallFloat.byte4ToInt((byte)i);
        }

    }

    private static class SDMScorer extends SimScorer {
        private final float boost;
        private final float k1;
        private final float b;
        private final Explanation idf;
        private final float avgdl;
        private final float[] cache;
        private final float weight;

        SDMScorer(float boost, float k1, float b, Explanation idf, float avgdl, float[] cache) {
            this.boost = boost;
            this.idf = idf;
            this.avgdl = avgdl;
            this.k1 = k1;
            this.b = b;
            this.cache = cache;
            this.weight = boost * idf.getValue().floatValue();
        }

        public float score(float freq, long encodedNorm) {
            double norm = (double)this.cache[(byte)((int)encodedNorm) & 255];
            return this.weight * (float)((double)freq / ((double)freq + norm));
        }

        public Explanation explain(Explanation freq, long encodedNorm) {
            List<Explanation> subs = new ArrayList(this.explainConstantFactors());
            Explanation tfExpl = this.explainTF(freq, encodedNorm);
            subs.add(tfExpl);
            return Explanation.match(this.weight * tfExpl.getValue().floatValue(), "score(freq=" + freq.getValue() + "), product of:", subs);
        }

        private Explanation explainTF(Explanation freq, long norm) {
            List<Explanation> subs = new ArrayList();
            subs.add(freq);
            subs.add(Explanation.match(this.k1, "k1, term saturation parameter", new Explanation[0]));
            float doclen = SDMSimilarity.LENGTH_TABLE[(byte)((int)norm) & 255];
            subs.add(Explanation.match(this.b, "b, length normalization parameter", new Explanation[0]));
            if ((norm & 255L) > 39L) {
                subs.add(Explanation.match(doclen, "dl, length of field (approximate)", new Explanation[0]));
            } else {
                subs.add(Explanation.match(doclen, "dl, length of field", new Explanation[0]));
            }

            subs.add(Explanation.match(this.avgdl, "avgdl, average length of field", new Explanation[0]));
            float normValue = this.k1 * (1.0F - this.b + this.b * doclen / this.avgdl);
            return Explanation.match((float)((double)freq.getValue().floatValue() / ((double)freq.getValue().floatValue() + (double)normValue)), "tf, computed as freq / (freq + k1 * (1 - b + b * dl / avgdl)) from:", subs);
        }

        private List<Explanation> explainConstantFactors() {
            List<Explanation> subs = new ArrayList();
            if (this.boost != 1.0F) {
                subs.add(Explanation.match(this.boost, "boost", new Explanation[0]));
            }

            subs.add(this.idf);
            return subs;
        }
    }
}
