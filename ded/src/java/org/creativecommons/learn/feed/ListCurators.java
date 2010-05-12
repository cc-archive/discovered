package org.creativecommons.learn.feed;
import org.creativecommons.learn.RdfStore;


import java.util.Collection;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.oercloud.Curator;

public class ListCurators {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// list feeds we're tracking
		Collection<Curator> curators = RdfStore.getSiteConfigurationStore().load(Curator.class); 
			
		for (Curator c : curators) {
			System.out.println(c.getName() + " (" + c.getUrl() + ")");
		}
		
	}

}
