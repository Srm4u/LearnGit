import java.io.IOException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;


public class Authentication {
/* This is a comment*/
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

			System.setProperty("webdriver.chrome.driver","E:\\sel\\autoIT\\chromedriver.exe");
			
			WebDriver driver = new ChromeDriver();
			
			driver.get("https://www.engprod-charter.net");
			
			Runtime.getRuntime().exec("E:\\sel\\autoIT\\Ranjan\\Authentication.exe");

	}

}
