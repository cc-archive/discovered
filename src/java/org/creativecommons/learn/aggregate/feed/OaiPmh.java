package org.creativecommons.learn.aggregate.feed;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.creativecommons.learn.aggregate.oaipmh.OaiDcMetadata;
import org.creativecommons.learn.aggregate.oaipmh.OerRecommender;
import org.creativecommons.learn.aggregate.oaipmh.OerSubmissions;
import org.creativecommons.learn.feed.IResourceExtractor;
import org.creativecommons.learn.oercloud.Feed;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.Header;
import se.kb.oai.pmh.IdentifiersList;
import se.kb.oai.pmh.MetadataFormat;
import se.kb.oai.pmh.MetadataFormatsList;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Set;
import se.kb.oai.pmh.SetsList;

/**
 * 
 * @author nathan
 */
public class OaiPmh {

	private final SimpleDateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd");

	protected Map<MetadataFormat, IResourceExtractor> getFormats(OaiPmhServer server) throws OAIException {

		Map<MetadataFormat, IResourceExtractor> result = new HashMap<MetadataFormat, IResourceExtractor>();
		
		MetadataFormatsList formats = server.listMetadataFormats();
		for (MetadataFormat f : formats.asList()) {
		
			if (f.getSchema().equals("http://www.openarchives.org/OAI/2.0/oai_dc.xsd"))
				result.put(f, new OaiDcMetadata(f));

			if (f.getSchema().equals("http://www.oercommons.org/oerr.xsd"));
				//result.put(f, new OerRecommender(f));

			if (f.getSchema().equals("http://www.oercommons.org/oers.xsd"));
				//result.put(f, new OerSubmissions(f));

			// oai_lom : http://ltsc.ieee.org/xsd/lomv1.0/lom.xsd
		}
		
		return result;
	}

	public void poll(Feed feed) {

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -5);

		System.out.println("foo");
		OaiPmhServer server = new OaiPmhServer("http://oercommons.org/oai");

		try {
			Map<MetadataFormat, IResourceExtractor> formats = getFormats(server);
			SetsList sets = server.listSets();

			for (MetadataFormat f : formats.keySet()) {

				for (Set s : sets.asList()) {

					IdentifiersList identifiers = server.listIdentifiers(f
							.getPrefix(), iso8601.format(calendar.getTime()),
							null, s.toString());

					for (Header header : identifiers.asList()) {

						System.out.println(header.getIdentifier());
						
						// look up the extractor for this format
						formats.get(f).process(feed, server, header.getIdentifier());

						// System.exit(1);
					}

					// XXX Resumption Tokens!

				}
			}

			System.out.println(sets.getResumptionToken().toString());

		} catch (OAIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
    public static void main(String[] args) {
		OaiPmh instance = new OaiPmh();
		instance.poll(null);
	}

}
