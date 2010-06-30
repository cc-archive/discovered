package org.creativecommons.learn.feed;

import java.sql.SQLException;
import java.util.Collection;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;

public class AddFeed {

	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {

		if (args.length < 3) {
			System.out.println("AddFeed");
			System.out.println("usage: AddFeed [feed_type] [feed_url] [curator_url]");
			System.out.println();

			System.exit(1);
		}

		String type = args[0];
		String url = args[1];
		String curator = args[2];
		
		addFeed(type, url, curator);
		
	}
	
	public static void addFeed(String type, String url, String curator) throws SQLException {
		RdfStore store = RdfStore.forDEd();
		
		/* Make sure we already have heard of that Curator. */
		Collection<Curator> curator_objs = store.load(Curator.class);
		Curator curator_obj = null;
		for (Curator c : curator_objs) {
			if (c.getUrl().equals(curator)) {
				curator_obj = c;
				break;
			}
		}
		if (curator_obj == null) {
			throw new IllegalArgumentException("You passed in a Curator about whom we have no data. You must run \"addcurator\" first (or maybe you typo'd the curator; check that argument).");
		}
		
		Feed feed = new Feed(url);
		feed.setFeedType(type);
		feed.setCurator(curator_obj); // It would be nice if this validated whether the curator exists.
		store.save(feed);
		
	}
	
}
