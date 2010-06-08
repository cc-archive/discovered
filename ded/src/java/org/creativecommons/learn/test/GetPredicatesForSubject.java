package org.creativecommons.learn.test;

import java.sql.SQLException;
import java.util.HashMap;

import org.creativecommons.learn.ProvenancePredicatePair;
import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.oercloud.Resource;

public class GetPredicatesForSubject extends DiscoverEdTestCase {
	public void test() throws SQLException, ClassNotFoundException {
		// Create a Resource, give it a dc:title.
		Resource r = new Resource("http://example.com/#resource");
		r.setTitle("my title");
		// Get an RdfStore for a certain URI
		String provenanceURI = "http://example.com/#store";
		RdfStore store = RdfStore.uri2RdfStore(provenanceURI);
		
		// Save the resource into the store
		store.save(r);
		
		// Invoke the method getPPP2ObjectMapForSubject (this is the method we're testing)
		HashMap<ProvenancePredicatePair, String> map = RdfStore.getPPP2ObjectMapForSubject(r.getUrl());
		
		// The output of this method should be a HashMap of ProvenanceAwarePredicates to Strings.
		// The keys of this map ought to be a list of ProvenanceAwarePredicates with length 1
		assertEquals(3, map.size());
		
		ProvenancePredicatePair p3 = null;
		for (ProvenancePredicatePair pair: map.keySet()) {
			if (pair.predicateURI.equals("http://purl.org/dc/elements/1.1/title")) {
				p3 = pair;
			}
		}
		
		// The single predicate in the list should have provenance = the uri mentioned at the beginning
		assertEquals(provenanceURI, p3.provenanceURI);

	}
}