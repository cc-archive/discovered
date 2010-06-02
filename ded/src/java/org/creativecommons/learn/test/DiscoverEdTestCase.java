package org.creativecommons.learn.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.creativecommons.learn.DEdConfiguration;
import org.creativecommons.learn.RdfStore;

import org.apache.hadoop.conf.Configuration;

import junit.framework.*;

public class DiscoverEdTestCase extends TestCase {
	
	public static void runSQL(String sql) throws SQLException {
		// FIXME: This is vulnerable to SQL injection (perhaps by one of us by accident).
		// But we should nuke this method anyhow.

		Configuration config = DEdConfiguration.create();
		Connection connection = DriverManager.getConnection(config.get("rdfstore.db.server_url"), config.get("rdfstore.db.user"), config.get("rdfstore.db.password"));

		Statement statement = connection.createStatement();
		System.err.println(sql);
		statement.executeUpdate(sql);
	}
	
	public static void dropDatabase(String dbname) throws SQLException {
		// Destroy the database
		String sql = "DROP DATABASE IF EXISTS " + dbname + ";";
		runSQL(sql);
	}
	
	public static void createDatabase(String dbname) throws SQLException {
		runSQL("CREATE DATABASE " + dbname + ";");
	}
	
	protected String[] list_of_quadstores_used = {
			"http://other.example.com/#site-configuration",
			"http://example.com/#site-configuration",
			"http://creativecommons.org/#site-configuration",
			"http://ocw.nd.edu/",
			"http://example.com/",
			"http://ocw.nd.edu/courselist/rss",
			"http://a6.creativecommons.org/~raffi/html_for_discovered_unit_tests/rss_pointing_to_i_know_my_title.xml",
			"http://a6.creativecommons.org/~raffi/html_for_discovered_unit_tests/i_know_my_title.html"
	};

	public void setUp() throws SQLException {
		RdfStore.setDatabaseName(getDatabaseName());
		String dbname = getDatabaseName();
		dropDatabase(dbname);
		createDatabase(dbname);
		System.err.println("Created a MySQL database named " + dbname);
	}
	
	public static String getDatabaseName() {
		// FIXME: Add the "Couldn't find a value" complaint to config.get()
		Configuration config = DEdConfiguration.create();
		String configPropertyName = "rdfstore.db.database_name_for_test_suite";
		String dbName = config.get(configPropertyName);
		if (dbName == null) {
			throw new RuntimeException("Couldn't find a value in the configuration file for " + configPropertyName);
		}
		return dbName;
	}
	
	public void tearDown() throws SQLException {
		dropDatabase(getDatabaseName());
	}
	
	    
}
