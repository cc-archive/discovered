package org.creativecommons.learn;

// JDK import
import java.util.logging.Logger;
import java.util.Collection;
import java.util.Iterator;

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

public class SourceIndexer implements IndexingFilter {
    
    public static final Log LOG = LogFactory.getLog(
					      SourceIndexer.class.getName());
    
    private Configuration conf;
    private TagLoader tagLoader = new TagLoader();

    public SourceIndexer() {
	LOG.info("Created SourceIndexer.");
    }

    public Document filter(Document doc, Parse parse, Text url, 
			   CrawlDatum datum, Inlinks inlinks)
	throws IndexingException {

	// add the source information
	String source = tagLoader.source(url.toString());
	
	if (source != null) {
	    Field sourceField = new Field(Search.SOURCE_FIELD, source,
					    Field.Store.YES, 
					    Field.Index.TOKENIZED);
	    sourceField.setBoost(Search.SOURCE_BOOST);
	    
	    doc.add(sourceField);
	}

	return doc;

    } // public Document filter
  
    public void setConf(Configuration conf) {
	this.conf = conf;
    }

    public Configuration getConf() {
	return this.conf;
    }  

} // SourceIndexer
