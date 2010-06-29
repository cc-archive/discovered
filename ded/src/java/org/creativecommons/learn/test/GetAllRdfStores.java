package org.creativecommons.learn.test;

import java.sql.SQLException;
import java.util.List;

import org.creativecommons.learn.RdfStore;

public class GetAllRdfStores extends DiscoverEdTestCase {

	public static void test() throws SQLException {
		RdfStore store = RdfStore.forDEd();
		List<String> uris = RdfStore.getAllKnownTripleStoreUris();
		assertEquals(RdfStore.SITE_CONFIG_URI, uris.get(0));
        store.close();
	}

	public static void testWorksTheSecondTime() throws SQLException {
		RdfStore store = RdfStore.forDEd();
		RdfStore the_same_store = RdfStore.forDEd();
		List<String> uris = RdfStore.getAllKnownTripleStoreUris();
		assertEquals(RdfStore.SITE_CONFIG_URI, uris.get(0));
		assertEquals(uris.size(), 1);
        store.close();
        the_same_store.close();
	}

}
