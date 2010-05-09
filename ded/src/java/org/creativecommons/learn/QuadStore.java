package org.creativecommons.learn;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import thewebsemantic.Bean2RDF;
import thewebsemantic.Filler;
import thewebsemantic.NotFoundException;
import thewebsemantic.RDF2Bean;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.VCARD;

import de.fuberlin.wiwiss.ng4j.NamedGraph;
import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
import de.fuberlin.wiwiss.ng4j.db.NamedGraphSetDB;
import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;

/**
 * 
 * @author nathan
 */
public class QuadStore {

	private IDBConnection conn = null;
	private ModelMaker maker = null;
	private Model model = null;

	private RDF2Bean loader = null;
	private Bean2RDF saver = null;
	
	private NamedGraph graph;
	
	private NamedGraphSet graphset;

	public QuadStore(String graphName) throws SQLException {
		
		String url = "jdbc:mysql://localhost/discovered?autoReconnect=true", 
			user = "discovered", 
			pass = "";
		
		Connection connection = DriverManager.getConnection(url, user, pass);
		
		
		if (graphset == null) {
			graphset = new NamedGraphSetDB(connection);
		}
		
		this.graph = graphset.createGraph(graphName);
		// ^^ FIXME We don't enforce that this is a NEW graph name. Is that safe?
		
		try {
			this.loader = new RDF2Bean(this.getModel());
			this.saver = new Bean2RDF(this.getModel());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void close() {
		try {
			// Close the database connection
			conn.close();
		} catch (SQLException ex) {
			Logger.getLogger(QuadStore.class.getName()).log(Level.SEVERE,
					null, ex);
		}

	} // close

	public Model getModel() throws ClassNotFoundException {

		if (model == null) {
			// create or open the default model
			this.model = graphset.asJenaModel(this.graph.getGraphName().getURI());
		}

		return this.model;

	} // getModel
	
	public void printCurators() {
		Query sparql = QueryFactory.create(
				"SELECT * WHERE { GRAPH ?graph { ?s ?p ?o } }");
		QueryExecution qe = QueryExecutionFactory.create(
				sparql, (Dataset) this.graphset);
	}
	
	public NamedGraph getGraph() {
		return this.graph;
	}
	
	public void addACuratorAndPrint() {
		/* Raffi is learning how to use this stuff. */
		
		// some definitions
		String personURI    = "http://mantinea/Diotima";
		String givenName    = "Diotima";
		String familyName   = "Mantinea";
		String fullName     = givenName + " " + familyName;

		// create an empty Model
		Model model = ModelFactory.createDefaultModel();

		// create the resource
		//   and add the properties cascading style
		Resource diotima
		  = model.createResource(personURI)
		         .addProperty(VCARD.FN, fullName)
		         .addProperty(VCARD.N,
		                      model.createResource()
		                           .addProperty(VCARD.Given, givenName)
		                           .addProperty(VCARD.Family, familyName));
		
		StmtIterator iter = model.listStatements();
		
		while(iter.hasNext()) {
			Statement stmt = iter.nextStatement();
			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
			RDFNode object = stmt.getObject();
			
			System.out.print(subject.toString() +
					" " + predicate.toString() + " ");
			if (object instanceof Resource) {
				System.out.print(object.toString());
			}
			else {
				// object is a literal, which means it's just a string of characters
				System.out.print(" \"" + object.toString() + "\"");
			}
			
			System.out.println(" .");
			
		}
	}
	
	
	

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
		return loader.load(c, id);
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

	public Resource saveDeep(Object bean) {
		return saver.saveDeep(bean);
	}

} // QuadStore
