package org.creativecommons.learn;

import java.util.Collection;

import org.creativecommons.learn.oercloud.IExtensibleResource;

import thewebsemantic.Filler;
import thewebsemantic.NotFoundException;
import thewebsemantic.RDF2Bean;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class RdfStoreReader {

	protected Model model = null;
	protected RDF2Bean loader = null;

	public RdfStoreReader(Model model) {
		this.model = model;

		this.loader = new RDF2Bean(this.model);
	}

	public Model getModel() {
		return this.model;
	} // getModel

	public void close() {
		// no-op
	}

	public boolean exists(Class<?> c, String id) {
		return this.getLoader().exists(c, id);
	}

	public boolean exists(String uri) {
		return this.getLoader().exists(uri);
	}

	public void fill(Object o, String propertyName) {
		this.getLoader().fill(o, propertyName);
	}

	public Filler fill(Object o) {
		return this.getLoader().fill(o);
	}

	public <T> T load(Class<T> c, String id) throws NotFoundException {
	
		T result = this.getLoader().load(c, id);
	
		Model model = this.getModel();
	
		if (result instanceof IExtensibleResource) {
			IExtensibleResource r = (IExtensibleResource) result;
	
			Resource subject = model.createResource(r.getUrl());
			StmtIterator statements = model.listStatements();
	
			while (statements.hasNext()) {
				com.hp.hpl.jena.rdf.model.Statement s = statements
						.nextStatement();
	
				if (s.getSubject().equals(subject)) {
					// ah-ha!
					r.addField(s.getPredicate(), s.getObject());
				}
			}
		}
	
		return result;
	}

	public <T> Collection<T> load(Class<T> c) {
	
		return this.getLoader().load(c);
	}

	public <T> T loadDeep(Class<T> c, String id)
			throws NotFoundException {
			
				return this.getLoader().loadDeep(c, id);
			}

	public <T> Collection<T> loadDeep(Class<T> c) {
	
		return this.getLoader().loadDeep(c);
	}

	protected RDF2Bean getLoader() {
		return this.loader;
	}

}