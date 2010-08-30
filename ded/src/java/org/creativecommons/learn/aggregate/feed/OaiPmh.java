package org.creativecommons.learn.aggregate.feed;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.creativecommons.learn.RdfStore;
import org.creativecommons.learn.RdfStoreFactory;
import org.creativecommons.learn.aggregate.oaipmh.NsdlDc;
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
import se.kb.oai.pmh.Record;
import se.kb.oai.pmh.RecordsList;
import se.kb.oai.pmh.Set;
import se.kb.oai.pmh.SetsList;
import thewebsemantic.NotFoundException;

/**
 * 
 * @author nathan
 */
public class OaiPmh {

	private Logger LOG = Logger.getLogger(OaiPmh.class.getName());

	private final SimpleDateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd");

	protected Map<MetadataFormat, IResourceExtractor> getFormats(
			OaiPmhServer server) throws OAIException {

		Map<MetadataFormat, IResourceExtractor> result = new HashMap<MetadataFormat, IResourceExtractor>();

		MetadataFormatsList formats = server.listMetadataFormats();
		for (MetadataFormat f : formats.asList()) {

			if (f.getSchema().equals("http://www.oercommons.org/oerr.xsd"))
				result.put(f, new OerRecommender(f));

			if (f.getSchema().equals(
					"http://www.openarchives.org/OAI/2.0/oai_dc.xsd"))
				result.put(f, new OaiDcMetadata(f));

			if (f.getSchema().equals("http://www.oercommons.org/oers.xsd"))
				result.put(f, new OerSubmissions(f));

			if (f.getSchema().equals("http://ns.nsdl.org/schemas/nsdl_dc/nsdl_dc_v1.02.xsd"))
				result.put(f, new NsdlDc(f));

			// oai_lom : http://ltsc.ieee.org/xsd/lomv1.0/lom.xsd
		}

		return result;
	}

	private Map<String, String> getSets(OaiPmhServer server) {

		Map<String, String> raw_setmap = new HashMap<String, String>();
		Map<String, String> sets = new HashMap<String, String>();

		Boolean moreSets = true;

		try {
			SetsList serversets = server.listSets();

			while (moreSets) {
				for (Set s : serversets.asList()) {
					raw_setmap.put(s.getSpec(), s.getName());
				}

				// check for resumption token...
				if (serversets.getResumptionToken() != null) {
					serversets = server.listSets(serversets
							.getResumptionToken());
					moreSets = true;
				} else {
					moreSets = false;
				}

			} // while more set specs may be retrieved

		} catch (OAIException e) {
			// Yeah, we probably don't support sets
			return sets;
		}

		// post-process to handle hierarchical sets
		for (String set_spec : raw_setmap.keySet()) {

			String[] spec_pieces = set_spec.split(":");
			StringBuilder partial_spec = new StringBuilder();
			StringBuilder name = new StringBuilder();

			for (int i = 0; i < spec_pieces.length; i++) {

				if (partial_spec.length() > 0)
					partial_spec.append(":");
				partial_spec.append(spec_pieces[i]);

				if (raw_setmap.containsKey(partial_spec.toString())) {
					name.append(raw_setmap.get(partial_spec.toString()));
					if (i < spec_pieces.length - 1) {
						name.append(": ");
					}

				} else {
					name.append(raw_setmap.get(spec_pieces[i]));
				}
			}

			if (name.toString() == null) {
				System.out.println(set_spec);
				throw new NullPointerException();
			}
			sets.put(set_spec, name.toString());

		} // for each set specification

		return sets;
	}

	public void poll(Feed feed) {
		this.poll(feed, false);
	}
	
	public void poll(Feed feed, boolean force) {
		RdfStore store = RdfStoreFactory.get().forProvenance(feed.getUri().toString());
		OaiPmhServer server = new OaiPmhServer(feed.getUri().toString());

		Boolean moreResults = true;
		Map<MetadataFormat, IResourceExtractor> formats;
		Map<String, String> sets;

		// get a list of formats supported by both the server and our aggregator
		try {
			formats = getFormats(server);
		} catch (OAIException e) {
			return;
		}

		// get a list of sets supported by the server and map them to their
		// names
		sets = getSets(server);

		// get the formatted date of the last import
		String last_import_date = null;
		if (!force) 
			last_import_date = iso8601.format(feed.getLastImport());
		
		// For each metadata format that we support, get out all the records
		// the server has:
		for (MetadataFormat f : formats.keySet()) {

			List<Record> records = null;
			try {
				
				records = server.listRecords(f.getPrefix(), 
						last_import_date, null, null).asList();
			} catch (OAIException e) {
				// Well, uh, if we hit an OAIException getting the list of records
				// available with this metadata format, then there is nothing we
				// can do. So then we skip this metadata format.
				continue;
			}

			for (Record record : records) {
				Header header = record.getHeader();
				// create the OaiResource if needed
				OaiResource resource = null;
				try {
					resource = store.load(
							OaiResource.class, header.getIdentifier());
					} catch (NotFoundException e) {
						resource = new OaiResource(header.getIdentifier());
				}

					// add the set as a subject heading
				for (String set_spec : header.getSetSpecs()) {
					if (sets.containsKey(set_spec)) {
						resource.getSubjects().add(sets.get(set_spec));
					}
				}
				try {
					store.save(resource);
				} catch (NullPointerException e) {
					System.out.println(resource);
					System.out.println(resource.getId());
					System.out.println();
					for (String foo : resource.getSubjects()) {
						System.out.println(foo);
					}

					throw e;
				}

				// look up the extractor for this format
				try {
					formats.get(f).process(feed, record,
							header.getIdentifier());
				} catch (OAIException e) {
					e.printStackTrace();
					continue;
				} catch (Exception e) {
					LOG.warning("An exception occured while aggregating " + f.getPrefix() + " for " + header.getIdentifier());
					LOG.warning("> " + e.getMessage());
					e.printStackTrace();
				}
			} // while more results...

		} // for each format...
	} // public void poll

	public static void main(String[] args) {
		OaiPmh instance = new OaiPmh();
		instance.poll(null);
	} // public static void main

}
