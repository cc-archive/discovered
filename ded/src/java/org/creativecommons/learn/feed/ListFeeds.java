package org.creativecommons.learn.feed;
import java.util.Collection;

import org.creativecommons.learn.RdfStoreFactory;
import org.creativecommons.learn.oercloud.Feed;

public class ListFeeds {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// list feeds we're tracking
		Collection<Feed> feeds = RdfStoreFactory.get().forDEd().load(Feed.class);
			
		for (Feed f : feeds) {
			System.out.println(f.getUri().toString() + " (" + f.getFeedType() + ", " + f.getCurator().getUrl() + " )");
		}
		
	} // main

} // ListFeeds
