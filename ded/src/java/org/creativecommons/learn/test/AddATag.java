package org.creativecommons.learn.test;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.searcher.Hit;
import org.apache.nutch.searcher.HitDetails;
import org.apache.nutch.searcher.Hits;
import org.apache.nutch.searcher.NutchBean;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.Summary;
import org.apache.nutch.util.NutchConfiguration;
import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.oercloud.Resource;
import org.xml.sax.SAXException;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebResponse;

public class AddATag extends DiscoverEdTestCase {

	public void test() throws SAXException, IOException {

		// Create a new resource with a title

		// FIXME: Add some data to Lucene that triggers one result for "water"
		// with the title "Water-themed result"
		String titleOfFirstResult = "Water-themed result";

		// You visit the general search page of a DiscoverEd instance
		WebConversation wc = new WebConversation();
		// FIXME: Use the properties file to figure out what URL to use below.
		String generalSearchPageURL = "http://localhost:8080/";
		WebResponse searchPageResponse = wc.getResponse(generalSearchPageURL);

		// You search for "water"
		WebForm searchForm = searchPageResponse.getForms()[0];
		searchForm.setParameter("query", "water");
		searchForm.submit();

		// You see some results for "water"
		WebResponse resultsPageResponse = wc.getCurrentPage();
		WebLink link = resultsPageResponse.getLinkWith(titleOfFirstResult);
		//assertNotNull(link);

		// You can click a button next to the first result labelled "Add a tag"
		// You can type "waste" into a field
		// You click submit
		// The result on the page immediately appears tagged with "waste"
		// You go back to the general search page and search again for
		// "water waste"
		// You see the result you edited
	}

	public void testControllerAddsTag() throws IOException, InterruptedException {

		String url = "http://a6.creativecommons.org/~raffi/html_for_discovered_unit_tests/AddATag/resource.html";
		crawlURLs(url);

		// <input type='hidden' value=''>
		org.creativecommons.learn.Tag.add("luceneID", "waste");

		// Assert that the Lucene document we edited appears in the results
		//assertSame(show[0].getUniqueKey(), "luceneID");

	}
	
	public void testAddAResourceAndFindItAgain() throws IOException, InterruptedException {
		
		// Add a URL that contains the string "Hi I'm a webpage"
		String url = "http://a6.creativecommons.org/~raffi/html_for_discovered_unit_tests/AddATag/resource.html";
		crawlURLs(url);

		// Use Nutch to search the index for the string "webpage"
		Hit[] hits = getUrlsOfHitsForAStringyQuery("webpage");
		
		System.out.println(hits[0].toString());
		
		// Find the webpage
		assertSame(hits[0].toString(), "qwer");
	}
	
	public static Hit[] getHitsForSearchTerm(String searchTerm) throws IOException {
		// Search Nutch for a particular search term.
		
		final Configuration conf = NutchConfiguration.create();
		final NutchBean bean = new NutchBean(conf);

		// Search the Lucene index for "waste"
		final Query query = Query.parse(searchTerm, conf);
		query.getParams().setMaxHitsPerDup(0);
		final Hits hits = bean.search(query);
		final int length = (int) Math.min(hits.getLength(), 10);
		final Hit[] hits2Show = hits.getHits(0, length);
		return hits2Show;
	}
}
