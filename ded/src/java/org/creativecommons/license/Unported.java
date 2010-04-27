/*
 * Unported.java
 *
 * Copyright 2007, Creative Commons
 * licensed under the GNU LGPL License; see licenses/LICENSE for details
 *
 */

package org.creativecommons.license;

/**
 *
 * @author nathan
 */
public class Unported implements IJurisdiction {
    
    /** Creates a new instance of Unported */
    public Unported() {
    }

    public String getTitle() {
        return this.getTitle("en");
    }

    public String getTitle(String lang) {
        return "Unported";
    }

    public int compareTo(Object other) {
        
        // Unported is semi-bogus, so we don't support sorting'
        throw new ClassCastException();
        
    }
    
}
