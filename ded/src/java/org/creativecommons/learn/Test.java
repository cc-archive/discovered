package org.creativecommons.learn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;

import com.hp.hpl.jena.rdf.model.Model;

import thewebsemantic.NotFoundException;
import junit.framework.*;

public class Test extends TestCase {
	
	public void runSqlAsRoot(String sql) throws SQLException {
		// FIXME: This is vulnerable to SQL injection (perhaps by one of us by accident).
		// But we should nuke this method anyhow.
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/mysql", "root", "aewo4Fen");
		Statement statement = connection.createStatement();
		statement.executeUpdate(sql);
	}
	
	public void dropDatabase(String dbname) throws SQLException {
		// Destroy the database
		String sql = "DROP DATABASE IF EXISTS " + dbname + ";";
		runSqlAsRoot(sql);
	}
	
	public void setDatabasePermissions(String dbname) throws SQLException {
		runSqlAsRoot("GRANT ALL ON " + dbname + ".* TO discovered");
	}
	
	public void createDatabase(String dbname) throws SQLException {
		runSqlAsRoot("CREATE DATABASE " + dbname + ";");
	}
	
	private String[] list_of_quadstores_used = {
			"http://other.example.com/#site-configuration",
			"http://creativecommons.org/#site-configuration",
	};

	public void setUp() throws SQLException {
		for (String uri : list_of_quadstores_used) {
			String dbname = QuadStore.uri2database_name(uri);
			setDatabasePermissions(dbname);
			dropDatabase(dbname);
			createDatabase(dbname);
			System.err.println("Create a MySQL database named " + dbname);
		}
	}
	
	public void tearDown() throws SQLException {
		for (String uri : list_of_quadstores_used) {
			String dbname = QuadStore.uri2database_name(uri);
			dropDatabase(dbname);
		}
	}
	
	/** A unit test that shows adding a curator works. 
	 * @throws SQLException 
	 * @throws ClassNotFoundException */
	public void addCurator() throws SQLException, ClassNotFoundException {
		String graphName = "http://creativecommons.org/#site-configuration";
		TripleStore store = QuadStore.uri2TripleStore(graphName);
		
		/* We have no Curators at the start */
		Collection<Curator> available_curators = store.load(org.creativecommons.learn.oercloud.Curator.class);
		assertEquals(0, available_curators.size());

		/* Create a Curator, as if we were using the command line */
		org.creativecommons.learn.feed.AddCurator.addCurator("MIT", "http://mit.edu/");

		available_curators = store.load(org.creativecommons.learn.oercloud.Curator.class);
		assertEquals(1, available_curators.size());
		
		/* Make sure we saved it correctly */
		Curator curator = available_curators.iterator().next();
		assertEquals(curator.getName(), "MIT");
		assertEquals(curator.getUrl(), "http://mit.edu/");
		
		/* Get a different TripleStore */
		TripleStore aDifferentStore = QuadStore.uri2TripleStore("http://other.example.com/#site-configuration");
		
		/* We have no Curators in the different QuadStore */
		Collection<Curator> aDifferentListOfCurators = aDifferentStore.load(org.creativecommons.learn.oercloud.Curator.class);
		assertEquals(0, aDifferentListOfCurators.size());
	}
	
	public void testAddCurator() throws SQLException, ClassNotFoundException {
		this.addCurator();
	}
	
	/** A unit test that shows adding a curator works. 
	 * @throws SQLException 
	 * @throws ClassNotFoundException */
	public void testAddFeed() throws SQLException, ClassNotFoundException {
		
		TripleStore store = QuadStore.uri2TripleStore("http://creativecommons.org/#site-configuration");
		
		this.addCurator();

		/* We have no Feeds at the start */
		Collection<Feed> available_feeds = store.load(org.creativecommons.learn.oercloud.Feed.class);
		assertEquals(0, available_feeds.size());

		/* Create a Feed, as if we were using the command line */
		org.creativecommons.learn.feed.AddFeed.addFeed("rss", "http://ocw.nd.edu/courselist/rss", "http://ocw.nd.edu/");

		available_feeds = store.load(org.creativecommons.learn.oercloud.Feed.class);
		assertEquals(1, available_feeds.size());
		
		/* Make sure we saved it correctly */
		Feed feed = available_feeds.iterator().next();
		assertEquals(feed.getCurator().getUrl(), "http://ocw.nd.edu/");
		assertEquals(feed.getFeedType(), "rss");
		assertEquals(feed.getUrl(), "http://ocw.nd.edu/courselist/rss");
	}
	
	// List feeds
	// List curators
	// Aggregate
		
    public void testIntegration()
    {
    	// Steps:
    	// 1. Add a curator to the triple store
    	// 2. Add a feed to the triple store
    	// 3. (templated on type of feed) aggregate package pulls data in
    	// ...? 
        assertTrue( "TestExample", true );
    }
    
}
