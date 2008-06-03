package org.creativecommons.learn.oercloud;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.creativecommons.learn.CCLEARN;
import org.creativecommons.learn.TripleStore;
import org.creativecommons.learn.aggregate.feed.OaiPmh;
import org.creativecommons.learn.aggregate.feed.Opml;

import thewebsemantic.Namespace;
import thewebsemantic.RdfProperty;
import thewebsemantic.Uri;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.feed.module.DCSubject;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

@Namespace("http://learn.creativecommons.org/ns#")
public class Feed {

	private Curator curator = null;
	private String url = null;
	private String feedType = null;

	public Feed(String url) {
		super();

		this.url = url;
	}

	@RdfProperty("http://learn.creativecommons.org/ns#hasCurator")
	public Curator getCurator() {
		return curator;
	}

	public void setCurator(Curator curator) {
		this.curator = curator;
	}

	@Uri
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@RdfProperty("http://learn.creativecommons.org/ns#feedType")
	public String getFeedType() {
		return feedType;
	}

	public void setFeedType(String feedType) {
		this.feedType = feedType;
	}

	protected void addEntry(SyndEntry entry) {

		// XXX check if the entry exists first...
		Resource new_entry = new Resource(entry.getUri());

		new_entry.getSources().add(this);
		new_entry.setTitle(entry.getTitle());
		new_entry.setDescription(entry.getDescription().getValue());

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

		TripleStore.get().saveDeep(new_entry);
	} // addEntry

	public void update() throws IOException {
		// get the contents of the feed and emit events for each

		// OPML
		if (this.getFeedType().toLowerCase().equals("opml")) {

			new Opml().poll(this);

		} else {
			try {
				SyndFeedInput input = new SyndFeedInput();
				URLConnection feed_connection = new URL(this.getUrl())
						.openConnection();
				feed_connection.setConnectTimeout(30000);
				feed_connection.setReadTimeout(60000);

				SyndFeed feed = input.build(new XmlReader(feed_connection));

				List<SyndEntry> feed_entries = feed.getEntries();

				for (SyndEntry entry : feed_entries) {

					// emit an event with the entry information
					this.addEntry(entry);

				} // for each entry
			} catch (IllegalArgumentException ex) {
				Logger.getLogger(Feed.class.getName()).log(Level.SEVERE,
						null, ex);
			} catch (FeedException ex) {
				// maybe OAI-PMH?
				try {
					new OaiPmh().poll(this);
				} catch (UnsupportedOperationException e) {

				}
				// XXX still need to log feed errors if it's not OAI-PMH
				Logger.getLogger(Feed.class.getName()).log(Level.SEVERE,
						null, ex);
			}

		} // not opml...
	} // poll

}
