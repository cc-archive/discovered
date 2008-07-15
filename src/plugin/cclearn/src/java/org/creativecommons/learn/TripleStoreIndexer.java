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
import org.creativecommons.learn.oercloud.OaiResource;
import org.creativecommons.learn.oercloud.Resource;

import thewebsemantic.NotFoundException;

public class TripleStoreIndexer implements IndexingFilter {

	public static final Log LOG = LogFactory.getLog(TripleStoreIndexer.class
			.getName());

	private Configuration conf;

	public TripleStoreIndexer() {
		LOG.info("Created TripleStoreIndexer.");
	}

	public Document filter(Document doc, Parse parse, Text url,
			CrawlDatum datum, Inlinks inlinks) throws IndexingException {

		try {

			Resource resource = TripleStore.get().loadDeep(Resource.class,
					url.toString());

			// Add the source / curator information
			indexSources(doc, resource);

			// Add subjects/tags
			indexSubjects(doc, resource);

			// Add education level
			indexEducationLevel(doc, resource);

			// Add language
			indexLangauge(doc, resource);

		} catch (NotFoundException e) {

			// no information on this resource...
			LOG.warn("Unable to index triple store information for "
					+ url.toString() + "; resource not found.");

		}

		return doc;
	} // public Document filter

	private void indexLangauge(Document document, Resource resource) {

		for (String ed_level : resource.getLanguages()) {

			Field languageField = new Field(Search.LANGUAGE_FIELD, ed_level,
					Field.Store.YES, Field.Index.UN_TOKENIZED);
			languageField.setBoost(Search.LANGUAGE_BOOST);

			document.add(languageField);

		}

	} // indexLanguage

	private void indexEducationLevel(Document document, Resource resource) {

		for (String ed_level : resource.getEducationLevels()) {

			Field edLevelField = new Field(Search.ED_LEVEL_FIELD, ed_level,
					Field.Store.YES, Field.Index.TOKENIZED);
			edLevelField.setBoost(Search.ED_LEVEL_BOOST);

			document.add(edLevelField);

		}

	} // indexEducationLevel

	private void indexSubjects(Document document, Resource resource) {

		for (String subject : resource.getSubjects()) {
			addTag(document, subject);
		}

		// related resource tags (oai-pmh)
		for (OaiResource related : resource.getSeeAlso()) {
			for (String subject : related.getSubjects()) {

				addTag(document, subject);
			}
		}

	} // indexSubjects

	private void indexSources(Document document, Resource resource) {
		for (Feed source : resource.getSources()) {

			Field sourceField = new Field(Search.FEED_FIELD, source.getUrl(),
					Field.Store.YES, Field.Index.TOKENIZED);
			sourceField.setBoost(Search.FEED_BOOST);
			document.add(sourceField);

			// if this feed has curator information attached, index it as well
			String curator_url = "";
			if (source.getCurator() != null) {
				curator_url = source.getCurator().getUrl();
			}

			Field curator = new Field(Search.CURATOR_FIELD, curator_url,
					Field.Store.YES, Field.Index.TOKENIZED);
			curator.setBoost(Search.CURATOR_BOOST);
			document.add(curator);

		}
	} // indexSources

	private void addTag(Document doc, String subject) {

		LOG.debug("Adding tag (" + subject + ").");

		Field tagsField = new Field(Search.TAGS_FIELD, subject,
				Field.Store.YES, Field.Index.TOKENIZED);
		tagsField.setBoost(Search.TAGS_BOOST);

		doc.add(tagsField);
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return this.conf;
	}

} // CuratorIndexer
