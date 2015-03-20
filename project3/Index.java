import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.lang.Math;
import java.util.Collections;


public class Index {
    
    private HashMap<String, HashMap<Document, Integer>> index = null; // Token: { Document: Count }
    private HashMap<String, Integer> documentCountIndex = null; // Token: Document Count
    private HashMap<String, Integer> tokenCountIndex = null; // Token: Instance Count (across corpus)
    private double totalDocuments = 0; // N
    private HashSet<String> stopwords = null;
    private HashSet<String> dictionary = null;
    private HashMap<String, HashSet<String>> soundexDictionary = null;
    private int maxDocuments = 5;
    private int totalWords = 0;
    private QueryLog queryLog = null;
    private String soundexCode = null;
    private HashSet<String> suggestions = null;
    private HashMap<String, Integer> unstemmedWordCount = null;
    
    public Index(ArrayList<Document> documents, HashSet<String> stopwords, HashSet<String> dictionary) {
        // Initialize
        this.index = new HashMap<String, HashMap<Document, Integer>>();
        this.documentCountIndex = new HashMap<String, Integer>();
        this.tokenCountIndex = new HashMap<String, Integer>();
        this.unstemmedWordCount = new HashMap<String, Integer>();
        this.stopwords = stopwords;
        this.setDictionary(dictionary);
        // Gather statistics
        this.totalDocuments += documents.size();
        boolean first = true;
        for(int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            HashMap<String, Integer> tokenCounts = doc.getTokenCounts();
            for(String token: tokenCounts.keySet()) {
                if(this.index.containsKey(token)) {
                    HashMap<Document, Integer> map = this.index.get(token);
                    map.put(doc, tokenCounts.get(token));
                    int count = this.documentCountIndex.get(token) + 1;
                    this.documentCountIndex.put(token, count);
                    this.tokenCountIndex.put(token, this.tokenCountIndex.get(token) + tokenCounts.get(token));
                } else {
                    HashMap<Document, Integer> map = new HashMap<Document, Integer>();
                    map.put(doc, tokenCounts.get(token));
                    this.index.put(token, map);
                    this.documentCountIndex.put(token, 1);
                    this.tokenCountIndex.put(token, tokenCounts.get(token));
                }
            }
            ArrayList<String> words = doc.getUnstemmedWords(stopwords);
            for(String word: words) {
                if(!this.unstemmedWordCount.containsKey(word)) {
                    this.unstemmedWordCount.put(word, 0);
                }
                this.unstemmedWordCount.put(word, this.unstemmedWordCount.get(word) + 1);
                this.totalWords++;
            }
            //~ if(Integer.parseInt(doc.toString()) == 101) {
                //~ first = false;
                //~ System.out.println(doc);
                //~ System.out.println(doc.tokenCounts);
                //~ System.out.println(doc.maxTypeTokenCount);
            //~ }
        }
        
    }
    
    public void setDictionary(HashSet<String> dictionary) {
        if(dictionary == null) {
            this.dictionary = new HashSet<String>();
        } else {
            this.dictionary = dictionary;
        }
        this.soundexDictionary = new HashMap<String, HashSet<String>>();
        for(String word: this.dictionary) {
            String soundexWord = TextTools.getSoundexCode(word);
            if(!this.soundexDictionary.containsKey(soundexWord)) {
                this.soundexDictionary.put(soundexWord, new HashSet<String>());
            }
            this.soundexDictionary.get(soundexWord).add(word);
            
        }
    }
    
    public void setQueryLog(QueryLog ql) {
        this.queryLog = ql;
    }
    
    /**
     * Query the search engine.
     * The query is corrected if there are misspelled words.
     * Then the documents are found and ranked.
     * Then the top 5 documents are taken and the results are stored in a String.
     * Return the resulting String.
     */
    public String query(String query) {
        this.soundexCode = null;
        this.suggestions = null;
    
        String originalQuery = query;
        query = this.correctQuery(query);
        System.out.println(query);
        
        ArrayList<String> queryParts = TextTools.tokenize(query, this.stopwords, true);
        HashMap<Document, Double> docScores = new HashMap<Document, Double>();
        if(Project3.DEBUG) {
            System.out.println("Index doc counts: ");
            System.out.println(this.documentCountIndex);
        }
        for(String queryWord: queryParts) {
            if(this.index.containsKey(queryWord)) {
                double idf = this.IDF(queryWord);
                if(Project3.DEBUG) System.out.println("IDF " + queryWord + " " + idf);
                for(Document doc: this.index.get(queryWord).keySet()) {
                    double tf = doc.TF(queryWord);
                    double score = tf*idf;
                    if(docScores.containsKey(doc)) {
                        docScores.put(doc, docScores.get(doc) + score);
                    } else {
                        docScores.put(doc, score);
                    }
                }
            }
        }
        
        StringBuilder result = new StringBuilder();
        result.append("<b>Original Query: </b>" + originalQuery + "<br />");
        result.append("<b>Corrected Query: </b>" + query + "<br />");
        if(this.soundexCode != null) {
            result.append("<b>Soundex Code: " + this.soundexCode + "</b><br />");
        }
        if(this.suggestions != null) {
            result.append("<b>Suggested Corrections: </b>");
            for(String sugg: this.suggestions) {
                result.append(sugg + " ");
            }
            result.append("<br />");
        }
        result.append("<br />");
        return result.toString() + this.topDocumentsToString(docScores, queryParts);
    }
    
    
    
