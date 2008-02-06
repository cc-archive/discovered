package org.creativecommons.learn;

import org.apache.nutch.searcher.FieldQueryFilter;

import java.util.logging.Logger;

// Commons imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class TagQueryFilter extends FieldQueryFilter {
    private static final Log LOG = 
	LogFactory.getLog(TagQueryFilter.class.getName());

    public TagQueryFilter() {
        super("tags", 5f);
        LOG.info("Added a OER tags query");
    }
  
}
