package org.creativecommons.learn.feed;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.RdfStoreFactory;
import org.creativecommons.learn.oercloud.Curator;

public class AddCurator {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws URISyntaxException 
	 */
	public static void main(String[] args) throws URISyntaxException {
		
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
		
	public static void addCurator(String name, URI uri) {
		RdfStore store = RdfStoreFactory.get().forDEd();
		
		Curator new_curator = new Curator(uri);
		new_curator.setName(name);
		store.save(new_curator);
	}

	public static void addCurator(String name, String url) throws URISyntaxException {		
		addCurator(name, new URI(url));
	}

}
