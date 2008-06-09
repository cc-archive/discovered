package org.creativecommons.learn.oercloud;

import java.util.Collection;
import java.util.Vector;

import thewebsemantic.Id;
import thewebsemantic.Namespace;
import thewebsemantic.RdfProperty;

@Namespace("http://learn.creativecommons.org/ns#")
public class OaiResource {

	private String id;
	private Collection<String> subjects = new Vector<String>();

	public OaiResource(String id) {
		this.id = id;
	}
	
	@Id
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@RdfProperty("http://purl.org/dc/elements/1.1/subject")
	public Collection<String> getSubjects() {
		return subjects;
	}

	public void setSubjects(Collection<String> subjects) {
		this.subjects = subjects;
	}
	
}
