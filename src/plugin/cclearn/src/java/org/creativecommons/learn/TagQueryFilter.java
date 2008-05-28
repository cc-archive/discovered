package org.creativecommons.learn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.searcher.FieldQueryFilter;

public class TagQueryFilter extends FieldQueryFilter {
    private static final Log LOG = 
    	LogFactory.getLog(TagQueryFilter.class.getName());

    public TagQueryFilter() {
        super(Search.TAGS_FIELD, Search.TAGS_BOOST);
        LOG.info("Added a OER tags query");
    }
  
}
