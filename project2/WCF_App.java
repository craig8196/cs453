//February 10, 2015:
//Example Java Class to access the correlation value of two given words

//If the two given words are same, the correlation value is 1

//If any of the words is not a correctly-spelled word, e.g., "asdfg",
//or if there is no correlation value for the word,  the returned
//correlation value is -1 (error value)

//For valid words in the word-correlation matrix, the correlation value
//is between 0 and 1

//The program needs Jsoup library
package winter2015_TAWork;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WCF_App {


	public static void main(String[] args) {
		try{

			String w1="cinema"; //stemmed version of a word
			String w2="movi"; //stemmed version of a word


			String myURL = "http://peacock.cs.byu.edu/CS453Proj2/?word1="+w1+"&word2="+w2;

			System.out.println("Fetching content: "+myURL);

			Document pageDoc = Jsoup.connect(myURL).get();
			String htmlContent = pageDoc.html();		
			Document contentDoc = Jsoup.parse(htmlContent);
			String contentVal = contentDoc.body().text();
			
			System.out.println(contentVal);

			Double val= Double.parseDouble(contentVal);

			System.out.println(val);

		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

}

////OUTPUT BELOW:
/*
Fetching content: http://peacock.cs.byu.edu/CS453Proj2/?word1=cinema&word2=movi
9.5289800583487E-7
9.5289800583487E-7
*/
