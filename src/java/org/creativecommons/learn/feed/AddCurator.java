package org.creativecommons.learn.feed;

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
		
		try {
			Curator new_c = Curator.create(url);
			new_c.setName(name);
			
		} catch (InstantiationException e1) {
			e1.printStackTrace();

			System.out.println("Curator already exists.");
			System.exit(1);
		}
				
	}

}
