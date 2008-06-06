package org.creativecommons.learn.feed;

import org.creativecommons.learn.TripleStore;

public class DumpN3 {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException {
		
		TripleStore.get().getModel().write(System.out, "N-TRIPLE");

	}

}
