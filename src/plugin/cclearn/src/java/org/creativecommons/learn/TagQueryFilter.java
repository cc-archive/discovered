package org.creativecommons.learn;

import org.apache.nutch.searcher.FieldQueryFilter;

import java.util.logging.Logger;

// Commons imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.creativecommons.learn.Search;

public class TagQueryFilter extends FieldQueryFilter {
    private static final Log LOG = 
	LogFactory.getLog(TagQueryFilter.class.getName());

    public TagQueryFilter() {
        super(Search.TAGS_FIELD, Search.TAGS_BOOST);
        LOG.info("Added a OER tags query");
    }
  
}
