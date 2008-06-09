package org.creativecommons.learn.aggregate.oaipmh;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.creativecommons.learn.TripleStore;
import org.creativecommons.learn.feed.IResourceExtractor;
import org.creativecommons.learn.oercloud.Resource;
import org.dom4j.Element;
import org.dom4j.Node;

import se.kb.oai.pmh.MetadataFormat;
import thewebsemantic.NotFoundException;

public abstract class OaiMetadataFormat implements IResourceExtractor{

	protected MetadataFormat format;

	public OaiMetadataFormat(MetadataFormat f) {
		super();

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

	protected String getNodeText(Element context, String xpath) {
		List<?> identifiers = context.selectNodes(xpath);
		if (identifiers.size() < 1) return null;
		return ((Node) identifiers.get(0)).getText();
	}

	protected Collection<String> getNodesText(Element context, String xpath) {
		
		Vector<String> nodes = new Vector<String>();
		
		List<Node> items = context.selectNodes(xpath);
		
		for (Node item : items) {
			nodes.add(item.getText());
		}
		
		return nodes;
	}

}