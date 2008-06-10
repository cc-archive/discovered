package org.creativecommons.learn.aggregate.feed;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.creativecommons.learn.TripleStore;
import org.creativecommons.learn.aggregate.oaipmh.OaiDcMetadata;
import org.creativecommons.learn.aggregate.oaipmh.OerRecommender;
import org.creativecommons.learn.aggregate.oaipmh.OerSubmissions;
import org.creativecommons.learn.feed.IResourceExtractor;
import org.creativecommons.learn.oercloud.Feed;
import org.creativecommons.learn.oercloud.OaiResource;

import se.kb.oai.OAIException;
import se.kb.oai.pmh.Header;
import se.kb.oai.pmh.IdentifiersList;
import se.kb.oai.pmh.MetadataFormat;
import se.kb.oai.pmh.MetadataFormatsList;
import se.kb.oai.pmh.OaiPmhServer;
import se.kb.oai.pmh.Set;
import se.kb.oai.pmh.SetsList;
import thewebsemantic.NotFoundException;

/**
 * 
 * @author nathan
 */
public class OaiPmh {

	private final SimpleDateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd");

	protected Map<MetadataFormat, IResourceExtractor> getFormats(
			OaiPmhServer server) throws OAIException {

		Map<MetadataFormat, IResourceExtractor> result = new HashMap<MetadataFormat, IResourceExtractor>();

		MetadataFormatsList formats = server.listMetadataFormats();
		for (MetadataFormat f : formats.asList()) {

			if (f.getSchema().equals(
					"http://www.openarchives.org/OAI/2.0/oai_dc.xsd"))
				result.put(f, new OaiDcMetadata(f));

			if (f.getSchema().equals("http://www.oercommons.org/oerr.xsd"))
				result.put(f, new OerRecommender(f));

			if (f.getSchema().equals("http://www.oercommons.org/oers.xsd"))
				result.put(f, new OerSubmissions(f));

			// oai_lom : http://ltsc.ieee.org/xsd/lomv1.0/lom.xsd
		}

		return result;
	}

	public void poll(Feed feed) {

		Boolean moreResults = true;
		Boolean moreSets = true;
		OaiPmhServer server = new OaiPmhServer("http://oercommons.org/oai");
		IdentifiersList identifiers = null;

		Map<MetadataFormat, IResourceExtractor> formats;
		SetsList sets = null;

		try {
			formats = getFormats(server);
		} catch (OAIException e) {
			return;
		}

		for (MetadataFormat f : formats.keySet()) {

			try {
				sets = server.listSets();
				moreSets = true;
			} catch (OAIException e1) {
				moreSets = false;
			}

			while (moreSets) {
				
				for (Set s : sets.asList()) {

					System.out.println("Processing " + s.getSpec() + " ("
							+ s.getName() + ")");

					moreResults = true;

					try {
						identifiers = server.listIdentifiers(f.getPrefix(),
								iso8601.format(feed.getLastImport()), null, s
										.toString());
					} catch (OAIException e) {
						continue;
					}

					while (moreResults) {

						for (Header header : identifiers.asList()) {

							System.out.println(header.getIdentifier());

							// create the OaiResource if needed
							OaiResource resource = null;
							if (TripleStore.get().exists(OaiResource.class,
									header.getIdentifier())) {
								try {
									resource = TripleStore.get().load(
											OaiResource.class,
											header.getIdentifier());
								} catch (NotFoundException e) {
								}
							} else {
								resource = new OaiResource(header
										.getIdentifier());
							}

							// add the set as a subject heading
							resource.getSubjects().add(s.getName());
							TripleStore.get().save(resource);

							// look up the extractor for this format
							try {
								formats.get(f).process(feed, server,
										header.getIdentifier());
							} catch (OAIException e) {
								e.printStackTrace();
								continue;
							}

						}

						// Resumption Token handling
						if (identifiers.getResumptionToken() != null) {
							// continue...
							try {
								identifiers = server
										.listIdentifiers(identifiers
												.getResumptionToken());
								moreResults = true;
							} catch (OAIException e) {
								e.printStackTrace();
								moreResults = false;
							}
						} else {
							moreResults = false;
						}
					} // while more results...
				} // for each set
				
				if (sets.getResumptionToken() != null) {
					// continue...
					try {
						sets = server.listSets(sets.getResumptionToken());
					} catch (OAIException e) {
						moreSets = false;
					}
					moreSets = true;
				} else {
					moreSets = false;
				}
			} // while more sets...
		}

	}

	public static void main(String[] args) {
		OaiPmh instance = new OaiPmh();
		instance.poll(null);
	}

}
