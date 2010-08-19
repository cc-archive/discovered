package org.creativecommons.learn;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;

import de.fuberlin.wiwiss.ng4j.Quad;

public class IndexFieldName {
	
	static Map<String, String> defaultNameSpaces = null;
	
	protected static Map<String, String> getDefaultNamespaces() {
		if (defaultNameSpaces == null) {
			// initialize the set of default mappings
			defaultNameSpaces = new HashMap<String, String>();
			defaultNameSpaces.put(CCLEARN.getURI(), CCLEARN.getDefaultPrefix());
			defaultNameSpaces.put("http://purl.org/dc/elements/1.1/", "dct");
			defaultNameSpaces.put("http://purl.org/dc/terms/", "dct");
			defaultNameSpaces.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");			
		}
		return defaultNameSpaces;
	}

	static public String toFieldName(Quad q) {
		
		String provenanceURI = q.getGraphName().getURI();
		
		Node predicateNode = q.getPredicate();
		String predicate = predicateNode.toString(); 
		
		// see if we want to collapse the predicate into a shorter convenience
		// value
		if (predicateNode.isURI()) {
			predicate = collapseResource(predicate);
		}
		
		String fieldName = makeCompleteFieldNameWithProvenance(provenanceURI, predicate);
		return fieldName;
	}
	
	public static String makeCompleteFieldNameWithProvenance(String provenanceURI, String thePredicatePartOfTheFieldName) {
		int tablePrefix = RdfStoreFactory.get().getOrCreateTablePrefixFromURIAsInteger(provenanceURI);
		return tablePrefix + "_" + thePredicatePartOfTheFieldName;
	}
	
	protected static String collapseResource(String uri) {
		/*
		 * Given a Resource URI, collapse it using our default namespace
		 * mappings if possible. This is purely a convenience.
		 */
		for (String ns_url : getDefaultNamespaces().keySet()) {
			if (uri.startsWith(ns_url)) {
				return uri.replace(ns_url, "_"
						+ getDefaultNamespaces().get(ns_url) + "_");
			}
		}

		return uri;

	} // collapseResource
	
	/*
	 * Maybe we'll need this. For now it's half-written.
	 */
	/*
	public static ProvenancePredicatePair fromFieldName(String fieldName) throws SQLException {
		String[] pieces = fieldName.split("_",1);
		int tablePrefix = Integer.parseInt(pieces[0]);
		String provenanceURI = RdfStore.getProvenanceURIFromTablePrefix(tablePrefix);
		
	}
	*/
}
