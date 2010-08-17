package org.creativecommons.learn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;

import thewebsemantic.NotFoundException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import de.fuberlin.wiwiss.ng4j.NamedGraph;
import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
import de.fuberlin.wiwiss.ng4j.Quad;
import de.fuberlin.wiwiss.ng4j.db.NamedGraphSetDB;

/**
 * 
 * @author nathan
 */
public class RdfStoreFactory {
    
	private final static Log LOG = LogFactory.getLog(RdfStoreFactory.class);

	private NamedGraphSet graphset = null;
	private static RdfStoreFactory instance = null;
	
	public static final String SITE_CONFIG_URI = "http://creativecommons.org/#site-configuration";

	public RdfStoreFactory(NamedGraphSet graphset) {
		
		this.graphset  = graphset;
		
	}

	public static RdfStoreFactory get() {
		if (instance == null) {
			instance = new RdfStoreFactory();
		}
		
		return instance;
		
	} // get
	
	private RdfStoreFactory() {
		/// default to the configured database-backed factory here?
        Configuration config = DEdConfiguration.create();

		try {
			Class.forName(config.get("rdfstore.db.driver"));

			Connection connection = DriverManager.getConnection(
					config.get("rdfstore.db.url"), 
					config.get("rdfstore.db.user"),
					config.get("rdfstore.db.password"));
			
			this.graphset = new NamedGraphSetDB(connection);

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * Returns the RdfStore devoted to feeds that the system administrator adds
	 * when configuring this DiscoverEd instance.
	 * 
	 * @throws SQLException
	 * */
	public RdfStore forDEd() {
		return this.forProvenance(RdfStoreFactory.SITE_CONFIG_URI);
	}

    /*
     * Create a database-backed RdfStore corresponding to data with a
     * particular provenance.
     */
	public RdfStore forProvenance(String provURI) {

		// create the named graph if it does not already exist
		if (!this.graphset.containsGraph(provURI)){
			this.graphset.createGraph(provURI);
		}
		
		// return the wrapper for this named graph
		return new RdfStore(ModelFactory.createModelForGraph(this.graphset.getGraph(provURI)));
	}
	
	@SuppressWarnings("unchecked")
	public Iterator<Quad> findQuads(Node provenance, Node subject, Node predicate, Node value) {
		return (Iterator<Quad>) this.graphset.findQuads(provenance, subject, predicate, value);
	}
	
	
	public Collection<String> getProvenancesThatKnowResourceWithThisURI(String resourceURI) {
		HashSet<String> provenances = new HashSet<String>();
		Iterator<Quad> it = this.findQuads(
				Node.ANY,
				Node.createURI(resourceURI),
				RDF.type.asNode(),
				CCLEARN.Resource.asNode());
		while (it.hasNext()) {
		    Quad q = it.next();
		    provenances.add(q.getGraphName().getURI());
		}
		return provenances;
	}

	/**
	 * This method returns an RdfStoreReader which, when queried, returns
	 * data from all provenances.
	 *
	 * @return
	 */
	public RdfStoreReader getReader() {
		return new RdfStoreReader(this.graphset.asJenaModel(SITE_CONFIG_URI));
		
	}
	public int getOrCreateTablePrefixFromURIAsInteger(String uri) {
		return uri.hashCode();
	}
//		try {
//			dbConnection = getDatabaseConnection();
//			createRdfStoresTableIfNeeded(dbConnection);
//
//			// Do we already have a table prefix? If so, return it.
//			java.sql.PreparedStatement matchingTablePrefixes = dbConnection
//					.prepareStatement("SELECT table_prefix FROM rdf_stores WHERE uri = ? ");
//			matchingTablePrefixes.setString(1, uri);
//			ResultSet cursor = matchingTablePrefixes.executeQuery();
//			if (cursor.next()) {
//				return cursor.getInt("table_prefix");
//			}
//
//			// Prepare a SQL statement that saves a row in a table called
//			// rdf_stores, and fill in the values
//			java.sql.PreparedStatement statement = dbConnection
//					.prepareStatement("INSERT INTO rdf_stores (uri) VALUES (?)");
//			statement.setString(1, uri);
//
//			// Run the statement
//			statement.executeUpdate();
//		} catch (SQLException e) {
//			e.printStackTrace();
//			throw new RuntimeException(
//					"Encountered a SQL error while trying to figure out where we keep the data on "
//							+ uri);
//		}
//
//		return getOrCreateTablePrefixFromURIAsInteger(uri);
//	}
//
//	public static String getOrCreateTablePrefixFromURI(String uri) {
//		return "" + getOrCreateTablePrefixFromURIAsInteger(uri);
//	}
//
//	public static String getProvenanceURIFromTablePrefix(int tablePrefix) {
//		try {
//			dbConnection = getDatabaseConnection();
//			createRdfStoresTableIfNeeded(dbConnection);
//
//			// Do we already have a table prefix? If so, return it.
//			java.sql.PreparedStatement matchingURIs = dbConnection
//					.prepareStatement("SELECT uri FROM rdf_stores WHERE table_prefix = ? ");
//			matchingURIs.setInt(1, tablePrefix);
//			ResultSet cursor = matchingURIs.executeQuery();
//			if (cursor.next()) {
//				return cursor.getString("uri");
//			}
//			// If we get down here, something went wrong.
//			throw new RuntimeException("Couldn't find the table prefix "
//					+ tablePrefix + " in the rdf_stores table");
//
//		} catch (SQLException e) {
//			e.printStackTrace();
//			throw new RuntimeException(
//					"Encountered a SQL error while trying to figure out the provenance URI "
//							+ "corresponding to the database table with prefix "
//							+ tablePrefix);
//		}
//	}

	public ArrayList<String> getAllKnownTripleStoreUris() {

		ArrayList<String> uris = new ArrayList<String>();
		
		Iterator<NamedGraph> graphs = this.graphset.listGraphs();
		while (graphs.hasNext()) {
			uris.add(graphs.next().getGraphName().toString());
		}

		LOG.info("RdfStore.getAllKnownTripleStoreUris detected these 3-stores: " + uris);

		return uris;
	}

	public NamedGraphSet getGraphset() {
		return graphset;
	}

	public void close() {

		this.graphset.close();
		
	} // close

	/*
	 * Get a mapping of (Provenance, Predicate) -> RDFNode
	 * 
	 * E.g., ("http://curators.org", "dc:educationLevel") -> "high school"
	 * 
	 * That means that Curators.org has labeled this resource as appropriate for
	 * high schoolers.
	 */
	// FIXME: Migrate this method to stop using getAllKnownTripleStoreUris
	public HashMap<ProvenancePredicatePair, Node> getPPP2ObjectMapForSubject(
			String subjectURL) {

		HashMap<ProvenancePredicatePair, Node> map = new HashMap<ProvenancePredicatePair, Node>();
		
		Iterator<Quad> iterator = this.findQuads(Node.ANY,
				Node.createURI(subjectURL),
				Node.ANY,
				Node.ANY);
		
		while (iterator.hasNext()) {
			Quad q = iterator.next();
			Node predicateNode = q.getPredicate();
			String provenanceURI = q.getGraphName().getURI().toString();
			
			ProvenancePredicatePair p3 = new ProvenancePredicatePair(
					provenanceURI, predicateNode);
			map.put(p3, q.getObject());
		}

		return map;
	}

	public HashMap<ProvenancePredicatePair, Node> getPPP2ObjectMapForSubjectAndPredicate(
			String subjectURI, String titlePredicate) {
		HashMap<ProvenancePredicatePair, Node> map = this
				.getPPP2ObjectMapForSubject(subjectURI);
		HashMap<ProvenancePredicatePair, Node> mapFiltered = new HashMap<ProvenancePredicatePair, Node>();
		for (Entry<ProvenancePredicatePair, Node> entry : map.entrySet()) {
			if (entry.getKey().predicateNode.toString().equals(titlePredicate)) {
				mapFiltered.put(entry.getKey(), entry.getValue());
			}
		}
		return mapFiltered;
	}

	public Set<String> getProvenanceURIsFromCuratorURI(
			String curatorURI) {
		/* Find the matching Curator, if any. */
		RdfStore store = this.forDEd();
		Curator relevantCurator;
		try {
			relevantCurator = store.load(Curator.class, curatorURI);
		} catch (NotFoundException e) {
			return new HashSet<String>();
		}
		Set<String> resultsSoFar = new HashSet<String>();

		// Otherwise, the answer is that the matching provenance URIs are the
		// URLs of all
		// the Feed objects. Let's collect those into resultsSoFar.
		Collection<Feed> all_feeds = store.load(Feed.class);

		// Search for Feeds whose curator is our friend, the relevantCurator
		for (Feed f : all_feeds) {
			if (f.getCurator().getUri().equals(relevantCurator.getUri())) {
				resultsSoFar.add(f.getUri().toString());
			}
		}

		return resultsSoFar;
	}


} // RdfStore
