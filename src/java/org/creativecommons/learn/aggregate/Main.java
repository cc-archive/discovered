/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.creativecommons.learn.aggregate;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.creativecommons.learn.oercloud.ObjectMgr;
import org.creativecommons.learn.oercloud.OerFeed;


/**
 *
 * @author nathan
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
/*
        EntityManager em = ObjectMgr.get().getEm();
        em.getTransaction().begin();
        
        Query q = em.createNamedQuery("Bookmark.findByBId");
        q.setParameter("bId", BigInteger.valueOf(59599));
        
        Bookmark b = (Bookmark) q.getSingleResult();
        System.out.println(b.getTitle());
        
        Tag t = new Tag(b, "foo");
        b.getTags().add(t);
        System.out.println(b.getUser());
        System.out.println(b.getUser().getBookmarks().size());
        
        em.getTransaction().commit();
        
/*
 for (Tag t : b.getTags()) {
            System.out.println(t.getTag());
            
        }
 */       
        // get a list of all available feeds
        List<OerFeed> all_feeds = ObjectMgr.get().getAllFeeds();

        // process each one
        for (OerFeed feed : all_feeds) {

            // XXX see if this feed needs to be re-imported
            if (feed.getLastImportDate().before(new Date())) {
                try {
                    // reimport necessary
                    feed.poll();
                    feed.setLastImportDate(new Date());
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            System.out.println(feed.getUrl());

        } // for each feed

    } // main

} // Main
