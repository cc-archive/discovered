package org.creativecommons.learn.feed;

import java.util.List;

import org.creativecommons.learn.oercloud.Curator;

public class ListCurators {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// list feeds we're tracking
		List<Curator> curators = Curator.getAll();
			
		for (Curator c : curators) {
			System.out.println(c.getName() + " (" + c.getUrl() + ")");
		}
		
	}

}
