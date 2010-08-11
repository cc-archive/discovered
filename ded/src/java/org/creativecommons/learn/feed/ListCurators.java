package org.creativecommons.learn.feed;
import java.util.Collection;

import org.creativecommons.learn.RdfStoreFactory;
import org.creativecommons.learn.oercloud.Curator;

public class ListCurators {

	/**
     * List feeds we're tracking
	 * @param args
	 */
	public static void main(String[] args) {
		
		Collection<Curator> curators = RdfStoreFactory.get().forDEd().load(Curator.class); 
			
		for (Curator c : curators) {
			System.out.println(c.getName() + " (" + c.getUrl() + ")");
		}
		
	}

}
