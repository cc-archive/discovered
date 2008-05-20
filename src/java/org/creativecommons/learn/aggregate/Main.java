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

    	// get a list of all available feeds
        List<OerFeed> all_feeds = ObjectMgr.get().getAllFeeds();

        // process each one
        for (OerFeed feed : all_feeds) {

            // XXX see if this feed needs to be re-imported
            if (feed.getLastImportDate().before(new Date())) {
                try {
                    // re-import necessary
                    feed.poll();
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    feed.setLastImportDate(new Date());                	
                }
            }
            
            System.out.println(feed.getUrl());

        } // for each feed

    } // main

} // Main
