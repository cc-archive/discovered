package org.creativecommons.learn;

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

	public void testForDEd() {
		// create the factory
		RdfStoreFactory store = new RdfStoreFactory(this.graphset);

		// get the wrapper for DEd configuration and add something
		RdfStore ded = store.forDEd();
		ded.save(new Curator("http://example.com/curator"));

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
		Resource r1 = new Resource("http://example.org/resource");
		r1.getSubjects().add("subject1");
		store.forProvenance("http://example.org/feed1").save(r1);

		Resource r2 = new Resource("http://example.org/resource");
		r2.getSubjects().add("subject2");
		store.forProvenance("http://example.org/feed2").save(r2);

		Resource r3 = new Resource("http://example.org/resource");
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
		//r = new RdfStore(store.asModel()).load(Resource.class, "http://example.org/resource");
		//System.out.println(r.getSubjects().size());
	}

	public void testGetAllKnownTripleStoreUris() {

		// create the factory
		RdfStoreFactory store = new RdfStoreFactory(this.graphset);

		// get the wrappers for multiple provenances and add information
		store.forProvenance("http://example.org/feed1").save(
				new Resource("http://example.org/resource"));
		store.forProvenance("http://example.org/feed2").save(
				new Resource("http://example.org/resource"));
		store.forProvenance("http://example.org/feed3").save(
				new Resource("http://example.org/resource"));

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

	public void testGetPPP2ObjectMapForSubject() {
		fail("Not yet implemented"); // TODO
	}

	public void testGetPPP2ObjectMapForSubjectAndPredicate() {
		fail("Not yet implemented"); // TODO
	}

	public void testGetProvenanceURIsFromCuratorShortName() {
		fail("Not yet implemented"); // TODO
	}

}
