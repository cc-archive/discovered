package org.creativecommons.learn.oercloud;

import java.util.List;

import thewebsemantic.Namespace;
import thewebsemantic.RdfProperty;
import thewebsemantic.Uri;

@Namespace("http://learn.creativecommons.org/ns#")
public class Curator {

	private String url = null;
	private String name = null;
	
	private List<Feed> feeds = null;
	
	public Curator(String url) {
		
		super();
		
		this.url = url;		
	}

	@RdfProperty("http://purl.org/dc/elements/1.1/title")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Uri
	public String getUrl() {
		return url;
	}

	/* (non-Javadoc)
	 * @see org.creativecommons.learn.oercloud.IRdfMapped#setUrl(java.lang.String)
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	public List<Feed> getFeeds() {
		return this.feeds;
	}

	public void setFeeds(List<Feed> feedList) {
		this.feeds = feedList;
	}
	
}
