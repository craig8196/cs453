import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;
import java.nio.file.Files;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.io.PrintWriter;
import java.lang.Math;

// Date handling
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Preprocessor {
    
    public static final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final long TEN_MINUTES = 10*60*1000; // Ten minutes in milliseconds.
    
    public static void main(String[] args) {
        String[] inputFiles = {
            "Clean-Data-01.txt",
            //~ "Clean-Data-02.txt",
            //~ "Clean-Data-03.txt",
            //~ "Clean-Data-04.txt",
            //~ "Clean-Data-05.txt",
            //~ "clean-data-test.txt", 
        };
        
        ArrayList<String> sw = Project1.tokenize(Project1.getText(new File("stopwords.txt")), null, false);
        HashSet<String> stopwords = new HashSet<String>(sw);
        Trie trie = new Trie(stopwords); // Used to sanitize queries
        
        HashMap<String, Pair> queryLogs = new HashMap<String, Pair>();
        for(int i = 0; i < inputFiles.length; i++) {
            String fileName = inputFiles[i];
            File file = new File(fileName);
            try {
                InputStream in = Files.newInputStream(file.toPath());
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line = br.readLine(); // Skip first line, contains column names
                int previousUser = -1;
                String previousQuery = null;
                long previousTime = 0;
                int currentUser = -2;
                String currentQuery = null;
                long currentTime = 0;
                while((line = br.readLine()) != null) { // Iterate through each line
                    String[] parts = splitLine(line, '\t');
                    int incrementMod = 0;
                    if(parts.length == 3) {
                        try {
                            currentUser = Integer.parseInt(parts[0]);
                            currentQuery = trie.sanitizeQuery(parts[1]);
                            currentTime = DATE_FORMATER.parse(parts[2]).getTime();
                            if(previousUser == currentUser) {
                                if(Math.abs(currentTime - previousTime) <= TEN_MINUTES && modifiedQuery(previousQuery, currentQuery)) { // Valid query to check for modification
                                    incrementMod = 1;
                                }
                            }
                            previousUser = currentUser;
                            previousQuery = currentQuery;
                            previousTime = currentTime;
                            if(!queryLogs.containsKey(currentQuery)) {
                                Pair temp = new Pair();
                                temp.left = 1;
                                temp.right = incrementMod;
                                queryLogs.put(currentQuery, temp);
                            } else {
                                Pair temp = queryLogs.get(currentQuery);
                                temp.left += 1; // Total number of queries
                                temp.right += incrementMod; // Total number of modifications
                            }
                        } catch(Exception e) {
                            System.out.println(e);
                            continue;   
                        }
                    }
                }
            } catch(Exception e) {
                System.out.println(e);
            }
        }
        
        try {
            PrintWriter writer = new PrintWriter("project2_trie_input.txt", "UTF-8");
            for(String key: queryLogs.keySet()) {
                Pair pair = queryLogs.get(key);
                if(pair.left > 1 && key.length() > 2) {
                    writer.println(key + "\t" + pair.left + "\t" + pair.right);
                }
            }
            writer.close();
        } catch(Exception e) {
            System.out.println(e);
        }
        
        return;
    }
    
    public static String[] splitLine(String line, char splitChar) {
        return line.split("" + splitChar);
    }
    
    public static boolean modifiedQuery(String first, String second) {
        if(first.equals(second)) {
            return false;
        }
        String[] p1 = first.split(" ");
        String[] p2 = second.split(" ");
        int maxLength = p1.length;
        if(p2.length < p1.length) {
            maxLength = p2.length;
        }
        
        boolean isSubstring = true;
        for(int i = 0; i < maxLength; i++) {
            if(!p1[i].equals(p2[i])) {
                isSubstring = false;
                break;
            }
        }
        return isSubstring;
    }
    
}
