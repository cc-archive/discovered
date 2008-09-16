package org.creativecommons.learn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.creativecommons.learn.search.MappedFieldQueryFilter;

public class LanguageFilter extends MappedFieldQueryFilter {
    private static final Log LOG = 
    	LogFactory.getLog(LanguageFilter.class.getName());

    public LanguageFilter() {
        super(Search.LANGUAGE_QUERY_FIELD, Search.LANGUAGE_INDEX_FIELD, Search.LANGUAGE_BOOST);
        LOG.info("Added a OER language query");
    }
  
}
