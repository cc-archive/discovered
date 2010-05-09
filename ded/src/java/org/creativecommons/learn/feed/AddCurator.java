package org.creativecommons.learn.feed;

import java.sql.SQLException;

import org.creativecommons.learn.QuadStore;
import org.creativecommons.learn.TripleStore;
import org.creativecommons.learn.oercloud.Curator;

public class AddCurator {

	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		
		if (args.length < 2) {
			System.out.println("AddCurator");
			System.out.println("usage: AddCurator [source_uri] [curator_name] [url] ");
			System.out.println();
			
			System.exit(1);
		}

		String graphName = args[0];
		String name = args[1];
		String url = args[2];
		addCurator(graphName, name, url);
	}
		
	public static void addCurator(String graphName, String name, String url) throws SQLException {
		
		QuadStore store = new QuadStore(graphName);
		
		Curator new_curator = new Curator(url);
		new_curator.setName(name);
		store.save(new_curator);
	}

}
