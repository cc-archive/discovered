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
public class Curator {

    private Resource url = null;

	public static Curator getOrCreate(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Curator create(String url) throws InstantiationException {
		// Create a new feed in the TripleStore if it does not already exist
		if (byUrl(url) != null) 
			throw new InstantiationException("Curator with this URL already exists");
		
		try {
			Model m = TripleStore.getModel();
			Resource r_url = m.createResource(url);
			m.add(r_url, RDF.type, CCLEARN.Curator);
			
			return new Curator(r_url);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	} // newFeed

	public static List<Curator> getAll() {
		
		// Return a list of Curator objects
		Vector<Curator> result = new Vector<Curator>();
		
		// Query the triple store for known feeds
		ResIterator feeds;
		try {
			feeds = TripleStore.getModel().listSubjectsWithProperty(RDF.type, CCLEARN.Curator);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		while (feeds.hasNext()) {
			result.add(new Curator(feeds.nextResource()));
		}
		
		return result;
	}

	public static Curator byUrl(String url) {
		
		// see if the feed already exists
		try {
			Model m = TripleStore.getModel();
			Resource r_url = m.createResource(url);
			
			if (m.listStatements(r_url, RDF.type, CCLEARN.Curator).hasNext() == true) {
				// it does; return the OerFeed object
				return new Curator(r_url);
			}
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public Curator(Resource url) {
    	
    	this.url = url;

    } // Curator

	public String getUrl() {
        return url.getURI();
    }

    public String getName() {
    	try {
			return TripleStore.getModel().getProperty(this.url, DC.title).getString();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			return this.getUrl();
		}
    }

    public void setName (String name) {
    	
    	try {
    		Statement old_name = TripleStore.getModel().getProperty(this.url, DC.title);
    		if (old_name != null) {
    			TripleStore.getModel().remove(old_name);
    		}
			
			TripleStore.getModel().add(this.url, DC.title, name);
			
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
        if (!(object instanceof Curator)) {
            return false;
        }
        Curator other = (Curator) object;
        return this.url.equals(other.url);
    }

    public String toString() {
        return "Curator[url=" + url + "]";
    }


}
