package org.creativecommons.learn.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.creativecommons.learn.DEdConfiguration;
import org.creativecommons.learn.RdfStore;

import org.apache.hadoop.conf.Configuration;

import junit.framework.*;

public abstract class DiscoverEdTestCase extends TestCase {
	
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
	
	public static String runCmd(String cmd) throws IOException, InterruptedException {
		/*
		 * This method runs a String command with PROPERTY_CONTAINING_RDFSTORE_DB_NAME value available in the environment.
		 * 
		 * Things you should know:
		 *     * We are using "String cmd" not "String[] cmd" because we are lazy. In theory it's dangerous. We know that.
		 *     * We set PROPERTY_CONTAINING_RDFSTORE_DB_NAME so that the Nutch code plus our plugins can look inside
		 *       the database that the test suite modifies, typically "discovered_test". This way, when the test suite runs 
		 *       "bin/nutch whatever", the Jena store it looks inside is the one modified by the test suite.
		 *       This language is a bit stilted, but the point is to let the test suite modify the Jena store
		 *       without modifying your real database. That way, you can run the test suite safely in an environment
		 *       where you're also running a live instance of DiscoverEd.  
		 */

		Runtime run = Runtime.getRuntime();
		
		/* Create an array to represent the environment in which the new command will run.
		 * 
		 *  First, we copy the existing environment...
		 */
		
		HashMap<String, String> environment = new HashMap<String, String>(System.getenv());
		
		// We need to reformat this map as a list of strings, each having the form "name=value"
		ArrayList<String> environmentReformatted = new ArrayList<String>();
		
		for (Map.Entry<String, String> entry: environment.entrySet()) {
			environmentReformatted.add(entry.getKey() + "=" + entry.getValue());
		}
		
		/* Then, add our new environment variable */
		environment.put("PROPERTY_CONTAINING_RDFSTORE_DB_NAME", "rdfstore.db.database_name_for_test_suite");
		
		// Finally we need to convert the environment into a String array
		String[] x = {};
		String[] environmentReformattedAgain = environmentReformatted.toArray(x);
		
		/* Finally, call run.exec() with our environment array as the second argument. */
		
		Process pr = run.exec(cmd, environmentReformattedAgain);
		pr.waitFor() ;
		BufferedReader buf = new BufferedReader( new InputStreamReader( pr.getInputStream() ) );
		
		String line = "";
		String output = "";
		
		while ( line != null) {
			System.out.println(line) ;
			line = buf.readLine();
			output += line + "\n";
		}
		return output;
	}
		
	    
}
