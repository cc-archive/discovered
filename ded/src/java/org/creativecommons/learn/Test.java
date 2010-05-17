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
			"http://ocw.nd.edu/"
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
	
	/** A unit test that shows adding a curator works. 
	 * @throws SQLException 
	 * @throws ClassNotFoundException */
	public void addCurator() throws SQLException, ClassNotFoundException {
		String graphName = "http://creativecommons.org/#site-configuration";
		RdfStore store = RdfStore.uri2RdfStore(graphName);
		
		/* We have no Curators at the start */
		Collection<Curator> available_curators = store.load(org.creativecommons.learn.oercloud.Curator.class);
		assertEquals(0, available_curators.size());

		/* Create a Curator, as if we were using the command line */
		org.creativecommons.learn.feed.AddCurator.addCurator("Notre Dame OCW", "http://ocw.nd.edu/");

		available_curators = store.load(org.creativecommons.learn.oercloud.Curator.class);
		assertEquals(1, available_curators.size());
		
		/* Make sure we saved it correctly */
		Curator curator = available_curators.iterator().next();
		assertEquals(curator.getName(), "Notre Dame OCW");
		assertEquals(curator.getUrl(), "http://ocw.nd.edu/");
		
		/* Get a different RdfStore */
		RdfStore aDifferentStore = RdfStore.uri2RdfStore("http://other.example.com/#site-configuration");
		
		/* We have no Curators in the different RdfStore */
		Collection<Curator> aDifferentListOfCurators = aDifferentStore.load(org.creativecommons.learn.oercloud.Curator.class);
		assertEquals(0, aDifferentListOfCurators.size());
		store.close();
	}
	
	public void testAddCurator() throws SQLException, ClassNotFoundException {
		this.addCurator();
	}
	
	/** A unit test that shows adding a curator works. 
	 * @throws SQLException 
	 * @throws ClassNotFoundException */
	public void testAddFeed() throws SQLException, ClassNotFoundException {
		
		RdfStore store = RdfStore.uri2RdfStore("http://creativecommons.org/#site-configuration");
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
	
	public void testURLTitleProvenance() throws SQLException {
		// When you have X in the site configuration triple store
		// and you take action Y
		// metadata Z is stored with provenance A
		// TripleStore.get()
			// getSiteConfigurationStore()
				// all of our calls ought to be audited: is the siteConfigurationStore the right one? If not, what is the right provenance?

		// X: Curator and RSS Feed 
		// Y: Aggregate
		// Z: The title of a Resource found by aggregating data from the feed
		// A: The URI of the Curator of the Feed
		
		// Add a curator		
		String curatorURI = "http://ocw.nd.edu/";
		org.creativecommons.learn.feed.AddCurator.addCurator("Notre Dame OpenCourseWare", curatorURI);
		
		// Add a feed to that curator's triple store. This feed says, in effect, "here's a URL, and here's its title".
		org.creativecommons.learn.feed.AddFeed.addFeed("rss", "http://ocw.nd.edu/courselist/rss", curatorURI);
		
		// Aggregate
		String[] args = {};
		org.creativecommons.learn.aggregate.Main.main(args);
		
		// Query that curator's RdfStore, find the triple URI-title-literal
		RdfStore store = RdfStore.uri2RdfStore(curatorURI);
		Collection<Resource> resources = store.load(org.creativecommons.learn.oercloud.Resource.class);
		Resource r = resources.iterator().next();
		String title = r.getTitle();
		assertTrue(title.length() > 0);
	}

	public void testResourcesCanGiveThemselvesATitle() throws SQLException {
		// Even if we have no curators and feeds, what if *right now* we are doing aggregate() on a Resource
		// that has its own dc:title stored in it using RDFa.
		
		// Let's say the Resource has URI "http://example.com/barbie" and dc:title "My Barbie Land"
		
		// So that means having this triple:
		// <http://example.com/barbie> <dc:title> "My Barbie Land"
		
		// Let's say the page also has this triple:
		// <http://microsoft.com/> <dc:license> "http://creativecommons.org/licenses/by-sa/3.0/us/"
		// since RDFa pages can contain metadata about *any* page.
		// 
		// With what provenance are these triples stored? (Note that all triples must have a provenance
		// because otherwise we wouldn't know what Jena triple store to look at.)
		
		// Let's store the triple with the provenance of "http://example.com/barbie"
		
		// Add a curator
		String curatorURI = "http://ocw.nd.edu/";
		org.creativecommons.learn.feed.AddCurator.addCurator("Notre Dame OpenCourseWare", curatorURI);
		
		// Add a feed to that curator's triple store. This feed says, in effect, "here's a URL, and here's its title".
		org.creativecommons.learn.feed.AddFeed.addFeed("rss", "http://ocw.nd.edu/courselist/rss", curatorURI);
		
		// Three tests
		// Can we get a title triple for URI u from a URI v, where u and v are in the same feed?
		// Can we get a title triple for URI u from the HTML content of v?
		// Can we get a title triple for URI u from its feed?
		
		// Aggregate
		String[] args = {};
		org.creativecommons.learn.aggregate.Main.main(args);
		
		// Query that curator's RdfStore, find the triple URI-title-literal
		RdfStore curatorStore = RdfStore.uri2RdfStore(curatorURI);
		Resource resourceInCuratorsStore = curatorStore.load(org.creativecommons.learn.oercloud.Resource.class).iterator().next();
		
		// Just double check that the curator's store's copy of the resource doesn't have a title
		assertEquals(resourceInCuratorsStore.getTitle(), null);
		
		// Now query the RdfStore for r.getURL()
		// This will give us the same resource, but when we access its getTitle method we'll see the title it uses to describe itself
		RdfStore store = RdfStore.uri2RdfStore(resourceInCuratorsStore.getUrl());
		Resource resourceInItsOwnStore = store.load(org.creativecommons.learn.oercloud.Resource.class).iterator().next();
		
		// Now there should be a title
		assertTrue(resourceInItsOwnStore.getTitle().length() > 0);
		
	}

	// X: Curator and OAI-PMH Feed
	// Y: Aggregate
	// Z: The title of a Resource found by aggregating data from the feed
	// A: The URI of the Curator of the Feed

	public void testIntegration()
    {
    	// Steps:
    	// 1. Add a curator to the triple store
    	// 2. Add a feed to the triple store
    	// 3. (templated on type of feed) aggregate package pulls data in
    	// ...? 
        assertTrue( "TestExample", true );
    }

    public void testAddFeedMustPointToCuratorWithinTheStore() throws SQLException {
    	try {
			RdfStore store = RdfStore.getSiteConfigurationStore();
			
			/* Try adding a feed where we have no data stored about
			 * the curator. */
			Collection<Feed> available_feeds = store.load(org.creativecommons.learn.oercloud.Feed.class);
			assertEquals(0, available_feeds.size());
	
			/* Create a Feed, as if we were using the command line */
			org.creativecommons.learn.feed.AddFeed.addFeed("rss", "http://ocw.nd.edu/courselist/rss", "http://ocw.nd.edu/");
    	} catch (IllegalArgumentException e) {
    		// sweet.
    		return;
    	}
    	assertFalse(true);
	}
    
}
