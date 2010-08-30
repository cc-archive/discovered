package org.creativecommons.learn.aggregate.oaipmh;

import java.net.URISyntaxException;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.RdfStoreFactory;
import org.creativecommons.learn.feed.IResourceExtractor;
import org.creativecommons.learn.oercloud.Feed;
import org.creativecommons.learn.oercloud.OaiResource;
import org.creativecommons.learn.oercloud.Resource;
import org.dom4j.Element;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.MetadataFormat;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;
import thewebsemantic.NotFoundException;

public class OerRecommender extends OaiMetadataFormat implements IResourceExtractor {

	public static String OERR = "oerr";
	public static String OERR_URL = "http://www.oercommons.org/oerr";
	
	public OerRecommender(MetadataFormat f) {

		super(f);
		
	}

	@Override
	public void process(Feed feed, Record oai_record, String identifier) throws OAIException, URISyntaxException {
		RdfStore store = RdfStoreFactory.get().forProvenance(feed.getUri().toString()); 

		Element metadata = oai_record.getMetadata();
		if (metadata == null) return;
		
		metadata.addNamespace(OERR, OERR_URL);

		// get a handle to the resource
		Resource resource = this.getResource(this.getNodeText(metadata, "//oerr:url"),
				store);
		
		// add source information
		resource.getSources().add(feed);
		
		// title, description
		resource.setTitle(this.getNodeText(metadata, "//oerr:title"));
		resource.setDescription(this.getNodeText(metadata, "//oerr:abstract"));
		
		// keywords / subjects
		resource.getSubjects().addAll(
				this.getNodesText(metadata, "//oerr:keywords/oerr:keyword"));
		
		// see also
		try {
			resource.getSeeAlso().add(RdfStoreFactory.get().forDEd().load(OaiResource.class, identifier));
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		store.save(resource);

	}

}
