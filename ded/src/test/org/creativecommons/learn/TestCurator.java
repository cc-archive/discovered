package org.creativecommons.learn;

import java.util.HashSet;

import org.creativecommons.learn.oercloud.Curator;

import junit.framework.TestCase;

public class TestCurator extends TestCase {
	public static void testCreatePowerSetOfCurators() {
		String curator1 = "http://example.com/#curator1";
		String curator2 = "http://example.com/#curator2";
		HashSet<String> curatorURIs = new HashSet<String>();
		curatorURIs.add(curator1);
		curatorURIs.add(curator2);
		
		HashSet<String> expected = new HashSet<String>();
		expected.add(curator1);
		expected.add(curator2);
		expected.add(curator1 + " " + curator2);
		assertEquals(expected,
				Curator.curatorUriPowerSetAsStrings(curatorURIs));
	}
}
