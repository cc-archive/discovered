package org.creativecommons.learn;

import com.hp.hpl.jena.rdf.model.RDFNode;

public class ProvenancePredicatePair {
	public String provenanceURI;
	public RDFNode predicateNode;
	public ProvenancePredicatePair(String provenanceURI, RDFNode predicateNode) {
		this.provenanceURI = provenanceURI;
		this.predicateNode = predicateNode;
	}
}
