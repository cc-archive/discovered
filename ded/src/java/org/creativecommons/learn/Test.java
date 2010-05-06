package org.creativecommons.learn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import org.creativecommons.learn.oercloud.Curator;

import thewebsemantic.NotFoundException;
import junit.framework.*;

public class Test extends TestCase {
	
	public void setUp () throws SQLException {
		// Create a database
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/", "root", "aewo4Fen");
		Statement statement = connection.createStatement();
		
		String sql = "CREATE DATABASE test";
		statement.executeUpdate(sql);
	}
	
	public void tearDown () throws SQLException {
		// Create a database
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/", "root", "aewo4Fen");
		Statement statement = connection.createStatement();
		
		String sql = "DROP DATABASE test";
		statement.executeUpdate(sql);
	}
	
	/** A unit test that shows adding a curator works. */
	public void testAddCurator() {
		TripleStore store = TripleStore.get();

		/* We have no Curators at the start */
		Collection<Curator> available_curators = store.load(org.creativecommons.learn.oercloud.Curator.class);
		assertEquals(available_curators.size(), 0);

		/* Create a Curator, as if we were using the command line */
		org.creativecommons.learn.feed.AddCurator.addCuratorWithNameAndUrl("MIT", "http://mit.edu/");

		available_curators = store.load(org.creativecommons.learn.oercloud.Curator.class);
		assertEquals(available_curators.size(), 1);
		
		/* Make sure we saved it correctly */
		Curator curator = available_curators.iterator().next();
		assertEquals(curator.getName(), "MIT");
		assertEquals(curator.getUrl(), "http://mit.edu/");
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
