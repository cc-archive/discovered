package org.creativecommons.learn.test;

import java.sql.SQLException;
import java.util.HashMap;

import org.creativecommons.learn.ProvenancePredicatePair;
import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.oercloud.Resource;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.impl.LiteralImpl;


public class GetPredicatesForSubject extends DiscoverEdTestCase {

	final String RESOURCE_URI = "http://example.com/#resource";
		
	public void test() throws SQLException, ClassNotFoundException {

		// Let's add two resources
		String provenanceURI = "http://example.com/#prov1";
		String provenanceURI2 = "http://example.com/#prov2";
		
		createResourceWithProvenance(RESOURCE_URI, provenanceURI);
		createResourceWithProvenance(RESOURCE_URI, provenanceURI2);
		
		// Invoke the method getPPP2ObjectMapForSubject (this is the method we're testing)
		// The output of this method should be a HashMap of ProvenancePredicatePairs to Strings.
		HashMap<ProvenancePredicatePair, RDFNode> map = RdfStore.getPPP2ObjectMapForSubject(RESOURCE_URI);
		
		boolean foundPair1 = false;
		boolean foundPair2 = false;
		
		for (ProvenancePredicatePair pair: map.keySet()) {
			if (pair.predicateNode.toString().equals("http://purl.org/dc/elements/1.1/title")) {
				if (pair.provenanceURI.equals(provenanceURI)) {
					foundPair1 = true;
				}
				if (pair.provenanceURI.equals(provenanceURI2)) {
					foundPair2 = true;
				}
			}
		}
		
		assertTrue(foundPair1);
		assertTrue(foundPair2);
	}
	
	public Resource createResourceWithProvenance(String resourceURI, String provenanceURI) throws SQLException {
		// Create a Resource, give it a dc:title.
		Resource r = new Resource(resourceURI);
		r.setTitle("my title");
		
		// Get an RdfStore for a certain URI
		RdfStore store = RdfStore.forProvenance(provenanceURI);
		// Save the resource into the store
		store.save(r);
		
		return r;
	}
}
