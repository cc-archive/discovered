package org.creativecommons.learn;

import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.QueryException;
import org.apache.nutch.searcher.QueryFilter;
import org.apache.nutch.searcher.Query.Clause;
import org.creativecommons.learn.oercloud.Curator;

public class DocumentExclusionBasedOnCuratorQueryFilter implements QueryFilter {
    private static final Log LOG = 
    	LogFactory.getLog(DocumentExclusionBasedOnCuratorQueryFilter.class.getName());
	private Configuration conf;

    public DocumentExclusionBasedOnCuratorQueryFilter() {
        LOG.info("Initialized query filter that excludes documents based on excludecurator");
    }
    
    private HashSet<String> getCuratorsToExclude(Clause[] clauses) {
    	HashSet<String> ret = new HashSet<String>();

        for (Clause c: clauses) {
          // skip any irrelevant clauses
          if (!c.getField().equals("excludecurator"))
            continue;

          // Because it is configured as a raw field, all clauses are Terms,
          // not Clauses. So getTerm() is enough.
          String curatorURI = c.getTerm().toString();
          LOG.warn("Picked up on a curator exclusion URI: " + curatorURI);
          ret.add(curatorURI);
		}
		return ret;
    }

    /**
     * The purpose of this filter is to exclude documents curated only by
     * curators that have been excluded through the excludecurator: query
     * parameter.
     * 
     * In this method, we create a string that represents the list of curators
     * that have been excluded. Call that the "curator blacklist string."
     * 
     * Our Lucene documents representing Resources have a column that contains
     * the list of curators that provided the document. So this method adds a
     * Lucene query to the active set of Lucene queries that insists that any
     * document returned during this search *not* match the generated
     * curator blacklist string.
     */
	@Override
	public BooleanQuery filter(Query input, BooleanQuery output)
			throws QueryException {
		// Create a HashSet of all the excluded curator URIs
		
    	HashSet<String> curatorURIsToExclude = this.getCuratorsToExclude(input.getClauses());
		
		// Then canonicalize them by sorting and joining on space
    	String canonicalForm = Curator.curatorUriCollectionAsString(curatorURIsToExclude);
    	
    	LOG.info("Canonical form: " + canonicalForm);
		
		// Then add a Lucene constraint ensuring that we exclude documents with just the curators we want to exclude
		LOG.info("Huh, we should be looking for documents that have their list of curators not set to equal " + canonicalForm);
        TermQuery luceneClause = new TermQuery(new Term(Search.ALL_CURATORS_INDEX_FIELD, canonicalForm));

        output.add(luceneClause, BooleanClause.Occur.MUST_NOT);

        return output;
	}

	@Override
	public Configuration getConf() {
		return this.conf;		
	}

	@Override
	public void setConf(Configuration arg0) {
		this.conf = arg0;
	}
  
}
