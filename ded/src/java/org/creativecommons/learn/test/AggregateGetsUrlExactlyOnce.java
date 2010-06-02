package org.creativecommons.learn.test;

import java.sql.SQLException;
import java.util.Collection;
import java.io.IOException;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.feed.AddCurator;
import org.creativecommons.learn.feed.AddFeed;
import org.creativecommons.learn.aggregate.FeedUpdater;
import org.creativecommons.learn.aggregate.Main;
import org.creativecommons.learn.oercloud.Resource;

public class AggregateGetsUrlExactlyOnce extends DiscoverEdTestCase {
	

	public void testAggregateGetsUrlExactlyOnce() throws IOException, SQLException {
		
		// Add a curator
		String curatorURI = "http://example.com/";
		AddCurator.addCurator("This doesn't matter for this test", curatorURI);
		
		// Add a feed to that curator's triple store. This feed says, in effect, "here's a URL".
		String testURIPrefix = "http://a6.creativecommons.org/~raffi/html_for_discovered_unit_tests/";
		String feedURI = testURIPrefix + "rss_pointing_to_i_know_my_title.xml";
		AddFeed.addFeed("rss", feedURI, curatorURI);
		
		FeedUpdater.startCountingGETs();
		
		// Aggregate
		String[] args = {};
		Main.main(args);
		assertEquals(1, FeedUpdater.getHowManyGETsSoFar()); 
		
		// Query the RdfStore corresponding to the feed. Find the triple URI, title, literal.
		RdfStore feedStore = RdfStore.uri2RdfStore(feedURI);
		Collection<Resource> resourcesInFeedStore = feedStore.load(Resource.class);
		assertEquals(1, resourcesInFeedStore.size());
	}

}