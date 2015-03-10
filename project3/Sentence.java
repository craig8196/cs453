import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.lang.Math;

public class Sentence implements Comparable<Sentence> {

    private String sentence = null;
    private int lineNumber = 0; // Actually sentence number.
    private double score = 0;

    public Sentence(String sentence, int lineNumber) {
        this.sentence = sentence;
        this.lineNumber = lineNumber;
    }
    
    public void score(ArrayList<String> queryParts, HashSet<String> stopwords) {
        double result = 0.0;
        
        // Whether the sentence is a heading
        if(isHeading()) {
            result += 1.0;
        }
        
        // 1st or 2nd line of the document
        if(this.lineNumber == 1 || this.lineNumber == 2) {
            result += 1.0;
        }
        
        // Total number of query terms in the sentence
        // Number of unique query terms in the sentence
        // Longest continuous run of query words in the sentence
        // Significance factor (density measure) on query words in the sentence
        // Mine 1: Number of terms in same stem class as query terms.
        // Mine 2: Negative number of non-query terms between extreme unique terms.
        // Mine 3: First sentence of paragraph.
        // Mine 4: Inverse word level edit distance.
        HashSet<String> queryWords = new HashSet<String>();
        HashSet<String> stemmedQueryWords = new HashSet<String>();
        for(String s: queryParts) {
            queryWords.add(s);
            stemmedQueryWords.add(TextTools.stemmer.stem(s));
        }
        HashSet<String> foundQueryWords = new HashSet<String>();
        String sentence = TextTools.removeApostrophes(this.sentence);
        ArrayList<String> tokens = TextTools.tokenize(sentence, null, false); // Unstemmed tokens.
        double numQueryTerms = 0;
        double numStemmedQueryTerms = 0;
        double maxRun = 0;
        double currentRun = 0;
        int indexFirstQueryTerm = -1;
        int indexSecondQueryTerm = -1;
        for(int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            String stem = TextTools.stemmer.stem(token);
            if(queryWords.contains(token)) {
                numQueryTerms++;
                currentRun++;
                if(indexFirstQueryTerm == -1) {
                    indexFirstQueryTerm = i;
                }
                indexSecondQueryTerm = i;
                foundQueryWords.add(token);
            } else {
                if(currentRun > maxRun) {
                    maxRun = currentRun;
                }
                currentRun = 0;
            }
            if(stemmedQueryWords.contains(stem)) {
                numStemmedQueryTerms++;
            }
        }
        double numUniqueQueryTerms = (double)foundQueryWords.size();
        double denominator = (double)Math.abs(indexFirstQueryTerm-indexSecondQueryTerm) + 1.0;
        double significanceFactor = (numQueryTerms*numQueryTerms)/denominator;
        
        result += numQueryTerms;
        result += numUniqueQueryTerms;
        result += maxRun;
        result += significanceFactor;
        // result += numStemmedQueryTerms;
        
        this.score = result;
    }
    private boolean isHeading() {
        return false;
    }
    
    
    public String toString() {
        String result = this.sentence.trim();
        return result;
    }
    
    public String toString(ArrayList<String> queryParts) {
        String result = this.sentence.trim();
        return result;
    }
    
    public int compareTo(Sentence s) {
        double temp = s.score - this.score;
        if (temp < 0) return -1;
        else if (temp > 0) return 1;
        else return 0;
    }
}
