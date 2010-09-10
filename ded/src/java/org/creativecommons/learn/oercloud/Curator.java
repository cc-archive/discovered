package org.creativecommons.learn.oercloud;

import java.util.HashSet;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.creativecommons.learn.CCLEARN;
import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.RdfStoreFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.vocabulary.RDF;

import de.fuberlin.wiwiss.ng4j.Quad;

import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfProperty;
import thewebsemantic.Sparql;

@Namespace("http://learn.creativecommons.org/ns#")
public class Curator {

	private URI uri = null;
	private String name = null;
	private final static Log LOG = LogFactory.getLog(RdfStore.class);

		
	public Curator(String url) throws URISyntaxException {
		this.uri = new URI(url);		
	}
	
	public Curator(URI uri) {
		this.uri = uri;
	}
	
	public static Curator getByUrl(RdfStore store, String url) {
		return null;
	}
	
	/*
	 * This method is probably pretty expensive to compute. It'd be
	 * nice to cache it, or otherwise do something smart.
	 */
	public int getNumberOfResources() {
		Collection<Feed> feeds = this.getFeeds();
		// for all of our feeds, look in each one's graph for 
		// statements like <?> rdf:type CCLEARN.RESOURCE
		
		// toss all those resources into a hashset and return the size at the end.
		
		HashSet<String> resources = new HashSet<String>();

		for (Feed f : feeds) {
			LOG.info("a feed indeed");
			Iterator<Quad> it = RdfStoreFactory.get().findQuads(
					Node.createURI(f.getUri().toString()),
					null,
					RDF.type.asNode(),
					CCLEARN.Resource.asNode());
			while (it.hasNext()) {
				Quad next = it.next();
				// make sure this objec thas type 
				resources.add(next.getSubject().getURI());
			}
		}
		return resources.size();
	}

	@RdfProperty("http://purl.org/dc/elements/1.1/title")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Deprecated
	public String getUrl() {
		return uri.toString();
	}

	@Id
	public URI getUri() {
		return uri;
	}
	
	public void setUri(URI uri) {
		this.uri = uri;
	}
	
	public Collection<Feed> getFeeds() {
		
		String query = ""
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
			+ "PREFIX cclearn: <http://learn.creativecommons.org/ns#> \n"
			+ "\n" + "SELECT ?s \n" + "WHERE { \n"
			+ "?s rdf:type cclearn:Feed .\n"
			+ "?s cclearn:hasCurator <" + this.getUrl() + ">. \n"
			+ "   }\n";
		
		return Sparql.exec(RdfStoreFactory.get().forDEd().getModel(), Feed.class, query);
	
	}

	public static String curatorUriCollectionAsString(Collection<String> curatorURIs) {
		ArrayList<String> sortedListOfCuratorURIs = new ArrayList<String>(curatorURIs);
	    java.util.Collections.sort(sortedListOfCuratorURIs);
	    String all_curators_string = StringUtils.join(sortedListOfCuratorURIs.iterator(), " ");
	    return all_curators_string;
	}
	
	private static HashSet<HashSet<String>> generatePowerSet(Collection<String> input) {
		ArrayList<String> startingPoint = new ArrayList<String>(input);
		/* 
		 * The power set gets big fast. If the input is more than six large, just bail out.
		 */
		if (startingPoint.size() > 6) {
			throw new RuntimeException("I am not going to put more than 64 curator strings in Lucene. Bailing out now.");
		}
		
		HashSet<HashSet<String>> thePowerSet = new HashSet<HashSet<String>>(); 
	
		// okay, now that the collection is flattened into an arraylist, we can use
		// binary counting to determine the contents.
		for (int i = 0; i < Math.pow(2, startingPoint.size()); i++) {
			System.err.println(" i is " + i);
			if (i == 0) {
				// skip this. We don't bother creating the empty set.
				continue;
			}
			
			// Now use bitwise operations to determine the contents of this subset.
			HashSet<String> thisSubset = new HashSet<String>();
			for (int j = 0 ; j < startingPoint.size(); j++) {
				int flagValue = (int) Math.pow(2, j);
				int masked = i & flagValue;
				if (masked != 0) {
	    			String thisString = startingPoint.get(j);
	    			thisSubset.add(thisString);
				}
			thePowerSet.add(thisSubset);
			}
		}
		
		HashSet<String> ret = new HashSet<String>();
		for (HashSet<String> subset : thePowerSet) {
			ret.add(Curator.curatorUriCollectionAsString(subset));
		}
	
		return thePowerSet;
	}
	
	public static Collection<String> curatorUriPowerSetAsStrings(Collection<String> curatorURIs) {
		HashSet<String> ret = new HashSet<String>();
		HashSet<HashSet<String>> powerSet = generatePowerSet(curatorURIs);
		for (HashSet<String> subSet : powerSet) {
			ret.add(curatorUriCollectionAsString(subSet));
		}

		return ret;
	}

}
