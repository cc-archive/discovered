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
import org.creativecommons.learn.oercloud.OaiResource;
import org.creativecommons.learn.oercloud.Resource;

import thewebsemantic.NotFoundException;

public class TagIndexer implements IndexingFilter {

	public static final Log LOG = LogFactory.getLog(TagIndexer.class.getName());

	private Configuration conf;

	public TagIndexer() {
		LOG.info("Created TagIndexer.");
	}

	public Document filter(Document doc, Parse parse, Text url,
			CrawlDatum datum, Inlinks inlinks) throws IndexingException {

		// add the tag/subject information
		try {

			Resource resource = TripleStore.get().load(Resource.class,
					url.toString());

			for (String subject : resource.getSubjects()) {

				addTag(doc, subject);

			}

			// related resource tags (oai-pmh)
			for (OaiResource related : resource.getSeeAlso()) {
				for (String subject : related.getSubjects()) {

					addTag(doc, subject);
				}
			}

		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return doc;
	} // public Document filter

	private void addTag(Document doc, String subject) {
		Field tagsField = new Field(Search.TAGS_FIELD, subject,
				Field.Store.YES, Field.Index.TOKENIZED);
		LOG.info("Adding tag (" + subject + ") to resource (" + doc.toString()
				+ ")");
		tagsField.setBoost(Search.TAGS_BOOST);

		doc.add(tagsField);
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return this.conf;
	}

} // TagIndexer
