package org.creativecommons.learn;

import java.sql.SQLException;
import java.util.Collection;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;
import org.creativecommons.learn.oercloud.Resource;

public class TestCuratorFeed extends DiscoverEdTestCase {
	
	/** A unit test that shows adding a curator works. 
	 * @throws SQLException 
	 * @throws ClassNotFoundException */
	public void addCurator() throws SQLException, ClassNotFoundException {
		RdfStore store = RdfStore.forDEd();
		
		/* We have no Curators at the start */
		Collection<Curator> available_curators = store.load(org.creativecommons.learn.oercloud.Curator.class);
		assertEquals(0, available_curators.size());

		/* Create a Curator, as if we were using the command line */
		org.creativecommons.learn.feed.AddCurator.addCurator("Notre Dame OCW", "http://ocw.nd.edu/");

		available_curators = store.load(org.creativecommons.learn.oercloud.Curator.class);
		assertEquals(1, available_curators.size());
		
		/* Make sure we saved it correctly */
		Curator curator = available_curators.iterator().next();
		assertEquals(curator.getName(), "Notre Dame OCW");
		assertEquals(curator.getUrl(), "http://ocw.nd.edu/");
		
		/* Get a different RdfStore */
		RdfStore aDifferentStore = RdfStore.forProvenance("http://other.example.com/");
		
		/* We have no Curators in the different RdfStore */
		Collection<Curator> aDifferentListOfCurators = aDifferentStore.load(org.creativecommons.learn.oercloud.Curator.class);
		assertEquals(0, aDifferentListOfCurators.size());
	}
	
	public void testAddCurator() throws SQLException, ClassNotFoundException {
		this.addCurator();
	}
	
	/** A unit test that shows adding a curator works. 
	 * @throws SQLException 
	 * @throws ClassNotFoundException */
	public void testAddFeed() throws SQLException, ClassNotFoundException {
		
		RdfStore store = RdfStore.forDEd();
		this.addCurator();

		/* We have no Feeds at the start */
		Collection<Feed> available_feeds = store.load(org.creativecommons.learn.oercloud.Feed.class);
		assertEquals(0, available_feeds.size());

		/* Create a Feed, as if we were using the command line */
		org.creativecommons.learn.feed.AddFeed.addFeed("rss", "http://ocw.nd.edu/courselist/rss", "http://ocw.nd.edu/");

		available_feeds = store.load(org.creativecommons.learn.oercloud.Feed.class);
		assertEquals(1, available_feeds.size());
		
		/* Make sure we saved it correctly */
		Feed feed = available_feeds.iterator().next();
		assertEquals(feed.getCurator().getUrl(), "http://ocw.nd.edu/");
		assertEquals(feed.getFeedType(), "rss");
		assertEquals(feed.getUrl(), "http://ocw.nd.edu/courselist/rss");
		store.close();
	}
	
	/** 
	You have an RSS feed in the site-configuration triple store with
	such-and-such a curator,
	The feed includes a particular resource
	The feed gives that resource a dc:title
	You aggregate
	You query the **feed**'s store for the resource
	You can access the dc:title with getTitle
	*/	
	public void testURLTitleProvenance() throws SQLException {
		// Add a curator		
		String curatorURI = "http://ocw.nd.edu/";
		org.creativecommons.learn.feed.AddCurator.addCurator("Notre Dame OpenCourseWare", curatorURI);
		
		// Add a feed to that curator's triple store. This feed says, in effect, "here's a URL, and here's its title".
		String feedURI = "http://ocw.nd.edu/courselist/rss";
		org.creativecommons.learn.feed.AddFeed.addFeed("rss", feedURI, curatorURI);
		
		// Aggregate
		String[] args = {};
		org.creativecommons.learn.aggregate.Main.main(args);
		
		// Query that curator's RdfStore, find the triple URI-title-literal
		RdfStore store = RdfStore.forProvenance(feedURI);
		Collection<Resource> resources = store.load(org.creativecommons.learn.oercloud.Resource.class);
		Resource r = resources.iterator().next();
		String title = r.getTitle();
		assertTrue(title.length() > 0);
	}

	// X: Curator and OAI-PMH Feed
	// Y: Aggregate
	// Z: The title of a Resource found by aggregating data from the feed
	// A: The URI of the Curator of the Feed

	public void testIntegration()
    {
    	// Steps:
    	// 1. Add a curator to the triple store
    	// 2. Add a feed to the triple store
    	// 3. (templated on type of feed) aggregate package pulls data in
    	// ...? 
        assertTrue( "TestExample", true );
    }

    public void testAddFeedMustPointToCuratorWithinTheStore() throws SQLException {
    	try {
			RdfStore store = RdfStore.forDEd();
			
			/* Try adding a feed where we have no data stored about
			 * the curator. */
			Collection<Feed> available_feeds = store.load(org.creativecommons.learn.oercloud.Feed.class);
			assertEquals(0, available_feeds.size());
	
			/* Create a Feed, as if we were using the command line */
			org.creativecommons.learn.feed.AddFeed.addFeed("rss", "http://ocw.nd.edu/courselist/rss", "http://ocw.nd.edu/");
    	} catch (IllegalArgumentException e) {
    		// sweet.
    		return;
    	}
    	assertFalse(true);
	}
    
	/**
	 * You have a OPML feed in the site-configuration triple store with
	 * such-and-such a curator
	 * The feed includes a particular resource
	 * The OPML feed can't make a dc:title assertion about the resource
	 * But the OPML file contains an RSS feed that makes a dc:title assertion about a Resource
	 * You aggregate
	 * You query that **RSS feed**'s store for the resource FIXME: Maybe it should be the OPML feed?
	 * You can access the dc:title with getTitle
	 */    
    public void testTitleKnowsItsOPMLFeedProvenance() {
    
    }

}
