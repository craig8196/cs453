import java.io.InputStreamReader;
import java.net.URLConnection;
import java.lang.StringBuilder;
import java.net.URL;
import java.io.BufferedReader;
import java.nio.charset.Charset;

public class WCFRetriever {

    private URLConnection conn = null;
    private InputStreamReader in = null;
    private static final double NORMALIZING_FACTOR = 1e-3;

    public WCFRetriever() {}
    
    public double getWCFScore(String word1, String word2) {
        double result = 0.0;
        if(word1.equals(word2)) {
            return result;
        }
        
        try {
            URL url = new URL("http://peacock.cs.byu.edu/CS453Proj2/?word1="+word1+"&word2="+word2);
            conn = url.openConnection();
            if(conn != null) {
                conn.setReadTimeout(60*1000);
            }
            if(conn != null && conn.getInputStream() != null) {
                in = new InputStreamReader(conn.getInputStream(), Charset.defaultCharset());
                BufferedReader buffer = new BufferedReader(in);
                if(buffer != null) {
                    StringBuilder sb = new StringBuilder();
                    int cp = 0;
                    while((cp = buffer.read()) != -1) {
                        sb.append((char) cp);
                    }
                    result = Double.parseDouble(sb.toString());
                    buffer.close();
                }
                in.close();
            }
        } catch(Exception e) {
            System.out.println(e);
        }
        
        if(result < 0) {
            result = 0;
        }
        
        if(result > 0) {
            result = result/NORMALIZING_FACTOR;
        }
        
        return result;
    }
}


