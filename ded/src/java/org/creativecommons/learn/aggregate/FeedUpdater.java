package org.creativecommons.learn.aggregate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.creativecommons.learn.DEdConfiguration;
import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.RdfStoreFactory;
import org.creativecommons.learn.aggregate.feed.OaiPmh;
import org.creativecommons.learn.aggregate.feed.Opml;
import org.creativecommons.learn.oercloud.Feed;
import org.creativecommons.learn.oercloud.Resource;
import org.creativecommons.learn.plugin.MetadataRetrievers;
import org.mortbay.log.Log;

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
	public static int howManyGETsSoFar;
	private static boolean pleaseCountGETs = false;
	private static Set<String> validFeedTypes;
	private MetadataRetrievers metadataRetrievers;

	public FeedUpdater(Feed feed) {
		this.feed = feed;
		this.metadataRetrievers = new MetadataRetrievers(DEdConfiguration
				.create());
	}

	/**
	 * Take the SyndEntry "entry", and add or update a corresponding Resource in
	 * our RdfStore.
	 * 
	 * @throws URISyntaxException 
	 */
	protected void addEntry(RdfStore store, SyndEntry entry)
			throws URISyntaxException {

		// XXX check if the entry exists first...
		String uri = entry.getUri();
		if (uri == null) {
			// Well, that sucks. What kind of lame feed entry has no URI? Regardless, ditch it.
			Log.warn("For some reason, I ran into a feed entry with a null URI. How bizarre. Skipping it.");
			return;
		}
		Resource r = new Resource(uri);

		// Back when SyndFeed parsed the feed, it read in from the feed
		// all of the metadata it could find for this URI. Now it has
		// made that metadata available in the object "entry".

		// In fact, this feed is one of the resource's "sources".
		// So let's add this feed to the resource's list of sources.
		r.getSources().add(feed);
		r.setTitle(entry.getTitle());

		// If the resource doesn't have a description, set it to the empty
		// string.
		// FIXME: Write a test checking the right behavior here.
		// (Is that meant to be entry.getDescription()?)
		if (r.getDescription() == null) {
			r.setDescription("");
		} else {
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

		System.err.println("URI: " + r.getUrl());
		// Load additional metadata from external sources
		metadataRetrievers.retrieve(r);
		
		store.saveDeep(r);
	} // addEntry
	
	/*
	 * Public users of this method could get the object and 
	 * mutate it (i.e., change what feed types are valid),
	 * thereby changing the way FeedUpdate works. Protecting ourself
	 * from this strikes me as not worth the bother.
	 */
	public static Set<String> getValidFeedTypes() {
		if (validFeedTypes == null) {
			validFeedTypes = new HashSet<String>();
			validFeedTypes.add("oai-pmh");
			validFeedTypes.add("rss");
			validFeedTypes.add("opml");
		}
		return validFeedTypes;
	}
	
	public static boolean isFeedTypeValid(String feedType) {
		return getValidFeedTypes().contains(feedType);
	}
	
	public void update(boolean force) throws IOException {
		// get the contents of the feed and emit events for each
		// FIXME: each what?

		RdfStore store = RdfStoreFactory.get().forProvenance(feed.getUri().toString());
		
		if (! isFeedTypeValid(feed.getFeedType())) {
			Logger.getLogger(Feed.class.getName()).warning(
					"Feed " + feed.getUri().toString() + " has unknown feed type of " +
					feed.getFeedType() + ". Skipping it during update.");
		}

		// OPML
		if (feed.getFeedType().toLowerCase().equals("opml")) {

			new Opml().poll(feed);

		} else if (feed.getFeedType().toLowerCase().equals("oai-pmh")) {

			new OaiPmh().poll(feed, force);

		} else {

			try {

				SyndFeedInput input = new SyndFeedInput();
				URLConnection feed_connection = new URL(feed.getUri().toString())
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
			} catch (URISyntaxException ex) {
				Logger.getLogger(Feed.class.getName()).log(Level.SEVERE, null,
						ex);
			} finally {

				/*
				 * The following code is used for testing. Not sure how I could
				 * have improved this without resorting to drastic measures à la
				 * the replies to this StackOverflow post: http://ur1.ca/03f3d
				 */
				if (pleaseCountGETs) {
					howManyGETsSoFar++;
				}
			}

		} // not opml...

        store.close();
	} // poll

	public static void startCountingGETs() {
		assert !pleaseCountGETs;
		howManyGETsSoFar = 0;
		pleaseCountGETs = true;
	}

	public static int getHowManyGETsSoFar() {
		return howManyGETsSoFar;
	}
}
