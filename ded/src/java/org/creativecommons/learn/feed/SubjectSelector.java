package org.creativecommons.learn.feed;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.Statement;

public class SubjectSelector implements Selector {

	Resource subject;
	
	public SubjectSelector(Resource subject) {
		super();
		
		this.subject = subject;
	}

	@Override
	public RDFNode getObject() {
		return null;
	}

	@Override
	public Property getPredicate() {
		return null;
	}

	@Override
	public Resource getSubject() {
		return this.subject;
	}

	@Override
	public boolean isSimple() {
		return true;
	}

	@Override
	public boolean test(Statement arg0) {
		return arg0.getSubject().equals(this.getSubject());
	}

}
