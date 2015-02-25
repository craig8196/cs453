import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.io.InputStreamReader;
import java.net.URLConnection;
import java.lang.StringBuilder;
import java.net.URL;
import java.io.BufferedReader;
import java.nio.charset.Charset;

public class Main {

    public static void main(String[] args) {
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d = sdf.parse("2006-04-22 23:51:18");

            System.out.println(d.toString());
            System.out.println(d.getTime());
        } catch (ParseException e) {
            System.out.println("error");
        }
        
        System.out.println(callURL("http://peacock.cs.byu.edu/CS453Proj2/?word1=fish&word2=pond"));

        return;
    }
    
    // Code from: http://crunchify.com/java-url-example-getting-text-from-url/
    public static String callURL(String myURL) {
		System.out.println("Requested URL:" + myURL);
		StringBuilder sb = new StringBuilder();
		URLConnection urlConn = null;
		InputStreamReader in = null;
		try {
			URL url = new URL(myURL);
			urlConn = url.openConnection();
			if (urlConn != null)
				urlConn.setReadTimeout(60 * 1000);
			if (urlConn != null && urlConn.getInputStream() != null) {
				in = new InputStreamReader(urlConn.getInputStream(),
						Charset.defaultCharset());
				BufferedReader bufferedReader = new BufferedReader(in);
				if (bufferedReader != null) {
					int cp;
					while ((cp = bufferedReader.read()) != -1) {
						sb.append((char) cp);
					}
					bufferedReader.close();
				}
			}
		in.close();
		} catch (Exception e) {
			throw new RuntimeException("Exception while calling URL:"+ myURL, e);
		} 
 
		return sb.toString();
	}
}


