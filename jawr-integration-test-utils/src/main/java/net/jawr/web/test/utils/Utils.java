package net.jawr.web.test.utils;

import static org.junit.Assert.assertEquals;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * Utility class.
 *  
 * @author ibrahim Chaehoi
 */
public class Utils {

	/**
	 * Removes the carriage return from he string passed in parameter.
	 * @param str the string
	 * @return a string without carriage return.
	 */
	public static String removeCarriageReturn(String str){
		return str.replaceAll("\r\n", "\n");
	}
	
	/**
	 * Removes the random references which are generated in the HTML page.
	 * @param content
	 * @return the content where the generated random references are removed.
	 */
	public static String removeGeneratedRandomReferences(String content){
		
		// remove session id
		content = content.replaceAll("\\.(png|gif|js|css)[^\\?% \"']*", ".$1");
		
		// Remove generated random parameter 
		content = content.replaceAll("\\.(png|gif|js|css)\\?d=[0-9]+", ".$1?d=11111");
		
		return content;
		
	}
	
	/**
	 * Assert that the content of the file retrieved from the class using the file name parameter
	 * equals the web response of the page
	 * @param clazz the class
	 * @param fileName the filename
	 * @param page the page
	 * @throws Exception if an exception occurs.
	 */
	public static void assertContentEquals(Class<?> clazz, String fileName, Page page) throws Exception{
		
		String content = FileUtils.readContent(clazz, fileName);
		String result = Utils.removeCarriageReturn(page.getWebResponse().getContentAsString());
		if(page instanceof HtmlPage){
			result = Utils
			.removeGeneratedRandomReferences(result);
		}
		assertEquals(content, result);
	}
	
	/**
	 * Assert that the generated actual link is equals to the expected link.
	 * @param expectedLink the expected link
	 * @param actualLink the actual link
	 */
	public static void assertGeneratedLinkEquals(String expectedLink, String actualLink){
		assertEquals(expectedLink, Utils.removeGeneratedRandomReferences(actualLink));
	}
	
}
