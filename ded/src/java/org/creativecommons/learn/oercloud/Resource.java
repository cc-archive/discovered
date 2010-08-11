package org.creativecommons.learn.oercloud;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.RdfStoreFactory;

import thewebsemantic.Namespace;
import thewebsemantic.NotFoundException;
import thewebsemantic.RdfProperty;
import thewebsemantic.Uri;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

@Namespace("http://learn.creativecommons.org/ns#")
public class Resource implements IExtensibleResource {

    private URI url = null;
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
    private HashMap<Property, HashSet<RDFNode>> fields = new HashMap<Property, HashSet<RDFNode>>();

    public Resource(URI url) {
        this.url = url;
    }
    
    public Resource(String url) throws URISyntaxException {
        this.url = new URI(url);
    }

    /* XXX Holy Jesus, this thing is going to be inefficient. */
    public Set<String> getAllCuratorURIs() {
    	HashSet<String> ret = new HashSet<String>();
        for (String provURI: RdfStoreFactory.get().getAllKnownTripleStoreUris()) {
        	RdfStore store = RdfStoreFactory.get().forProvenance(provURI);
        	try {
        		Resource thing = store.load(Resource.class, this.getUrl());
        		if (thing != null) {
        			// okay, so the provenance URI we got was probably the feed.
        			// in that case, look for a curator.
        			Feed f = RdfStoreFactory.get().forDEd().load(Feed.class, provURI);
        			String curatorURI = f.getCurator().getUrl();        		
        			ret.add(curatorURI);
        		}
        	} catch (NotFoundException nfe) {
        		// in that case, simply do not add this provenance.
        	}
        }
        return ret;
    }

    @Deprecated
    public String getUrl() {
        return url.toString();
    }

    @Uri
    public URI getUri() {
        return url;
    }

    public void setUri(URI url) {
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
     * (non-Javadoc)
     * 
     * @see
     * org.creativecommons.learn.oercloud.IExtensibleResource#addField(com.hp
     * .hpl.jena.graph.Node_URI, com.hp.hpl.jena.graph.Node)
     */
    public void addField(Property predicate, RDFNode object) {
        if (!this.fields.containsKey(predicate)) {
            this.fields.put(predicate, new HashSet<RDFNode>());
        }

        this.fields.get(predicate).add(object);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.creativecommons.learn.oercloud.IExtensibleResource#getFieldValues
     * (com.hp.hpl.jena.graph.Node_URI)
     */
    public Set<RDFNode> getFieldValues(Property predicate) {

        if (this.fields.containsKey(predicate))
            return this.fields.get(predicate);

        return null;
    }

    public HashMap<Property, HashSet<RDFNode>> getFields() {

        return this.fields;
    }
}
