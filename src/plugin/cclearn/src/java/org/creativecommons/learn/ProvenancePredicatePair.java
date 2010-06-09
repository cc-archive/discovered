package org.creativecommons.learn;

import java.sql.SQLException;

import com.hp.hpl.jena.rdf.model.RDFNode;

public class ProvenancePredicatePair {
	public String provenanceURI;
	public RDFNode predicateNode;
	public ProvenancePredicatePair(String provenanceURI, RDFNode predicateNode) {
		this.provenanceURI = provenanceURI;
		this.predicateNode = predicateNode;
	}
	public String toFieldName() throws SQLException {
		int tablePrefix = RdfStore.getOrCreateTablePrefixFromURIAsInteger(this.provenanceURI); 
		return tablePrefix + "_" + this.predicateNode.toString();
	}
	
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
