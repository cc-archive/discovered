/**
 * 
 */
package org.creativecommons.learn.search;

import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
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
	protected MappedFieldQueryFilter(String query_field, String index_field, float boost) {
		this.query_field = query_field;
		this.index_field = index_field;
		this.boost = boost;
	}

	public BooleanQuery filter(Query input, BooleanQuery output)
			throws QueryException {

		// examine each clause in the Nutch query
		Clause[] clauses = input.getClauses();
		for (int i = 0; i < clauses.length; i++) {
			Clause c = clauses[i];

			// skip non-matching clauses
			if (!c.getField().equals(query_field))
				continue;

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

			// construct appropriate Lucene clause
			org.apache.lucene.search.Query luceneClause;
			if (c.isPhrase()) {
				Phrase nutchPhrase = c.getPhrase();
				Query.Term[] terms = nutchPhrase.getTerms();
				PhraseQuery lucenePhrase = new PhraseQuery();
				for (int j = 0; j < terms.length; j++) {
					lucenePhrase.add(new Term(index_field, terms[j].toString()));
				}
				luceneClause = lucenePhrase;
			} else {
				luceneClause = new TermQuery(new Term(index_field, c.getTerm()
						.toString()));
			}

			// set boost
			luceneClause.setBoost(boost);
			// add it as specified in query

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
