import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.lang.Math;
import java.util.regex.Matcher;

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
        // Mine 1: Number of terms in same stem class as query terms
        // Mine 2: Negative number of non-query terms between extreme unique terms
        // Mine 3: Max Words / Word level edit distance
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
        double numberNonQueryTerms = 0.0;
        int indexFirstQueryTerm = -1;
        int indexSecondQueryTerm = -1;
        for(int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            String stem = TextTools.stemmer.stem(token);
            System.out.println(token);
            System.out.println(stem);
            if(!stopwords.contains(token)) {
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
                if(stemmedQueryWords.contains(stem) && !stemmedQueryWords.contains(token)) {
                    numStemmedQueryTerms++;
                }
                if(!stemmedQueryWords.contains(stem)) {
                    numberNonQueryTerms++;
                    System.out.println("here2");
                }
            }
        }
        
        if(currentRun > maxRun) {
            maxRun = currentRun;
        }
        
        double numUniqueQueryTerms = (double)foundQueryWords.size();
        double denominator = (double)Math.abs(indexFirstQueryTerm-indexSecondQueryTerm) + 1.0;
        double significanceFactor = (numQueryTerms*numQueryTerms)/denominator;
        
        System.out.println(indexFirstQueryTerm);
        
        
        //~ for(int j = indexFirstQueryTerm; j != indexSecondQueryTerm; j++) {
            //~ String token = tokens.get(j);
            //~ System.out.println("here");
            //~ if(!stemmedQueryWords.contains(TextTools.stemmer.stem(token))) {
                //~ numberNonQueryTerms++;
            //~ }
        //~ }
        double editDistance = (double)TextTools.getWordEditDistance(queryParts, tokens);
        if(editDistance == 0) {
            editDistance++;
        }
        double editDistanceBonus = (double) Math.max(tokens.size(), queryParts.size()) / editDistance;
        
        System.out.println(this.sentence);
        result += numQueryTerms;
        System.out.println(numQueryTerms);
        result += numUniqueQueryTerms;
        System.out.println(numUniqueQueryTerms);
        result += maxRun;
        System.out.println(maxRun);
        result += significanceFactor;
        System.out.println(significanceFactor);
        // Mine
        result += 2*numStemmedQueryTerms;
        result += Math.max(numQueryTerms, 2.0)/Math.max(numberNonQueryTerms, 1.0);
        System.out.println(numberNonQueryTerms);
        result += editDistanceBonus;
        
        this.score = result;
    }
    private boolean isHeading() {
        return false;
    }
    
    
    public String toString() {
        String result = this.sentence.trim();
        System.out.println("Score: " + this.score);
        System.out.println("Sentence: " + result);
        return result;
    }
    
    public String toString(HashSet<String> stemmedQueryParts) {
        System.out.println("Score: " + this.score);
        System.out.println("Sentence: " + this.sentence.trim());
        StringBuilder result = new StringBuilder();
        String text = this.sentence.trim();
        
        int previousIndex = 0;
        
        Matcher m = TextTools.p.matcher(text);
        while(m.find()) {
            String token = m.group(0);
            int tokenIndex = m.start(0);
            String stemmedToken = TextTools.stemmer.stem(TextTools.removeApostrophes(token.toLowerCase()));
            
            String preceedingText = text.substring(previousIndex, tokenIndex);
            result.append(preceedingText);
            
            if(stemmedQueryParts.contains(stemmedToken)) {
                result.append("<b>");
                result.append(token);
                result.append("</b>");
            } else {
                result.append(token);
            }
            
            previousIndex = tokenIndex + token.length();
        }
        
        String endingText = text.substring(previousIndex, text.length());
        result.append(endingText);
        result.append(".");
        
        return result.toString();
    }
    
    public int compareTo(Sentence s) {
        double temp = s.score - this.score;
        if (temp < 0) return -1;
        else if (temp > 0) return 1;
        else return 0;
    }
}
