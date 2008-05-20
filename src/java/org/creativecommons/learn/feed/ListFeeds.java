package org.creativecommons.learn.feed;

import java.util.List;

import org.creativecommons.learn.oercloud.OerFeed;

public class ListFeeds {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// list feeds we're tracking
		List<OerFeed> feeds = OerFeed.getAllFeeds();
			
		for (OerFeed f : feeds) {
			System.out.println(f.getUrl() + " (" + f.getFeedType() + ")");
		}
		
	} // main

} // ListFeeds
