package org.creativecommons.learn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;
import org.creativecommons.learn.oercloud.Resource;

import com.hp.hpl.jena.rdf.model.Model;

import org.apache.hadoop.conf.Configuration;

import thewebsemantic.NotFoundException;
import junit.framework.*;

public class Test extends TestCase {
	
	public static void runSqlAsRoot(String sql) throws SQLException {
		// FIXME: This is vulnerable to SQL injection (perhaps by one of us by accident).
		// But we should nuke this method anyhow.

		Configuration config = DEdConfiguration.create();

		Connection connection = DriverManager.getConnection(config.get("rdfstore.db.server_url"), config.get("rdfstore.db.root_user"), config.get("rdfstore.db.root_password"));

		Statement statement = connection.createStatement();
		System.err.println(sql);
		statement.executeUpdate(sql);
	}
	
	public static void dropDatabase(String dbname) throws SQLException {
		// Destroy the database
		String sql = "DROP DATABASE IF EXISTS " + dbname + ";";
		runSqlAsRoot(sql);
	}
	
	public static void setDatabasePermissions(String dbname) throws SQLException {
		runSqlAsRoot("GRANT ALL ON " + dbname + ".* TO discovered");
	}
	
	public static void createDatabase(String dbname) throws SQLException {
		runSqlAsRoot("CREATE DATABASE " + dbname + ";");
	}
	
	private String[] list_of_quadstores_used = {
			"http://other.example.com/#site-configuration",
			"http://example.com/#site-configuration",
			"http://creativecommons.org/#site-configuration",
			"http://ocw.nd.edu/",
			"http://example.com/",
			"http://a6.creativecommons.org/~raffi/html_for_discovered_unit_tests/rss_pointing_to_i_know_my_title.xml",
			"http://a6.creativecommons.org/~raffi/html_for_discovered_unit_tests/i_know_my_title"
	};

	public void setUp() throws SQLException {
		for (String uri : list_of_quadstores_used) {
			String dbname = RdfStore.uri2database_name(uri);
			setDatabasePermissions(dbname);
			dropDatabase(dbname);
			createDatabase(dbname);
			System.err.println("Create a MySQL database named " + dbname);
		}
	}
	
	public void tearDown() throws SQLException {
		for (String uri : list_of_quadstores_used) {
			String dbname = RdfStore.uri2database_name(uri);
			dropDatabase(dbname);
		}
	}
	
	    
}
