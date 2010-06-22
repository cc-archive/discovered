package org.creativecommons.learn;
import org.creativecommons.learn.RdfStore;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

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
        HashMap <String,String> DEFAULT_NAMESPACES = null;

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
			m = RdfStore.getSiteConfigurationStore().getModel();
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

		System.out.println("TripleStoreIndexer has been constructed");


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
		
		LOG.info("RdfStore: Begin indexing " + url.toString());

		// Index all triples
		LOG.debug("RdfStore: indexing all triples.");
		indexTriples(doc, url);
		
		// Follow special cases (curator)
		LOG.debug("RdfStore: indexing special cases.");
		
		LOG.warn("TripleStoreIndexer is about to try to index this URL: " + url.toString());

		try {
			
			Resource resource = RdfStore.getSiteConfigurationStore().loadDeep(Resource.class,
					url.toString());
			this.indexSources(doc, resource);
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

		String subjectURL = url.toString();
		HashMap<ProvenancePredicatePair, RDFNode> map = RdfStore.getPPP2ObjectMapForSubject(subjectURL);
		
		// Index the triples
		for (Entry<ProvenancePredicatePair, RDFNode> entry: map.entrySet()) {
			ProvenancePredicatePair p3 = entry.getKey();
			RDFNode objectNode = entry.getValue();
			
			// FIXME: Instead of simply using the predicate URI below, use a
			// string that varies according to the predicate AND provenance.
			this.indexStatement(doc, p3, objectNode);
		}
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
    }

	private void indexStatement(NutchDocument doc, ProvenancePredicatePair p3,
			RDFNode obj_node) {

		Field.Index tokenized = Field.Index.NOT_ANALYZED;

		// index a single statement
		String object = obj_node.toString();

		// process the object...
		if (obj_node.isLiteral()) {
			object = ((Literal) obj_node).getValue().toString();
			tokenized = Field.Index.ANALYZED;
		}
		
		String fieldName = null; 
		fieldName = p3.toFieldName();
		// ^ This is the same as a predicate with the provenance encoded into it
		
		LOG.debug("Adding to document (" + fieldName + ", " + object + ").");

		doc.add(fieldName, object);

	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return this.conf;
	}

} // TripleStoreIndexer
