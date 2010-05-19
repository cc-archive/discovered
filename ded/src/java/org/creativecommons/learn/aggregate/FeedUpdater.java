package org.creativecommons.learn.aggregate;
import org.creativecommons.learn.RdfStore;


import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.aggregate.feed.OaiPmh;
import org.creativecommons.learn.aggregate.feed.Opml;
import org.creativecommons.learn.oercloud.Feed;
import org.creativecommons.learn.oercloud.Resource;

import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.feed.module.DCSubject;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class FeedUpdater {

	private Feed feed;

	public FeedUpdater(Feed feed) {
		this.feed = feed;
	}

	/** Take the SyndEntry "entry", and add or update
	 * a corresponding Resource in our RdfStore. */
	protected void addEntry(RdfStore store, SyndEntry entry) {

		// XXX check if the entry exists first...
		Resource r = new Resource(entry.getUri());
		
		// Back when SyndFeed parsed the feed, it read in from the feed
		// all of the metadata it could find for this URI. Now it's
		// made that metadata available in the object "entry".

		// In fact, this feed is one of the resource's "sources".
		// So let's add this feed to the resource's list of sources.
		r.getSources().add(feed);
		r.setTitle(entry.getTitle());
		
		// If the resource doesn't have a description, set it to the empty string.
		// FIXME: Write a test checking the right behavior here.
		// (Is that meant to be entry.getDescription()?) 
		if (r.getDescription() == null) {
			r.setDescription("");
		}
		else {
			r.setDescription(entry.getDescription().getValue());
		}

		// FIXME: How is this different from dc:category below?
		
		// Could we learn anything from the feed about the various
		// "categories" this resource belongs in? 
		for (Object category : entry.getCategories()) {
			r.getSubjects().add(((SyndCategory) category).getName());
		}

		// add actual Dublin Core metadata using the DC Module
		DCModule dc_metadata = (DCModule) entry.getModule(DCModule.URI);

		// dc:category
		List<DCSubject> subjects = dc_metadata.getSubjects();
		for (DCSubject s : subjects) {
			r.getSubjects().add(s.getValue());
		}

		// dc:type
		List<String> types = dc_metadata.getTypes();
		r.getTypes().addAll(types);

		// dc:format
		List<String> formats = dc_metadata.getFormats();
		r.getFormats().addAll(formats);

		// dc:contributor
		List<String> contributors = dc_metadata.getContributors();
		r.getContributors().addAll(contributors);

		store.saveDeep(r);
	} // addEntry

	public void update(boolean force) throws IOException, SQLException {
		// get the contents of the feed and emit events for each
		// FIXME: each what?
		RdfStore store = RdfStore.uri2RdfStore(feed.getUrl());
			
		// OPML
		if (feed.getFeedType().toLowerCase().equals("opml")) {

			new Opml().poll(feed);

		} else if (feed.getFeedType().toLowerCase().equals("oai-pmh")) {

			new OaiPmh().poll(feed, force);
			
		} else {
			
			try {
				SyndFeedInput input = new SyndFeedInput();
				URLConnection feed_connection = new URL(feed.getUrl())
						.openConnection();
				feed_connection.setConnectTimeout(30000);
				feed_connection.setReadTimeout(60000);

				SyndFeed rome_feed = input
						.build(new XmlReader(feed_connection));

				List<SyndEntry> feed_entries = rome_feed.getEntries();

				for (SyndEntry entry : feed_entries) {

					// emit an event with the entry information
					this.addEntry(store, entry);

				} // for each entry
			} catch (IllegalArgumentException ex) {
				Logger.getLogger(Feed.class.getName()).log(Level.SEVERE, null,
						ex);
			} catch (FeedException ex) {
				// maybe OAI-PMH?
				try {
					new OaiPmh().poll(feed);
				} catch (UnsupportedOperationException e) {

				}
				// XXX still need to log feed errors if it's not OAI-PMH
				Logger.getLogger(Feed.class.getName()).log(Level.SEVERE, null,
						ex);
			}

		} // not opml...
	} // poll

}
