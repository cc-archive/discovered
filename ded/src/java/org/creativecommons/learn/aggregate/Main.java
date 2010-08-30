/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.creativecommons.learn.aggregate;
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
import org.creativecommons.learn.RdfStoreFactory;
import org.creativecommons.learn.oercloud.Feed;


/**
 *
 * @author nathan
 */
public class Main {

	/**
	 * Create an object that will allow us to parse the arguments
	 * passed to this class on the command line.
	 */
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
     * Get a list of feeds from the site configuration store.
     * (more needs to be written here)
     * 
     * @param args the command line arguments
     * @throws SQLException 
     */
    public static void main(String[] args) {

		// create the parser
		CommandLineParser parser = new GnuParser();
		CommandLine line = null;

		try {
			// parse the command line arguments
			line = parser.parse(getOptions(), args);
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			
			// exit with an exit code of 1
			System.exit(1);
		}

		if (line.hasOption("help")) {
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("aggregate", getOptions());

			System.exit(0);
		}
    	
    	// All date calculations will be made with respect to yesterday.
		
		// Create a Calendar object that means "yesterday"
    	Calendar oneDayAgo = Calendar.getInstance();
    	oneDayAgo.add(Calendar.DATE, -1);
    	
    	// Get a list of all available feeds in the site configuration store
    	Collection<Feed> all_feeds = RdfStoreFactory.get().forDEd().loadDeep(Feed.class);
    	
    	// If the user specifies a particular curator,
    	// only collect feeds by that curator
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
    	
    	// Are we forcing updates?
    	boolean force = false;
    	if (line.hasOption("force")) force = true;
    	
        // Process each feed
        for (Feed feed : all_feeds) {

        	System.out.println(feed.getUri().toString());
        	Date import_date = new Date();

        	// see if this feed needs to be re-imported
        	boolean feedIsOld = feed.getLastImport().before( oneDayAgo.getTime() );
            if (force || feedIsOld) {
                try {
                    // re-import necessary
                	System.out.println("updating...");

                	FeedUpdater updater = new FeedUpdater(feed);
                	updater.update(force);
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                	feed.setLastImport(import_date);
                	RdfStoreFactory.get().forDEd().save(feed);
                }
            }
            
            System.out.println(feed.getUri().toString());

        } // for each feed

    } // main

} // Main