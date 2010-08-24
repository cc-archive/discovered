package org.creativecommons.learn;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.creativecommons.learn.RdfStore;

import de.fuberlin.wiwiss.ng4j.NamedGraph;

public class TestGetAllRdfStores extends DiscoverEdTestCase {

	public static void test() throws SQLException {
		RdfStore store = RdfStoreFactory.get().forDEd();
		Iterator<NamedGraph> it = RdfStoreFactory.get().listProvenanceGraphs();
		assertEquals(RdfStoreFactory.SITE_CONFIG_URI, it.next().getGraphName().getURI());
		assertFalse(it.hasNext());
        store.close();
	}

	public static void testWorksTheSecondTime() throws SQLException {
		RdfStore store = RdfStoreFactory.get().forDEd();
		RdfStore the_same_store = RdfStoreFactory.get().forDEd();
		Iterator<NamedGraph> it = RdfStoreFactory.get().listProvenanceGraphs();
		assertEquals(RdfStoreFactory.SITE_CONFIG_URI, it.next().getGraphName().getURI());
		assertFalse(it.hasNext());
        store.close();
        the_same_store.close();
	}

}
