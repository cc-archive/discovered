package org.creativecommons.learn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EducationLevelFilter extends MappedFieldQueryFilter {
    private static final Log LOG = 
    	LogFactory.getLog(EducationLevelFilter.class.getName());

    public EducationLevelFilter() {
        super(Search.ED_LEVEL_QUERY_FIELD, Search.ED_LEVEL_INDEX_FIELD, Search.ED_LEVEL_BOOST);
        LOG.info("Added a OER education level query");
    }
  
}
