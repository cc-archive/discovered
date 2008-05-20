/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.creativecommons.learn.aggregate.feed;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.creativecommons.learn.TripleStore;
import org.creativecommons.learn.oercloud.OerFeed;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

/**
 *
 * @author nathan
 */
public class Opml {

    public void poll(OerFeed feed) {
        try {

            // load the OPML feed as a JDOM document
            Document opml = new SAXBuilder().build(new URL(feed.getUrl()));

            // iterate over outline nodes
            List<Element> nodes = XPath.selectNodes(opml, "//outline");

            for (Element n : nodes) {
                
                // check if this feed already exists
                if (OerFeed.feedByUrl(n.getAttributeValue("xmlUrl")) != null)
                    continue;
                
                // nope...
                OerFeed node_feed = null;
                
                if (n.getAttributeValue("type").equals("include")) {
                    // explicit inclusion
                    node_feed = OerFeed.newFeed(n.getAttributeValue("url"));
                    node_feed.setFeedType("opml");

                } else {
                    // assume it's a feed... which to us is anything else
                	node_feed = OerFeed.newFeed(n.getAttributeValue("xmlUrl"));
                    node_feed.setFeedType(n.getAttributeValue("type"));
                }
            }
            
        } catch (JDOMException ex) {
            Logger.getLogger(Opml.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Opml.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
