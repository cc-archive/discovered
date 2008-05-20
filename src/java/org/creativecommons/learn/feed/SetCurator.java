package org.creativecommons.learn.feed;

import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.OerFeed;

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

		OerFeed feed = OerFeed.feedByUrl(feed_url);
		Curator curator = Curator.byUrl(curator_url);

		feed.setCurator(curator.getUrl());

	}

}
