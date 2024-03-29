package org.creativecommons.learn;

import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.util.NutchConfiguration;
import org.creativecommons.learn.oercloud.Resource;

import thewebsemantic.NotFoundException;

import com.hp.hpl.jena.rdf.model.Model;

public class TestRDFaParser extends DiscoverEdTestCase {

	public void testFilterWithXhtml() {

		String url = "http://example.com/resource";

		StringBuilder html = new StringBuilder();
		html.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		html.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML+RDFa 1.0//EN\"");
		html.append("\"http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd\">");
		html.append("<html xmlns=\"http://www.w3.org/1999/xhtml\"");
		html.append("xmlns:dc=\"http://purl.org/dc/elements/1.1/\"");
		html.append("version=\"XHTML+RDFa 1.0\" xml:lang=\"en\">");
		html.append("<head>");
		html.append("<title property=\"dc:title\">This title is provided by the page itself</title>");
		html.append("</head>");
		html.append("<body>");
		html.append("</body>");
		html.append("</html>");

		Content content = new Content(url, url, html.toString().getBytes(),
				"text/html", new Metadata(), NutchConfiguration
						.createCrawlConfiguration());

		Resource r;
		// assert that we don't know anything about this resource
		try {
			r = RdfStoreFactory.get().forProvenance(url).loadDeep(
					Resource.class, url);
		} catch (NotFoundException ex) {

		} finally {
			assert (false);
		}

		// pass it through the RDFaParser (normally called at crawl time)
		RDFaParser parser = new RDFaParser();
		parser.filter(content, new ParseResult(url), new HTMLMetaTags(), null);

		// confirm that the title was extracted
		r = RdfStoreFactory.get().forProvenance(url).load(Resource.class, url);
		assertNotNull(r.getTitle());

	}

	public void testFilterWithHtml() {

		String url = "http://example.com/resource";

		StringBuilder html = new StringBuilder();
		html.append("<html xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:foo=\"http://example.com/ns#\">");
		html.append("<head>");
		html.append("<title property=\"dc:title\">This title is provided by the page itself</title>");
		html.append("</head>");
		html.append("<body>");
		html.append("<a href=\"http://creativecommons.org/licenses/by/3.0/\" rel=\"license\">license</a>");
		html.append("<a href=\"http://example.com/foo\" rel=\"foo:bar\">foo:bar</a>");
		html.append("</body>");
		html.append("</html>");

		Content content = new Content(url, url, html.toString().getBytes(),
				"text/html", new Metadata(), NutchConfiguration
						.createCrawlConfiguration());

		RdfStore store = RdfStoreFactory.get().forProvenance(url);
		Resource r;

		// assert that we don't know anything about this resource
		try {
			r = store.loadDeep(Resource.class, url);
		} catch (NotFoundException ex) {

		} finally {
			assert (false);
		}

		// pass it through the RDFaParser (normally called at crawl time)
		RDFaParser parser = new RDFaParser();
		parser.filter(content, new ParseResult(url), new HTMLMetaTags(), null);

		// confirm that the title was extracted
		r = RdfStoreFactory.get().forProvenance(url).load(Resource.class, url);
		assertNotNull(r.getTitle());

		// confirm the license was extracted
		Model model = store.getModel();
		assert (r.getFieldValues(
				model.createProperty("http://www.w3.org/1999/xhtml/license")).
				contains(model.createResource("http://creativecommons.org/licenses/by/3.0/").asNode()));

		// confirm other predicates (foo:bar) were extracted
		assert (r.getFieldValues(
				model.createProperty("http://example.com/ns#bar")).
				contains(model.createResource("http://example.com/foo").asNode()));

	}
}
