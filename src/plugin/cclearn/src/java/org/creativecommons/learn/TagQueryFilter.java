package org.creativecommons.learn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TagQueryFilter extends MappedFieldQueryFilter {
    private static final Log LOG = 
    	LogFactory.getLog(TagQueryFilter.class.getName());

    public TagQueryFilter() {
        super(Search.TAGS_QUERY_FIELD, Search.TAGS_INDEX_FIELD, Search.TAGS_BOOST);
        LOG.info("Added a OER tags query");
    }
  
}
