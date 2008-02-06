package org.creativecommons.learn;

// JDK import
import java.util.logging.Logger;

// Commons imports
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


// Nutch imports
import org.apache.nutch.util.LogUtil;
import org.apache.nutch.fetcher.FetcherOutput;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.parse.Parse;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;

// Lucene imports
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;

// CC Learn imports
import org.creativecommons.learn.oercloud.TagLoader;
import org.creativecommons.learn.Search;
import java.sql.SQLException;

public class TagIndexer implements IndexingFilter {
    
    public static final Log LOG = LogFactory.getLog(TagIndexer.class.getName());
    
    private Configuration conf;
    private TagLoader tagLoader = new TagLoader();

    public TagIndexer() {
	LOG.info("Created TagIndexer.");
    }

    public Document filter(Document doc, Parse parse, Text url, 
			   CrawlDatum datum, Inlinks inlinks)
	throws IndexingException {


	try {
	// load the tag list from the database
	String allTags = tagLoader.tagsAsString(url.toString());
	Field tagsField = new Field(Search.TAGS_FIELD, allTags, Field.Store.YES,
				    Field.Index.TOKENIZED);
	tagsField.setBoost(Search.TAGS_BOOST);
	LOG.info("Adding tags (" + allTags + ") to resource (" + 
		 url.toString() + ")");
	doc.add(tagsField);

	} catch (SQLException e) {
	    LOG.info(e.getMessage());
	}

	return doc;


    } // public Document filter
  
    public void setConf(Configuration conf) {
	this.conf = conf;
    }

    public Configuration getConf() {
	return this.conf;
    }  

} // TagIndexer
