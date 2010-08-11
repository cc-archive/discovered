package org.creativecommons.learn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
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
	public Collection<String> getProvenancesThatKnowResourceWithThisURI(String resourceURI) {
		HashSet<String> provenances = new HashSet<String>();
		Iterator it = this.graphset.findQuads(
				Node.ANY,
				Node.createURI(resourceURI),
				RDF.type.asNode(),
				CCLEARN.Resource.asNode());
		while (it.hasNext()) {
		    Quad q = (Quad) it.next();
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
	public HashMap<ProvenancePredicatePair, RDFNode> getPPP2ObjectMapForSubject(
			String subjectURL) {

		HashMap<ProvenancePredicatePair, RDFNode> map = new HashMap<ProvenancePredicatePair, RDFNode>();

		for (String provenanceURI : this.getAllKnownTripleStoreUris()) {
            RdfStore store = this.forProvenance(provenanceURI);

			Model m;
			m = store.getModel();

			// Create a new query
			String queryString = "SELECT ?p ?o " + "WHERE {" + "      <"
					+ subjectURL.toString() + "> ?p ?o ." + "      }";
			Query query = QueryFactory.create(queryString);

			// Execute the query and obtain results
			QueryExecution qe = QueryExecutionFactory.create(query, m);

			com.hp.hpl.jena.query.ResultSet cursor = qe.execSelect();

			// Index the triples
			while (cursor.hasNext()) {
				QuerySolution stmt = cursor.nextSolution();
				RDFNode predicateNode = stmt.get("p");
				ProvenancePredicatePair p3 = new ProvenancePredicatePair(
						provenanceURI, predicateNode);
				RDFNode objectNode = stmt.get("o");
				System.out
						.println("Found a triple for " + subjectURL + ": "
								+ predicateNode.toString() + " courtesy of "
								+ provenanceURI + ", value is "
								+ objectNode.toString());
				map.put(p3, objectNode);
			}

			// Important - free up resources used running the query
			qe.close();
		}

		return map;
	}

	public HashMap<ProvenancePredicatePair, RDFNode> getPPP2ObjectMapForSubjectAndPredicate(
			String subjectURI, String titlePredicate) {
		HashMap<ProvenancePredicatePair, RDFNode> map = this
				.getPPP2ObjectMapForSubject(subjectURI);
		HashMap<ProvenancePredicatePair, RDFNode> mapFiltered = new HashMap<ProvenancePredicatePair, RDFNode>();
		for (Entry<ProvenancePredicatePair, RDFNode> entry : map.entrySet()) {
			if (entry.getKey().predicateNode.toString().equals(titlePredicate)) {
				mapFiltered.put(entry.getKey(), entry.getValue());
			}
		}
		return mapFiltered;
	}

	public ArrayList<String> getProvenanceURIsFromCuratorShortName(
			String curatorShortName) {
		/* Find the matching Curator, if any. */
		Curator relevantCurator = null;
		ArrayList<String> resultsSoFar = new ArrayList<String>();

		RdfStore store = this.forDEd();
		Collection<Curator> curators = store.load(Curator.class);

		/* FIXME: This is a really lame way to do a query. */
		for (Curator c : curators) {
			if (c.getName().equals(curatorShortName)) {
				relevantCurator = c;
				break;
			}
		}

		// Okay, if there's no matching Curator, we bail out now.
		if (relevantCurator == null) {
			return resultsSoFar;
		}

		// Otherwise, the answer is that the matching provenance URIs are the
		// URLs of all
		// the Feed objects. Let's collect those into resultsSoFar.
		Collection<Feed> all_feeds = store.load(Feed.class);

		// Search for Feeds whose curator is our friend, the relevantCurator
		for (Feed f : all_feeds) {
			if (f.getCurator().getUrl().equals(relevantCurator.getUrl())) {
				resultsSoFar.add(f.getUrl());
			}
		}

		return resultsSoFar;
	}


} // RdfStore
