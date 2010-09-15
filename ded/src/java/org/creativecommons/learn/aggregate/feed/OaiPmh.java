package org.creativecommons.learn.aggregate.feed;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

	private Map<MetadataFormat, IResourceExtractor> getFormats(
			OaiPmhServer server) {

		Map<MetadataFormat, IResourceExtractor> result = new HashMap<MetadataFormat, IResourceExtractor>();

		MetadataFormatsList formats;
		try {
			formats = server.listMetadataFormats();
		} catch (OAIException e) {
			// Well, I guess we can't have a list of formats.
			// Returning the empty list.
			return result;
		}
			
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

		Map<MetadataFormat, IResourceExtractor> formats;
		Map<String, String> sets;

		// get a list of formats supported by both the server and our aggregator
		formats = getFormats(server);

		// get a list of sets supported by the server and map them to their
		// names
		sets = getSets(server);

		// For each metadata format that we support, get out all the records
		// the server has.
		
		// This also creates any missing OaiResource objects missing from
		// the RdfStore.
		for (Entry<MetadataFormat, IResourceExtractor> entry : formats.entrySet()) {
			MetadataFormat format = entry.getKey();
			IResourceExtractor extractor = entry.getValue();
			
			OaiPmhRecordIterator recordIterator = new OaiPmhRecordIterator(server,
					format, feed.getLastImport());
			
			while (recordIterator.hasNext()) {
				Record record = recordIterator.next();
				Header header = record.getHeader();
				URI resourceURI = URI.create(header.getIdentifier());
				LOG.info("Slurping in " + resourceURI.toString() + " with format " + format.toString());
				
				// create the OaiResource if needed
				OaiResource resource = null;
				try {
					resource = store.load(OaiResource.class, resourceURI.toString());
				} catch (NotFoundException e) {
					resource = new OaiResource(resourceURI.toString());
				}
	
				// add the set as a subject heading
				for (String set_spec : header.getSetSpecs()) {
					if (sets.containsKey(set_spec)) {
						resource.getSubjects().add(sets.get(set_spec));
					}
				}
				
				// Now that we have created the Resource and added the sets
				// as subjects, save it in the store.
				store.save(resource);

				try {
					extractor.process(feed, record, resourceURI.toString());
				} catch (URISyntaxException e) {
					// Well, I know the URI is valid because resourceURI is already
					// a java.net.URI object.
					
					// Given that, we should refactor IResourceExtractor to take
					// a java.net.URI.
					
					// For now, if this occurs, blow up aggregation.
					throw new RuntimeException(e);
				} catch (OAIException e) {
					// Oh, well, we failed to process the feed + record and extract
					// information from it. We can presume this failed for some reason
					// having to do with the server.
					
					// If things go well, when we aggregate again next time, we'll pick
					// up those data anyway. So we just squelch the exception.
				}
			} // while more results
				
		} // for each format...
	} // public void poll

	public static void main(String[] args) {
		OaiPmh instance = new OaiPmh();
		instance.poll(null);
	} // public static void main

}
