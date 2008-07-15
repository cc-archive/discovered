package org.creativecommons.learn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.searcher.FieldQueryFilter;

public class LanguageFilter extends FieldQueryFilter {
    private static final Log LOG = 
    	LogFactory.getLog(LanguageFilter.class.getName());

    public LanguageFilter() {
        super(Search.LANGUAGE_FIELD, Search.LANGUAGE_BOOST);
        LOG.info("Added a OER language query");
    }
  
}
