package org.creativecommons.learn.oercloud;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

public interface IExtensibleResource {

	public abstract String getUrl();
	
	public abstract void addField(Property predicate, RDFNode object);

	public abstract HashMap<Property, HashSet<RDFNode>> getFields();
	
	public abstract Set<RDFNode> getFieldValues(Property predicate);

}