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
import org.creativecommons.learn.aggregate.handlers.CCLEARN;
import org.creativecommons.learn.aggregate.handlers.TripleStore;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class ResourceSourceIndexer implements IndexingFilter {

	public static final Log LOG = LogFactory.getLog(ResourceSourceIndexer.class
			.getName());

	private Configuration conf;

	public ResourceSourceIndexer() {
		LOG.info("Created ResourceSourceIndexer.");
	}

	public Document filter(Document doc, Parse parse, Text url,
			CrawlDatum datum, Inlinks inlinks) throws IndexingException {

		// add the source information
		try {
			Model ts = new TripleStore().getModel();
			
			NodeIterator sources = ts.listObjectsOfProperty(ts.createResource(url.toString()), 
					CCLEARN.source);
			while (sources.hasNext()) {
				RDFNode source = sources.nextNode();
				
				Field sourceField = new Field(Search.SOURCE_FIELD, source.toString(),
						Field.Store.YES, Field.Index.TOKENIZED);
				sourceField.setBoost(Search.SOURCE_BOOST);

				doc.add(sourceField);
				
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return doc;
	} // public Document filter

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return this.conf;
	}

} // ResourceSourceIndexer
