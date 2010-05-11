package org.creativecommons.learn.feed;

import java.util.Collection;

import org.creativecommons.learn.TripleStore;
import org.creativecommons.learn.oercloud.Curator;

public class ListCurators {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// list feeds we're tracking
		Collection<Curator> curators = QuadStore.getSiteConfigurationStore().load(Curator.class); 
			
		for (Curator c : curators) {
			System.out.println(c.getName() + " (" + c.getUrl() + ")");
		}
		
	}

}
