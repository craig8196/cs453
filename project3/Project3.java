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


public class Project3 {
    public static boolean DEBUG = false;

    public static void main(String[] args) {
        // Get stopwords.
        ArrayList<String> stopwordTokens = tokenize(getText(new File("stopwords.txt")), null, false);
        HashSet<String> stopwords = new HashSet<String>(stopwordTokens);
        if(DEBUG) System.out.println(stopwordTokens);
        // Collect tokens and document names.
        File dir = new File("documents/");
        File[] files = dir.listFiles();
        ArrayList<Document> documents = new ArrayList<Document>();
        for(int i = 0; i < files.length; i++) {
            String text = getText(files[i]);
            Document document = new Document(files[i].toString(), tokenize(text, stopwords, true), text);
            documents.add(document);
        }
        
        QueryLog ql = new QueryLog(new File("query_log.txt"), TextTools.getDictionary(new File("dictionary.txt")));
        Index index = new Index(documents, stopwords, TextTools.getDictionary(new File("dictionary.txt")));
        index.setQueryLog(ql);
        String[] queries = {
            "sentenced to prision",
            "open cuort case",
            "entretainment group",
            "tv axtor",
            "scheduled movie screning",
            //~ "movie action",
        };
        
        try {
            PrintWriter writer = new PrintWriter("output.html", "UTF-8");
            for(int i = 0; i < queries.length; i++) {
                writer.println(index.query(queries[i]));
            }
            writer.close();
        } catch(Exception ex) {
            System.out.println(ex);
            System.exit(1);
        }
        
        return;
    }
    
    public static PorterStemmer stemmer = new PorterStemmer();
    public static Pattern p = Pattern.compile("\\p{Alpha}+");
    public static ArrayList<String> tokenize(String s, HashSet<String> stopwords, boolean stem) {
        ArrayList<String> result = new ArrayList<String>();
        Matcher m = p.matcher(s);
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
    
    public static String getText(File file) {
        StringBuilder result = new StringBuilder();
        try {
            InputStream in = Files.newInputStream(file.toPath());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while((line = br.readLine()) != null) {
                result.append(line);
            }
        } catch(Exception ex) {
        }
        
        return result.toString();
    }
}
