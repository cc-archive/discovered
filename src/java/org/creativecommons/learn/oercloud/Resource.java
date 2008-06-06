package org.creativecommons.learn.oercloud;

import java.util.Collection;
import java.util.Vector;

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
	private Collection<String> formats = new Vector<String>();
	private Collection<String> contributors = new Vector<String>();
	private Collection<String> languages = new Vector<String>();
	
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

}

