package org.creativecommons.delicious;

import java.net.URI;

import org.creativecommons.learn.oercloud.Resource;

import junit.framework.TestCase;

public class DeliciousRetrieverTest extends TestCase {

    private DeliciousRetriever testRetriever = new DeliciousRetriever();

    public void testRetrieve() {
        Resource testResource = new Resource(URI.create("http://yahoo.com"));
        assertEquals(testResource.getSubjects().size(), 0);
        testRetriever.retrieve(testResource);
        assertTrue(testResource.getSubjects().size() > 0);
    }

}
