import java.util.ArrayList;
import java.util.Collections;
import java.lang.Math;

public class QuerySuggester {
    
    private Trie trie = null;
    private WCFRetriever wcfRetriever = new WCFRetriever();
    private static PorterStemmer stemmer = new PorterStemmer();
    
    public QuerySuggester(Trie trie) {
        this.trie = trie;
    }
    
    /**
     * Takes an original query and returns suggestions.
     * An empty array is returned if there are no suggestions.
     */
    public ArrayList<QuerySuggestion> suggestQueries(String originalQuery) {
        ArrayList<QuerySuggestion> results = new ArrayList<QuerySuggestion>();
        String sanitizedQuery = trie.sanitizeQuery(originalQuery);
        Trie.Node originalQueryNode = trie.find(sanitizedQuery);
        //~ System.out.println("Sanitized Query: " + sanitizedQuery);
        if(originalQueryNode != null) {
            ArrayList<Trie.Node> nodes = new ArrayList<Trie.Node>();
            ArrayList<String> suggestedQueries = new ArrayList<String>();
            StringBuilder sb = new StringBuilder();
            sb.append(originalQuery);
            originalQueryNode.findAll(nodes, suggestedQueries, sb);
            for(int i = 0; i < nodes.size(); i++) {
                Trie.Node sq = nodes.get(i);
                String sqString = suggestedQueries.get(i);
                if(!sqString.equals(sanitizedQuery) && sqString.split(" ").length > 1) {
                    double r = this.rank(sanitizedQuery, originalQueryNode, sqString, sq);
                    results.add(new QuerySuggestion(sqString, r));
                }
            }
        }
        
        Collections.sort(results);
        
        return results;
    }
    
    private double rank(String originalQuery, Trie.Node original, String suggQuery, Trie.Node sugg) {
        double result = 0;
        
        String[] parts1 = originalQuery.split(" ");
        String[] parts2 = suggQuery.split(" ");
        String word1 = "";
        if(parts1.length > 0) {
            word1 = parts1[parts1.length-1];
        }
        String word2 = "";
        if(parts2.length > 0) {
            word2 = parts2[parts2.length-1];
        }
        
        word1 = stemmer.stem(word1);
        word2 = stemmer.stem(word2);
        
        double normalizedFreq = ((double)sugg.getOccuranceCount())/((double)this.trie.maxOccuranceCount);
        double normalizedMod = ((double)sugg.getModificationCount())/((double)this.trie.maxModificationCount);
        double normalizedWCF = wcfRetriever.getWCFScore(word1, word2);
        double min = Math.min(normalizedFreq, normalizedMod);
        min = Math.min(min, normalizedWCF);
        
        result = (normalizedFreq + normalizedMod + normalizedWCF)/(1-min);
        
        //~ if(originalQuery.equals("tiger")) {
            //~ System.out.println(suggQuery + ": " + normalizedFreq + " " + normalizedMod + " " + normalizedWCF);
        //~ }
        
        return result;
    }
}
