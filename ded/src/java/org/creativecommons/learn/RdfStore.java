package org.creativecommons.learn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.creativecommons.learn.oercloud.IExtensibleResource;

import thewebsemantic.Bean2RDF;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * 
 * @author nathan
 */
public class RdfStore extends RdfStoreReader {
    
	private final static Log LOG = LogFactory.getLog(RdfStore.class);

	private Bean2RDF saver = null;

	public RdfStore(Model model) {
		super(model);

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

	private Bean2RDF getSaver() {
		return this.saver;
	}

} // RdfStore
