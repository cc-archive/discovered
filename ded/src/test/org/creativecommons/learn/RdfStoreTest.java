package org.creativecommons.learn;

import junit.framework.TestCase;

import org.creativecommons.learn.oercloud.Resource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;

public class RdfStoreTest extends TestCase {

	public void testSaveIExtensibleResource() {

        String provenanceURI = "http://example.com/#provenance";

		// create a Jena model
		Model model = ModelFactory.createDefaultModel();

		// create a Triple Store with that model
		RdfStore store = RdfStore.forProvenance(provenanceURI, model);

		// create a Resource
		Resource r = new Resource("http://example.org/resource");

		// add some metadata
		r.setTitle("Title");
		r.addField(model.createProperty("http://example.org/foo"), model
				.createLiteral("Hello, World"));

		// save the resource
		store.save(r);

		// assert that the correct triple exists in the model
		com.hp.hpl.jena.rdf.model.Resource subject_resource = model
				.createResource("http://example.org/resource");

		assertTrue(model.contains(subject_resource, RDF.type, CCLEARN.Resource));
		assertTrue(model.contains(subject_resource, DC.title, model
				.createLiteral("Title")));
		assertTrue(model.contains(subject_resource, model
				.createProperty("http://example.org/foo"), model
				.createLiteral("Hello, World")));

	}

	public void testLoadClassOfIExtensibleResourceString() {

        String provenanceURI = "http://example.com/#provenance";

		// create a Jena model
		Model model = ModelFactory.createDefaultModel();

		// create a Triple Store for our model
		RdfStore store = RdfStore.forProvenance(provenanceURI, model);

		// create a Resource and save it -- "stub" declaration
		Resource without_metadata = new Resource("http://example.org/resource");
		store.save(without_metadata);

		// add triples for a Resource with metadata
		com.hp.hpl.jena.rdf.model.Resource subject_resource = model
				.createResource("http://example.org/resource");

		model.add(subject_resource, DC.title, "The Title");
		model.add(subject_resource, model
				.createProperty("http://example.org/foo"), "bar");
		model.add(subject_resource, model
				.createProperty("http://example.org/bar"), model
				.createResource("http://creativecommons.org"));

		// load the Resource using the TripleStore interface
		Resource r = store.load(Resource.class, "http://example.org/resource");

		// assert the properties and fields are loaded correctly
		assertEquals(r.getTitle(), "The Title");
		assertTrue(r.getFields().containsKey(model.createProperty("http://example.org/foo")));
		assertTrue(r.getFields().containsKey(model.createProperty("http://example.org/bar")));
		
		assertEquals(r.getFieldValues(model.createProperty("http://example.org/foo")).size(), 1);
		assertEquals(r.getFieldValues(model.createProperty("http://example.org/foo")).toArray()[0],
				ResourceFactory.createPlainLiteral("bar"));
		
		assertEquals(r.getFieldValues(model.createProperty("http://example.org/bar")).size(), 1);
		assertEquals(r.getFieldValues(model.createProperty("http://example.org/bar")).toArray()[0],
				ResourceFactory.createResource("http://creativecommons.org"));

	}
}
