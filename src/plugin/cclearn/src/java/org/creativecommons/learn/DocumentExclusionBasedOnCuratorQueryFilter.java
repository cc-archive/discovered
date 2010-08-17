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
import org.apache.nutch.searcher.Query.Phrase;
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
			// FIXME: Use "-curator", not "excludecurator". We're just avoiding
			// the minus sign to make things easier at this stage of
			// development.
			if (c.getField().equals("excludecurator")) {
				/* NOTE: This is some batty code.
				 * 
				 * As far as I can tell, Nutch will preprocess the following query:
				 * 	excludecurator:http://ocw.nd.edu/
				 * 
				 *  into ("excludecurator", {"http", "ocw", "nd", "edu"})
				 *  
				 * I'm going to de-preprocess it back. I realize that is utter insanity.
				 * 
				 * There is this class called RawFieldQueryFilter but I don't yet see how it works.
				 * 
				 * I've been advised to read DateQueryFilter but don't yet see how that works differently.
				 * 
				 */
				// Find the value of this field
				if (c.isPhrase()) {
					StringBuffer buffer = new StringBuffer();;
					LOG.info("Got a phrase");
					Phrase nutchPhrase = c.getPhrase();
					LOG.info("Phrase has toString: " + nutchPhrase.toString());
					LOG.info("Clause has toStriong: " + c.toString());
					Query.Term[] terms = nutchPhrase.getTerms();
					for (int j = 0; j < terms.length; j++) {
						// if this is the first term, make sure it is http
						if (j == 0) {
							String s = terms[j].toString();
							if (s.equals("http")) {
								buffer.append("http://");
								LOG.info("Added http");
							}
							else {
								LOG.info("Weird, terms[0].toString() wasn't http.");
							}
							continue; // next term, then!
						}
						
						// Normally, we suffix term toString values with ".".
						String suffix = ".";
						// But the last one should be suffixed with "/"
						if (j == (terms.length - 1)) {
							suffix = "/";
						}

						buffer.append(terms[j].toString());
						buffer.append(suffix);
					}
					ret.add(buffer.toString());
				} else {
					LOG.info("Got a term, I guess.");
					ret.add(c.getTerm().toString());
				}
			}
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
