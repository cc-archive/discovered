package org.creativecommons.learn.feed;

import org.creativecommons.learn.TripleStore;
import org.creativecommons.learn.oercloud.Curator;

public class AddCurator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length < 2) {
			System.out.println("AddCurator");
			System.out.println("usage: AddCurator [curator_name] [url] ");
			System.out.println();
			
			System.exit(1);
		}

		String name = args[0];
		String url = args[1];
		
		Curator new_curator = new Curator(url);
		new_curator.setName(name);
		
		TripleStore.get().save(new_curator);
						
	}

}
