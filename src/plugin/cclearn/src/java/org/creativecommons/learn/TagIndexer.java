package org.creativecommons.learn;

// JDK import
import java.util.Collection;
import java.util.Iterator;

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
import org.creativecommons.learn.oercloud.Bookmark;
import org.creativecommons.learn.oercloud.ObjectMgr;
import org.creativecommons.learn.oercloud.Tag;

public class TagIndexer implements IndexingFilter {
    
    public static final Log LOG = LogFactory.getLog(TagIndexer.class.getName());
    
    private Configuration conf;

    public TagIndexer() {
    	LOG.info("Created TagIndexer.");
    }

    public Document filter(Document doc, Parse parse, Text url, 
			   CrawlDatum datum, Inlinks inlinks)
	throws IndexingException {


    	// load the tag list from the database
    	Bookmark resource = ObjectMgr.get().getBookmark(url.toString());
    	
    	return doc;
    	/*
    	Collection<Tag> tags = resource.getTags();

    	Iterator<Tag> tagIterator = tags.iterator();

    	while (tagIterator.hasNext()) {
    		Tag tag = tagIterator.next();

    		Field tagsField = new Field(Search.TAGS_FIELD, tag.getTag(), Field.Store.YES,
				    	Field.Index.TOKENIZED);
    		tagsField.setBoost(Search.TAGS_BOOST);
    		LOG.info("Adding tag (" + tag + ") to resource (" + 
    				url.toString() + ")");
    		doc.add(tagsField);

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

} // TagIndexer
