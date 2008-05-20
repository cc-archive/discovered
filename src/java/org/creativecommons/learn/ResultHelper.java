package org.creativecommons.learn;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.nutch.searcher.HitDetails;
import org.creativecommons.learn.oercloud.Curator;

public class ResultHelper {

	public static String getLicenseImage(String license_url) {
		// return the URL to the license image for the specified license

		if (license_url.indexOf("http://creativecommons.org/licenses") != 0)
			// not a CC license
			return null;

		String result = license_url.replace(
				"http://creativecommons.org/licenses/",
				"http://i.creativecommons.org/l/");
		result += "80x15.png";

		return result;

	} // getLicenseImage

	public static String getTagQueryHref(HttpServletRequest request, String tag) {

		// starting with a request, add a tag filter to the search
		// and return the resulting HREF
		String query = request.getParameter("query");

		query = query + "+tag:\"" + tag + "\"";
		return request.getRequestURL().toString() + "?query=" + query;

	} // getTagQueryHref

	public static String[] getCuratorLinks(HttpServletRequest request, HitDetails result) {

		// get the curator(s) and resolve them to human readable name(s)
		Vector<String> curator_links = new Vector<String>();
		String[] curators = result.getValues(Search.CURATOR_FIELD);
		
		for (String curator_url : curators) {
			Curator c = Curator.byUrl(curator_url);

			curator_links.add(
					"<a href=\"" + getCuratorQueryHref(request, curator_url) + "\">" + c.getName() + "</a> ");
		}
		
		return (String[]) curator_links.toArray();
		
	}
	
	public static String getCuratorQueryHref(HttpServletRequest request, String curator) {

		// starting with a request, add a tag filter to the search
		// and return the resulting HREF
		String query = request.getParameter("query");

		query = query + "+" + Search.CURATOR_FIELD + ":\"" + curator + "\"";
		return request.getRequestURL().toString() + "?query=" + query;

	} // getSourceQueryHref

} // ResultHelper
