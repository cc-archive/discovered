package org.creativecommons.learn;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import com.mysql.jdbc.PreparedStatement;

import de.fuberlin.wiwiss.ng4j.NamedGraph;
import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
import de.fuberlin.wiwiss.ng4j.db.NamedGraphSetDB;
import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;

/**
 * 
 * @author nathan
 */
public class QuadStore {
	
	// lollerskates
	// don't need it yet
	// private HashMap<String, TripleStore> provenance2triplestore;
	
	private static final String SITE_CONFIG_URI = "http://example.com#site-configuration";
	
	public static String uri2database_name(String uri) {
		/* FIXME: This is likely to give us conflicts */
		int hash = Math.abs(uri.hashCode());
		return "ded_" + hash;
	}
	
	public static TripleStore uri2TripleStore(String uri) throws SQLException {
		System.err.println("making triple store for " + uri);
		/** FIXME:
		 * One day, cache these in a HashMap.
		 */
		// Calculate the right database name to use.
		String dbname = uri2database_name(uri);
		
		// Make sure we have permission to use it
		Connection root_connection = DriverManager.getConnection("jdbc:mysql://localhost/", "root", "aewo4Fen");
		java.sql.Statement grant_statement = root_connection.createStatement();
		grant_statement.executeUpdate("GRANT ALL ON " + dbname  + ".* TO discovered");
		
		// Create the Jena database connection
		DBConnection conn = new DBConnection(
				"jdbc:mysql://localhost/" + dbname + "?autoReconnect=true", 
				"discovered", 
				"",
				"mysql");
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
