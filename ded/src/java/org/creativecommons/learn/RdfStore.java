package org.creativecommons.learn;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.creativecommons.learn.oercloud.IExtensibleResource;

import thewebsemantic.Bean2RDF;
import thewebsemantic.Filler;
import thewebsemantic.NotFoundException;
import thewebsemantic.RDF2Bean;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * 
 * @author nathan
 */
public class RdfStore {
    
	private final static Log LOG = LogFactory.getLog(RdfStore.class);

	private Model model = null;
	private RDF2Bean loader = null;
	private Bean2RDF saver = null;

	public RdfStore(Model model) {
		super();

		this.model = model;

		this.loader = new RDF2Bean(this.model);
		this.saver = new Bean2RDF(this.model);
	}

	public Model getModel() {
		return this.model;
	} // getModel
	
	public void close() {
		// no-op
	}

	/* Delegate Methods */
	/* **************** */

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

	public <T> T loadDeep(Class<T> c, String id) throws NotFoundException {

		return this.getLoader().loadDeep(c, id);
	}

	public <T> Collection<T> loadDeep(Class<T> c) {

		return this.getLoader().loadDeep(c);
	}

	public void delete(Object bean) {
		this.getSaver().delete(bean);
	}

	private void saveFields(IExtensibleResource bean) {

		Model model = this.getModel();

		for (Property predicate : bean.getFields().keySet()) {
			for (RDFNode object : bean.getFieldValues(predicate)) {
				model.add(model.createResource(bean.getUrl()), predicate,
						object);
			}
		}
	}

	public Resource save(Object bean) {
		return this.getSaver().save(bean);
	}

	public Resource save(IExtensibleResource bean) {
		Resource resource = this.getSaver().save(bean);

		saveFields(bean);
		return resource;
	}

	public Resource saveDeep(Object bean) {
		return this.getSaver().saveDeep(bean);
	}

	public Resource saveDeep(IExtensibleResource bean) {
		Resource resource = this.getSaver().saveDeep(bean);
		saveFields(bean);

		return resource;
	}

	private RDF2Bean getLoader() {
		return this.loader;
	}

	private Bean2RDF getSaver() {
		return this.saver;
	}

} // RdfStore
