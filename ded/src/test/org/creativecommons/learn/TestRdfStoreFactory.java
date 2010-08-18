package org.creativecommons.learn;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import junit.framework.TestCase;

import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Resource;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.vocabulary.RDF;

import de.fuberlin.wiwiss.ng4j.Quad;
import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;

public class TestRdfStoreFactory extends TestCase {

	private NamedGraphSetImpl graphset;

	protected void setUp() throws Exception {
		super.setUp();

		this.graphset = new NamedGraphSetImpl();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testGet() {
		
		// get a reference to the singleton
		RdfStoreFactory store = RdfStoreFactory.get();
		
		// assert that the graphset is not null
		assertNotNull(store.getGraphset());
		
		// get the DEd configuration store
		assertNotNull(store.forDEd());
		
		// we get the same instance with subsequent calls
		assertEquals(store, RdfStoreFactory.get());
		
	}

	public void testGetProvenenacesForResourceURI() {
		// create the factory
		RdfStoreFactory store = new RdfStoreFactory(this.graphset);

		// get the wrappers for multiple provenances and add information
		Resource r1 = new Resource(URI.create("http://example.org/resource"));
		r1.getSubjects().add("subject1");
		store.forProvenance("http://example.org/feed1").save(r1);

		Resource r2 = new Resource(URI.create("http://example.org/resource"));
		r2.getSubjects().add("subject2");
		store.forProvenance("http://example.org/feed2").save(r2);

		Resource r3 = new Resource(URI.create("http://example.org/resource"));
		r3.getSubjects().add("subject3");
		store.forProvenance("http://example.org/feed3").save(r3);
		
		Collection<String> actual = store.getProvenancesThatKnowResourceWithThisURI("http://example.org/resource");
		HashSet<String> expected = new HashSet<String>();
		expected.add("http://example.org/feed3");
		expected.add("http://example.org/feed2");
		expected.add("http://example.org/feed1");
		assertEquals(expected, actual);
	}

	public void testForDEd() {
		// create the factory
		RdfStoreFactory store = new RdfStoreFactory(this.graphset);

		// get the wrapper for DEd configuration and add something
		RdfStore ded = store.forDEd();
		ded.save(new Curator(URI.create("http://example.com/curator")));

		// assert the quad exists in the factory
		assert (store.getGraphset()
				.containsGraph(RdfStoreFactory.SITE_CONFIG_URI));

		Quad quad = new Quad(Node.createURI(RdfStoreFactory.SITE_CONFIG_URI),
				Node.createURI("http://example.com/curator"),
				RDF.type.asNode(), CCLEARN.Curator.asNode());

		assert (store.getGraphset().containsQuad(quad));
	}

	public void testForProvenance() {

		// create the factory
		RdfStoreFactory store = new RdfStoreFactory(this.graphset);

		// get the wrappers for multiple provenances and add information
		Resource r1 = new Resource(URI.create("http://example.org/resource"));
		r1.getSubjects().add("subject1");
		store.forProvenance("http://example.org/feed1").save(r1);

		Resource r2 = new Resource(URI.create("http://example.org/resource"));
		r2.getSubjects().add("subject2");
		store.forProvenance("http://example.org/feed2").save(r2);

		Resource r3 = new Resource(URI.create("http://example.org/resource"));
		r3.getSubjects().add("subject3");
		store.forProvenance("http://example.org/feed3").save(r3);

		// retrieve the resource for a specific provenance and check for leakage
		Resource r = store.forProvenance("http://example.org/feed1").load(
				Resource.class, "http://example.org/resource");
		assertEquals(r.getSubjects().size(), 1);
		assert(r.getSubjects().contains("subject1"));
		assertFalse(r.getSubjects().contains("subject3"));
		
		r = store.forProvenance("http://example.org/feed2").load(
				Resource.class, "http://example.org/resource");
		assertEquals(r.getSubjects().size(), 1);
		System.out.println(r.getSubjects().size());
		assert(r.getSubjects().contains("subject2"));
		assertFalse(r.getSubjects().contains("subject3"));
		
		// get the full resource
		r = store.getReader().load(Resource.class, "http://example.org/resource");
		assertEquals(r.getSubjects().size(), 3);
	}

	public void testGetAllKnownTripleStoreUris() {

		// create the factory
		RdfStoreFactory store = new RdfStoreFactory(this.graphset);

		// get the wrappers for multiple provenances and add information
		store.forProvenance("http://example.org/feed1").save(
				new Resource(URI.create("http://example.org/resource")));
		store.forProvenance("http://example.org/feed2").save(
				new Resource(URI.create("http://example.org/resource")));
		store.forProvenance("http://example.org/feed3").save(
				new Resource(URI.create("http://example.org/resource")));

		// list the known graphs and assert our provenances are there
		assertEquals(store.getAllKnownTripleStoreUris().size(), 3);
		assert (store.getAllKnownTripleStoreUris()
				.contains("http://example.org/feed2"));
	}

	public void testGetGraphset() {
		// create the factory
		RdfStoreFactory store = new RdfStoreFactory(this.graphset);

		// assert that we get the same graphset out
		assert (store.getGraphset().equals(this.graphset));
	}

	public void testGetPPP2ObjectMapForSubjectAndPredicate() {
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
