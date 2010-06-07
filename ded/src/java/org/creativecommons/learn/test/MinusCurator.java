package org.creativecommons.learn.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.creativecommons.learn.DEdConfiguration;
import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.TripleStoreIndexer;
import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;
import org.creativecommons.learn.oercloud.Resource;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;

import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.document.Field;
import org.apache.nutch.searcher.Hit;
import org.apache.nutch.searcher.Hits;
import org.apache.nutch.searcher.NutchBean;
import org.apache.nutch.searcher.Query;

public class MinusCurator extends DiscoverEdTestCase {

	public void test() throws Exception {

		final String URL_PREFIX = "http://a6.creativecommons.org/~raffi/html_for_discovered_unit_tests/MinusCurator/";
		final String PAGE_ONE_URL = URL_PREFIX + "1.html";
		final String PAGE_TWO_URL = URL_PREFIX + "2.html";
		final String PAGE_THREE_URL = URL_PREFIX + "3.html";

		// Add some pages to the database...
		
		RdfStore siteConfigStore = RdfStore.getSiteConfigurationStore();

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
		RdfStore nsdlFeedStore = RdfStore.uri2RdfStore(nsdlFeed.getUrl());
		Resource pageOneAccordingToNSDL = new Resource(PAGE_ONE_URL);

		pageOneAccordingToNSDL.setSubjects(justChemistry);
		nsdlFeedStore.save(pageOneAccordingToNSDL);
		
		// A second page was tagged by MIT OCW with subject:Chemistry
		RdfStore ocwFeedStore = RdfStore.uri2RdfStore(ocwFeed.getUrl());
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
		
		// Let's crawl...
		
		// First we need a URLs file
		BufferedWriter writer = new BufferedWriter(new FileWriter("urls"));
		writer.write(PAGE_ONE_URL + "\n");
		writer.write(PAGE_TWO_URL + "\n");
		writer.write(PAGE_THREE_URL + "\n");
		writer.close();
		
		// Then we crawl.
		String crawlDirectory = "crawl"; // FIXME: Get this from the configuration
		String[] args = {"urls", "-dir", crawlDirectory, "-depth", "1"};
		org.apache.nutch.crawl.Crawl.main(args);
		
		// Let's talk to Nutch.
		// Try asking for subject:chemistry
		// We should get all three results
		assertSame(getHitsForAStringyQuery("subject:chemistry").getTotal(),
				3);
		
		// Now ask for "subject:chemistry -curator:NSDL"
		// We should get only the second and third pages
		Hits hitsFromSecondQuery = getHitsForAStringyQuery("subject:chemistry -curator:NSDL");
		Hit hitOne = hitsFromSecondQuery.getHit(0);
		Hit hitTwo = hitsFromSecondQuery.getHit(1);
		assertTrue(false); // FIXME: Check that these are the correct hits.
	}
	
	private static Hits getHitsForAStringyQuery(String q) throws IOException {
		// Pardon the weird name, we just didn't want to call it a query string
		final Configuration CONFIGURATION = DEdConfiguration.create();
		NutchBean bean = new NutchBean(CONFIGURATION);
		Query query = Query.parse(q, CONFIGURATION);
		Hits hits = bean.search(query, 10);
		return hits;
	}

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
