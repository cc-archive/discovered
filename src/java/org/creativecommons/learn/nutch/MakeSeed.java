package org.creativecommons.learn.nutch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.creativecommons.learn.CCLEARN;
import org.creativecommons.learn.TripleStore;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class MakeSeed {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length != 1) {
			throw new RuntimeException(
					"Must provide a single argument -- the output directory for the seed list.");			
		}
		
		// create the target directory if necessary
		File seed_dir = new File(args[0]);
		if (!seed_dir.exists()) {
			seed_dir.mkdirs();
		} else
		if (!seed_dir.isDirectory()) {
			throw new RuntimeException("Seed directory must be a directory");
		}

		// get the seed filename
		File seed_file = new File(seed_dir, new Date().toString().replace(" ", "_").replace(":", "_"));
		
		// get a handle to the TripleStore
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(seed_file));

			Model store_model = TripleStore.get().getModel();

			// write out all resources to the seed list
			ResIterator subjects = store_model.listSubjectsWithProperty(RDF.type, CCLEARN.Resource);
			while (subjects.hasNext()) {
				Resource subject = subjects.nextResource();
				out.write(subject.getURI() + "\n");
			}
			
			out.close();
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	} // main

} // MakeSeed
