package org.creativecommons.learn;

// JDK imports
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/** Utility to load DiscoverEd configuration  */
public class DEdConfiguration {
  
  private DEdConfiguration() {}                 // singleton

  /** Load the configuration properties for DiscoverEd. */
  public static Properties load() {
	  
      Properties props = new Properties();
      try{
	  props.load(ClassLoader.getSystemResourceAsStream("discovered.properties"));
      } catch (java.io.IOException e) {
	  e.printStackTrace();
      }

      return props;

  }

}

