/*
 * CC.java
 *
 * Copyright 2007, Creative Commons
 * licensed under the GNU LGPL License; see licenses/LICENSE for details
 *
 */

package org.creativecommons.license;

import com.hp.hpl.jena.rdf.model.*;

/**
 *
 * @author Nathan R. Yergler <nathan@creativecommons.org>
 *
 * CC vocabulary class for namespace http://creativecommons.org/ns#
 *
 */
public class CC {
    
    private static final String NS ="http://creativecommons.org/ns#";
    private static Model m = ModelFactory.createDefaultModel();
    
    public static final Resource NAMESPACE = m.createResource( NS );
    
    public static final Resource License = m.createResource(NS + "License");
    public static final Resource Work = m.createResource(NS + "Work");
    public static final Resource Jurisdiction = m.createResource(NS + "Jurisdiction");

    public static final Property requires = m.createProperty(NS, "requires");
    public static final Property permits = m.createProperty(NS, "permits");
    public static final Property prohibits = m.createProperty(NS, "prohibits");
   
    public static final Resource ShareAlike = m.createResource(NS + "ShareAlike");
    public static final Resource CommercialUse = m.createResource(NS + "CommercialUse");
    public static final Resource DerivativeWorks = m.createResource(NS + "DerivativeWorks");
    
    public static final Property legalcode = m.createProperty(NS, "legalcode");
    public static final Property jurisdiction = m.createProperty(NS, "jurisdiction");
   
} // CC
