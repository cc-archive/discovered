package org.creativecommons.learn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
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
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;
import org.creativecommons.learn.oercloud.IExtensibleResource;

/**
 * 
 * @author nathan
 */
public class RdfStore {
    
	private final static Log LOG = LogFactory.getLog(RdfStore.class);

	protected static HashMap<String, RdfStore> cache = null;
	private static String databaseName = null;
	private static Connection dbConnection = null;
    
	private Model model = null;
	private RDF2Bean loader = null;
	private Bean2RDF saver = null;
    private IDBConnection jenaDBConnection = null;
	private String uri = null;

	public RdfStore(Model model, IDBConnection connection) {
		super();

		this.model = model;
		this.jenaDBConnection = connection;

	}

	private RdfStore(String provenanceURI) {
		this.uri = provenanceURI;
		Configuration config = DEdConfiguration.create();

		// XXX register the JDBC driver
		// Class.forName(config.get("rdfstore.db.driver")); // Load the Driver

		// Create the Jena database connection
		DBConnection conn = new DBConnection(config
				.get("rdfstore.db.server_url")
				+ getDatabaseName() + "?autoReconnect=true", config
				.get("rdfstore.db.user"), config.get("rdfstore.db.password"),
				config.get("rdfstore.db.type"));

		IRDBDriver driver = conn.getDriver();

		ModelMaker maker = ModelFactory.createModelRDBMaker(conn);

		String tableNamePrefix = getOrCreateTablePrefixFromURI(uri);
		driver.setTableNamePrefix(tableNamePrefix);

		this.model = maker.createDefaultModel();
		this.jenaDBConnection = conn;

		this.loader = new RDF2Bean(this.model);
		this.saver = new Bean2RDF(this.model);
	}
	
	public static void emptyCache() {
		RdfStore.cache = null;
	}

	/**
	 * Returns the RdfStore devoted to feeds that the system administrator adds
	 * when configuring this DiscoverEd instance.
	 * 
	 * @throws SQLException
	 * */
	public static RdfStore forDEd() {
		return RdfStore.forProvenance(RdfStore.SITE_CONFIG_URI);
	}

	public static RdfStore forModel(Model model) {
		return new RdfStore(model, null);
	}

	private static HashMap<String, RdfStore> getCache() {
		if (cache == null) {
			cache = new HashMap<String, RdfStore>();
		}
		return cache;
	}

    /*
     * Create a database-backed RdfStore corresponding to data with a
     * particular provenance.
     */
	public static RdfStore forProvenance(String provURI) {
		/**
		 * For speed, let's make a cache of RdfStores, at most ten.
         *
		 * Since we don't have a cache HashMap<Model, RdfStore>, this may
		 * eventually cause use to exceed MySQL's connection limit. So it's
		 * super important that callers to this method use store.close()
		 */

		/*
		 * When we "create" an RdfStore, actually look in the cache first to see
		 * if it's there.
		 */
		if (RdfStore.getCache().containsKey(provURI)) {
			return RdfStore.getCache().get(provURI);
		}

		// If we get this far, then the RdfStore wasn't cached.
		failIfCacheIsFull();

		RdfStore store = new RdfStore(provURI);

		RdfStore.getCache().put(provURI, store);

		return store;
	}

	private static void failIfCacheIsFull() {
		/*
		 * If the RdfStore has more than 10 entries, throw an entry away at
		 * random.
		 */
		if (RdfStore.getCache().size() >= 10) {
            throw new RuntimeException("The cache is full. Be sure to .close() your RdfStores when you're finished with them.");
            // If we don't throw an RTE here, we eventually open too many MySQL
            // connections, and MySQL itself will utter its terrible complaint.
		}
	}

	public static int getOrCreateTablePrefixFromURIAsInteger(String uri) {
		try {
			dbConnection = getDatabaseConnection();
			createRdfStoresTableIfNeeded(dbConnection);

			// Do we already have a table prefix? If so, return it.
			java.sql.PreparedStatement matchingTablePrefixes = dbConnection
					.prepareStatement("SELECT table_prefix FROM rdf_stores WHERE uri = ? ");
			matchingTablePrefixes.setString(1, uri);
			ResultSet cursor = matchingTablePrefixes.executeQuery();
			if (cursor.next()) {
				return cursor.getInt("table_prefix");
			}

			// Prepare a SQL statement that saves a row in a table called
			// rdf_stores, and fill in the values
			java.sql.PreparedStatement statement = dbConnection
					.prepareStatement("INSERT INTO rdf_stores (uri) VALUES (?)");
			statement.setString(1, uri);

			// Run the statement
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Encountered a SQL error while trying to figure out where we keep the data on "
							+ uri);
		}

