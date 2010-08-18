/**
 * 
 */
package org.creativecommons.learn.feed;

import java.net.URI;
import java.net.URISyntaxException;

import org.creativecommons.learn.DiscoverEdTestCase;
import org.creativecommons.learn.RdfStoreFactory;
import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;

/**
 * @author nyergler
 *
 */
public class TestAddFeed extends DiscoverEdTestCase {

	private static final String EXAMPLE_FEED_URL = "http://example.com/rss";
	private static final String EXAMPLE_CURATOR_URL = "http://example.com/curator/";

	/**
	 * Test method for {@link org.creativecommons.learn.feed.AddFeed#addFeed(java.lang.String, java.lang.String, java.lang.String)}.
	 * 
	 * @throws URISyntaxException 
	 */
	public void testAddFeed() throws URISyntaxException {
		
		// create a new curator
		Curator curator = new Curator(URI.create(EXAMPLE_CURATOR_URL));
		curator.setName("Example Curator");
		RdfStoreFactory.get().forDEd().save(curator);
		
		// call addFeed
		AddFeed.addFeed("rss", EXAMPLE_FEED_URL, EXAMPLE_CURATOR_URL);
		
		// assert the Feed was added correctly
		Feed feed = RdfStoreFactory.get().getReader().load(Feed.class, EXAMPLE_FEED_URL);
		assert(feed.getUri().toString().equals(EXAMPLE_FEED_URL));
		assert(feed.getCurator().equals(curator));
		
	}

	/**
	 * Test method for {@link org.creativecommons.learn.feed.AddFeed#addFeed(java.lang.String, java.lang.String, java.lang.String)}.
	 * This test confirms that an exception is thrown if an unknown Curator 
	 * URI is passed in.
	 * 
	 * @throws URISyntaxException 
	 * 
	 */
	public void testAddFeedWithInvalidCurator() throws URISyntaxException {
		
		// call addFeed
		try {
			AddFeed.addFeed("rss", EXAMPLE_FEED_URL, "http://example.com/unknown_curator");
		} catch (IllegalArgumentException e) {
			// just as expected.
		} finally {
			// no exception thrown, assert false to fail the test
			assert(false);
		}
	}

}
