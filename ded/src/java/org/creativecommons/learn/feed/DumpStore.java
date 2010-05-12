package org.creativecommons.learn.feed;
import org.creativecommons.learn.RdfStore;


import org.creativecommons.learn.RdfStore;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;

public class DumpStore {

	
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException {
		
		if (args.length == 1 && args[0].equals("--help")) {
			System.out.println("DumpStore");
			System.out.println("usage: DumpStore [format]");
			System.out.println("where format is a Jena output format, ie: 'N-TRIPLE', 'RDF/XML', 'RDX/XML-ABBREV'");
			System.out.println("if not provided, default to RDF/XML");
			System.out.println();

			System.exit(1);
		}
		
		// determine the output format, defaulting to RDF/XML
		String format = (args.length > 0) ? args[0] : "RDF/XML";
		
		// get an iterator for all subjects
		Model store = RdfStore.getSiteConfigurationStore().getModel();
		ResIterator subjects = store.listSubjects();
		
		// write out one subject at a time
		while (subjects.hasNext()) {
			store.query(new SubjectSelector(subjects.nextResource())).
					write(System.out, format);
		}
		
	}

}
