package org.creativecommons.learn.plugin;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.plugin.Extension;
import org.apache.nutch.plugin.ExtensionPoint;
import org.apache.nutch.plugin.PluginRepository;
import org.apache.nutch.plugin.PluginRuntimeException;
import org.apache.nutch.util.ObjectCache;
import org.creativecommons.learn.oercloud.Resource;

public class MetadataRetrievers {

	public static final String MetadataRetriever_ORDER = "MetadataRetriever.order";

	public final static Log LOG = LogFactory.getLog(MetadataRetrievers.class);

	private MetadataRetriever[] MetadataRetrievers;

	public MetadataRetrievers(Configuration conf) {
		/* Get MetadataRetriever.order property */
		String order = conf.get(MetadataRetriever_ORDER);
		ObjectCache objectCache = ObjectCache.get(conf);
		this.MetadataRetrievers = (MetadataRetriever[]) objectCache
				.getObject(MetadataRetriever.class.getName());
		if (this.MetadataRetrievers == null) {
			/*
			 * If ordered filters are required, prepare array of filters based
			 * on property
			 */
			String[] orderedFilters = null;
			if (order != null && !order.trim().equals("")) {
				orderedFilters = order.split("\\s+");
			}
			try {
				ExtensionPoint point = PluginRepository.get(conf)
						.getExtensionPoint(MetadataRetriever.X_POINT_ID);
				if (point == null)
					throw new RuntimeException(MetadataRetriever.X_POINT_ID
							+ " not found.");
				Extension[] extensions = point.getExtensions();
				HashMap<String, MetadataRetriever> filterMap = new HashMap<String, MetadataRetriever>();
				for (int i = 0; i < extensions.length; i++) {
					Extension extension = extensions[i];
					MetadataRetriever filter = (MetadataRetriever) extension
							.getExtensionInstance();
					LOG.info("Adding " + filter.getClass().getName());
					if (!filterMap.containsKey(filter.getClass().getName())) {
						filterMap.put(filter.getClass().getName(), filter);
					}
				}
				/*
				 * If no ordered filters required, just get the filters in an
				 * indeterminate order
				 */
				if (orderedFilters == null) {
					objectCache.setObject(MetadataRetriever.class.getName(),
							filterMap.values()
									.toArray(new MetadataRetriever[0]));
					/* Otherwise run the filters in the required order */
				} else {
					ArrayList<MetadataRetriever> filters = new ArrayList<MetadataRetriever>();
					for (int i = 0; i < orderedFilters.length; i++) {
						MetadataRetriever filter = filterMap
								.get(orderedFilters[i]);
						if (filter != null) {
							filters.add(filter);
						}
					}
					objectCache.setObject(MetadataRetriever.class.getName(),
							filters.toArray(new MetadataRetriever[filters
									.size()]));
				}
			} catch (PluginRuntimeException e) {
				throw new RuntimeException(e);
			}
			this.MetadataRetrievers = (MetadataRetriever[]) objectCache
					.getObject(MetadataRetriever.class.getName());
		}
	}

	/** Run all defined filters. */
	public Resource retrieve(Resource resource) {
		for (int i = 0; i < this.MetadataRetrievers.length; i++) {
			resource = this.MetadataRetrievers[i].retrieve(resource);
		}

		return resource;

	}

}
