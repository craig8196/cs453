import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.Math;


/**
 * Simple tools to manipulate text for C S 453 projects.
 */
public class TextTools {

    public static void main(String[] args) {
        // Test getSoundexCode
        String[] soundexInput = {
            "a",
            "extenssions",
            "extensions",
            "marshmellow",
            "marshmallow",
            "brimingham",
            "birmingham",
            "poiner",
            "pointer",
        };
        String[] soundexOutput = {
            "A000",
            "E235",
            "E235",
            "M625",
            "M625",
            "B655",
            "B655",
            "P560",
            "P536",
        };
        for(int i = 0; i < soundexInput.length; i++) {
            try {
                assert soundexOutput[i].equals(getSoundexCode(soundexInput[i]));
            } catch(AssertionError e) {
                System.out.println(soundexInput[i]);
                System.out.println(getSoundexCode(soundexInput[i]));
                System.out.println(soundexOutput[i]);
            }
        }
        
        // Test getEditDistance
        String[] getEditDistanceWord1 = {
            "",
            "hello",
            "kitten",
            "sitting",
            "Saturday",
            "Sunday",
        };
        String[] getEditDistanceWord2 = {
            "",
            "hello",
            "sitting",
            "kitten",
            "Sunday",
            "Saturday",
        };
        int[] getEditDistanceOutput = {
            0,
            0,
            3,
            3,
            3,
            3,
        };
        for(int i = 0; i < getEditDistanceOutput.length; i++) {
            try {
                assert getEditDistanceOutput[i] == getEditDistance(getEditDistanceWord1[i], getEditDistanceWord2[i]);
            } catch(AssertionError e) {
                System.out.println(getEditDistanceWord1[i]);
                System.out.println(getEditDistanceWord2[i]);
                System.out.println(getEditDistanceOutput[i]);
            }
        }
        
        // Test getDictionary
        int DICTIONARY_SIZE = 439396; // Apostrophes are removed so the number of lines won't match.
        HashSet<String> dictionary = getDictionary(new File("dictionary.txt"));
        assert DICTIONARY_SIZE == dictionary.size();
    }
    
    private TextTools() {}
    
    /**
     * Removes all characters not in the set [a-zA-Z\{whitespace}] 
     * and puts the string in lowercase. Also, whitespace is all replaced 
     * with spaces.
     * Return sanitized String.
     */
    public static String sanitize(String text) {
        text = text.toLowerCase();
        text = text.replaceAll("[\\p{Space}]+", " ");
        text = text.replaceAll("[^a-z ]", "");
        return text.trim();
    }
    
    /**
     * Return same string without apostrophes.
     */
    public static String removeApostrophes(String text) {
        return text.replaceAll("[']+", "");
    }
    
    /**
     * Tokenize the text spliting on spaces and remove any tokens if they
     * are in the stopwords list.
     * Return tokens.
     */
    public static PorterStemmer stemmer = new PorterStemmer();
    public static Pattern p = Pattern.compile("\\p{Alpha}+");
    public static ArrayList<String> tokenize(String text, HashSet<String> stopwords, boolean stem) {
        ArrayList<String> result = new ArrayList<String>();
        Matcher m = p.matcher(text);
        while(m.find()) {
            String token = m.group(0).toLowerCase();
            try {
                if(token.length() > 1) {
                    String tok = null;
                    if(stem) tok = stemmer.stem(token);
                    else tok = token;
                    //~ tok = stemmer.stem(token); // This is for testing like the next line is...
                    //~ token = tok; // This is inserted to experiment with checking stems in stopwords.
                    
                    if(stopwords == null) {
                        result.add(tok);
                    } else if(!stopwords.contains(token)) {
                        result.add(tok);
                    }
                }
            } catch(Exception ex) {
                System.out.println("\""+token+"\"");
                System.out.println(ex);
                System.exit(1);
            }
        }
        return result;
    }
    
