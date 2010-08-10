package org.creativecommons.learn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;
import org.creativecommons.learn.oercloud.Resource;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.searcher.Hit;
import org.apache.nutch.searcher.Hits;
import org.apache.nutch.searcher.NutchBean;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.util.NutchConfiguration;

public class TestMinusCurator extends DiscoverEdTestCase {

	public void dontRunThisReallyLongTestForNow() throws Exception {

		final String URL_PREFIX = "http://a6.creativecommons.org/~raffi/html_for_discovered_unit_tests/MinusCurator/";
		final String PAGE_ONE_URL = URL_PREFIX + "1.html";
		final String PAGE_TWO_URL = URL_PREFIX + "2.html";
		final String PAGE_THREE_URL = URL_PREFIX + "3.html";
		
		// Let's build the plugins
		runCmd("ant");

		// Add some pages to the database...
		
		RdfStore siteConfigStore = RdfStore.forDEd();

		Curator nsdl = new Curator("http://example.com/#nsdl");
		nsdl.setName("nsdl");
		siteConfigStore.save(nsdl);
		
		Feed nsdlFeed = new Feed("http://example.com/#nsdlfeed");
		nsdlFeed.setCurator(nsdl);
		siteConfigStore.save(nsdlFeed);
		
		Curator ocw = new Curator("http://example.com/#ocw");
		ocw.setName("ocw");
		siteConfigStore.save(ocw);

		Feed ocwFeed = new Feed("http://example.com/#ocwfeed");
		ocwFeed.setCurator(ocw);
		siteConfigStore.save(ocwFeed);
		
		Collection<String> justChemistry = new ArrayList<String>();
		justChemistry.add("chemistry");
		
		// One page was tagged by the NSDL with subject:Chemistry
		RdfStore nsdlFeedStore = RdfStore.forProvenance(nsdlFeed.getUrl());
		Resource pageOneAccordingToNSDL = new Resource(PAGE_ONE_URL);

		pageOneAccordingToNSDL.setSubjects(justChemistry);
		nsdlFeedStore.save(pageOneAccordingToNSDL);
		
		// A second page was tagged by MIT OCW with subject:Chemistry
		RdfStore ocwFeedStore = RdfStore.forProvenance(ocwFeed.getUrl());
		Resource pageTwoAccordingToOCW = new Resource(PAGE_TWO_URL);
		pageTwoAccordingToOCW.setSubjects(justChemistry);
		ocwFeedStore.save(pageTwoAccordingToOCW);
		
		// A third page was tagged by both MIT OCW and NSDL with
		// subject:Chemistry
		Resource pageThreeAccordingToNSDL = new Resource(PAGE_THREE_URL);
		pageThreeAccordingToNSDL.setSubjects(justChemistry);
		nsdlFeedStore.save(pageThreeAccordingToNSDL);
		
		Resource pageThreeAccordingToOCW = new Resource(PAGE_THREE_URL);
		// ^^ Notice that the two URIs above are the same.
		pageThreeAccordingToOCW.setSubjects(justChemistry);
		ocwFeedStore.save(pageThreeAccordingToOCW);
		
		// Let's just add a resource to the site configuration store and see if we can find it via the nutch searcherbean.
		siteConfigStore.save(pageThreeAccordingToNSDL);
		
		RdfStore siteConfigStoreStrikesBack = RdfStore.forDEd();
		Collection<Resource> resourcez = siteConfigStoreStrikesBack.load(org.creativecommons.learn.oercloud.Resource.class);
		assertTrue(resourcez.size() > 0);
		
		String url3  = pageThreeAccordingToNSDL.getUrl();
		
		Resource resource = RdfStore.forDEd().loadDeep(Resource.class, url3);
		assertSame(resource.getSubjects().iterator().next(), justChemistry.iterator().next());
		
		// Let's crawl...
		
		// First we need a URLs file
		runCmd("mkdir urls_dir");
		BufferedWriter writer = new BufferedWriter(new FileWriter("urls_dir/list_of_urls"));
		writer.write(PAGE_ONE_URL + "\n");
		writer.write(PAGE_TWO_URL + "\n");
		writer.write(PAGE_THREE_URL + "\n");
		writer.close();
		
		runCmd("rm -rf crawl/");
		
		// Then we crawl.
		String cmd = "bin/nutch crawl urls_dir -dir crawl -depth 1";
		runCmd(cmd);
		
		// Let's talk to Nutch.
		// Try asking for tag:chemistry (that's how to find pages with the subject Chemistry)
		// We should get all three results
		ArrayList<String> titles = getUrlsOfHitsForAStringyQuery("jellybeans");
		assertSame(3, titles.size());
		
		ArrayList<String> titles2 = getUrlsOfHitsForAStringyQuery("tag:chemistry");
		assertSame(3, titles2.size());
		
		// Now ask for "subject:chemistry -curator:NSDL"
		// We should get only the second and third pages
		ArrayList<String> hitsFromSecondQuery = getUrlsOfHitsForAStringyQuery("jellybeans excludecurator:NSDL");
		String hitOne = hitsFromSecondQuery.get(0);
		String hitTwo = hitsFromSecondQuery.get(1);
		assertTrue(false); // FIXME: Check that these are the correct hits.
	}



	
	/*
	String[] pieces = line.split("/", 1);
	if (pieces.length > 1) {
		urls.add(pieces[1]);
	}
	*/

/*	public void testCreateFieldFromPredicateAndObject() {
		String feedURI = "http://example.com/#feed";
		RdfStore store = RdfStore.uri2RdfStore(feedURI);

		// Create a subject
		String subjectURI = "http://example.com/#subject";
		Resource subject = new Resource(subjectURI);

		// Add a predicate and object.
		subject.setTitle("your uncle wears polyester");
		store.save(subject);

		RDFNode predicate = new RDFNode();
		RDFNode object;

		// Pull it out of the triple store as a triple

		// See if we can convert that triple into a lucene field
		TripleStoreIndexer indexer = new TripleStoreIndexer();
		Field field = indexer.createFieldFromPredicateAndObject(predicate,
				object);
	}
	*/
}
