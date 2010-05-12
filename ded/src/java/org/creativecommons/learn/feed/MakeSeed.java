package org.creativecommons.learn.feed;
import org.creativecommons.learn.RdfStore;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.creativecommons.learn.CCLEARN;
import org.creativecommons.learn.RdfStore;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class MakeSeed {

	@SuppressWarnings("static-access")
	private static Options getOptions() {

		Options options = new Options();

		Option help = new Option("help", "Print command line arguments");
		Option seed_dir = OptionBuilder.withArgName("seeddir").hasArg()
				.withDescription("Write the seed file to this directory")
				.isRequired(false).create("seeddir");
		Option curator = OptionBuilder.withArgName("curator").hasArgs()
				.withDescription(
						"Only seed URLs belonging to these curator(s).")
				.isRequired(false).create("curator");

		options.addOption(help);
		options.addOption(seed_dir);
		options.addOption(curator);

		return options;
	}

	/**
	 * @param args
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws ClassNotFoundException {

		// create the parser
		CommandLineParser parser = new GnuParser();
		CommandLine line = null;

		try {
			// parse the command line arguments
			line = parser.parse(getOptions(), args);
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
		}

		if (line.hasOption("help")) {
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("seed", getOptions());

			System.exit(0);
		}

		PrintWriter output = new PrintWriter(System.out);

		if (line.hasOption("seeddir")) {
			// create the target directory if necessary
			File seed_dir = new File(args[0]);
			if (!seed_dir.exists()) {
				seed_dir.mkdirs();
			} else if (!seed_dir.isDirectory()) {
				throw new RuntimeException("Seed directory must be a directory");
			}

			// get the seed filename
			File seed_file = new File(seed_dir, new Date().toString().replace(
					" ", "_").replace(":", "_"));

			try {
				output = new PrintWriter(new FileWriter(seed_file));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
		}

		Model store_model = RdfStore.getSiteConfigurationStore().getModel();

		// see if we're dealing with all resources or for a specific curator
		if (!line.hasOption("curator")) {
			// all resources

			// write out all resources to the seed list
			ResIterator subjects = store_model.listSubjectsWithProperty(
					RDF.type, CCLEARN.Resource);
			while (subjects.hasNext()) {
				Resource subject = subjects.nextResource();
				output.write(subject.getURI() + "\n");
			}

		} else {
			// one or more curators -- build a list of feeds
			String[] curators = line.getOptionValues("curator");

			for (String curator_url : curators) {

				// write out all resources to the seed list
				String queryString = ""
						+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
						+ "PREFIX cclearn: <http://learn.creativecommons.org/ns#> \n"
						+ "\n" + "SELECT ?r \n" + "WHERE { \n"
						+ "   ?r rdf:type cclearn:Resource . \n"
						+ "   ?r cclearn:source ?f . \n"
						+ "   ?f cclearn:hasCurator <" + curator_url + ">. \n"
						+ "   }\n";

				QueryExecution qexec = QueryExecutionFactory.create(
						queryString, store_model);
				try {
					ResultSet results = qexec.execSelect();
					for (; results.hasNext();) {
						QuerySolution soln = results.nextSolution();
						Resource r = soln.getResource("r");

						output.write(r.getURI() + "\n");
					}
				} finally {
					qexec.close();
				}

			}
		}

		output.close();

	} // main
} // MakeSeed
