package org.creativecommons.learn.feed;

import java.sql.SQLException;

import javax.naming.NamingException;

import org.creativecommons.learn.TripleStore;

public class DumpStore {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws NamingException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, NamingException, SQLException {
		
		if (args.length == 1 && args[0].equals("--help")) {
			System.out.println("DumpStore");
			System.out.println("usage: DumpStore [format]");
			System.out.println("where format is a Jena output format, ie: 'N-TRIPLE', 'RDF/XML', 'RDX/XML-ABBREV'");
			System.out.println("if not provided, default to RDF/XML");
			System.out.println();

			System.exit(1);
		}
		
		String format = (args.length > 0) ? args[0] : "RDF/XML";
		
		TripleStore.get().getModel().write(System.out, format);

	}

}
