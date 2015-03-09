import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.HashMap;


public class QueryLog {
    
    public static void main(String[] args) {
        // Test QueryLog
        QueryLog ql = new QueryLog(new File("query_log.txt"), TextTools.getDictionary(new File("dictionary.txt")));
        System.out.println(ql.getProbabilityErrorWordGivenCorrection("prisson", "prison"));
        System.out.println(ql.getProbabilityErrorWordGivenCorrection("screning", "screening"));
    }
    
    private HashSet<String> dictionary = null;
    private HashMap<Long, String[]> correctQueries = null;
    private HashMap<String, HashMap<Long, Integer>> misspelledWords = null;
    private HashMap<String, HashMap<String, Integer>> misspelledCounts = null;
    private HashMap<String, Integer> correctedCounts = null;
    
    public QueryLog(File logFile, HashSet<String> dictionary) {
        this.dictionary = dictionary;
        this.correctQueries = new HashMap<Long, String[]>();
        this.misspelledWords = new HashMap<String, HashMap<Long, Integer>>();
        try {
            InputStream in = Files.newInputStream(logFile.toPath());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine(); // Get rid of header.
            while((line = br.readLine()) != null) {
                // Do something with the line.
                String[] parts = line.split("\t");
                long sessionID = Long.parseLong(parts[0]);
                addSessionQuery(sessionID, parts[1]);
            }
        } catch(Exception e) {
            System.out.println("Failed to load log file.");
        }
        initializeCounts();
    }
    // Helper function to tell if a misspelled word exists.
    private boolean containsMisspelling(String[] words) {
        for(int i = 0; i < words.length; i++) {
            if(!this.dictionary.contains(words[i])) {
                return true;
            }
        }
        return false;
    }
    // Helper function to parse query logs.
    private void addSessionQuery(long sessionID, String query) {
        String[] queryParts = TextTools.sanitize(query).split(" ");
        if(containsMisspelling(queryParts)) {
            // Iterate through misspelled words.
            for(int i = 0; i < queryParts.length; i++) {
                String word = queryParts[i];
                if(!this.dictionary.contains(word)) {
                    if(!this.misspelledWords.containsKey(word)) {
                        this.misspelledWords.put(word, new HashMap<Long, Integer>());
                    }
                    HashMap<Long, Integer> sessionIndices = this.misspelledWords.get(word);
                    sessionIndices.put(sessionID, i);
                }
            }
        } else {
            this.correctQueries.put(sessionID, queryParts);
        }
    }
    private void initializeCounts() {
        this.misspelledCounts = new HashMap<String, HashMap<String, Integer>>();
        this.correctedCounts = new HashMap<String, Integer>();
        for(String e: this.misspelledWords.keySet()) {
            HashMap<Long, Integer> sessionIndices = this.misspelledWords.get(e);
            for(Long sessionID: sessionIndices.keySet()) {
                int index = sessionIndices.get(sessionID);
                if(this.correctQueries.containsKey(sessionID)) {
                    String[] parts = this.correctQueries.get(sessionID);
                    if(index < parts.length) {
                        String w = parts[index];
                        this.addWordCorrectionOccurance(w, e);
                        //~ if(TextTools.getSoundexCode(e) == TextTools.getSoundexCode(w)) {
                            //~ this.addWordCorrectionOccurance(w, e);
                        //~ } else {
                            //~ System.out.println("Words not misspelling of eachother: " + e + " " + w); 
                        //~ }
                    } else {
                        //~ System.out.println("Sessions misaligned: " + sessionID);
                    }
                } else {
                    //~ System.out.println("Missing correct session: " + sessionID);
                }
            }
        }
    }
    private void addWordCorrectionOccurance(String w, String e) {
        if(!this.misspelledCounts.containsKey(w)) {
            this.misspelledCounts.put(w, new HashMap<String, Integer>());
            this.correctedCounts.put(w, 0);
        }
        int totalCount = this.correctedCounts.get(w) + 1;
        this.correctedCounts.put(w, totalCount);
        HashMap<String, Integer> errorWords = this.misspelledCounts.get(w);
        if(!errorWords.containsKey(e)) {
            errorWords.put(e, 1);
        } else {
            errorWords.put(e, errorWords.get(e) + 1);
        }
    }
    
    // Part of the Noisy Channel Model.
    // P(e|w)
    public double getProbabilityErrorWordGivenCorrection(String e, String w) {
        double result = 0;
        //~ System.out.println(this.misspelledCounts);
        if(this.correctedCounts.containsKey(w)) {
            double denominator = (double)this.correctedCounts.get(w);
            double numerator = 0.0;
            HashMap<String, Integer> errorCounts = this.misspelledCounts.get(w);
            if(errorCounts.containsKey(e)) {
                numerator = (double)errorCounts.get(e);
            }
            
            result = numerator/denominator;
        }
        
        return result;
    }
}
