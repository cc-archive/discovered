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

import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.HtmlParseFilter;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.protocol.Content;
import org.creativecommons.learn.oercloud.Resource;
import org.w3c.dom.DocumentFragment;

public class RDFaParser implements HtmlParseFilter {
	public static final Log LOG = LogFactory.getLog(RDFaParser.class);

	private Configuration conf;

	public ParseResult filter(Content content, ParseResult parseResult,
			HTMLMetaTags metaTags, DocumentFragment doc) {
		LOG.info("Entering RDFaParser filter for " + content.getUrl());

		String uri = content.getUrl();

		RdfStore store = RdfStoreFactory.get().forProvenance(uri);

		try {
			// This is not a no-op
			// Register the RDFaReader with Jena
			Class.forName("net.rootdev.javardfa.jena.RDFaReader");
			
			// create the Resource in this provenance
			store.save(new Resource(uri));
			
			store.getModel().read(new ByteArrayInputStream(content
					.getContent()),
					uri, "HTML");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return parseResult;
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return this.conf;
	}
}