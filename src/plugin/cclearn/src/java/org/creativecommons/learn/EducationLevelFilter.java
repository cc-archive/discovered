package org.creativecommons.learn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.searcher.FieldQueryFilter;

public class EducationLevelFilter extends FieldQueryFilter {
    private static final Log LOG = 
    	LogFactory.getLog(EducationLevelFilter.class.getName());

    public EducationLevelFilter() {
        super(Search.ED_LEVEL_FIELD, Search.ED_LEVEL_BOOST);
        LOG.info("Added a OER education level query");
    }
  
}
