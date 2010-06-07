package org.creativecommons.learn.oercloud;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Vector;

import net.rootdev.javardfa.JenaStatementSink;
import net.rootdev.javardfa.ParserFactory;
import net.rootdev.javardfa.StatementSink;
import net.rootdev.javardfa.ParserFactory.Format;

import org.creativecommons.learn.RdfStore;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import thewebsemantic.Namespace;
import thewebsemantic.RdfProperty;
import thewebsemantic.Uri;

@Namespace("http://learn.creativecommons.org/ns#")
public class Resource {

	private String url = null;
	private String title = null;
	private String description = null;
	private Collection<String> subjects = new Vector<String>();
	private Collection<String> creators = new Vector<String>();
	
	private Collection<Feed> sources = new Vector<Feed>();

	private Collection<String> types = new Vector<String>();
	private Collection<String> edlevels = new Vector<String>();
	private Collection<String> formats = new Vector<String>();
	private Collection<String> contributors = new Vector<String>();
	private Collection<String> languages = new Vector<String>();
	
	private Collection<OaiResource> seeAlso = new Vector<OaiResource>();
	
	public Resource(String url) {
		this.url = url;
	}
	
	@Uri
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	@RdfProperty("http://purl.org/dc/elements/1.1/title")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@RdfProperty("http://purl.org/dc/elements/1.1/description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@RdfProperty("http://purl.org/dc/elements/1.1/subject")
	public Collection<String> getSubjects() {
		return subjects;
	}

	public void setSubjects(Collection<String> subjects) {
		this.subjects = subjects;
	}

	@RdfProperty("http://learn.creativecommons.org/ns#educationLevel")
	public Collection<String> getEducationLevels() {
		return edlevels;
	}
	
	public void setEducationLevels(Collection<String> levels) {
		this.edlevels = levels;
	}
	
	@RdfProperty("http://purl.org/dc/elements/1.1/type")
	public Collection<String> getTypes() {
		return types;
	}

	public void setTypes(Collection<String> types) {
		this.types = types;
	}

	@RdfProperty("http://purl.org/dc/elements/1.1/format")
	public Collection<String> getFormats() {
		return formats;
	}

	public void setFormats(Collection<String> formats) {
		this.formats = formats;
	}

	@RdfProperty("http://purl.org/dc/elements/1.1/contributor")
	public Collection<String> getContributors() {
		return contributors;
	}

	public void setContributors(Collection<String> contributors) {
		this.contributors = contributors;
	}

	@RdfProperty("http://learn.creativecommons.org/ns#source")
	public Collection<Feed> getSources() {
		return sources;
	}

	public void setSources(Collection<Feed> sources) {
		this.sources = sources;
	}
	
	@RdfProperty("http://purl.org/dc/elements/1.1/creator")
	public Collection<String> getCreators() {
		return creators;
	}

	public void setCreators(Collection<String> creators) {
		this.creators = creators;
	}

	@RdfProperty("http://purl.org/dc/elements/1.1/language")
	public Collection<String> getLanguages() {
		return languages;
	}

	public void setLanguages(Collection<String> languages) {
		this.languages = languages;
	}

	@RdfProperty("http://www.w3.org/2000/01/rdf-schema#seeAlso")
	public Collection<OaiResource> getSeeAlso() {
		return seeAlso;
	}

	public void setSeeAlso(Collection<OaiResource> seeAlso) {
		this.seeAlso = seeAlso;
	}

	/*
	 * GET a URI, e.g., http://example.com/, inspect it for RDFa, convert that
	 * RDFa into triples, and say that
	 * "these triples come from http://example.com/".
	 * 
	 * FIXME: By doing things this way -- looking up the URI here, as opposed to
	 * re-using any HTML we already GET'ted, we are hitting the network once per
	 * call to this function -- more than we need to.
	 * 
	 * FIXME: At time of writing, this method is only called by
	 * FeedUpdater.addEntry, so only entries in RSS feeds will get their RDFa
	 * parsed. OAI-PMH feeds are left out, for example.
	 */
	public static void parseURIForRDFa(String uri) {
		
		System.err.println("parseURIForRDFa called");
		
		RdfStore store = null;
		// FIXME: When we test this, it turns out using the format "XHTML"
		// works. If we fail to find triples inside RDFa-bearing files, this is
		// one line of code that might be the cause of the problem.
        Format format = Format.XHTML;
        StatementSink sink = null;
        XMLReader reader = null;
        
		try {
			store = RdfStore.uri2RdfStore(uri);
			sink = new JenaStatementSink(store.getModel());
			reader = ParserFactory.createReaderForFormat(sink, format);
			reader.parse(uri);
		}
		catch (SQLException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		catch (SAXException e) { e.printStackTrace(); }
		catch (ClassNotFoundException e) { e.printStackTrace(); }
	}
}

