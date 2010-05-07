package org.creativecommons.learn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;

import thewebsemantic.NotFoundException;
import junit.framework.*;

public class Test extends TestCase {
	
	public void setUp () throws SQLException {

		// Create a database
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/", "root", "aewo4Fen");
		Statement statement = connection.createStatement();
		
		statement.executeUpdate("GRANT ALL ON oercloud.* TO discovered");
		
		tearDown();
		
		String sql = "CREATE DATABASE discovered";
		statement.executeUpdate(sql);
	}
	
	public void tearDown () throws SQLException {
		
		// Create a database
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/", "root", "aewo4Fen");
		Statement statement = connection.createStatement();
		
		String sql = "DROP DATABASE IF EXISTS discovered";
		statement.executeUpdate(sql);
	}
	
	/** A unit test that shows adding a curator works. */
	public QuadStore testAddCurator() {
		
		QuadStore store = new QuadStore("http://creativecommons.org/#site-configuration");
		
		/* We have no Curators at the start */
		Collection<Curator> available_curators = store.load(org.creativecommons.learn.oercloud.Curator.class);
		assertEquals(0, available_curators.size());

		/* Create a Curator, as if we were using the command line */
		org.creativecommons.learn.feed.AddCurator.addCuratorWithNameAndUrl("MIT", "http://mit.edu/");

		available_curators = store.load(org.creativecommons.learn.oercloud.Curator.class);
		assertEquals(1, available_curators.size());
		
		/* Make sure we saved it correctly */
		Curator curator = available_curators.iterator().next();
		assertEquals(curator.getSource(), "http://creativecommons.org/#site-configuration"); // FIXME
		assertEquals(curator.getName(), "MIT");
		assertEquals(curator.getUrl(), "http://mit.edu/");
		
		/* Get a different QuadStore */
		QuadStore aDifferentStore = new QuadStore("http://example.com/#site-configuration");
		
		/* We have no Curators in the different QuadStore */
		Collection<Curator> aDifferentListOfCurators = aDifferentStore.load(org.creativecommons.learn.oercloud.Curator.class);
		assertEquals(0, aDifferentListOfCurators.size());
		
		return store;

	}

	/** A unit test that shows adding a curator works. */
	public void testAddFeed() {
		
		QuadStore store = this.testAddCurator(); 

		/* We have no Feeds at the start */
		Collection<Feed> available_feeds = store.load(org.creativecommons.learn.oercloud.Feed.class);
		assertEquals(0, available_feeds.size());

		/* Create a Feed, as if we were using the command line */
		org.creativecommons.learn.feed.AddFeed.addFeed("rss", "http://ocw.nd.edu/courselist/rss", "http://ocw.nd.edu/");

		available_feeds = store.load(org.creativecommons.learn.oercloud.Feed.class);
		assertEquals(1, available_feeds.size());
		
		/* Make sure we saved it correctly */
		Feed feed = available_feeds.iterator().next();
		assertEquals(feed.getSource(), "http://creativecommons.org/#site-configuration"); // FIXME
		assertEquals(feed.getCurator().getUrl(), "http://ocw.nd.edu/");
		assertEquals(feed.getFeedType(), "rss");
		assertEquals(feed.getUrl(), "http://ocw.nd.edu/courselist/rss");
	}
		
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
