package org.creativecommons.learn;

import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.creativecommons.learn.oercloud.IExtensibleResource;

import thewebsemantic.Bean2RDF;
import thewebsemantic.Filler;
import thewebsemantic.NotFoundException;
import thewebsemantic.RDF2Bean;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * 
 * @author nathan
 */
public class TripleStore {

	private static TripleStore instance = null;

	private IDBConnection conn = null;
	private ModelMaker maker = null;
	private Model model = null;

	private RDF2Bean loader = null;
	private Bean2RDF saver = null;

	public TripleStore() {
		try {
			this.model = this.getModel();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		configureLoader();

	}

	public TripleStore(Model model) {

		this.model = model;

		configureLoader();
	}

	private void configureLoader() {
		try {
			this.loader = new RDF2Bean(this.getModel());
			this.saver = new Bean2RDF(this.getModel());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static TripleStore get() {

		if (instance == null) {
			instance = new TripleStore();
		}

		return instance;
	}

	private void open() throws ClassNotFoundException {
		Configuration config = DEdConfiguration.create();

		// register the JDBC driver
		Class.forName(config.get("rdfstore.db.driver")); // Load the Driver

		// Create the Jena database connection
		this.conn = new DBConnection(config.get("rdfstore.db.url"), config
				.get("rdfstore.db.user"), config.get("rdfstore.db.password"),
				config.get("rdfstore.db.type"));
		this.maker = ModelFactory.createModelRDBMaker(conn);

	} // open

	private void close() {
		try {
			// Close the database connection
			conn.close();
		} catch (SQLException ex) {
			Logger.getLogger(TripleStore.class.getName()).log(Level.SEVERE,
					null, ex);
		}

	} // close

	public Model getModel() throws ClassNotFoundException {

		if (model == null) {
			if (maker == null) {
				this.open();
			}

			// create or open the default model
			this.model = maker.createDefaultModel();
		}

		return this.model;

	} // getModel

	/* Delegate Methods */
	/* **************** */

	public boolean exists(Class<?> c, String id) {
		return loader.exists(c, id);
	}

	public boolean exists(String uri) {
		return loader.exists(uri);
	}

	public void fill(Object o, String propertyName) {
		loader.fill(o, propertyName);
	}

	public Filler fill(Object o) {
		return loader.fill(o);
	}

	public <T> T load(Class<T> c, String id) throws NotFoundException {
		T result = loader.load(c, id);

		if (result instanceof IExtensibleResource) {
			IExtensibleResource r = (IExtensibleResource) result;

			Resource subject = this.model.createResource(r.getUrl());
			StmtIterator statements = this.model.listStatements();

			while (statements.hasNext()) {
				Statement s = statements.nextStatement();

				if (s.getSubject().equals(subject)) {
					// ah-ha!
					r.addField(s.getPredicate(), s.getObject());
				}
			}
		}

		return result;
	}

	public <T> Collection<T> load(Class<T> c) {
		return loader.load(c);
	}

	public <T> T loadDeep(Class<T> c, String id) throws NotFoundException {
		return loader.loadDeep(c, id);
	}

	public <T> Collection<T> loadDeep(Class<T> c) {
		return loader.loadDeep(c);
	}

	public void delete(Object bean) {
		saver.delete(bean);
	}

	public Resource save(Object bean) {
		return saver.save(bean);
	}

	public Resource save(IExtensibleResource bean) {
		Resource resource = saver.save(bean);

		for (Property predicate : bean.getFields().keySet()) {
			for (RDFNode object : bean.getFieldValues(predicate)) {
				this.model.add(this.model.createResource(bean.getUrl()),
						predicate, object);
			}
		}
		return resource;
	}

	public Resource saveDeep(Object bean) {
		return saver.saveDeep(bean);
	}

} // TripleStore
