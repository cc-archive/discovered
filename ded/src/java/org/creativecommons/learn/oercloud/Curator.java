package org.creativecommons.learn.oercloud;

import java.util.Collection;
import java.util.List;

import org.creativecommons.learn.TripleStore;

import thewebsemantic.Namespace;
import thewebsemantic.RdfProperty;
import thewebsemantic.Sparql;
import thewebsemantic.Uri;

@Namespace("http://learn.creativecommons.org/ns#")
public class Curator {

	private String url = null;
	private String name = null;
		
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
	
	public Collection<Feed> getFeeds() {
		
		String query = ""
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
			+ "PREFIX cclearn: <http://learn.creativecommons.org/ns#> \n"
			+ "\n" + "SELECT ?s \n" + "WHERE { \n"
			+ "?s rdf:type cclearn:Feed .\n"
			+ "?s cclearn:hasCurator <" + this.getUrl() + ">. \n"
			+ "   }\n";
		
		try {
			return Sparql.exec(TripleStore.get().getModel(), Feed.class, query);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return null;
	}
	
	public String getSource() {
		return "http://creativecommons.org/#site-configuration";
	}
	
}
