package org.creativecommons.learn.feed;
import org.creativecommons.learn.RdfStore;


import java.util.Collection;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.oercloud.Feed;

public class ListFeeds {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// list feeds we're tracking
		Collection<Feed> feeds = RdfStore.getSiteConfigurationStore().load(Feed.class);
			
		for (Feed f : feeds) {
			System.out.println(f.getUrl() + " (" + f.getFeedType() + ", " + f.getCurator().getUrl() + " )");
		}
		
	} // main

} // ListFeeds
