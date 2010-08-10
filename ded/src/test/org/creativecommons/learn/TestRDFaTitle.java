package org.creativecommons.learn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.oercloud.Resource;

public class TestRDFaTitle extends DiscoverEdTestCase {

	// The URIs in the following code will all begin with this path:
	final String URI_PREFIX_FOR_TEST_PAGES = "http://a6.creativecommons.org/~raffi/html_for_discovered_unit_tests/";
	
	public void testResourcesCanGiveThemselvesATitle() throws Exception {
		// 
		// Here are some URI we will use / expect to encounter during this test.
		String feedURI = URI_PREFIX_FOR_TEST_PAGES + "rss_pointing_to_i_know_my_title.xml";
		String uriOfPageWithRDFa = URI_PREFIX_FOR_TEST_PAGES + "i_know_my_title.html";
		
		// Let's say we're aggregating a Resource with URI "http://example.com/barbie" and dc:title "My Barbie Land"
		
		// So that means having this triple:
		// <http://example.com/barbie> <dc:title> "My Barbie Land"
		
		// With what provenance are these triples stored? (Note that all triples must have a provenance
		// because otherwise we wouldn't know what Jena triple store to look at.)
		
		// Let's store the triple with the provenance of "http://example.com/barbie"
		
		// Add a curator
		String curatorURI = "http://example.com/";
		org.creativecommons.learn.feed.AddCurator.addCurator("This doesn't matter for this test", curatorURI);
		
		// Add a feed to that curator's triple store. This feed says, in effect, "here's a URL".
		
		org.creativecommons.learn.feed.AddFeed.addFeed("rss", feedURI, curatorURI);
		
		// Aggregate
		String[] args = {};
		org.creativecommons.learn.aggregate.Main.main(args);
		
		// Query the RdfStore corresponding to the feed.
		RdfStore feedStore = RdfStore.forProvenance(feedURI);
		Collection<Resource> resourcesInFeedStore = feedStore.load(org.creativecommons.learn.oercloud.Resource.class);
		
		// There should be a resource corresponding to the feed.
		assertEquals(1, resourcesInFeedStore.size());
		Resource resourceInFeedStore = resourcesInFeedStore.iterator().next();
		
		// Just double check that the feed store's copy of the resource doesn't
		// have a title. This isn't the primary thing we're checking in this
		// test, but it is nice to know by the end of the test that even pages
		// whose feeds don't give them titles can still give themselves titles.
		assertEquals(null, resourceInFeedStore.getTitle());
		
		// Now query the RdfStore for r.getURL()
		// This will give us the same resource, but when we access its getTitle method we'll see the title it uses to describe itself
		// The feed above should have pointed to an HTML page. Let's make sure that this HTML page has the URI we expect
		String resourceURI = resourceInFeedStore.getUrl();
		assertEquals(uriOfPageWithRDFa, resourceURI);
		
		// Now actually find the title.
		RdfStore store = RdfStore.forProvenance(resourceURI);
		Collection<Resource> resources = store.load(org.creativecommons.learn.oercloud.Resource.class);
		assertEquals(1, resources.size());
		Resource resourceInItsOwnStore = resources.iterator().next();
		assertEquals("This title is provided by the page itself", resourceInItsOwnStore.getTitle());
		
	}
	
	/*
	 * 
	 * We should write these tests:


	You have a OAI-PMH feed in the site-configuration triple store with
	such-and-such a curator,
	and the feed includes a particular resource,
	and the feed gives that resource a dc:title
	You aggregate,
	You query that **feed**'s store for the resource
	You can access the dc:title with getTitle
	
		The OAI-PMH client library might have some tests.

	You have a RSS feed in the site-configuration triple store with
	such-and-such a curator
	The feed contains a resource whose HTML content specifies a dc:title in RDFa
	You aggregate,
	You query that **resource**'s store for the resource
	You can access the dc:title with getTitle

	We're likely to test just the dc:title, since such a test passes, the
	other metadata are surely stored with the same/correct provenance as
	well.
	*/
}