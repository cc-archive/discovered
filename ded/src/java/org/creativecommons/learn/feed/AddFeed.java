package org.creativecommons.learn.feed;

import java.net.URISyntaxException;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.RdfStoreFactory;
import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;

import thewebsemantic.NotFoundException;

public class AddFeed {

	/**
	 * @param args
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws URISyntaxException {

		if (args.length < 3) {
			System.out.println("AddFeed");
			System.out.println("usage: AddFeed [feed_type] [feed_url] [curator_url]");
			System.out.println();

			System.exit(1);
		}
		
		addFeed(args[0], args[1], args[2]);
		
	}
	
	public static void addFeed(String type, String url, String curator_uri) throws URISyntaxException {
		
		RdfStore store = RdfStoreFactory.get().forDEd();
		
		/* Make sure we already have heard of that Curator. */
		Curator curator;
		try {
			curator = store.load(Curator.class, curator_uri);
		}
		catch (NotFoundException e) {
			throw new IllegalArgumentException("You passed in a Curator about whom we have no data. You must run \"addcurator\" first (or maybe you typo'd the curator; check that argument).");
		}

		// create the new feed
		Feed feed = new Feed(url);
		feed.setFeedType(type);
		feed.setCurator(curator);

		store.save(feed);
		
	}
	
}
