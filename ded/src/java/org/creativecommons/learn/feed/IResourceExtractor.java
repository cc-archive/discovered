package org.creativecommons.learn.feed;

import java.net.URISyntaxException;

import org.creativecommons.learn.oercloud.Feed;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Record;

public interface IResourceExtractor {

	public void process(Feed feed, Record record, String identifier) throws OAIException, URISyntaxException;
	
}
