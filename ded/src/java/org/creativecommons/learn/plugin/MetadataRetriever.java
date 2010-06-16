package org.creativecommons.learn.plugin;

import org.creativecommons.learn.oercloud.Resource;

public interface MetadataRetriever {
	  /** The name of the extension point. */
	  final static String X_POINT_ID = MetadataRetriever.class.getName();

	  /**
	   * Retrieve metadata for the Resource.  Return a new or modified Resource.
	   * 
	   * @param resource aggregated Resource.
	   * @return modified (or a new) Reource containing added metadata
	   */
	  Resource retrieve(Resource resource);

}
