/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.creativecommons.learn.aggregate;
import org.creativecommons.learn.RdfStore;


import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.oercloud.Feed;


/**
 *
 * @author nathan
 */
public class Main {

	@SuppressWarnings("static-access")
	private static Options getOptions() {

		Options options = new Options();

		Option help = new Option("help", "Print command line arguments");
		Option force = OptionBuilder.withArgName("force")
				.hasArg(false)
				.withDescription("Ignore last-aggregation date; use carefully with OAI-PMH repositories.")
				.isRequired(false).create("force");
		Option curator = OptionBuilder.withArgName("curator")
				.hasArgs()
				.withDescription(
						"Only seed URLs belonging to these curator(s).")
				.isRequired(false).create("curator");

		options.addOption(help);
		options.addOption(force);
		options.addOption(curator);

		return options;
	}

    /**
     * @param args the command line arguments
     * @throws SQLException 
     */
    public static void main(String[] args) throws SQLException {

		// create the parser
		CommandLineParser parser = new GnuParser();
		CommandLine line = null;

		try {
			// parse the command line arguments
			line = parser.parse(getOptions(), args);
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
		}

		if (line.hasOption("help")) {
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("aggregate", getOptions());

			System.exit(0);
		}
    	
    	// set the update interval
    	// (one day)
    	Calendar oneDayAgo = Calendar.getInstance();
    	// Well, let's subtract a day.
    	oneDayAgo.add(Calendar.DATE, -1);
    	
    	// get a list of all available feeds
    	Collection<Feed> all_feeds = RdfStore.getSiteConfigurationStore().loadDeep(Feed.class);
    	
    	// filter by curator if necessary
    	if (line.hasOption("curator")) {
    		Collection<Feed> filtered_feeds = new Vector<Feed>();
    		
    		String[] curators = line.getOptionValues("curator");
    		
    		for (Feed f : all_feeds) {

    			boolean keep = false;
    			for (String c : curators) {
    				if (c.equals(f.getCurator().getUrl())) keep = true;
    			}
    			
    			if (keep) filtered_feeds.add(f);
    		}
    		
    		all_feeds = filtered_feeds;
    	}
    	
    	// see if we're forcing updates
    	boolean force = false;
    	if (line.hasOption("force")) force = true;
    	
        // process each one
        for (Feed feed : all_feeds) {

        	System.out.println(feed.getUrl());
        	Date import_date = new Date();

        	// see if this feed needs to be re-imported
            if (force || feed.getLastImport().before( oneDayAgo.getTime() )) {
                try {
                    // re-import necessary
                	System.out.println("updating...");

                	new FeedUpdater(feed).update(force);
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    feed.setLastImport(import_date);
                    RdfStore.getSiteConfigurationStore().save(feed);
                }
            }
            
            System.out.println(feed.getUrl());

        } // for each feed

    } // main

} // Main
