package org.creativecommons.learn.feed;
import org.creativecommons.learn.RdfStoreFactory;

public class DumpStore {

	
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException {
		
		if (args.length == 1 && args[0].equals("--help")) {
			System.out.println("DumpStore");
			System.out.println("usage: DumpStore [format]");
			System.out.println("where format is the output format, ie: 'TRIX', 'TRIG', 'RDF/XML', 'N-TRIPLE' and 'N3'");
			System.out.println("if not provided, default to TRIX");
			System.out.println();

			System.exit(1);
		}
		
		// determine the output format, defaulting to TRIX
		String format = (args.length > 0) ? args[0] : "TRIX";
		
		RdfStoreFactory.get().getGraphset().write(System.out, format, null);

	}

}
