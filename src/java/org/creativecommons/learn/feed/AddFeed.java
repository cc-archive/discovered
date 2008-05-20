package org.creativecommons.learn.feed;

import org.creativecommons.learn.oercloud.OerFeed;

public class AddFeed {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length < 2) {
			System.out.println("AddFeed");
			System.out.println("usage: AddFeed [feed_type] [feed_url]");
			System.out.println();
			
			System.exit(1);
		}

		String type = args[0];
		String url = args[1];
		
		try {
			OerFeed new_feed = OerFeed.newFeed(url);
			new_feed.setFeedType(type);
			
		} catch (InstantiationException e1) {
			e1.printStackTrace();

			System.out.println("Feed already exists.");
			System.exit(1);
		}
				
	}

}
