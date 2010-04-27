/*
 * IJurisdiction.java
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
public interface IJurisdiction extends Comparable {
    String getTitle();

    String getTitle(String lang);

    String toString();
    
}
