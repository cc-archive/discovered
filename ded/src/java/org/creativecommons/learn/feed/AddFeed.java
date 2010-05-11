package org.creativecommons.learn.feed;

import java.sql.SQLException;

import org.creativecommons.learn.QuadStore;
import org.creativecommons.learn.TripleStore;
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
		String graphName = "http://creativecommons.org/#site-configuration";
		TripleStore store = QuadStore.uri2TripleStore(graphName);
		
		Feed feed = new Feed(url);
		feed.setFeedType(type);
		feed.setCurator(new Curator(curator)); // It would be nice if this validated whether the curator exists.
		store.save(feed);
		
	}
	
}
