package org.creativecommons.learn.feed;
import org.creativecommons.learn.RdfStore;


import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.oercloud.Feed;

import thewebsemantic.NotFoundException;

public class DeleteFeed {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		if (args.length != 1) {
			System.out.println("DeleteFeed");
			System.out.println("usage: DeleteFeed [feed_url]");
			System.out.println();
			
			System.exit(1);
		}

		String url = args[0];
		
		//  make sure the feed exists
		if (!(RdfStore.getSiteConfigurationStore().exists(Feed.class, url))) {			
			System.out.println("Feed does not exist");
			System.exit(1);
		}
		
		try {
			RdfStore.getSiteConfigurationStore().delete(
					RdfStore.getSiteConfigurationStore().load(Feed.class, url)
					);
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
