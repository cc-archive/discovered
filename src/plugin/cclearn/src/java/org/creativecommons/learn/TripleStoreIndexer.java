package org.creativecommons.learn;

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
		LOG.info("Created TripleStoreIndexer.");

		// initialize the set of default mappings
		DEFAULT_NAMESPACES = new HashMap<String, String>();
		DEFAULT_NAMESPACES.put(CCLEARN.getURI(), CCLEARN.getDefaultPrefix());
		DEFAULT_NAMESPACES.put("http://purl.org/dc/elements/1.1/", "dct");
		DEFAULT_NAMESPACES.put("http://purl.org/dc/terms/", "dct");
		DEFAULT_NAMESPACES.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#",
				"rdf");

	}

	public Document filter(Document doc, Parse parse, Text url,
			CrawlDatum datum, Inlinks inlinks) throws IndexingException {

		try {
			LOG.info("TripleStore: indexing " + url.toString());

			// Index all triples
			LOG.debug("TripleStore: indexing all triples.");
			indexTriples(doc, url);

			// Follow special cases (curator)
			LOG.debug("TripleStore: indexing special cases.");
			this.indexSources(doc, TripleStore.get().loadDeep(Resource.class,
					url.toString()));

		} catch (NotFoundException e) {
			LOG.warn("Could not find " + url.toString() + " in the Triple Store.");
			e.printStackTrace();
		} catch (Exception e) {
			LOG.error("An error occured while indexing " + url.toString());
			e.printStackTrace();
		}
		
		// Return the document
		return doc;

	} // public Document filter

	private void indexTriples(Document doc, Text url) {
		Model m;
		try {
			m = TripleStore.get().getModel();
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

	private void indexSources(Document document, Resource resource) {

		for (Feed source : resource.getSources()) {

			Field sourceField = new Field(Search.FEED_FIELD, source.getUrl(),
					Field.Store.YES, Field.Index.TOKENIZED);
			sourceField.setBoost(Search.FEED_BOOST);
			document.add(sourceField);

			// if this feed has curator information attached, index it as well
			String curator_url = "";
			if (source.getCurator() != null) {
				curator_url = source.getCurator().getUrl();
			}

			Field curator = new Field(Search.CURATOR_INDEX_FIELD, curator_url,
					Field.Store.YES, Field.Index.UN_TOKENIZED);
			curator.setBoost(Search.CURATOR_BOOST);
			document.add(curator);
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

	private void indexStatement(Document doc, RDFNode pred_node,
			RDFNode obj_node) {
		Field.Index tokenized = Field.Index.UN_TOKENIZED;

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
			tokenized = Field.Index.TOKENIZED;
		}

		// add the field to the document
		Field statementField = new Field(predicate, object, Field.Store.YES,
				tokenized);

		LOG.debug("Adding (" + predicate + ", " + object + ").");

		doc.add(statementField);
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return this.conf;
	}

} // CuratorIndexer
