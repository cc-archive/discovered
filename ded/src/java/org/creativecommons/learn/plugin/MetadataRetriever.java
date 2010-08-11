package org.creativecommons.learn.plugin;

import org.creativecommons.learn.oercloud.Resource;

/**
 * 
 * The plugin interface for modifying {@link Resource}s as they are processed 
 * during aggregation. 
 * 
 * @author Nathan R. Yergler <nathan@yergler.net>
 * @see Resource
 * @see org.creativecommons.learn.aggregate.FeedUpdater
 * 
 */
public interface MetadataRetriever {
	  /** The name of the extension point. */
	  final static String X_POINT_ID = MetadataRetriever.class.getName();

	  /**
	   * Retrieve metadata for the {@link Resource}.  Return a new or modified Resource.
	   * 
	   * @param resource aggregated Resource.
	   * @return modified (or a new) Resource containing added metadata
	   */
	  Resource retrieve(Resource resource);

}
