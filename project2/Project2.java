import java.util.HashMap;
import java.util.Collections;
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
import java.io.Console;

// Date handling
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Project2 {
    public static void main(String[] args) {
        try {
            ArrayList<String> sw = Project1.tokenize(Project1.getText(new File("stopwords.txt")), null, false);
            HashSet<String> stopwords = new HashSet<String>(sw);
            Trie trie = new Trie(stopwords);
            File file = new File("project2_trie_input.txt");
            InputStream in = Files.newInputStream(file.toPath());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while((line = br.readLine()) != null) { // Iterate through each line
                String[] parts = line.split("\t");
                if(parts.length == 3) {
                    String query = parts[0];
                    int occuranceCount = Integer.parseInt(parts[1]);
                    int modificationCount = Integer.parseInt(parts[2]);
                    trie.add(query, occuranceCount, modificationCount);
                }
            }
            //~ trie.printAll();
            
            QuerySuggester suggestor = new QuerySuggester(trie);
            Console c = System.console();
            while(true) {
                if(c == null) return;
                String query = c.readLine("Enter your query: ");
                if(query.equals("")) {
                    return;
                }
                ArrayList<QuerySuggestion> sq = suggestor.suggestQueries(query);
                if(sq.size() == 0) {
                    System.out.println("No suggestions.");
                }
                for(int sqIndex = 0; sqIndex < sq.size() && sqIndex < 10; sqIndex++) {
                    QuerySuggestion suggestion = sq.get(sqIndex);
                    System.out.println(suggestion);
                }
            }
        } catch(Exception e) {
            e.printStackTrace(System.out);
            System.out.println(e);
        }
        return;
    }
}
