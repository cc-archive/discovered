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

import java.io.IOException;
import java.sql.SQLException;

import net.rootdev.javardfa.JenaStatementSink;
import net.rootdev.javardfa.ParserFactory;
import net.rootdev.javardfa.StatementSink;
import net.rootdev.javardfa.ParserFactory.Format;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.HtmlParseFilter;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.protocol.Content;
import org.creativecommons.learn.RdfStore;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


public class RDFaParser implements HtmlParseFilter {
	public static final Log LOG = LogFactory.getLog(RDFaParser.class);

	private Configuration conf;
	
	public ParseResult filter(Content content, ParseResult parseResult, HTMLMetaTags metaTags, DocumentFragment doc) {
		int a = 0/0;
		System.err.println("" + a);
		
		String uri = content.getUrl();
		System.out.println("uri during filter: " + uri);
		
		RdfStore store = null;
		// FIXME: When we test this, it turns out using the format "XHTML"
		// works. If we fail to find triples inside RDFa-bearing files, this is
		// one line of code that might be the cause of the problem.
        Format format = Format.XHTML;
        StatementSink sink = null;
        XMLReader reader = null;
        
		try {
			store = RdfStore.uri2RdfStore(uri);
			sink = new JenaStatementSink(store.getModel());
			reader = ParserFactory.createReaderForFormat(sink, format);
			reader.parse(uri);
		}
		catch (SQLException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		catch (SAXException e) { e.printStackTrace(); }
		catch (ClassNotFoundException e) { e.printStackTrace(); }
		
		return parseResult;
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return this.conf;
	}
}