package org.creativecommons.learn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public abstract class DiscoverEdTestCase extends TestCase {
	
	
	protected String[] list_of_quadstores_used = {
			"http://other.example.com/#site-configuration",
			"http://example.com/#site-configuration",
			"http://creativecommons.org/#site-configuration",
			"http://ocw.nd.edu/",
			"http://example.com/",
			"http://ocw.nd.edu/courselist/rss",
			"http://a6.creativecommons.org/~raffi/html_for_discovered_unit_tests/rss_pointing_to_i_know_my_title.xml",
			"http://a6.creativecommons.org/~raffi/html_for_discovered_unit_tests/i_know_my_title.html"
	};
	
	public void setUp() {

		RdfStoreFactory.get().getGraphset().clear();
		
	}
	
	public void tearDown(){
	}
	
	public static ArrayList<String> runCmd(String cmd) throws IOException, InterruptedException {
		/*
		 * This method runs a String command with PROPERTY_CONTAINING_RDFSTORE_DB_NAME value available in the environment.
		 * 
		 * Things you should know:
		 *     * We are using "String cmd" not "String[] cmd" because we are lazy. In theory it's dangerous. We know that.
		 *     * We set PROPERTY_CONTAINING_RDFSTORE_DB_NAME so that the Nutch code plus our plugins can look inside
		 *       the database that the test suite modifies, typically "discovered_test". This way, when the test suite runs 
		 *       "bin/nutch whatever", the Jena store it looks inside is the one modified by the test suite.
		 *       This language is a bit stilted, but the point is to let the test suite modify the Jena store
		 *       without modifying your real database. That way, you can run the test suite safely in an environment
		 *       where you're also running a live instance of DiscoverEd.  
		 */

		Runtime run = Runtime.getRuntime();
		
		/* Create an array to represent the environment in which the new command will run.
		 * 
		 *  First, we copy the existing environment...
		 */
		
		HashMap<String, String> environment = new HashMap<String, String>(System.getenv());
		
		// We need to reformat this map as a list of strings, each having the form "name=value"
		ArrayList<String> environmentReformatted = new ArrayList<String>();
		
		for (Map.Entry<String, String> entry: environment.entrySet()) {
			environmentReformatted.add(entry.getKey() + "=" + entry.getValue());
		}
		
		/* Then, add our new environment variable */
		environment.put("PROPERTY_CONTAINING_RDFSTORE_DB_NAME", "rdfstore.db.database_name_for_test_suite");
		
		// Finally we need to convert the environment into a String array
		String[] x = {};
		String[] environmentReformattedAgain = environmentReformatted.toArray(x);
		
		/* Finally, call run.exec() with our environment array as the second argument. */
		
		Process pr = run.exec(cmd, environmentReformattedAgain);
		pr.waitFor() ;
		BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) );
		
		String line = "";
		ArrayList<String> lines = new ArrayList<String>();
		
		while (line != null) {
			lines.add(line);
			System.out.println(line);
			line = buf.readLine();
		}
		return lines;
	}

	public void crawlURLs(ArrayList<String> urls) throws IOException, InterruptedException, Exception {
		// Take a list of URLs and prepare them for indexing.
		
		// First we need a URLs file
		runCmd("mkdir urls_dir");
		BufferedWriter writer = new BufferedWriter(new FileWriter(
				"urls_dir/list_of_urls"));
		for (String url : urls) {
			writer.write(url + "\n");
		}
		writer.close();

		runCmd("rm -rf crawl/");

		// Then we crawl.
        String[] crawlArguments = "crawl urls_dir -depth 1".split(" ");
        org.apache.nutch.crawl.Crawl.main(crawlArguments);
	}
	
	public void crawlURLs(String url) throws IOException, InterruptedException, Exception {
		ArrayList<String> urls = new ArrayList<String>();
		urls.add(url);
		crawlURLs(urls);
	}
	
	public static ArrayList<String> getUrlsOfHitsForAStringyQuery(String q) throws IOException, InterruptedException {
		// Pardon the weird name, we just didn't want to call it a query string
		
		String searchCommand = "bin/nutch org.apache.nutch.searcher.NutchBean " + q;
		ArrayList<String> output = runCmd(searchCommand);
		ArrayList<String> urls = new ArrayList<String>();
		int lineNumber = 0;
		for (String line: output) {
			if (lineNumber % 2 == 0 && lineNumber > 0) {
				urls.add(line);
				System.err.println("Adding 'url': " + line);
			}
			lineNumber++;
		}
		return urls;
	}

}
