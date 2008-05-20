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
import org.creativecommons.learn.aggregate.handlers.CCLEARN;
import org.creativecommons.learn.aggregate.handlers.TripleStore;
import org.creativecommons.learn.oercloud.Bookmark;
import org.creativecommons.learn.oercloud.ObjectMgr;
import org.creativecommons.learn.oercloud.Tag;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;

public class TagIndexer implements IndexingFilter {
    
    public static final Log LOG = LogFactory.getLog(TagIndexer.class.getName());
    
    private Configuration conf;

    public TagIndexer() {
    	LOG.info("Created TagIndexer.");
    }

    public Document filter(Document doc, Parse parse, Text url, 
			   CrawlDatum datum, Inlinks inlinks)
	throws IndexingException {

		// add the tag/subject information
		try {
			Model ts = new TripleStore().getModel();
			
			NodeIterator subjects = ts.listObjectsOfProperty(ts.createResource(url.toString()), 
					DC.subject);
			while (subjects.hasNext()) {
				RDFNode subject = subjects.nextNode();
				
				Field tagsField = new Field(Search.TAGS_FIELD, subject.toString(),
						Field.Store.YES, Field.Index.TOKENIZED);
	    		LOG.info("Adding tag (" + subject.toString() + ") to resource (" + 
	    				url.toString() + ")");
				tagsField.setBoost(Search.TAGS_BOOST);

				doc.add(tagsField);
				
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

} // TagIndexer
