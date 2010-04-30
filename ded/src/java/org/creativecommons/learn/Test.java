package org.creativecommons.learn;

import java.util.Collection;

import org.creativecommons.learn.oercloud.Curator;

import thewebsemantic.NotFoundException;
import junit.framework.*;
public class Test extends TestCase {
	
	/** A unit test that shows adding a curator works. */
	public void testAddCurator() {
		/* Like, did it get saved? */
		TripleStore store = TripleStore.get();

		/* We have no Curators at the start */
		Collection<Curator> available_curators = store.load(org.creativecommons.learn.oercloud.Curator.class);
		assertEquals(available_curators.size(), 0);

		/* Create a Curator, as if we were using the command line */
		org.creativecommons.learn.feed.AddCurator.addCuratorWithNameAndUrl("MIT", "http://mit.edu/");

		available_curators = store.load(org.creativecommons.learn.oercloud.Curator.class);
		assertEquals(available_curators.size(), 1);
		
		/* Make sure we saved it correctly */
		Curator curator = available_curators.iterator().next();
		assertEquals(curator.getName(), "MIT");
		assertEquals(curator.getUrl(), "http://mit.edu/");
	}

		
    public void testIntegration()
    {
    	// Steps:
    	// 1. Add a curator to the triple store
    	// 2. Add a feed to the triple store
    	// 3. (templated on type of feed) aggregate package pulls data in
    	// ...? 
        assertTrue( "TestExample", true );
    }

}
