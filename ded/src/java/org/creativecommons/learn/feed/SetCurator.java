package org.creativecommons.learn.feed;
import org.creativecommons.learn.RdfStore;


import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;

import thewebsemantic.NotFoundException;

public class SetCurator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length != 2) {
			System.out.println("SetCurator");
			System.out.println("usage: SetCurator [feed_url] [curator_url]");
			System.out.println();

			System.exit(1);
		}

		String feed_url = args[0];
		String curator_url = args[1];

		Feed feed;
		try {
			feed = RdfStore.getSiteConfigurationStore().load(Feed.class, feed_url);
			Curator curator = new Curator(curator_url);

			feed.setCurator(curator);
			
			RdfStore.getSiteConfigurationStore().save(feed);
		} catch (NotFoundException e) {

			System.out.println("Feed (" + feed_url + ") not found.");
			System.exit(1);
		}

	}

}
