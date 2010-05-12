package org.creativecommons.learn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import thewebsemantic.Bean2RDF;
import thewebsemantic.Filler;
import thewebsemantic.NotFoundException;
import thewebsemantic.RDF2Bean;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Resource;

import org.apache.hadoop.conf.Configuration;

/**
 * 
 * @author nathan
 */
public class RdfStore {

	private IDBConnection conn = null;
	private ModelMaker maker = null;
	private Model model = null;

	private RDF2Bean loader = null;
	private Bean2RDF saver = null;

	public RdfStore(ModelMaker maker, IDBConnection connection) {
		super();
		this.maker = maker;
		this.conn = connection;
		this.init();
	}
	
	private void init() {
		this.model = maker.createDefaultModel();
		this.loader = new RDF2Bean(this.model);
		this.saver = new Bean2RDF(this.model);
	}
	
	public static final String SITE_CONFIG_URI = "http://creativecommons.org/#site-configuration";
	
	public static String uri2database_name(String uri) {
		/* FIXME: This is likely to give us conflicts */
		int hash = Math.abs(uri.hashCode());
		return "ded_" + hash;
	}
	
	public static RdfStore uri2RdfStore(String uri) throws SQLException {

	    Configuration config = DEdConfiguration.create();

		System.err.println("making triple store for " + uri);
		/** FIXME:
		 * One day, cache these in a HashMap.
		 */
		// Calculate the right database name to use.
		String dbname = uri2database_name(uri);


		// XXX register the JDBC driver
		// Class.forName(config.get("rdfstore.db.driver")); // Load the Driver
		
		// Make sure we have permission to use it
		Connection root_connection = DriverManager.getConnection(config.get("rdfstore.db.server_url"), config.get("rdfstore.db.root_user"), config.get("rdfstore.db.root_password"));
		java.sql.Statement grant_statement = root_connection.createStatement();
		grant_statement.executeUpdate("GRANT ALL ON " + dbname  + ".* TO discovered");
		
		// Create the Jena database connection
		DBConnection conn = new DBConnection(
				config.get("rdfstore.db.server_url") + dbname + "?autoReconnect=true", 
				config.get("rdfstore.db.user"), 
				config.get("rdfstore.db.password"),
				config.get("rdfstore.db.type"));
		ModelMaker maker = ModelFactory.createModelRDBMaker(conn);
		
		return new RdfStore(maker, conn);
	}
	
	@SuppressWarnings("unchecked")
	public static List getAllKnownTripleStoreUris() {
		return null;
	}
	
	/**
	 * Returns the TripleStore devoted to feeds that the system administrator
	 * adds when configuring this DiscoverEd instance.
	 * @throws SQLException 
	 * */
	public static RdfStore getSiteConfigurationStore() {
		try {
			return RdfStore.uri2RdfStore(RdfStore.SITE_CONFIG_URI);
		}
		catch(SQLException e) {
			throw new RuntimeException("Merde, there was an SQL error "
					+ "while trying access the site configuration database.");
		}
	}
	@SuppressWarnings("unused")
	private RdfStore() {
		// private constructor
		super();
		init();
	}

	private void close() {
		try {
			// Close the database connection
			conn.close();
		} catch (SQLException ex) {
			Logger.getLogger(RdfStore.class.getName()).log(Level.SEVERE,
					null, ex);
		}

	} // close
	
	public Model getModel() throws ClassNotFoundException {
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
	
} // TripleStore
