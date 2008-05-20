package org.creativecommons.learn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.searcher.FieldQueryFilter;

public class CuratorNameQueryFilter extends FieldQueryFilter {
    private static final Log LOG = 
	LogFactory.getLog(CuratorNameQueryFilter.class.getName());

    public CuratorNameQueryFilter() {
        super(Search.CURATOR_NAME_FIELD, Search.CURATOR_BOOST);
        LOG.info("Added a curator name query");
    }
  
}
