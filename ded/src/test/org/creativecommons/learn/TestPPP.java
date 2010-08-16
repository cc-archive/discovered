package org.creativecommons.learn;

import java.net.URI;
import java.util.HashMap;

import org.creativecommons.learn.oercloud.Resource;

import com.hp.hpl.jena.graph.Node;

public class TestPPP extends DiscoverEdTestCase {
	public void test() {
		
		// Create a resource with a provenance
		String subjectURI = "http://example.com/#subject";
		String provenanceURI = "http://example.com/#provenance";
		RdfStore store = RdfStoreFactory.get().forProvenance(provenanceURI);
		Resource r = new Resource(URI.create(subjectURI));
		r.setTitle("my title");
		String titlePredicate = "http://purl.org/dc/elements/1.1/title";
		String titlePredicateAbbrev = "_dct_title";
		store.save(r);
		
		HashMap<ProvenancePredicatePair, Node> map =
			RdfStoreFactory.get().getPPP2ObjectMapForSubjectAndPredicate(subjectURI, titlePredicate);
		ProvenancePredicatePair p3 = map.keySet().iterator().next();

		assertEquals(p3.toFieldName(), 
				RdfStoreFactory.get().getOrCreateTablePrefixFromURIAsInteger(provenanceURI) + "_" + titlePredicateAbbrev);
		
	}
}
