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
		TripleStore store = TripleStore.get();
		store.save(r);
		
		// Now, ask the TripleStoreIndexer what column names it has.		
		TripleStoreIndexer indexer = new TripleStoreIndexer();
		
		Collection<String> got = indexer.getAllPossibleColumnNames();
		assertTrue(got.contains("_dct_title"));
	}
}
