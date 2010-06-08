package org.creativecommons.learn.test;

import java.util.List;

import org.creativecommons.learn.RdfStore;

public class GetAllRdfStores extends DiscoverEdTestCase {

	public static void test() {
		RdfStore store = RdfStore.getSiteConfigurationStore();
		List<String> uris = RdfStore.getAllKnownTripleStoreUris();
		assertSame(RdfStore.SITE_CONFIG_URI, uris.get(0));
	}
	
}