package org.creativecommons.learn;

import java.sql.SQLException;
import java.util.List;

import org.creativecommons.learn.RdfStore;

public class TestGetAllRdfStores extends DiscoverEdTestCase {

	public static void test() throws SQLException {
		RdfStore store = RdfStoreFactory.get().forDEd();
		List<String> uris = RdfStoreFactory.get().getAllKnownTripleStoreUris();
		assertEquals(RdfStoreFactory.SITE_CONFIG_URI, uris.get(0));
        store.close();
	}

	public static void testWorksTheSecondTime() throws SQLException {
		RdfStore store = RdfStoreFactory.get().forDEd();
		RdfStore the_same_store = RdfStoreFactory.get().forDEd();
		List<String> uris = RdfStoreFactory.get().getAllKnownTripleStoreUris();
		assertEquals(RdfStoreFactory.SITE_CONFIG_URI, uris.get(0));
		assertEquals(uris.size(), 1);
        store.close();
        the_same_store.close();
	}

}
