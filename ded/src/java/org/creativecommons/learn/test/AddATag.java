package org.creativecommons.learn.test;

import java.io.IOException;
import java.util.ArrayList;

public class AddATag extends DiscoverEdTestCase {


	public void testControllerAddsTag() throws Exception {

		String url = "http://a6.creativecommons.org/~raffi/html_for_discovered_unit_tests/AddATag/resource.html";
		crawlURLs(url);

		// <input type='hidden' value=''>
		org.creativecommons.learn.Tag.add("luceneID", "waste");

		// Assert that the Lucene document we edited appears in the results
		//assertSame(show[0].getUniqueKey(), "luceneID");

	}
	
	public void testAddAResourceAndFindItAgain() throws Exception {
		
		// Add a URL that contains the string "Hi I'm a webpage"
		String url = "http://a6.creativecommons.org/~raffi/html_for_discovered_unit_tests/AddATag/resource.html";
		crawlURLs(url);

		// Use Nutch to search the index for the string "webpage"
		ArrayList<String> hits = getUrlsOfHitsForAStringyQuery("webpage");
	
		// Find the webpage
		assertSame(hits.get(0), url);
	}
	

}
