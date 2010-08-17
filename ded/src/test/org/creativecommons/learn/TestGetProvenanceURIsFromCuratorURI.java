package org.creativecommons.learn;

import java.net.URI;
import java.util.Set;

import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;

public class TestGetProvenanceURIsFromCuratorURI extends DiscoverEdTestCase {
	private Curator c;
	private Feed f;
	
	public void setUp() {
		super.setUp();
		// Pre-conditions
		
		// Create a Curator with a nickname
		c = new Curator(URI.create("http://example.com/#curator"));
		c.setName("shorty");
		
		// That curator has a feed
		f = new Feed(URI.create("http://example.com/#feed"));
		f.setCurator(c);
		
		// Create a different Curator with a nickname
		Curator distractor = new Curator(URI.create("http://example.com/#curator_the_distractor"));
		distractor.setName("mwaha!");
		
		// That second curator has a feed
		Feed distractorFeed = new Feed(URI.create("http://example.com/#distractor_feed"));
		distractorFeed.setCurator(distractor);
		
		RdfStore store = RdfStoreFactory.get().forDEd();
		store.save(c);
		store.save(f);
		store.save(distractor);
		store.save(distractorFeed);		
	}
	
	public void testFindAppropriateFeeds() {
		Set<String> uris = RdfStoreFactory.get().getProvenanceURIsFromCuratorURI(c.getUri().toString());
		
		// That there above variable "uris" should be a list of URIs of provenances (= feed URLs) pertaining to the curator named "shorty"
		assertEquals(uris.iterator().next(), f.getUri().toString());

		// We didn't grab the distractor curator, did we? If we did, then the
		// size() would be 2, obviously.
		assertEquals(1, uris.size());
	}
	
	public void testExcludeNonexistentCurator() {
		Set<String> uris = RdfStoreFactory.get().getProvenanceURIsFromCuratorURI("http://example.com/no-such-curator");
		assertEquals(0, uris.size());
	}
}

