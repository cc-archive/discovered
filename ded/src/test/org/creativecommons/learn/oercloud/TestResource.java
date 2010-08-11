package org.creativecommons.learn.oercloud;

import java.net.URI;

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
