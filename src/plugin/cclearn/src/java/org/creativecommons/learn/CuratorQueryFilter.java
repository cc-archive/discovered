package org.creativecommons.learn;

import org.apache.nutch.searcher.FieldQueryFilter;

import java.util.logging.Logger;

// Commons imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.creativecommons.learn.Search;

public class CuratorQueryFilter extends FieldQueryFilter {
    private static final Log LOG = 
	LogFactory.getLog(CuratorQueryFilter.class.getName());

    public CuratorQueryFilter() {
        super(Search.CURATOR_FIELD, Search.CURATOR_BOOST);
        LOG.info("Added a OER curator query");
    }
  
}
