/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.creativecommons.learn.aggregate.feed;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.RdfStoreFactory;
import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * 
 * @author nathan
 */
public class Opml {

	private SyndFeed loadFeed(String feed_url) {

		try {
			// try to retrieve the feed
			SyndFeedInput input = new SyndFeedInput();
			URLConnection feed_connection;
			feed_connection = new URL(feed_url).openConnection();
			feed_connection.setConnectTimeout(30000);
			feed_connection.setReadTimeout(60000);

			return input.build(new XmlReader(feed_connection));

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FeedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	} // loadFeed

	public void poll(Feed feed) {
		try {
			RdfStore store = RdfStoreFactory.get().forProvenance(feed.getUrl());
			// load the OPML feed as a JDOM document
			Document opml = new SAXBuilder().build(new URL(feed.getUrl()));

			// iterate over outline nodes
			List<Element> nodes = XPath.selectNodes(opml, "//outline");

			for (Element n : nodes) {

				// nope...
				Feed node_feed = null;

				if (n.getAttributeValue("type").equals("include")) {
					// explicit inclusion
					node_feed = new Feed(n.getAttributeValue("url"));
					node_feed.setFeedType("opml");

				} else {
					// assume it's a feed... which to us is anything else
					String feed_url = n.getAttributeValue("xmlUrl");
					if (feed_url == null)
						// fallback to the url if it's slightly mis-formed
						feed_url = n.getAttributeValue("url");
					
					// check if this feed already exists
					System.out.println(feed_url);
					if (store.exists(Feed.class, feed_url))
						continue;

					// create the new Feed object
					node_feed = new Feed(feed_url);
					node_feed.setFeedType(n.getAttributeValue("type"));

					// try to retrieve the feed so we can intelligently set the
					// Curator
					SyndFeed rome_feed = this.loadFeed(node_feed.getUrl());
					Curator curator = null;

					if (rome_feed != null) {
						// see if we already have a Curator with the URL
						if (rome_feed.getLink() != null) {
							// the feed has a link
							if (RdfStoreFactory.get().forDEd().exists(Curator.class,
									rome_feed.getLink())) {
								curator = RdfStoreFactory.get().forDEd().load(Curator.class,
										rome_feed.getLink());
							} else {
								curator = new Curator(rome_feed.getLink());
								curator.setName(rome_feed.getTitle());

								RdfStoreFactory.get().forDEd().save(curator);
							}
						}
					} // if we were able to retrieve the feed
					else {
						// XXX we were unable to retrieve the feed; bail out
						continue;

					}

					// set the curator
					node_feed.setCurator(curator);
					
				} // non-OPML feed

				store.save(node_feed);
				// XXX poll here?
			}
		store.close();


		} catch (JDOMException ex) {
			Logger.getLogger(Opml.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(Opml.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
