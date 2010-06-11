package org.creativecommons.learn.feed;

import java.sql.SQLException;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.oercloud.Curator;

public class AddCurator {

	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		
		if (args.length < 2) {
			System.out.println("AddCurator");
			System.out.println("usage: AddCurator [curator_name] [url] ");
			System.out.println();
			
			System.exit(1);
		}

		String name = args[0];
		String url = args[1];
		addCurator(name, url);
	}
		
	public static void addCurator(String name, String url) throws SQLException {
		String graphName = "http://creativecommons.org/#site-configuration";
		RdfStore store = RdfStore.uri2RdfStore(graphName);
		
		Curator new_curator = new Curator(url);
		new_curator.setName(name);
		store.save(new_curator);
	}

}
