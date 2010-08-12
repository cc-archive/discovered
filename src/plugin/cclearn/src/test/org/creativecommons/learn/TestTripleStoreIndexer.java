package org.creativecommons.learn;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.hadoop.conf.Configuration;
import org.creativecommons.learn.oercloud.Curator;
import org.creativecommons.learn.oercloud.Feed;
import org.creativecommons.learn.oercloud.Resource;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;

public class TestTripleStoreIndexer extends DiscoverEdTestCase {
	public static void testCreateTripleStoreResourceCache() {
		// First, create a Resource that appears in feeds curated by multiple organizations
		Resource r1 = new Resource(URI.create("http://example.com/#resource"));

		// curator 1
		Curator c1 = new Curator(URI.create("http://example.com/#curator1"));
		RdfStoreFactory.get().forDEd().save(c1);
		
		// feed 1
		Feed f1 = new Feed(URI.create("http://example.com/#feed1"));
		f1.setCurator(c1);
		RdfStoreFactory.get().forDEd().save(f1);
		
		// provenance 1
		RdfStore store1 = RdfStoreFactory.get().forProvenance(f1.getUrl());
		store1.save(r1);
		
		Resource r2 = new Resource(URI.create("http://example.com/#resource2"));
		
		// curator 2
		Curator c2 = new Curator(URI.create("http://example.com/#curator2"));
		RdfStoreFactory.get().forDEd().save(c2);
		
		// feed 1
		Feed f2 = new Feed(URI.create("http://example.com/#feed2"));
		f2.setCurator(c2);
		RdfStoreFactory.get().forDEd().save(f2);
		
		// provenance 2
		RdfStore store2 = RdfStoreFactory.get().forProvenance(f2.getUrl());
		store2.save(r2);
		
		// Then, test that we can know ask the Resource to tell us all the URIs that have curated it
		TripleStoreIndexer tsi = new TripleStoreIndexer();
		
		HashMap<String, HashSet<String>> map = tsi.getProvenanceResourceCache();
		
		HashSet<String> prov1_resources = map.get(f1.getUrl());
		HashSet<String> prov1_resources_expected = new HashSet<String>();
		prov1_resources_expected.add(r1.getUrl());
		assertEquals(prov1_resources, prov1_resources_expected);
		
		HashSet<String> prov2_resources = map.get(f2.getUrl());
		HashSet<String> prov2_resources_expected = new HashSet<String>();
		prov2_resources_expected.add(r2.getUrl());
		assertEquals(prov2_resources, prov2_resources_expected);
			
	}

	public static void testGenerateAllPossibleColumnNames() {
		// first, create a Triple (admittedly, in the siteConfigurationStore) that
		// has a predicate that's attached to a Resource.
		Resource r = new Resource(URI.create("http://example.com/#resource"));
		r.setTitle("A title");
		RdfStore store = RdfStoreFactory.get().forDEd();
		store.save(r);
		
		// Now, ask the TripleStoreIndexer what column names it has.		
		TripleStoreIndexer indexer = new TripleStoreIndexer();
		
		Collection<String> got = indexer.getAllPossibleFieldNames();
		assertTrue(got.contains(RdfStoreFactory.get().getOrCreateTablePrefixFromURIAsInteger(RdfStoreFactory.SITE_CONFIG_URI) + "__dct_title"));
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
        RdfStore site_store = RdfStoreFactory.get().forDEd();
         
        /* second, create such a Resource
         * ------------------------------ */
        Curator c = new Curator(URI.create("http://example.com/#i_am_a_curator"));
        Feed f = new Feed(URI.create("http://example.com/#i_am_a_feed"));
        f.setCurator(c);

        site_store.save(c);
        site_store.save(f);

        Resource r = new Resource(URI.create("http://example.com/#i_am_a_resource"));
        // Make the resource's education level = xyz
        Model m = site_store.getModel();
        Property edLevel = m.createProperty(customPredicateURI);
        Literal xyz = m.createLiteral("xyz");
        r.addField(edLevel, xyz);
        r.getSources().add(f);
        RdfStoreFactory.get().forProvenance(f.getUrl()).save(r);

        /* third, can the triple store indexer find this custom field and its
         * values?
         * ----------------------- */
        Collection<String> values = indexer.getValuesForCustomLuceneFieldName(
                r.getUrl(), customLuceneFieldName);
        System.out.println(values);
        assertTrue(values.contains(customPredicateValue));
        
        /* will it properly declare the custom field name to Lucene? */
        assertTrue(indexer.getAllPossibleFieldNames().contains(customLuceneFieldName));
    }

    /* fourth, verify there is a Lucene column called "educationLevel" in a
     * Lucene/Nutch document corresponding to the Resource we created in
     * step 2 */

    // TODO: in a separate test, run the test above, and add an extra step:
    // do a search for educationLevel:xyz and find it
}
