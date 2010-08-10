package org.creativecommons.learn;

import java.util.ArrayList;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;

public class TestGetProvenanceURIsFromCuratorShortName extends DiscoverEdTestCase {
	public void test() {
		
		// Pre-conditions
		
		// Create a Curator with a nickname
		Curator c = new Curator("http://example.com/#curator");
		c.setName("shorty");
		
		// That curator has a feed
		Feed f = new Feed("http://example.com/#feed");
		f.setCurator(c);
		
		// Create a different Curator with a nickname
		Curator distractor = new Curator("http://example.com/#curator_the_distractor");
		distractor.setName("mwaha!");
		
		// That second curator has a feed
		Feed distractorFeed = new Feed("http://example.com/#distractor_feed");
		distractorFeed.setCurator(distractor);
		
		RdfStore store = RdfStore.forDEd();
		store.save(c);
		store.save(f);
		store.save(distractor);
		store.save(distractorFeed);
		
		ArrayList<String> uris = RdfStore.getProvenanceURIsFromCuratorShortName("shorty");
		
		// That there above variable "uris" should be a list of URIs of provenances (= feed URLs) pertaining to the curator named "shorty"
		assertEquals(uris.get(0), f.getUrl());
		
		// We didn't grab the distractor curator, did we? If we did, then the
		// size() would be 2, obviously.
		assertEquals(uris.size(), 1);
		
	}
}

