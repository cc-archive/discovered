package org.creativecommons.learn;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;

import junit.framework.TestCase;

public class TestMappedFieldQueryFilter extends DiscoverEdTestCase {
	
	Curator c;
	Feed f;
	Feed distractorFeed;
	
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
		distractorFeed = new Feed(URI.create("http://example.com/#distractor_feed"));
		distractorFeed.setCurator(distractor);
		
		RdfStoreFactory factory = RdfStoreFactory.get(); 
		RdfStore dedStore = factory.forDEd();
		dedStore.save(c);
		dedStore.save(f);
		dedStore.save(distractor);
		dedStore.save(distractorFeed);
		
		// Make sure that provenances exist for the two feeds
		RdfStore fStore = factory.forProvenance(f.getUri().toString());
		fStore.save(f);
		
		RdfStore distractorFeedStore = factory.forProvenance(distractorFeed.getUri().toString());
		distractorFeedStore.save(distractorFeed);		
	}
	
	public void testGetActiveProvenances() {
		HashSet<String> excludedCuratorURIs = new HashSet<String>();
		excludedCuratorURIs.add(c.getUri().toString());
		
		HashSet<String> feedURIsNotFromThisCurator = new HashSet<String>();
		feedURIsNotFromThisCurator.add(distractorFeed.getUri().toString());
		feedURIsNotFromThisCurator.add(RdfStoreFactory.SITE_CONFIG_URI);
	
		// First, exclude the curator c. This means that only the distractor is enabled.
		Collection<String> activeProvenancesWhenExcludingC = MappedFieldQueryFilter.getActiveProvenanceURIs(excludedCuratorURIs);
		assertEquals(feedURIsNotFromThisCurator, activeProvenancesWhenExcludingC);
		
		HashSet<String> allFeedURIs = new HashSet<String>();
		allFeedURIs.add(f.getUri().toString());
		allFeedURIs.add(distractorFeed.getUri().toString());
		allFeedURIs.add(RdfStoreFactory.SITE_CONFIG_URI);
		
		// Then, exclude no curators. This means both the distractor and f are enabled.
		Collection <String> activeProvenancesWhenExcludingNothing = MappedFieldQueryFilter.getActiveProvenanceURIs(new HashSet<String>());
		assertEquals(allFeedURIs, activeProvenancesWhenExcludingNothing);
	}
}
