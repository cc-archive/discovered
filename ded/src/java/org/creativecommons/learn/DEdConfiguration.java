/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.creativecommons.learn;

// Hadoop imports
import org.apache.hadoop.conf.Configuration;

/** Utility to create Hadoop {@link Configuration}s that include DEd-specific
 * resources.  */
public class DEdConfiguration {
  
  private DEdConfiguration() {}                 // singleton

  /** Create a {@link Configuration} for DiscoverEd. */
  public static Configuration create() {
    Configuration conf = new Configuration();
    addDiscoverEdResources(conf);
    return conf;
  }

  /**
   * Add the standard DEd resources to {@link Configuration}.
   * 
   * @param conf               Configuration object to which
   *                           configuration is to be added.
   *
   */
  private static Configuration addDiscoverEdResources(Configuration conf) {
    conf.addResource("discovered.xml");

    return conf;
  }
}

