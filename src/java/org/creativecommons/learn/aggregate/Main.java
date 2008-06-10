/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.creativecommons.learn.aggregate;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.creativecommons.learn.TripleStore;
import org.creativecommons.learn.oercloud.Feed;


/**
 *
 * @author nathan
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

    	Calendar calendar = Calendar.getInstance();
    	// set the update interval
    	// (one day)
    	calendar.add(Calendar.DATE, -1);
    	
    	// get a list of all available feeds
    	Collection<Feed> all_feeds = TripleStore.get().load(Feed.class);
    	
        // process each one
        for (Feed feed : all_feeds) {

        	System.out.println(feed.getUrl());
        	
            // see if this feed needs to be re-imported
            if (feed.getLastImport().before( calendar.getTime() )) {
                try {
                    // re-import necessary
                	System.out.println("updating...");

                	new FeedUpdater(feed).update();
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    feed.setLastImport(new Date());
                    TripleStore.get().save(feed);
                }
            }
            
            System.out.println(feed.getUrl());

        } // for each feed

    } // main

} // Main
