package org.creativecommons.learn;

// JDK import
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.parse.Parse;
import org.creativecommons.learn.oercloud.Feed;
import org.creativecommons.learn.oercloud.Resource;

import thewebsemantic.NotFoundException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class CuratorIndexer implements IndexingFilter {

	public static final Log LOG = LogFactory.getLog(CuratorIndexer.class
			.getName());

	private Configuration conf;

	public CuratorIndexer() {
		LOG.info("Created CuratorIndexer.");
	}

	public Document filter(Document doc, Parse parse, Text url,
			CrawlDatum datum, Inlinks inlinks) throws IndexingException {

		// add the source information
		try {
			Resource this_doc = TripleStore.get().load(Resource.class, url.toString());
			
			for (Feed source : this_doc.getSources()) {
				
				Field sourceField = new Field(Search.FEED_FIELD, source.getUrl(),
						Field.Store.YES, Field.Index.TOKENIZED);
				sourceField.setBoost(Search.FEED_BOOST);
				doc.add(sourceField);

				// if this feed has curator information attached, index it as well
				String curator_url = "";
				if (source.getCurator() != null) {
					curator_url = source.getCurator().getUrl();
				}

				Field curator = new Field(Search.CURATOR_FIELD, curator_url,
						Field.Store.YES, Field.Index.TOKENIZED);
				curator.setBoost(Search.CURATOR_BOOST);
				doc.add(curator);
				
			}

		} catch (NotFoundException e) {
			
			// no information on this resource...
		}
		
		return doc;
	} // public Document filter

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return this.conf;
	}

} // CuratorIndexer
