package org.creativecommons.learn;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;

/**
 * 
 * @author nathan
 */
public class QuadStore {
	
	// lollerskates
	// don't need it yet
	// private HashMap<String, TripleStore> provenance2triplestore;
	
	private static final String SITE_CONFIG_URI = "http://creativecommons.org/#site-configuration";
	
	public static String uri2database_name(String uri) {
		/* FIXME: This is likely to give us conflicts */
		int hash = Math.abs(uri.hashCode());
		return "ded_" + hash;
	}
	
	public static TripleStore uri2TripleStore(String uri) throws SQLException {

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
		
		return new TripleStore(maker, conn);
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
	public static TripleStore getSiteConfigurationStore() {
		try {
			return QuadStore.uri2TripleStore(QuadStore.SITE_CONFIG_URI);
		}
		catch(SQLException e) {
			throw new RuntimeException("Merde, there was an SQL error "
					+ "while trying access the site configuration database.");
		}
	}

} // QuadStore