    /**
     * Compute the IDF score of the given word.
     * word -- word to find the IDF score of
     * Return IDF score.
     */
    private double IDF(String word) {
        if(this.index.containsKey(word)) {
            return (Math.log(this.totalDocuments) - Math.log((double) this.documentCountIndex.get(word))) / Math.log(2);
        } else {
            return 0;
        }
    }
    
    
    /**
     * Correct misspelled words in the query.
     * query -- the sanitized query string
     * Return the corrected query.
     */
    public String correctQuery(String query) {
        System.out.println("Correcting Query: " + query);
        if(this.queryLog == null) {
            return query;
        } else {
            String[] queryParts = query.split(" ");
            for(int i = 0; i < queryParts.length; i++) {
                String part = queryParts[i];
                if(!this.dictionary.contains(part)) {
                    String soundexPart = TextTools.getSoundexCode(part);
                    if(this.soundexDictionary.containsKey(soundexPart)) {
                        String correction = null;
                        double maxProbability = 0;
                        HashSet<String> suggestions = this.soundexDictionary.get(soundexPart);
                        this.suggestions = new HashSet<String>();
                        for(String candidate: suggestions) {
                            if(TextTools.getEditDistance(part, candidate) < 3) {
                                this.suggestions.add(candidate);
                                double candidateProbability = this.getProbabilityWord(candidate)*this.queryLog.getProbabilityErrorWordGivenCorrection(part, candidate);
                                if(correction == null || maxProbability < candidateProbability) {
                                    correction = candidate;
                                    maxProbability = candidateProbability;
                                }
                            }
                        }
                        if(correction != null) {
                            queryParts[i] = correction;
                            this.soundexCode = TextTools.getSoundexCode(correction);
                        }
                    }
                }
            }
            query = this.join(queryParts, " ");
            System.out.println("Corrected query: " + query);
            return query;
        }
    }
    private String join(String[] strings, String between) {
        if(strings.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < strings.length - 1; i++) {
            result.append(strings[i]);
            result.append(between);
        }
        result.append(strings[strings.length - 1]);
        return result.toString();
    }
    
    private String topDocumentsToString(HashMap<Document, Double> docScores, ArrayList<String> query) {
        StringBuilder result = new StringBuilder();
        ArrayList<Pair> pairs = new ArrayList<Pair>();
        for(Document doc: docScores.keySet()) {
            pairs.add(new Pair(doc, docScores.get(doc)));
        }
        Collections.sort(pairs);
        
        for(int i = 0; i < this.maxDocuments && i < pairs.size(); i++) {
            result.append("<b>Document: " + pairs.get(i).doc.toString() + "</b><br />"); // Score: " + pairs.get(i).score + "\n\n");
            //~ result.append(pairs.get(i).doc.getFirstNChars(96) + "\n\n");
            result.append(pairs.get(i).doc.getTopNSentences(2, query, this.stopwords) + "<br /><br />");
            Document d = pairs.get(i).doc;
            //~ System.out.println(d);
            //~ for(int j = 0; j < query.size(); j++)
                //~ System.out.println(d.TF(query.get(j)));
        }
        return result.toString();
    }
    
    // The second part of the Noisy Channel Model
    // P(w)
    public double getProbabilityWord(String word) {
        //~ word = TextTools.stemmer.stem(word);
        if(this.tokenCountIndex.containsKey(word)) {
            return ((double)this.unstemmedWordCount.get(word))/((double)this.totalWords);
        } else {
            return 0.0;
        }
    }
    
    public String toString() {
        return this.index.toString() + "\n" + this.documentCountIndex.toString() + "\n" + this.tokenCountIndex.toString();
    }
}
