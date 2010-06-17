package org.creativecommons.learn;
import org.creativecommons.learn.RdfStore;


import java.sql.SQLException;
import java.util.HashMap;
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


	public static final Log LOG = LogFactory.getLog(TripleStoreIndexer.class
			.getName());

	private Configuration conf;

	public TripleStoreIndexer() {
		
		LOG.info("Created TripleStoreIndexer.");
		
		System.out.println("TripleStoreIndexer has been constructed");


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
		
		LOG.info("RdfStore: indexing! " + url.toString());

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
			
			// FIXME: Instead of simply using the predicate uri below, use a
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


	public Field createFieldFromPredicateAndObject(String predicate,
			String object, Field.Index tokenized) {
		
		System.out.println("TripleStoreIndexer is creating a field with predicate: " + predicate + " and object: " + object);
		
		// add the field to the document
		Field statementField = new Field(predicate, object, Field.Store.YES,
				tokenized);
		return statementField;
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
		try {
			fieldName = p3.toFieldName();
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Couldn't convert " + p3.toString() + " to a fieldname.");
		}
		
		Field statementField = this.createFieldFromPredicateAndObject(fieldName, object, tokenized);
		
		LOG.debug("Adding (" + fieldName + ", " + object + ").");

		LuceneWriter.add(doc, statementField);
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return this.conf;
	}

} // CuratorIndexer
