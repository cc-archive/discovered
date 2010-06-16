package org.creativecommons.dummy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.creativecommons.learn.oercloud.Resource;
import org.creativecommons.learn.plugin.MetadataRetriever;

public class DummyRetriever implements MetadataRetriever {

	public final static Log LOG = LogFactory.getLog(DummyRetriever.class);

	@Override
	public Resource retrieve(Resource resource) {
		LOG.info("Called dummy retrieve for " + resource.getUrl());

		return resource;
		
	}
}
