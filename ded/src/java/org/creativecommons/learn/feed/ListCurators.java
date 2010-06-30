package org.creativecommons.learn.feed;
import org.creativecommons.learn.RdfStore;


import java.util.Collection;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.oercloud.Curator;

public class ListCurators {

	/**
     * List feeds we're tracking
	 * @param args
	 */
	public static void main(String[] args) {
		
		Collection<Curator> curators = RdfStore.forDEd().load(Curator.class); 
			
		for (Curator c : curators) {
			System.out.println(c.getName() + " (" + c.getUrl() + ")");
		}
		
	}

}
