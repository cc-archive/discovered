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
import org.creativecommons.learn.oercloud.ObjectMgr;

public class ResourceSourceIndexer implements IndexingFilter {

	public static final Log LOG = LogFactory.getLog(ResourceSourceIndexer.class
			.getName());

	private Configuration conf;

	public ResourceSourceIndexer() {
		LOG.info("Created ResourceSourceIndexer.");
	}

	public Document filter(Document doc, Parse parse, Text url,
			CrawlDatum datum, Inlinks inlinks) throws IndexingException {

		return doc;
		/*
		// add the source information
		String source = ObjectMgr.get().getBookmark(url.toString()).getUser().getName();

		if (source != null) {
			Field sourceField = new Field(Search.SOURCE_FIELD, source,
					Field.Store.YES, Field.Index.TOKENIZED);
			sourceField.setBoost(Search.SOURCE_BOOST);

			doc.add(sourceField);
		}

		return doc;
*/
	} // public Document filter

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return this.conf;
	}

} // ResourceSourceIndexer
