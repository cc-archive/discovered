package org.creativecommons.learn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.search.BooleanQuery;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.QueryException;
import org.apache.nutch.searcher.QueryFilter;

public class DocumentExclusionBasedOnCuratorQueryFilter implements QueryFilter {
    private static final Log LOG = 
    	LogFactory.getLog(DocumentExclusionBasedOnCuratorQueryFilter.class.getName());
	private Configuration conf;

    public DocumentExclusionBasedOnCuratorQueryFilter() {
        LOG.info("Initialized query filter that excludes documents based on excludecurator");
    }

	@Override
	public BooleanQuery filter(Query input, BooleanQuery translation)
			throws QueryException {
		// TODO Auto-generated method stub
		LOG.info("Huh, we should have filtered. FIXME: Write a filter here.");
		return null;
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
