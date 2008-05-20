/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.creativecommons.learn.aggregate.feed;

import org.creativecommons.learn.aggregate.*;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.creativecommons.learn.oercloud.ObjectMgr;
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
            ObjectMgr.get().getEm().getTransaction().begin();
            for (Element n : nodes) {
                
                // check if this feed already exists
                if (ObjectMgr.get().feedByUrl(n.getAttributeValue("xmlUrl")) != null)
                    continue;
                
                // nope...
                OerFeed node_feed = new OerFeed();
                node_feed.setUrl(n.getAttributeValue("xmlUrl"));
                node_feed.setUser(feed.getUser());
                
                if (n.getAttributeValue("type").equals("include")) {
                    // explicit inclusion
                    node_feed.setFeedType("opml");
                    node_feed.setUrl(n.getAttributeValue("url"));

                } else {
                    // assume it's a feed... which to us is anything else
                    node_feed.setFeedType(n.getAttributeValue("type"));
                }
                
                // persist the new feed
                ObjectMgr.get().getEm().persist(node_feed);
            }
            
            ObjectMgr.get().getEm().getTransaction().commit();
        } catch (JDOMException ex) {
            Logger.getLogger(Opml.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Opml.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
