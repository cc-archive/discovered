package org.creativecommons.learn.oercloud;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.RdfStoreFactory;

import com.hp.hpl.jena.rdf.model.ResourceFactory;

import junit.framework.TestCase;

public class TestResource extends TestCase {

	public void testAddField() {

		Resource r = new Resource(URI.create("http://example.org/foo"));
		Object[] expected = new Object[1];
		expected[0] = ResourceFactory.createPlainLiteral("value");

		r.addField(ResourceFactory.createProperty("http://example.org/bar"),
				ResourceFactory.createPlainLiteral("value"));

		Object[] fieldValues = r.getFields().get(
				ResourceFactory.createProperty("http://example.org/bar"))
				.toArray();
		
		assertEquals(fieldValues.length, expected.length);
		assertTrue(fieldValues[0].equals(expected[0]));

	}

	public static void testResourceCanListItsProvenances() {
		// First, create a Resource that appears in feeds curated by multiple organizations
		Resource r = new Resource(URI.create("http://example.com/#resource"));
		r.setTitle("A title");

		// curator 1
		Curator c1 = new Curator(URI.create("http://example.com/#curator1"));
		RdfStoreFactory.get().forDEd().save(c1);

		// feed 1
		Feed f1 = new Feed(URI.create("http://example.com/#feed1"));
		f1.setCurator(c1);
		RdfStoreFactory.get().forDEd().save(f1);

		// provenance 1
		RdfStore store1 = RdfStoreFactory.get().forProvenance(f1.getUrl());
		store1.save(r);

		// curator 2
		Curator c2 = new Curator(URI.create("http://example.com/#curator2"));
		RdfStoreFactory.get().forDEd().save(c2);

		// feed 1
		Feed f2 = new Feed(URI.create("http://example.com/#feed2"));
		f2.setCurator(c2);
		RdfStoreFactory.get().forDEd().save(f2);

		// provenance 2
		RdfStore store2 = RdfStoreFactory.get().forProvenance(f2.getUrl());
		store2.save(r);

		// Then, test that we can know ask the Resource to tell us all the URIs that have curated it
		Collection<String> curatorURIs = r.getAllCuratorURIs();

		HashSet<String> expected = new HashSet<String>();
		expected.add(c1.getUrl());
		expected.add(c2.getUrl());

		assertEquals(curatorURIs, expected);

	}
	public void testGetFieldValues() {

		Resource r = new Resource(URI.create("http://example.org/foo"));

		// Fields is empty when we begin
		assertEquals(r.getFields().size(), 0);

		// We'll add a value
		r.addField(ResourceFactory.createProperty("http://example.org/bar"),
				ResourceFactory.createPlainLiteral("value"));

		// We expect one Field to exist
		assertEquals(r.getFields().size(), 1);

		assertEquals(r.getFieldValues(
				ResourceFactory.createProperty("http://example.org/bar"))
				.size(), 1);

	}

	public void testGetFields() {
		Resource r = new Resource(URI.create("http://example.org/foo"));

		// Add two fields with a value each
		r.addField(ResourceFactory.createProperty("http://example.org/ns#a"),
				ResourceFactory.createPlainLiteral("1"));
		r.addField(ResourceFactory.createProperty("http://example.org/ns#b"),
				ResourceFactory.createResource("http://example.org/3"));

		assertEquals(r.getFields().size(), 2);

		assertTrue(r.getFields().containsKey(
				ResourceFactory.createProperty("http://example.org/ns#a")));
		assertTrue(r.getFields().containsKey(
				ResourceFactory.createProperty("http://example.org/ns#b")));

		assertEquals(r.getFields().get(
				ResourceFactory.createProperty("http://example.org/ns#a"))
				.size(), 1);
	}
}
