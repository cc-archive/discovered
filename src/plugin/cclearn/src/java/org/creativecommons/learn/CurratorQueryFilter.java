package org.creativecommons.learn;

import org.apache.nutch.searcher.FieldQueryFilter;

import java.util.logging.Logger;

// Commons imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.creativecommons.learn.Search;

public class CurratorQueryFilter extends FieldQueryFilter {
    private static final Log LOG = 
	LogFactory.getLog(CurratorQueryFilter.class.getName());

    public CurratorQueryFilter() {
        super(Search.CURRATOR_FIELD, Search.CURRATOR_BOOST);
        LOG.info("Added a OER source query");
    }
  
}
