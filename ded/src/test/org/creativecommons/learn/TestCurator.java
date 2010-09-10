package org.creativecommons.learn;

import java.net.URI;
import java.util.HashSet;

import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;
import org.creativecommons.learn.oercloud.Resource;

public class TestCurator extends DiscoverEdTestCase {
	public static void testCreatePowerSetOfCurators() {
		String curator1 = "http://example.com/#curator1";
		String curator2 = "http://example.com/#curator2";
		HashSet<String> curatorURIs = new HashSet<String>();
		curatorURIs.add(curator1);
		curatorURIs.add(curator2);
		
		HashSet<String> expected = new HashSet<String>();
		expected.add(curator1);
		expected.add(curator2);
		expected.add(curator1 + " " + curator2);
		assertEquals(expected,
				Curator.curatorUriPowerSetAsStrings(curatorURIs));
	}
	
	public static void testNumberOfResources() {
		RdfStoreFactory x = RdfStoreFactory.get();
		
		RdfStore store = x.forDEd();
		
		// Create two resources curated by the same organization
		Curator c = new Curator(URI.create("http://example.com/#curator"));
		store.save(c);
		
		// the curator created a feed...
		Feed f = new Feed(URI.create("http://example.com/#feed"));
		f.setCurator(c);
		store.save(f);
		
		// and toss some data about some resources into the feed's store
		RdfStore feedStore = x.forProvenance(f.getUri().toString());
	
		Resource r1 = new Resource(URI.create("http://example.com/#resource1"));
		Resource r2 = new Resource(URI.create("http://example.com/#resource2"));
		feedStore.save(r1);
		feedStore.save(r2);
	
		// make sure the curators's .getNumberOfResources() method returns 2
		assertSame(c.getNumberOfResources(), 2);
		
		
	}
}
