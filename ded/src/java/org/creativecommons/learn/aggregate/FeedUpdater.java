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

	protected void addEntry(RdfStore store, SyndEntry entry) {

		// XXX check if the entry exists first...
		Resource new_entry = new Resource(entry.getUri());

		new_entry.getSources().add(feed);
		new_entry.setTitle(entry.getTitle());
		
		if (new_entry.getDescription() != null) 
			new_entry.setDescription(entry.getDescription().getValue());
		else
			new_entry.setDescription("");

		for (Object category : entry.getCategories()) {
			new_entry.getSubjects().add(((SyndCategory) category).getName());
		}

		// add actual Dublin Core metadata using the DC Module
		DCModule dc_metadata = (DCModule) entry.getModule(DCModule.URI);

		// dc:category
		List<DCSubject> subjects = dc_metadata.getSubjects();
		for (DCSubject s : subjects) {
			new_entry.getSubjects().add(s.getValue());
		}

		// dc:type
		List<String> types = dc_metadata.getTypes();
		new_entry.getTypes().addAll(types);

		// dc:format
		List<String> formats = dc_metadata.getFormats();
		new_entry.getFormats().addAll(formats);

		// dc:contributor
		List<String> contributors = dc_metadata.getContributors();
		new_entry.getContributors().addAll(contributors);

		store.saveDeep(new_entry);
	} // addEntry

	public void update(boolean force) throws IOException, SQLException {
		// get the contents of the feed and emit events for each
		RdfStore store = RdfStore.uri2RdfStore(feed.getCurator().getUrl());
			
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