		return getOrCreateTablePrefixFromURIAsInteger(uri);
	}

	public static String getOrCreateTablePrefixFromURI(String uri) {
		return "" + getOrCreateTablePrefixFromURIAsInteger(uri);
	}

	public static String getProvenanceURIFromTablePrefix(int tablePrefix) {
		try {
			dbConnection = getDatabaseConnection();
			createRdfStoresTableIfNeeded(dbConnection);

			// Do we already have a table prefix? If so, return it.
			java.sql.PreparedStatement matchingURIs = dbConnection
					.prepareStatement("SELECT uri FROM rdf_stores WHERE table_prefix = ? ");
			matchingURIs.setInt(1, tablePrefix);
			ResultSet cursor = matchingURIs.executeQuery();
			if (cursor.next()) {
				return cursor.getString("uri");
			}
			// If we get down here, something went wrong.
			throw new RuntimeException("Couldn't find the table prefix "
					+ tablePrefix + " in the rdf_stores table");

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Encountered a SQL error while trying to figure out the provenance URI "
							+ "corresponding to the database table with prefix "
							+ tablePrefix);
		}
	}

	// Connect to the database
	private static Connection getDatabaseConnection() throws SQLException {
		if (dbConnection == null) {
			Configuration config = DEdConfiguration.create();
			dbConnection = DriverManager.getConnection(
					getDatabaseConnectionURL(), config.get("rdfstore.db.user"),
					config.get("rdfstore.db.password"));
		}
		return dbConnection;
	}

	private static void createRdfStoresTableIfNeeded(Connection connection) {

		// FIXME: Add an index to the "uri" column.

		// Try to create table
		try {
			executeSQL("CREATE TABLE IF NOT EXISTS rdf_stores (table_prefix INTEGER PRIMARY KEY AUTO_INCREMENT, uri VARCHAR(1000))");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void executeSQL(String sql) throws SQLException {
		Statement statement = getDatabaseConnection().createStatement();
		statement.executeUpdate(sql);
	}

	private static String getDatabaseConnectionURL() {
		Configuration config = DEdConfiguration.create();
		String url = config.get("rdfstore.db.server_url") + getDatabaseName()
				+ "?autoReconnect=true";
		return url;
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

	public static ArrayList<String> getAllKnownTripleStoreUris() {
		Statement statement;

		ArrayList<String> uris = new ArrayList<String>();
		try {
			statement = getDatabaseConnection()
					.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
			ResultSet resultSet = statement
					.executeQuery("SELECT uri, table_prefix FROM rdf_stores");
			while (resultSet.next()) {
				String uri = resultSet.getString("uri");
				uris.add(uri);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("sql error");
		}

        LOG.info("RdfStore.getAllKnownTripleStoreUris detected these 3-stores: " + uris);

		return uris;
	}

	public void close() {

		// if no connection was supplied, nothing to do here
		if (this.jenaDBConnection == null) {
			return;
		}

		try {
			// Close the database connection
			this.jenaDBConnection.close();
		} catch (SQLException ex) {
			Logger.getLogger(RdfStore.class.getName()).log(Level.SEVERE, null,
					ex);
		}

	} // close

	public Model getModel() {
		return this.model;
	} // getModel

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

	public static void setDatabaseName(String databaseName) {
		RdfStore.databaseName = databaseName;
	}

	/*
	 * Get a mapping of (Provenance, Predicate) -> RDFNode
	 * 
	 * E.g., ("http://curators.org", "dc:educationLevel") -> "high school"
	 * 
	 * That means that Curators.org has labeled this resource as appropriate for
	 * high schoolers.
	 */
	public static HashMap<ProvenancePredicatePair, RDFNode> getPPP2ObjectMapForSubject(
			String subjectURL) {

		HashMap<ProvenancePredicatePair, RDFNode> map = new HashMap<ProvenancePredicatePair, RDFNode>();

		for (String provenanceURI : RdfStore.getAllKnownTripleStoreUris()) {
            RdfStore store = RdfStore.forProvenance(provenanceURI);

			Model m;
			m = store.getModel();

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
				RDFNode predicateNode = stmt.get("p");
				ProvenancePredicatePair p3 = new ProvenancePredicatePair(
						provenanceURI, predicateNode);
				RDFNode objectNode = stmt.get("o");
				System.out
						.println("Found a triple for " + subjectURL + ": "
								+ predicateNode.toString() + " courtesy of "
								+ provenanceURI + ", value is "
								+ objectNode.toString());
				map.put(p3, objectNode);
			}

			// Important - free up resources used running the query
			qe.close();
            store.close();
		}

		return map;
	}

	public HashMap<ProvenancePredicatePair, RDFNode> getPPP2ObjectMapForSubjectAndPredicate(
			String subjectURI, String titlePredicate) {
		HashMap<ProvenancePredicatePair, RDFNode> map = RdfStore
				.getPPP2ObjectMapForSubject(subjectURI);
		HashMap<ProvenancePredicatePair, RDFNode> mapFiltered = new HashMap<ProvenancePredicatePair, RDFNode>();
		for (Entry<ProvenancePredicatePair, RDFNode> entry : map.entrySet()) {
			if (entry.getKey().predicateNode.toString().equals(titlePredicate)) {
				mapFiltered.put(entry.getKey(), entry.getValue());
			}
		}
		return mapFiltered;
	}

	public static ArrayList<String> getProvenanceURIsFromCuratorShortName(
			String curatorShortName) {
		/* Find the matching Curator, if any. */
		Curator relevantCurator = null;
		ArrayList<String> resultsSoFar = new ArrayList<String>();

		RdfStore store = RdfStore.forDEd();
		Collection<Curator> curators = store.load(Curator.class);

		/* FIXME: This is a really lame way to do a query. */
		for (Curator c : curators) {
			if (c.getName().equals(curatorShortName)) {
				relevantCurator = c;
				break;
			}
		}

		// Okay, if there's no matching Curator, we bail out now.
		if (relevantCurator == null) {
			return resultsSoFar;
		}

		// Otherwise, the answer is that the matching provenance URIs are the
		// URLs of all
		// the Feed objects. Let's collect those into resultsSoFar.
		Collection<Feed> all_feeds = store.load(Feed.class);

		// Search for Feeds whose curator is our friend, the relevantCurator
		for (Feed f : all_feeds) {
			if (f.getCurator().getUrl().equals(relevantCurator.getUrl())) {
				resultsSoFar.add(f.getUrl());
			}
		}

		return resultsSoFar;
	}

	private RDF2Bean getLoader() {
		return this.loader;
	}

	private Bean2RDF getSaver() {
		return this.saver;
	}

	// encode one (t,p) into a Lucene-compatible string column name

	// decode one Lucene-compatible string column name into (t, p)

} // RdfStore
