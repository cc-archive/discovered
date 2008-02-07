package org.creativecommons.learn;

import org.apache.nutch.searcher.FieldQueryFilter;

import java.util.logging.Logger;

// Commons imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.creativecommons.learn.Search;

public class SourceQueryFilter extends FieldQueryFilter {
    private static final Log LOG = 
	LogFactory.getLog(SourceQueryFilter.class.getName());

    public SourceQueryFilter() {
        super(Search.SOURCE_FIELD, Search.SOURCE_BOOST);
        LOG.info("Added a OER source query");
    }
  
}
