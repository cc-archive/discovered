package org.creativecommons.learn;

import java.util.Collection;

import org.apache.hadoop.conf.Configuration;
import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;
import org.creativecommons.learn.oercloud.Resource;

import com.hp.hpl.jena.rdf.model.impl.LiteralImpl;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;

import junit.framework.TestCase;

public class TripleStoreIndexerTest extends TestCase {
	public static void testGenerateAllPossibleColumnNames() {
		// first, create a Triple (admittedly, in the siteConfigurationStore) that
		// has a predicate that's attached to a Resource.
		Resource r = new Resource("http://example.com/#resource");
		r.setTitle("A title");
		RdfStore store = RdfStore.forDEd();
		store.save(r);
		
		// Now, ask the TripleStoreIndexer what column names it has.		
		TripleStoreIndexer indexer = new TripleStoreIndexer();
		
		Collection<String> got = indexer.getAllPossibleFieldNames();
		assertTrue(got.contains("1__dct_title"));
	}

    public static void testAddSearchableColumnViaConfigurationFile() {
        final String customLuceneFieldName = "educationlevel";
        final String customPredicateURI = "http://example.com/#educationLevel";
        final String customPredicateValue = "xyz";

        /* first, create a configuration file saying that educationlevel:xyz is
         * supposed to search for Resources with a triple of
         * http://example.com/#educationLevel set to "xyz"
         * ---------------------------------------------- */
    	
        TripleStoreIndexer indexer = new TripleStoreIndexer();
        Configuration customFieldsConfiguration = indexer.getCustomFieldConfiguration();
        customFieldsConfiguration.set(customLuceneFieldName, customPredicateURI);
        RdfStore site_store = RdfStore.forDEd();
         
        /* second, create such a Resource
         * ------------------------------ */
        Curator c = new Curator("http://example.com/#i_am_a_curator");
        Feed f = new Feed("http://example.com/#i_am_a_feed");
        f.setCurator(c);

        site_store.save(c);
        site_store.save(f);

        Resource r = new Resource("http://example.com/#i_am_a_resource");
        r.addField(site_store.getModel().createProperty(customPredicateURI),
        		site_store.getModel().createLiteral("xyz"));
        r.getSources().add(f);
        RdfStore.uri2RdfStore(f.getUrl()).save(r);

        /* third, can the triple store indexer find this custom field and its
         * values?
         * ----------------------- */
        Collection<String> values = indexer.getValuesForCustomLuceneFieldName(
                r.getUrl(), customLuceneFieldName);
        System.out.println(indexer.getValuesForCustomLuceneFieldName(r.getUrl(), customLuceneFieldName));
        assertTrue(values.contains(customPredicateValue));
    }

    /* fourth, verify there is a Lucene column called "educationLevel" in a
     * Lucene/Nutch document corresponding to the Resource we created in
     * step 2 */

    // TODO: in a separate test, run the test above, and add an extra step:
    // do a search for educationLevel:xyz and find it
}
