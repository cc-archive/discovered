package org.creativecommons.learn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import thewebsemantic.Bean2RDF;
import thewebsemantic.Filler;
import thewebsemantic.NotFoundException;
import thewebsemantic.RDF2Bean;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.impl.IRDBDriver;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Resource;
import com.mysql.jdbc.PreparedStatement;

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

	private static String databaseName = null;

	private static Connection connection = null;

	public RdfStore(ModelMaker maker, IDBConnection connection) {
		super();

		this.maker = maker;
		this.conn = connection;

		this.init();
	}

	public static String getOrCreateTablePrefixFromURI(String uri) throws SQLException {
		connection = getDatabaseConnection();
		createRdfStoresTableIfNeeded(connection);
		
		// Do we already have a table prefix? If so, return it.
		java.sql.PreparedStatement matchingTablePrefixes = connection.prepareStatement(
				"SELECT uri, table_prefix FROM rdf_stores WHERE uri = ? ");
		matchingTablePrefixes.setString(1, uri);
		ResultSet cursor = matchingTablePrefixes.executeQuery();
		if (cursor.next()) {
			return "" + cursor.getInt("table_prefix");
		}
		
		// Prepare a SQL statement that saves a row in a table called
		// rdf_stores, and fill in the values
		java.sql.PreparedStatement statement = connection
				.prepareStatement("INSERT INTO rdf_stores (uri) VALUES (?)");
		statement.setString(1, uri);

		// Run the statement
		statement.executeUpdate();
		
		return getOrCreateTablePrefixFromURI(uri);
	}

	// Connect to the database
	public static Connection getDatabaseConnection() throws SQLException {
		if (connection == null) {
			Configuration config = DEdConfiguration.create();
			connection = DriverManager.getConnection(
					getDatabaseConnectionURL(), config.get("rdfstore.db.user"),
					config.get("rdfstore.db.password"));
		}
		return connection;
	}

	public static void createRdfStoresTableIfNeeded(Connection connection) {

		// FIXME: Add an index to the "uri" column.
		
		// Try to create table
		try {
			executeSQL("CREATE TABLE IF NOT EXISTS rdf_stores (table_prefix INTEGER PRIMARY KEY AUTO_INCREMENT, uri VARCHAR(1000))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void executeSQL(String sql) throws SQLException {
		Statement statement = getDatabaseConnection().createStatement();
		statement.executeUpdate(sql);
	}

	public static String getDatabaseConnectionURL() {
		Configuration config = DEdConfiguration.create();
		String url = config.get("rdfstore.db.server_url") + getDatabaseName()
				+ "?autoReconnect=true";
		return url;
	}

	private void init() {
		this.model = maker.createDefaultModel();
		this.loader = new RDF2Bean(this.model);
		this.saver = new Bean2RDF(this.model);
	}

	public static final String SITE_CONFIG_URI = "http://creativecommons.org/#site-configuration";

	public static String getDatabaseName() {
		if (RdfStore.databaseName == null) {
			Configuration config = DEdConfiguration.create();

			// Let's find the name of the database to use.
			// Usually we want to look up the name in the config file under the
			// property "rdfstore.db.database_name"
			// If we're using RdfStore during testing, however, we might want to
			// use a different database, so let's check to see if the testing
			// suite has set an environment variable to indicate this.
			String propertyContainingRdfStoreDatabaseName = System
					.getenv("PROPERTY_CONTAINING_RDFSTORE_DB_NAME");
			if (propertyContainingRdfStoreDatabaseName == null) {
				propertyContainingRdfStoreDatabaseName = "rdfstore.db.database_name";
			}
			RdfStore.databaseName = config
					.get(propertyContainingRdfStoreDatabaseName);
		}
		return RdfStore.databaseName;
	}

	public static RdfStore uri2RdfStore(String uri, String databaseName)
			throws SQLException {
		/**
		 * FIXME: One day, cache these mappings (uri to rdfstore) in a HashMap.
		 */
		Configuration config = DEdConfiguration.create();

		System.err.println("making triple store for " + uri);

		// XXX register the JDBC driver
		// Class.forName(config.get("rdfstore.db.driver")); // Load the Driver

		// Create the Jena database connection
		DBConnection conn = new DBConnection(config
				.get("rdfstore.db.server_url")
				+ databaseName + "?autoReconnect=true", config
				.get("rdfstore.db.user"), config.get("rdfstore.db.password"),
				config.get("rdfstore.db.type"));

		IRDBDriver driver = conn.getDriver();

		String tableNamePrefix = getOrCreateTablePrefixFromURI(uri);
		driver.setTableNamePrefix(tableNamePrefix);

		ModelMaker maker = ModelFactory.createModelRDBMaker(conn);

		getOrCreateTablePrefixFromURI(uri);

		return new RdfStore(maker, conn);
	}

	public static RdfStore uri2RdfStore(String uri) throws SQLException {
		return uri2RdfStore(uri, getDatabaseName());
	}

	public static ArrayList<String> getAllKnownTripleStoreUris() throws SQLException {
		Statement statement = getDatabaseConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
				ResultSet.CONCUR_READ_ONLY);
		ResultSet resultSet = statement.executeQuery("SELECT uri, table_prefix FROM rdf_stores");
		ArrayList<String> uris = new ArrayList<String>();
		while (resultSet.next()) {
			String uri = resultSet.getString("uri");
			uris.add(uri);
		}
		return uris;
	}

	/**
	 * Returns the TripleStore devoted to feeds that the system administrator
	 * adds when configuring this DiscoverEd instance.
	 * 
	 * @throws SQLException
	 * */
	public static RdfStore getSiteConfigurationStore() {
		try {
			return RdfStore.uri2RdfStore(RdfStore.SITE_CONFIG_URI);
		} catch (SQLException e) {
			e.printStackTrace();
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

	public void close() {
		try {
			// Close the database connection
			conn.close();
		} catch (SQLException ex) {
			Logger.getLogger(RdfStore.class.getName()).log(Level.SEVERE, null,
					ex);
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

	public static void setDatabaseName(String databaseName) {
		RdfStore.databaseName = databaseName;
	}
	
	
	// get all t,p,o for url
	public static HashMap<ProvenancePredicatePair, String> getPPP2ObjectMapForSubject(String subjectURL) throws SQLException, ClassNotFoundException {
	
		HashMap<ProvenancePredicatePair, String> map = new HashMap<ProvenancePredicatePair, String>();
		
		for (String provenanceURI : RdfStore.getAllKnownTripleStoreUris()) {
			Model m;
			m = RdfStore.uri2RdfStore(provenanceURI).getModel();

			// Create a new query
			String queryString = "SELECT ?p ?o " + "WHERE {" + "      <"
					+ subjectURL.toString() + "> ?p ?o ." + "      }";
			Query query = QueryFactory.create(queryString);

			// Execute the query and obtain results
			QueryExecution qe = QueryExecutionFactory.create(query, m);

			com.hp.hpl.jena.query.ResultSet cursor = qe.execSelect();

			// Index the triples
			while (cursor.hasNext()) {
				QuerySolution stmt = cursor.nextSolution();
				String predicateURI = stmt.get("p").toString();
				ProvenancePredicatePair p3 = new ProvenancePredicatePair(
						provenanceURI, predicateURI);
				String object = stmt.get("o").toString();
				System.out.println("Found a triple for " + subjectURL + ": "
						+ predicateURI + " courtesy of " + provenanceURI
						+ ", value is " + object);
				map.put(p3, object);
			}

			// Important - free up resources used running the query
			qe.close();
		}

		return map;
	}
	
	// encode one (t,p) into a Lucene-compatible string column name
	
	// decode one Lucene-compatible string column name into (t, p)

} // RdfStore
