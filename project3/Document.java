import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Collections;
import java.text.BreakIterator;


public class Document {

    private String name = null;
    private ArrayList<String> tokens = null;
    private String text = null;
    private HashMap<String, Integer> tokenCounts = null;
    private double maxTypeTokenCount = 0; // max_l(freq(l, d))
    private static Pattern p = Pattern.compile("\\p{Digit}+");
    private static Pattern whiteSpace = Pattern.compile("\\s+");
    
    public Document(String name, ArrayList<String> tokens, String text) {
        this.name = name;
        this.tokens = tokens;
        this.text = text;
        this.tokenCounts = new HashMap<String, Integer>();
        for(String token: tokens) {
            if(this.tokenCounts.containsKey(token)) {
                this.tokenCounts.put(token, this.tokenCounts.get(token) + 1);
            } else {
                this.tokenCounts.put(token, 1);
            }
        }
        
        // Find the number of occurances of the max token 
        for(Integer i: this.tokenCounts.values()) {
            if(i > this.maxTypeTokenCount) {
                this.maxTypeTokenCount = i;
            }
        }
        
        if (Project3.DEBUG) {
            System.out.println(name);
            System.out.println(tokens);
            System.out.println(text);
            System.out.println(tokenCounts);
            System.out.println(maxTypeTokenCount);
        }
    }
    
    public boolean contains(String token) {
        return tokenCounts.containsKey(token);
    }
    
    public String toString() {
        try {
            Matcher m = p.matcher(this.name);
            m.find();
            return m.group(0);
        } catch(Exception ex) {
            return "-1";
        }
    }
    
    public String getFirstNChars(int numChars) {
        if(this.text.length() < numChars) {
            return this.text;
        } else {
            return this.text.substring(0, numChars);
        }
    }
    
    public String getFirstNTokens(int n) {
        int i = 0;
        int end = 0;
        Matcher m = whiteSpace.matcher(this.text);
        while(m.find() && i < n) {
            i++;
            end = m.end(0);
        }
        return this.text.substring(0, end);
    }
    
    public HashMap<String, Integer> getTokenCounts() {
        return this.tokenCounts;
    }
    
    public String getTopNSentences(int n, ArrayList<String> queryParts, HashSet<String> stopwords) {
        StringBuilder result = new StringBuilder();
        
        BreakIterator b = BreakIterator.getSentenceInstance();
        b.setText(this.text);
        
        int prev = 0;
        int curr = 0;
        int sentenceNumber = 1;
        ArrayList<Sentence> sentences = new ArrayList<Sentence>();
        //~ while((curr = b.next()) != BreakIterator.DONE) {
            //~ Sentence s = new Sentence(this.text.substring(prev, curr), sentenceNumber);
            //~ s.score(queryParts, stopwords);
            //~ sentences.add(s);
            //~ prev = curr;
            //~ sentenceNumber++;
        //~ }
        String[] temp = this.text.split("[.]");
        for(int i = 0; i < temp.length; i++) {
            Sentence s = new Sentence(temp[i], sentenceNumber);
            s.score(queryParts, stopwords);
            sentences.add(s);
        }
        
        Collections.sort(sentences);
        
        for(int i = 0; i < n && i < sentences.size(); i++) {
            result.append(sentences.get(i).toString());
            result.append("... ");
        }
        
        return result.toString();
    }
    
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    
    public double TF(String token) {
        double result = 0;
        if(this.tokenCounts.containsKey(token)) {
            result = ((double) this.tokenCounts.get(token)) / this.maxTypeTokenCount;
        }
        if(Project3.DEBUG) System.out.println("TF " + this.name + " " + token + " " + result);
        return result;
    }
}
