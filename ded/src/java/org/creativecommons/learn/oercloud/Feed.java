package org.creativecommons.learn.oercloud;

import org.creativecommons.learn.RdfStore;

import java.util.Collection;
import java.util.Date;

import thewebsemantic.Namespace;
import thewebsemantic.RdfProperty;
import thewebsemantic.Sparql;
import thewebsemantic.Uri;

@Namespace("http://learn.creativecommons.org/ns#")
public class Feed {

	private Curator curator = null;
	private String url = null;
	private String feedType = null;
	private Date lastImport = new Date(0);
	
	public Feed(String url) {
		this.url = url;
	}

	@RdfProperty("http://learn.creativecommons.org/ns#hasCurator")
	public Curator getCurator() {
		return curator;
	}

	public void setCurator(Curator curator) {
		this.curator = curator;
	}

	@Uri
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@RdfProperty("http://learn.creativecommons.org/ns#feedType")
	public String getFeedType() {
		return feedType;
	}

	public void setFeedType(String feedType) {
		this.feedType = feedType;
	}
	
	@RdfProperty("http://learn.creativecommons.org/ns#lastImportDate")
	public Date getLastImport() {
		return lastImport;
	}
	
	public void setLastImport(Date lastImport) {
		this.lastImport = lastImport;
	}

	public Collection<Resource> getResources() {
		
		String query = ""
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
			+ "PREFIX cclearn: <http://learn.creativecommons.org/ns#> \n"
			+ "\n" + "SELECT ?s \n" + "WHERE { \n"
			+ "?s rdf:type cclearn:Resource .\n"
			+ "?s cclearn:source <" + this.getUrl() + ">. \n"
			+ "   }\n";
		
		return Sparql.exec(RdfStore.forDEd().getModel(), Resource.class, query);
		
	}
	
	public String getSource() {
		return RdfStore.SITE_CONFIG_URI; 
	}
	
}
