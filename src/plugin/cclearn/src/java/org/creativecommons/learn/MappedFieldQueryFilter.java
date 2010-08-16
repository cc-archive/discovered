/**
 * 
 */
package org.creativecommons.learn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.nutch.analysis.CommonGrams;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.QueryException;
import org.apache.nutch.searcher.QueryFilter;
import org.apache.nutch.searcher.Query.Clause;
import org.apache.nutch.searcher.Query.Phrase;

/**
 * Translate query fields to search a differently-named field, as indexed by an
 * IndexingFilter. Best for tokenized fields.
 */
public class MappedFieldQueryFilter implements QueryFilter {

	private String query_field;
	private String index_field;
	private float boost = 1.0f;
	private Configuration conf;
	private CommonGrams commonGrams;

	/** Construct for the named field. */
	protected MappedFieldQueryFilter(String field) {
		this(field, field, 1.0f);
	}

	/** Construct for the named field, boosting as specified. */
	protected MappedFieldQueryFilter(String query_field, String index_field,
			float boost) {
		this.query_field = query_field;
		this.index_field = index_field;
		this.boost = boost;
	}
	
	public static HashSet<String> getActiveProvenanceURIs(Collection<String> excludedCuratorURIs) {
		HashSet<String> allProvenanceURIs = new HashSet<String>(RdfStoreFactory.get().getAllKnownTripleStoreUris());
		RdfStoreFactory factory = RdfStoreFactory.get();
		for (String excludeMyFeeds : excludedCuratorURIs) {
			Collection <String> feedsToExclude = factory.getProvenanceURIsFromCuratorURI(excludeMyFeeds);
			allProvenanceURIs.removeAll(feedsToExclude);
		}
		return allProvenanceURIs;		
	}
	
	// FIXME: Test this. This is almost definitely wrong.
	public static Set<String> getActiveProvenanceURIs(Query input) {
		Set<String> curatorURIsToExclude = new HashSet<String>();
		for (Clause c: input.getClauses()) {
			
			// FIXME: Use "-curator", not "excludecurator". We're just avoiding
			// the minus sign to make things easier at this stage of
			// development.
			if (c.getField().equals("excludecurator")) {
				
				// Find the value of this field
				if (c.isPhrase()) {
					Phrase nutchPhrase = c.getPhrase();
					Query.Term[] terms = nutchPhrase.getTerms();
					PhraseQuery lucenePhrase = new PhraseQuery();

					for (int j = 0; j < terms.length; j++) {
						curatorURIsToExclude.add(terms[j].toString());
					}
				} else {
					curatorURIsToExclude.add(c.getTerm().toString());
				}
			}
		}
		
		return getActiveProvenanceURIs(curatorURIsToExclude);
	}

	protected org.apache.lucene.search.Query takeClauseAndCreateAMegaQueryContainingCertainProvenances(
			Collection<String> provenanceURIs, Clause c) {
		DisjunctionMaxQuery ret = new DisjunctionMaxQuery(0);

		// optimize phrase clause
		if (c.isPhrase()) {
			String[] opt = this.commonGrams.optimizePhrase(c.getPhrase(),
					query_field);
			if (opt.length == 1) {
				c = new Clause(new Query.Term(opt[0]), c.isRequired(), c
						.isProhibited(), getConf());
			} else {
				c = new Clause(new Phrase(opt), c.isRequired(), c
						.isProhibited(), getConf());
			}
		}

		// Now, once per provenance, create a Query (we call it
		// "provenanceSpecificQuery"), and add it to the larger query as a
		// disjunction.

		// Do this once per provenance:
		for (String provenanceURI : provenanceURIs) {

			String fieldName = ProvenancePredicatePair.makeCompleteFieldNameWithProvenance(
					provenanceURI, index_field);
			
			// construct appropriate Lucene clause
			org.apache.lucene.search.Query provenanceSpecificQuery;
			if (c.isPhrase()) {
				Phrase nutchPhrase = c.getPhrase();
				Query.Term[] terms = nutchPhrase.getTerms();
				PhraseQuery lucenePhrase = new PhraseQuery();

				for (int j = 0; j < terms.length; j++) {
					Term t = new Term(fieldName, terms[j].toString());
					lucenePhrase.add(t);
				}
				provenanceSpecificQuery = lucenePhrase;
			} else {
				provenanceSpecificQuery = new TermQuery(new Term(
						fieldName, c.getTerm().toString()));
			}

			// set boost
			provenanceSpecificQuery.setBoost(boost);

			// add it as specified in query
			ret.add(provenanceSpecificQuery);

		}
		return ret;
	}

	public BooleanQuery filter(Query input, BooleanQuery output)
			throws QueryException {
		
		Set<String> activeProvenanceURIs = getActiveProvenanceURIs(input);
		
		// examine each clause in the Nutch query
		Clause[] clauses = input.getClauses();
				
		for (int i = 0; i < clauses.length; i++) {
			Clause c = clauses[i];

			// skip non-matching clauses
			if (!c.getField().equals(query_field))
				continue;

			org.apache.lucene.search.Query luceneClause = takeClauseAndCreateAMegaQueryContainingCertainProvenances(
					activeProvenanceURIs, c);
			output.add(luceneClause,
					(c.isProhibited() ? BooleanClause.Occur.MUST_NOT : (c
							.isRequired() ? BooleanClause.Occur.MUST
							: BooleanClause.Occur.SHOULD)));
		}

		// return the modified Lucene query
		return output;
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
		this.commonGrams = new CommonGrams(conf);
	}

	public Configuration getConf() {
		return this.conf;
	}

}
