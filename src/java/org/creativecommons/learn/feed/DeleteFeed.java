package org.creativecommons.learn.feed;

import org.creativecommons.learn.CCLEARN;
import org.creativecommons.learn.TripleStore;
import org.creativecommons.learn.oercloud.OerFeed;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.RDF;

public class DeleteFeed {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		if (args.length != 1) {
			System.out.println("DeleteFeed");
			System.out.println("usage: DeleteFeed [feed_url]");
			System.out.println();
			
			System.exit(1);
		}

		String url = args[0];
		
		//  make sure the feed exists
		OerFeed new_feed = OerFeed.feedByUrl(url);
		if (new_feed == null) {
			System.out.println("Feed does not exist");
			System.exit(1);
		}
		
		try {
			Model m = TripleStore.getModel();
			m.remove(m.createResource(url), RDF.type, CCLEARN.feed);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
