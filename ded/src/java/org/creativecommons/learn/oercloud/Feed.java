package org.creativecommons.learn.oercloud;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;

import org.creativecommons.learn.RdfStoreFactory;

import thewebsemantic.Namespace;
import thewebsemantic.RdfProperty;
import thewebsemantic.Sparql;
import thewebsemantic.Uri;

@Namespace("http://learn.creativecommons.org/ns#")
public class Feed {

	private Curator curator = null;
	private URI uri = null;
	private String feedType = null;
	private Date lastImport = new Date(0);
	
	public Feed(String url) throws URISyntaxException {
		this.uri = new URI(url);
	}

	public Feed(URI uri) {
		this.uri = uri;
	}

	@RdfProperty("http://learn.creativecommons.org/ns#hasCurator")
	public Curator getCurator() {
		return curator;
	}

	public void setCurator(Curator curator) {
		this.curator = curator;
	}

	@Deprecated
	public String getUrl() {
		return uri.toString();
	}

	@Uri
	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
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
		
		return Sparql.exec(RdfStoreFactory.get().forDEd().getModel(), Resource.class, query);
		
	}
	
	public String getSource() {
		return RdfStoreFactory.SITE_CONFIG_URI; 
	}
	
}
