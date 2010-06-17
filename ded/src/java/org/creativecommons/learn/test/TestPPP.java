package org.creativecommons.learn.test;

import java.sql.SQLException;
import java.util.HashMap;

import org.creativecommons.learn.ProvenancePredicatePair;
import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.oercloud.Resource;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class TestPPP extends DiscoverEdTestCase {
	public void test() throws SQLException {
		
		// Create a resource with a provenance
		String subjectURI = "http://example.com/#subject";
		String provenanceURI = "http://example.com/#provenance";
		RdfStore store = RdfStore.forProvenance(provenanceURI);
		Resource r = new Resource(subjectURI);
		r.setTitle("my title");
		String titlePredicate = "http://purl.org/dc/elements/1.1/title";
		String titlePredicateAbbrev = "_dct_title";
		store.save(r);
		
		HashMap<ProvenancePredicatePair, RDFNode> map =
			store.getPPP2ObjectMapForSubjectAndPredicate(subjectURI, titlePredicate);
		ProvenancePredicatePair p3 = map.keySet().iterator().next();
		assertEquals(p3.toFieldName(), "1_" + titlePredicateAbbrev);
		
	}
}
