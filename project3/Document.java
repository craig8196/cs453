import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


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
