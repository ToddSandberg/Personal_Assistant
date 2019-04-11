import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class NewsScrape {
	public static String email = Credentials.GOOGLE_EMAIL;
	public static String mypassword = Credentials.GOOGLE_PASSWORD;
	public static void main(String [] args) {
		System.out.println(getArticle("https://news.google.com/articles/CBMiZmh0dHBzOi8vaGFja2FkYXkuY29tLzIwMTkvMDQvMDgvdGhlLTNkLXByaW50aW5nLWRyZWFtLWlzLXN0aWxsLWFsaXZlLWF0LTIwMTlzLW1pZHdlc3QtcmVwcmFwLWZlc3RpdmFsL9IBAA?hl=en-US&gl=US&ceid=US%3Aen"));
	}
	public static HashMap<String,String> getNews() {
		try {
			//Change to be wherever your chromedriver is
			System.setProperty("webdriver.chrome.driver",Settings.Chrome_Driver_Location);
		    WebDriver driver = new ChromeDriver();
		    //Login to google
		    driver.get("https://accounts.google.com/signin/v2/identifier?service=accountsettings&hl=en-US&continue=https%3A%2F%2Fmyaccount.google.com%2Fintro&csig=AF-SEnZoRokERrOqZFJq%3A1554749650&flowName=GlifWebSignIn&flowEntry=ServiceLogin");
		    driver.findElement(By.id("identifierId")).sendKeys(email,Keys.ENTER);
		    Thread.sleep(3000);
		    driver.findElement(By.name("password")).sendKeys(mypassword,Keys.ENTER);
		    Thread.sleep(4000);
		    //Navigate to your news
		    driver.navigate().to("https://news.google.com/foryou?hl=en-US&gl=US&ceid=US%3Aen");
		    //Get main area
		    WebElement main = driver.findElement(By.cssSelector(".lBwEZb.BL5WZb.xP6mwf"));
		    //Get subclasses of main area
		    List<WebElement> articles = main.findElements(By.xpath("*"));
		    //Hashmap of titles and links to articles
		    HashMap<String,String> titles = new HashMap<String,String>();
		    for(int x=0;x<articles.size();x++) {
		    	try {
		    		//Current article
			    	WebElement we = articles.get(x);
			    	//Get article title
			    	WebElement text = we.findElement(By.cssSelector("h3[class*='ipQwMb']"));
				    String title = text.getText();
				    //Get article link
				    String link = we.findElement(By.cssSelector("a[class*='VDXfz']")).getAttribute("href");
				    titles.put(title, link);
		    	}catch(Exception e) {
		    		e.printStackTrace();
		    	}
		    }
		    driver.close();
		    return titles;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getArticle(String address) {
		try {
			String text = "";
			//TODO change to your
			System.setProperty("webdriver.chrome.driver",Settings.Chrome_Driver_Location);
		    WebDriver driver = new ChromeDriver();
		    driver.get(address);
		    List<WebElement> stuff = driver.findElements(By.tagName("p"));
			for (WebElement headline : stuff) {
				text+=headline.getText()+" ";
			}
			driver.close();
			return text;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
