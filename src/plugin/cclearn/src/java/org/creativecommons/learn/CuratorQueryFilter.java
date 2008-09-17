package org.creativecommons.learn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.creativecommons.learn.search.MappedRawFieldQueryFilter;

public class CuratorQueryFilter extends MappedRawFieldQueryFilter {
	private static final Log LOG = LogFactory.getLog(
			CuratorQueryFilter.class.getName());

	public CuratorQueryFilter() {
		super(Search.CURATOR_QUERY_FIELD, Search.CURATOR_INDEX_FIELD, Search.CURATOR_BOOST);
		LOG.info("Added a OER source query");
	}

}
