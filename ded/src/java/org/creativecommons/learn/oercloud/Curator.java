package org.creativecommons.learn.oercloud;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.RdfStoreFactory;

import thewebsemantic.Namespace;
import thewebsemantic.RdfProperty;
import thewebsemantic.Sparql;
import thewebsemantic.Id;

@Namespace("http://learn.creativecommons.org/ns#")
public class Curator {

	private URI uri = null;
	private String name = null;
		
	public Curator(String url) throws URISyntaxException {
		this.uri = new URI(url);		
	}
	
	public Curator(URI uri) {
		this.uri = uri;
	}
	
	public static Curator getByUrl(RdfStore store, String url) {
		return null;
	}

	@RdfProperty("http://purl.org/dc/elements/1.1/title")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Deprecated
	public String getUrl() {
		return uri.toString();
	}

	@Id
	public URI getUri() {
		return uri;
	}
	
	public void setUri(URI uri) {
		this.uri = uri;
	}
	
	public Collection<Feed> getFeeds() {
		
		String query = ""
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
			+ "PREFIX cclearn: <http://learn.creativecommons.org/ns#> \n"
			+ "\n" + "SELECT ?s \n" + "WHERE { \n"
			+ "?s rdf:type cclearn:Feed .\n"
			+ "?s cclearn:hasCurator <" + this.getUrl() + ">. \n"
			+ "   }\n";
		
		return Sparql.exec(RdfStoreFactory.get().forDEd().getModel(), Feed.class, query);
	
	}
	
}
