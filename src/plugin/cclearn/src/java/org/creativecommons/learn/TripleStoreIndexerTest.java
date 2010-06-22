package org.creativecommons.learn;

import java.util.Collection;

import org.creativecommons.learn.oercloud.Resource;

import junit.framework.TestCase;

public class TripleStoreIndexerTest extends TestCase {
	public static void testGenerateAllPossibleColumnNames() {
		// first, create a Triple (admittedly, in the siteConfigurationStore) that
		// has a predicate that's attached to a Resource.
		Resource r = new Resource("http://example.com/#resource");
		r.setTitle("A title");
		RdfStore store = RdfStore.getSiteConfigurationStore();
		store.save(r);
		
		// Now, ask the TripleStoreIndexer what column names it has.		
		TripleStoreIndexer indexer = new TripleStoreIndexer();
		
		Collection<String> got = indexer.getAllPossibleFieldNames();
		assertTrue(got.contains("1__dct_title"));
	}
}
