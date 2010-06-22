package org.creativecommons.learn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.indexer.lucene.LuceneWriter;
import org.apache.nutch.parse.Parse;
import org.creativecommons.learn.oercloud.Feed;
import org.creativecommons.learn.oercloud.Resource;

import thewebsemantic.NotFoundException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class TripleStoreIndexer implements IndexingFilter {

	protected Map<String, String> DEFAULT_NAMESPACES;

	public Collection<String> getAllPossibleColumnNames() {
		HashSet<String> ret = new HashSet<String>();

		// FIXME: This needs to be run once per RdfStore in the future
		// Create a new query
		String queryString = "SELECT ?s ?p ?o " +
				"WHERE { ?s ?p ?o .}";

		Query query = QueryFactory.create(queryString);


		// Execute the query and obtain results
		Model m;
		try {
			m = TripleStore.get().getModel();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// FIXME: Handle this in the getModel() method
			throw new RuntimeException("uhhhh");
		}
		QueryExecution qe = QueryExecutionFactory.create(query, m);
		ResultSet results = qe.execSelect();
		

		// Index the triples
		while (results.hasNext()) {
			QuerySolution stmt = results.nextSolution();
			String predicate_uri = stmt.get("p").toString();
			ret.add(collapseResource(predicate_uri));
		}
		
		// Important - free up resources used running the query
		qe.close();
		ret.add(Search.FEED_FIELD);
		ret.add(Search.CURATOR_INDEX_FIELD);

		return ret;
	}

	public static final Log LOG = LogFactory.getLog(TripleStoreIndexer.class
			.getName());

	private Configuration conf;

	public TripleStoreIndexer() {
		LOG.info("Created TripleStoreIndexer.");

		// initialize the set of default mappings
		DEFAULT_NAMESPACES = new HashMap<String, String>();
		DEFAULT_NAMESPACES.put(CCLEARN.getURI(), CCLEARN.getDefaultPrefix());
		DEFAULT_NAMESPACES.put("http://purl.org/dc/elements/1.1/", "dct");
		DEFAULT_NAMESPACES.put("http://purl.org/dc/terms/", "dct");
		DEFAULT_NAMESPACES.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#",
				"rdf");

	}


	@Override
	public void addIndexBackendOptions(Configuration conf) {
		for (String lucene_column_name : getAllPossibleColumnNames()) {
			LuceneWriter.addFieldOptions(lucene_column_name,
					LuceneWriter.STORE.YES, LuceneWriter.INDEX.UNTOKENIZED, conf);
		}

	} // addIndexBackendOptions

	@Override
	public NutchDocument filter(NutchDocument doc, Parse parse, Text url,
			CrawlDatum datum, Inlinks inlinks) throws IndexingException {
        // This method is called once per URL that Nutch is crawling, during
        // the "Indexing" phase

		try {
			LOG.info("TripleStore: Begin indexing " + url.toString());

			// Index all triples
            // FIXME: Does this mean all triples, or just the triples related to the url?
			LOG.debug("TripleStore: Indexing all triples related to that URL");
			indexTriples(doc, url);

			// Follow special cases (curator)
			LOG.debug("TripleStore: Indexing special cases.");
			this.indexSources(doc, TripleStore.get().loadDeep(Resource.class,
					url.toString()));

		} catch (NotFoundException e) {
			LOG.warn("Could not find " + url.toString()
					+ " in the Triple Store.");
			e.printStackTrace();
		} catch (Exception e) {
			LOG.error("An error occured while indexing " + url.toString());
			e.printStackTrace();
		}

		// Return the document
		return doc;

	} // public Document filter

	private void indexTriples(NutchDocument doc, Text url) {

        // We'll be retrieving triples from the Jena store. We access them
        // through a model.
		Model m;
		try {
			m = TripleStore.get().getModel();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			LOG.error("Unable to get model: " + e.toString());

			return;
		}

        // Create a new query that selects all the triples from the Jena store
        // whose subject is the url in question
		String queryString = "SELECT ?p ?o " + "WHERE {" + "      <"
				+ url.toString() + "> ?p ?o ." + "      }";
		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, m);
		ResultSet results = qe.execSelect();

		// Index the triples!
		while (results.hasNext()) {
			QuerySolution stmt = results.nextSolution();
			this.indexStatement(doc, stmt.get("p"), stmt.get("o"));
		}

		// Important - free up resources used running the query
		qe.close();
	}

	private void indexSources(NutchDocument doc, Resource resource) {

		for (Feed source : resource.getSources()) {

			// add the feed URL to the resource 
			doc.add(Search.FEED_FIELD, source.getUrl());

			// if this feed has curator information attached, index it as well
			String curator_url = "";
			if (source.getCurator() != null) {
				curator_url = source.getCurator().getUrl();
			}

			// LuceneWriter.add(doc, curator);
			doc.add(Search.CURATOR_INDEX_FIELD, curator_url);
		}
	}

	protected String collapseResource(String uri) {
		/*
		 * Given a Resource URI, collapse it using our default namespace
		 * mappings if possible. This is purely a convenience.
		 */
		
		for (String ns_url : DEFAULT_NAMESPACES.keySet()) {
			if (uri.startsWith(ns_url)) {
				return uri.replace(ns_url, "_" + DEFAULT_NAMESPACES.get(ns_url)
						+ "_");
			}
		}

		return uri;

	} // collapseResource

	private void indexStatement(NutchDocument doc, RDFNode pred_node,
			RDFNode obj_node) {
		
		Field.Index tokenized = Field.Index.NOT_ANALYZED;

		// index a single statement
		String predicate = pred_node.toString();
		String object = obj_node.toString();

		// see if we want to collapse the predicate into a shorter convenience
		// value
		if (pred_node.isResource()) {
			predicate = collapseResource(pred_node.toString());
		}

		// process the object...
		if (obj_node.isLiteral()) {
			object = ((Literal) obj_node).getValue().toString();
			tokenized = Field.Index.ANALYZED;
		}

		LOG.debug("Adding to document (" + predicate + ", " + object + ").");

		doc.add(predicate, object);

	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return this.conf;
	}

} // CuratorIndexer
