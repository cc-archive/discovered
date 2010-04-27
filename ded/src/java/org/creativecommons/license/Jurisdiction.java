/*
 * Jurisdiction.java
 *
 * Copyright 2007, Creative Commons
 * licensed under the GNU LGPL License; see licenses/LICENSE for details
 *
 */

package org.creativecommons.license;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.vocabulary.DC;

/**
 *
 * @author nathan
 */
public class Jurisdiction 
    implements IJurisdiction {
    
    private static final String JURIS_BASE = "http://creativecommons.org/international/";
    
    private String uri = null;
    
    /** Creates a new instance of Jurisdiction */
    public Jurisdiction(String uri) {
        this.uri = uri;
    } 
    
    
    public static Jurisdiction byId(String id) {
        return new Jurisdiction(JURIS_BASE + id + "/");
    }
    
    public String toString() {
        
        return this.uri;
        
    } // toString

    public String getTitle() {
        return this.getTitle("en");
    }
    
    public String getTitle(String lang) {
        
        Literal title = Store.get().literal(this.uri, DC.title, lang);
        
        if (title != null) {
            return title.getString();
        }
        
        return "";
    }

    public int compareTo(Object other) {
        
        return this.getTitle().compareTo(((Jurisdiction)other).getTitle());

    }
} // Jurisdiction
