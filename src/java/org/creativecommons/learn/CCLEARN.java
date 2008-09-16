package org.creativecommons.learn;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class CCLEARN {

    protected static final String uri ="http://learn.creativecommons.org/ns#";
	protected static final String default_prefix = "ccl";
	
    /** returns the URI for this schema
        @return the URI for this schema
    */
    public static String getURI()
        { return uri; }

	public static String getDefaultPrefix() {
		return default_prefix;
	}
	
    protected static final Resource resource( String local )
        { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property( String local )
        { return ResourceFactory.createProperty( uri, local ); }

    public static final Resource Resource = resource("Resource");
	public static final Resource Feed = resource("Feed");

	public static final Resource Curator = resource("Curator");
	
	public static final Property feedType = property("feedType");
	public static final Property source = property("source");
	public static final Property hasCurator = property("hasCurator");

}