    /**
     * file -- valid file; not null
     * Return the text from the file.
     */
    public static String getText(File file) {
        StringBuilder result = new StringBuilder();
        try {
            InputStream in = Files.newInputStream(file.toPath());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while((line = br.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }
        } catch(Exception ex) {
        }
        
        return result.toString();
    }
    
    /**
     * Implementation of the soundex code as specified in
     * "Search Engines: Information Retrieval in Practice" by Croft, Metzler, and Strohman (page 195).
     * word -- the word to generate a soundex code from; not null
     * Return the soundex code as a string.
     */
    public static String getSoundexCode(String word) {
        if(word.length() == 0) {
            return word;
        }
        
        StringBuilder result = new StringBuilder(word.toLowerCase());
        
        // Keep the first letter (in uppercase).
        char c = result.charAt(0);
        result.setCharAt(0, Character.toUpperCase(c));
        
        
        for(int i = 1; i < result.length(); i++) {
            c = result.charAt(i);
            switch(c) {
                // Replace these letters with hyphens: a,e,i,o,u,y,h,w
                case 'a':
                case 'e':
                case 'i':
                case 'o':
                case 'u':
                case 'y':
                case 'h':
                case 'w': c = '-'; break;
                // Replace the other letters by numbers as follows:
                // 1: b,f,p,v
                // 2: c,g,j,k,q,s,x,z
                // 3: d,t
                // 4: l
                // 5: m,n
                // 6: r
                case 'b':
                case 'f':
                case 'p':
                case 'v': c = '1'; break;
                case 'c':
                case 'g':
                case 'j':
                case 'k':
                case 'q':
                case 's':
                case 'x':
                case 'z': c = '2'; break;
                case 'd':
                case 't': c = '3'; break;
                case 'l': c = '4'; break;
                case 'm':
                case 'n': c = '5'; break;
                case 'r': c = '6'; break;
            }
            result.setCharAt(i, c);
        }
        
        // Delete adjacent repeats of a number.
        for(int i = 1; i < result.length() - 1;) {
            c = result.charAt(i);
            char next = result.charAt(i + 1);
            if(c == next) {
                result.deleteCharAt(i + 1);
            } else {
                i++;
            }
        }
        
        // Delete the hyphens.
        for(int i = 1; i < result.length(); i++) {
            if(result.charAt(i) == '-') {
                result.deleteCharAt(i);
            }
        }
        
        // Keep the first three numbers or pad out with zeros.
        int RESULT_LENGTH = 4; // The result must be this length.
        if(result.length() < 4) {
            int diff = RESULT_LENGTH - result.length();
            for(int i = 0; i < diff; i++) {
                result.append('0');
            }
        }
        
        return result.substring(0, RESULT_LENGTH);
    }
    
    /**
     * Compute the Levenshtein Distance between two words.
     * It is the users responsibility to take case of the upper or lower case letters.
     * word1, word2 -- the words to get the minimal edit distance between; not null
     * Return the edit distance between the two words.
     */
    public static int getEditDistance(String word1, String word2) {
        int lenWord1 = word1.length() + 1;
        int lenWord2 = word2.length() + 1;
        int[][] distanceMatrix = new int[lenWord1][lenWord2];
        
        for(int i = 0; i < lenWord1; i++) {
            distanceMatrix[i][0] = i;
        }
        for(int j = 0; j < lenWord2; j++) {
            distanceMatrix[0][j] = j;
        }
        
        for(int i = 1; i < lenWord1; i++) {
            for(int j = 1; j < lenWord2; j++) {
                int cost = 0;
                if(word1.charAt(i - 1) != word2.charAt(j - 1)) {
                    cost = 1;
                }
                int value = Math.min(distanceMatrix[i - 1][j] + 1, 
                                     distanceMatrix[i][j - 1] + 1);
                value = Math.min(distanceMatrix[i - 1][j - 1] + cost, value);
                distanceMatrix[i][j] = value;
            }
        }
        
        return distanceMatrix[lenWord1 - 1][lenWord2 - 1];
    }
    
    /**
     * Compute the Levenshtein Distance between two words.
     * It is the users responsibility to take case of the upper or lower case letters.
     * word1, word2 -- the words to get the minimal edit distance between; not null
     * Return the edit distance between the two words.
     */
    public static int getWordEditDistance(ArrayList<String> word1, ArrayList<String> word2) {
        int lenWord1 = word1.size() + 1;
        int lenWord2 = word2.size() + 1;
        int[][] distanceMatrix = new int[lenWord1][lenWord2];
        
        for(int i = 0; i < lenWord1; i++) {
            distanceMatrix[i][0] = i;
        }
        for(int j = 0; j < lenWord2; j++) {
            distanceMatrix[0][j] = j;
        }
        
        for(int i = 1; i < lenWord1; i++) {
            for(int j = 1; j < lenWord2; j++) {
                int cost = 0;
                if(!word1.get(i-1).equals(word2.get(j-1))) {
                    cost = 1;
                }
                int value = Math.min(distanceMatrix[i - 1][j] + 1, 
                                     distanceMatrix[i][j - 1] + 1);
                value = Math.min(distanceMatrix[i - 1][j - 1] + cost, value);
                distanceMatrix[i][j] = value;
            }
        }
        
        return distanceMatrix[lenWord1 - 1][lenWord2 - 1];
    }
    
    public static HashSet<String> getDictionary(File wordFile) {
        String text = getText(wordFile);
        text = sanitize(text);
        HashSet<String> result = new HashSet<String>();
        ArrayList<String> tokens = tokenize(text, null, false);
        return new HashSet<String>(tokens);
    }
}
