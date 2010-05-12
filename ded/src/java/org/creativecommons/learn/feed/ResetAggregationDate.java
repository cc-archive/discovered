package org.creativecommons.learn.feed;
import org.creativecommons.learn.QuadStore;


import java.util.Date;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.oercloud.Feed;

import thewebsemantic.NotFoundException;

public class ResetAggregationDate {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length < 1) {
			System.out.println("ResetFeed");
			System.out.println("usage: ResetFeed [feed_url] ");
			System.out.println();
			
			System.exit(1);
		}

		String feed_url = args[0];
		
		Feed feed = null;
		try {
			feed = QuadStore.getSiteConfigurationStore().load(Feed.class, feed_url);
		} catch (NotFoundException e) {
			
			System.out.println("Feed " + feed_url + " not found.");
			System.exit(1);
		}

		feed.setLastImport(new Date(0));
				
		QuadStore.getSiteConfigurationStore().save(feed);
						
	}

}
