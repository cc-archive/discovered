package org.creativecommons.learn.aggregate.oaipmh;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.creativecommons.learn.TripleStore;
import org.creativecommons.learn.feed.IResourceExtractor;
import org.creativecommons.learn.oercloud.Feed;
import org.creativecommons.learn.oercloud.Resource;
import org.dom4j.Element;
import org.dom4j.Node;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.MetadataFormat;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import thewebsemantic.NotFoundException;

public class OaiDcMetadata implements IResourceExtractor{

/*
	    <element ref="dc:title"/>
	    <element ref="dc:creator"/>
	    <element ref="dc:subject"/>
	    <element ref="dc:description"/>
	    <element ref="dc:publisher"/>
	    <element ref="dc:contributor"/>
	    <element ref="dc:date"/>
	    <element ref="dc:type"/>
	    <element ref="dc:format"/>
	    <element ref="dc:identifier"/>
	    <element ref="dc:source"/>
	    <element ref="dc:language"/>
	    <element ref="dc:relation"/>
	    <element ref="dc:coverage"/>
	    <element ref="dc:rights"/>
*/

	private MetadataFormat format;

	public OaiDcMetadata(MetadataFormat f) {
		
		this.format = f; 

	}

	public Resource getResource(String url) {
		
		Resource result = null;
		
		if (TripleStore.get().exists(Resource.class, url)) {
			try {
				result = TripleStore.get().load(Resource.class, url);
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			result = new Resource(url);
		}
		
		return result;
	}
	
	private String getNodeText(Element context, String element_name) {
		List<?> identifiers = context.selectNodes("//" + element_name);
		if (identifiers.size() < 1) return null;
		return ((Node) identifiers.get(0)).getText();
	}

	private Collection<String> getNodes(Element context, String element_name) {
		
		Vector<String> nodes = new Vector<String>();
		
		List<Node> items = context.selectNodes("//" + element_name);
		
		for (Node item : items) {
			nodes.add(item.getText());
		}
		
		return nodes;
	}

	@Override
	public void process(Feed feed, OaiPmhServer server, String identifier) throws OAIException {
		
		// Retrieve the resource metadata from the server
		Record oai_record = server.getRecord(identifier, this.format.getPrefix());
		Element metadata = oai_record.getMetadata();
		
		// get the item URL (dc:identifier)
		String DC = metadata.getNamespaceForURI("http://purl.org/dc/elements/1.1/").getPrefix();
				
		// load or create the Resource object
		Resource item = getResource(getNodeText(metadata, DC + ":identifier"));
		item.getSources().add(feed);

		// title
		item.setTitle(getNodeText(metadata, DC + ":title"));
		
		// creator
		item.getCreators().addAll(getNodes(metadata, DC + ":creator"));
		
		// subject(s)
		item.getSubjects().addAll(getNodes(metadata, DC + ":subject"));
		
		// description
		item.setDescription(getNodeText(metadata, DC + ":description"));
		
		// contributor(s)
		item.getContributors().addAll(getNodes(metadata, DC + ":contributor"));
		
		// format(s)
		item.getFormats().addAll(getNodes(metadata, DC + ":format"));
		
		// language(s)
		item.getLanguages().addAll(getNodes(metadata, DC + ":language"));
		
		// type(s)
		item.getTypes().addAll(getNodes(metadata, DC + ":type"));
		
		// persist the Resource
		TripleStore.get().save(item);
	}

}
