import java.util.HashSet;
import java.util.HashMap;

public class Sentence {

    private String sentence = null;
    private int lineNumber = 0;

    public Sentence(String sentence, int lineNumber) {
        this.sentence = sentence;
        this.lineNumber = lineNumber;
    }
    
    public double score(ArrayList<String> queryParts) {
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
        HashSet<String> queryWords = new HashSet<String>(queryParts);
        HashMap<String, Integer> counts = new HashMap<String, Integer>();
        String sentence = TextTools.removeApostrophes(this.sentence);
        String[] tokens = TextTools.tokenize(sentence);
        for(int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            
        }
        
        return result;
    }
    private boolean isHeading() {
        return false;
    }
    private 
}
