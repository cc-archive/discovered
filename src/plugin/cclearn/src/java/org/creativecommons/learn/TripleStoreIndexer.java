package org.creativecommons.learn;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
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
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class TripleStoreIndexer implements IndexingFilter {
        HashMap <String,String> DEFAULT_NAMESPACES = null;

	public Collection<String> getAllPossibleFieldNames() {
		HashSet<String> fieldNames = new HashSet<String>();

		// Get all the Rdfstores. Explode all the possible column names.
		// For each RdfStore, get all the predicates. For each predicate, create a
		// ProvenancePredicatePair, and add its .toFieldName() to a list. Return
		// the list.
		
		ArrayList<String> provenanceURIs = RdfStoreFactory.get().getAllKnownTripleStoreUris();
		
		for (String provenanceURI: provenanceURIs) {
			
			// Create a query
			String queryString = "SELECT ?s ?p ?o WHERE { ?s ?p ?o .}";
			Query query = QueryFactory.create(queryString);

			// Execute the query and obtain results
            RdfStore store = RdfStoreFactory.get().forProvenance(provenanceURI);
			Model model;
			model = store.getModel();

			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet results = qe.execSelect();

			while (results.hasNext()) {
				QuerySolution stmt = results.nextSolution();
				RDFNode predicate = stmt.get("p");
				ProvenancePredicatePair pair = new ProvenancePredicatePair(provenanceURI, predicate);
				String fieldName = pair.toFieldName(); //column name, field name, same thing
				fieldNames.add(fieldName);
			}
			
			// Important - free up resources used running the query
			qe.close();
            store.close();
		}
		
		// Add field names that come from the site-specific field_names.xml configuration file.
		for (Entry<String, String> entry : customFieldConfiguration) {
			String key = entry.getKey();
			fieldNames.add(key);
		}
		
		fieldNames.add(Search.FEED_FIELD);
		fieldNames.add(Search.CURATOR_INDEX_FIELD);
		fieldNames.add(Search.ALL_CURATORS_INDEX_FIELD);

		return fieldNames;
	}

	public static final Log LOG = LogFactory.getLog(TripleStoreIndexer.class
			.getName());

	private Configuration conf;

	private Configuration customFieldConfiguration;

	public TripleStoreIndexer() {

		LOG.info("Created TripleStoreIndexer.");

		// initialize the set of default mappings
		DEFAULT_NAMESPACES = new HashMap<String, String>();
		DEFAULT_NAMESPACES.put(CCLEARN.getURI(), CCLEARN.getDefaultPrefix());
		DEFAULT_NAMESPACES.put("http://purl.org/dc/elements/1.1/", "dct");
		DEFAULT_NAMESPACES.put("http://purl.org/dc/terms/", "dct");
		DEFAULT_NAMESPACES.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#",
				"rdf");

		this.customFieldConfiguration = new Configuration();
		this.customFieldConfiguration.addResource("discovered-search-prefixes.xml");

		System.out.println("TripleStoreIndexer has been constructed");
	}

	public Configuration getCustomFieldConfiguration() {
	    return this.customFieldConfiguration;
	}

	@Override
	public void addIndexBackendOptions(Configuration conf) {
		for (String lucene_column_name : getAllPossibleFieldNames()) {
            LOG.info("Adding field " + lucene_column_name);
			LuceneWriter.addFieldOptions(lucene_column_name,
					LuceneWriter.STORE.YES, LuceneWriter.INDEX.UNTOKENIZED, conf);
		}

	} // addIndexBackendOptions
	
    /*
     * If there are no values for that field name we return null
     */
    public Collection<String> getValuesForCustomLuceneFieldName(String resourceURI, String fieldName) {
    	HashSet<String> values = new HashSet<String>();
    	/* First, figure out which RDF predicate URI this fieldName refers to. */
    	String predicateURI = this.customFieldConfiguration.get(fieldName);
    	if (predicateURI == null) {
    		LOG.warn("Yikes, you queried the IndexFilter for a custom field name that is not configured: " + fieldName);
    		return values;
    	}

        LOG.info("predicate URI: " + predicateURI);

        /* Now, each triple store, do a query looking for triples matching
         * <resourceURI> <predicateURI> object.
    	 * Aggregate those objects into the HashSet.
    	 */
    	for (String provenanceURI: RdfStoreFactory.get().getAllKnownTripleStoreUris()) {
            LOG.info("Looping over triple store uri " + provenanceURI +
                    " looking for triples matching " + resourceURI +
                    predicateURI + " _____");
    		RdfStore store = RdfStoreFactory.get().forProvenance(provenanceURI);
    		Model model = store.getModel();
    		SimpleSelector selector = new SimpleSelector(
    				model.createResource(resourceURI), 
    				model.createProperty(predicateURI),
    				(RDFNode) null);
   												      
    		StmtIterator statements = model.listStatements(selector);
    		while (statements.hasNext()) {
                LOG.info("Looping over statement for subj: " + resourceURI +
                        ", pred: " + fieldName);
    			Statement statement = statements.next();
    			RDFNode node = statement.getObject();
    			if (node.isLiteral()) {
    				Literal literal = (Literal) node;
    				values.add(literal.getString());
    			} else if (node.isResource()) {
    				Resource r = (Resource) node;
    				values.add(r.getUrl());
    			} else {
    				String asString = node.toString();
    				LOG.warn("Weird, a node of an unusual type: " + asString);
    				values.add(asString);
    			}
    		}
            store.close();
    	}

    	return values;
    }

	@Override
	public NutchDocument filter(NutchDocument doc, Parse parse, Text url,
			CrawlDatum datum, Inlinks inlinks) throws IndexingException {
         // This method is called once per URL that Nutch is crawling, during
         // the "Indexing" phase
		
		LOG.info("RdfStore: Begin indexing " + url.toString());

		// Index all triples
		LOG.info("RdfStore: indexing all triples.");
		indexTriples(doc, url);
		
        // The discovered-search-prefixes.xml file configures, in a
        // site-specific way, what Lucene column names to use for certain
        // predicates. Here, we index those. FIXME: This is done in a
        // provenance-naive way.
        //
        // FIXME: customFieldConfiguration appears to be
        // containing much more than it should. We really only want to add the
        // DiscoverEd custom fields, not the Hadoop built-in "custom fields"
		for (Entry<String, String> entry : customFieldConfiguration) {
            LOG.info("I might add this to Lucene: " + entry.getKey() + ": " + entry.getValue());
			String fieldName = entry.getKey();
	        Collection<String> values = this.getValuesForCustomLuceneFieldName(
	                url.toString(), fieldName);
	        for (String value : values) {
                LOG.info("Adding to Lucene... " + fieldName + ", " + value);
	        	doc.add(fieldName, value);
	        }
		}

		// Follow special cases (curator)
		LOG.debug("RdfStore: indexing special cases.");
		LOG.warn("TripleStoreIndexer is about to try to index this URL: " + url.toString());

        for (String provURI: RdfStoreFactory.get().getAllKnownTripleStoreUris()) {
            RdfStore store = RdfStoreFactory.get().forProvenance(provURI);
            try {
                Resource resource = store.loadDeep(Resource.class, url.toString());
                this.indexSources(doc, resource);
            }
            catch (NotFoundException e) {
                // Silence this error.
            } 
            finally {
                store.close();
            }
        }

        LOG.info("TripleStoreIndexer: Calculating all curators string.");
        Resource resource = RdfStoreFactory.get().getReader().load(
        		Resource.class, url.toString());
        String all_curators_string = resource.getAllCuratorURIsInCanonicalForm();
        doc.removeField(Search.ALL_CURATORS_INDEX_FIELD);
        doc.add(Search.ALL_CURATORS_INDEX_FIELD, all_curators_string);
        LOG.info("TripleStoreIndexer: Stored all curators for " + url.toString() + " as " + all_curators_string);

		// Return the document
		return doc;

	} // public Document filter
	
	private void indexTriples(NutchDocument doc, Text url) {

		String subjectURL = url.toString();
		HashMap<ProvenancePredicatePair, RDFNode> map = RdfStoreFactory.get().getPPP2ObjectMapForSubject(subjectURL);
		
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

	private HashMap<String, HashSet<String>> provenanceResourceCache;
	
	public HashMap<String, HashSet<String>> getProvenanceResourceCache() {
		if (provenanceResourceCache == null) {
			provenanceResourceCache = new HashMap<String, HashSet<String>>();
	    	for (String provenanceURI: RdfStoreFactory.get().getAllKnownTripleStoreUris()) {
	
	    		RdfStore store = RdfStoreFactory.get().forProvenance(provenanceURI);
	    		
	    		for (Resource r : store.load(Resource.class)) {
	    			String resourceURI = r.getUrl();

	    			HashSet<String> available_resources = provenanceResourceCache.get(provenanceURI);
	    			if (available_resources == null) {
	    				available_resources = new HashSet<String>();
	    			}
	    			available_resources.add(resourceURI);
	    			provenanceResourceCache.put(provenanceURI, available_resources);
	    			
	    		}
	    	}
		}
		return provenanceResourceCache;
		
	}

} // TripleStoreIndexer
