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


public class Project1 {
    public static boolean DEBUG = false;
    
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
