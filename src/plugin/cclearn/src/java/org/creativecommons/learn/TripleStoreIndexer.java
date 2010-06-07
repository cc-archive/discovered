package org.creativecommons.learn;
import org.creativecommons.learn.RdfStore;


import java.util.HashMap;
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

	public static final Log LOG = LogFactory.getLog(TripleStoreIndexer.class
			.getName());

	private Configuration conf;

	public TripleStoreIndexer() {
		
		// Throw an exception loudly.
		int a = 0/0;
		System.err.println("" + a);
		
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

		// Define the curator and feed fields
		LuceneWriter.addFieldOptions(Search.CURATOR_INDEX_FIELD, LuceneWriter.STORE.YES,
				LuceneWriter.INDEX.UNTOKENIZED, conf);

		LuceneWriter.addFieldOptions(Search.FEED_FIELD, LuceneWriter.STORE.YES,
				LuceneWriter.INDEX.UNTOKENIZED, conf);

	} // addIndexBackendOptions

	@Override
	public NutchDocument filter(NutchDocument doc, Parse parse, Text url,
			CrawlDatum datum, Inlinks inlinks) throws IndexingException {

		try {
			LOG.info("RdfStore: indexing " + url.toString());

			// Index all triples
			LOG.debug("RdfStore: indexing all triples.");
			indexTriples(doc, url);

			// Follow special cases (curator)
			LOG.debug("RdfStore: indexing special cases.");
			this.indexSources(doc, RdfStore.getSiteConfigurationStore().loadDeep(Resource.class,
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
		Model m;
		try {
			m = RdfStore.getSiteConfigurationStore().getModel();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			LOG.error("Unable to get model; " + e.toString());

			return;
		}

		// Create a new query
		String queryString = "SELECT ?p ?o " + "WHERE {" + "      <"
				+ url.toString() + "> ?p ?o ." + "      }";

		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, m);
		ResultSet results = qe.execSelect();

		// Index the triples
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

		// add the field to the document
		Field statementField = new Field(predicate, object, Field.Store.YES,
				tokenized);

		LOG.debug("Adding (" + predicate + ", " + object + ").");

		LuceneWriter.add(doc, statementField);
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return this.conf;
	}

} // CuratorIndexer
