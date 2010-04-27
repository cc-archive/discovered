package org.creativecommons.learn.feed;

import org.creativecommons.learn.TripleStore;
import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;

public class AddFeed {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length < 2) {
			System.out.println("AddFeed");
			System.out
					.println("usage: AddFeed [feed_type] [feed_url] [curator_url]");
			System.out.println();

			System.exit(1);
		}

		String type = args[0];
		String url = args[1];

		Feed new_feed = new Feed(url);
		new_feed.setFeedType(type);

		if (args.length > 2) {
			Curator curator = new Curator(args[2]);
			new_feed.setCurator(curator);
		}

		TripleStore.get().save(new_feed);
		
	}

}
