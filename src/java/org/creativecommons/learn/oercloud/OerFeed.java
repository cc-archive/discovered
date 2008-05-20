/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.creativecommons.learn.oercloud;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.creativecommons.learn.CCLEARN;
import org.creativecommons.learn.TripleStore;
import org.creativecommons.learn.aggregate.feed.OaiPmh;
import org.creativecommons.learn.aggregate.feed.Opml;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
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

/**
 *
 * @author nathan
 */
public class OerFeed {

    private Resource url = null;

	public static OerFeed newFeed(String feed_url) throws InstantiationException {
		// Create a new feed in the TripleStore if it does not already exist
		if (feedByUrl(feed_url) != null) 
			throw new InstantiationException("Feed with this URL already exists");
		
		try {
			Model m = TripleStore.getModel();
			Resource r_feed_url = m.createResource(feed_url);
			m.add(r_feed_url, RDF.type, CCLEARN.feed);
			
			return new OerFeed(r_feed_url);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	} // newFeed

	public static List<OerFeed> getAllFeeds() {
		
		// Return a list of OerFeed objects for all feeds we know about
		Vector<OerFeed> result = new Vector<OerFeed>();
		
		// Query the triple store for known feeds
		ResIterator feeds;
		try {
			feeds = TripleStore.getModel().listSubjectsWithProperty(RDF.type, CCLEARN.feed);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		while (feeds.hasNext()) {
			result.add(new OerFeed(feeds.nextResource()));
		}
		
		return result;
	}

	public static OerFeed feedByUrl(String feed_url) {
		
		// see if the feed already exists
		try {
			Model m = TripleStore.getModel();
			Resource r_feed_url = m.createResource(feed_url);
			
			if (m.listStatements(r_feed_url, RDF.type, CCLEARN.feed).hasNext() == true) {
				// it does; return the OerFeed object
				return new OerFeed(r_feed_url);
			}
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public OerFeed(Resource feed_url) {
    	
    	url = feed_url;

    } // OerFeed

    public void updateEntry(OerFeed feed, SyndEntry entry) {
        try {
            Model model = TripleStore.getModel();

            // add the basic assertions about the resource (title, source, etc)
            Resource res = model.createResource(entry.getUri());
            model.add(res, DC.title, model.createLiteral(entry.getTitle()));
            model.add(res, RDF.type, CCLEARN.resource);
            model.add(res, CCLEARN.source, model.createResource(feed.getUrl()));
            
            model.add(res, DC.description, 
                    model.createLiteral(entry.getDescription().getValue()));
                        
            // add categories, mapped to dc:subject
            for (Object category : entry.getCategories()) {
                model.add(res, DC.subject, 
                        model.createLiteral( ((SyndCategory)category).getName() ));
            } // for each category
            
            // add actual Dublin Core metadata using the DC Module
	        DCModule dc_metadata = (DCModule)entry.getModule(DCModule.URI);

	        // dc:category
        	List<DCSubject> subjects = dc_metadata.getSubjects();      	
        	for (DCSubject s : subjects) {
                model.add(res, DC.subject, 
                        model.createLiteral(s.getValue()));
        	}

        	// dc:type
        	List<String> types = dc_metadata.getTypes();
        	for (String type : types) {
                model.add(res, DC.type, model.createLiteral(type));
        	}

        	// dc:format
        	List<String> formats = dc_metadata.getFormats();
        	for (String format : formats) {
                model.add(res, DC.type, model.createLiteral(format));
        	}
        	
        	// dc:contributor
        	List<String> contributors = dc_metadata.getContributors();
        	for (String contributor : contributors) {
                model.add(res, DC.type, model.createLiteral(contributor));
        	}
        	
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TripleStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    } // updateEntry

    public void update() throws IOException {
        // get the contents of the feed and emit events for each
        
        // OPML
        if (this.getFeedType().toLowerCase().equals("opml")) {
            
            new Opml().poll(this);
            
        } else {
            try {
                SyndFeedInput input = new SyndFeedInput();
                URLConnection feed_connection = new URL(this.getUrl()).openConnection();
                feed_connection.setConnectTimeout(30000);
                feed_connection.setReadTimeout(60000);
                
                SyndFeed feed = input.build(new XmlReader(feed_connection));

                List<SyndEntry> feed_entries = feed.getEntries();

                for (SyndEntry entry : feed_entries) {

                    // emit an event with the entry information
                	this.updateEntry(this, entry);

                } // for each entry
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(OerFeed.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FeedException ex) {
                // maybe OAI-PMH?
            	try {
            		new OaiPmh().poll(this);
            	} catch (UnsupportedOperationException e) {
            		
            	}
                // XXX still need to log feed errors if it's not OAI-PMH
                Logger.getLogger(OerFeed.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } // not opml...
    } // poll
    
    public String getUrl() {
        return url.getURI();
    }
/*
    public Date getLastImportDate() {
        return new Date(this.getLastImport().intValue() * 1000);
    }
    
    public void setLastImportDate(Date lastImportDate) {
        this.setLastImport(BigInteger.valueOf(
                BigInteger.valueOf(lastImportDate.getTime()).intValue() / 1000
                ));
    }
*/  
    public String getFeedType() {
    	try {
			return TripleStore.getModel().getProperty(this.url, CCLEARN.feedType).getString();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			return null;
		}
    }

    public void setFeedType(String feedType) {
    	
    	try {
    		Statement old_type = TripleStore.getModel().getProperty(this.url, CCLEARN.feedType);
    		if (old_type != null) {
    			TripleStore.getModel().remove(old_type);
    		}
			
			TripleStore.getModel().add(this.url, CCLEARN.feedType, feedType);
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (url != null ? url.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OerFeed)) {
            return false;
        }
        OerFeed other = (OerFeed) object;
        return this.url.equals(other.url);
    }

    public String toString() {
        return "OerFeed[url=" + url + "]";
    }


}
