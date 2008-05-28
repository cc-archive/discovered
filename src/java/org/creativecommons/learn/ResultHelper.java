package org.creativecommons.learn;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.nutch.searcher.HitDetails;
import org.creativecommons.learn.oercloud.Curator;

public class ResultHelper {

	public static String getLicenseImage(String license_url) {
		// return the URL to the license image for the specified license
try{
		if ((license_url == null) || (license_url.indexOf("http://creativecommons.org/licenses") != 0))
			// not a CC license
			return null;

		String result = license_url.replace(
				"http://creativecommons.org/licenses/",
				"http://i.creativecommons.org/l/");
		result += "80x15.png";

		return result;
		} catch (NullPointerException e) {
		   return null;
		   }

	} // getLicenseImage

	public static String getLicenseQueryLink(HttpServletRequest request, String license_uri) {

		// starting with a request, add a tag filter to the search
		// and return the resulting HREF
		String query = request.getParameter("query");

		query = query + "+" + Search.LICENSE_FIELD + ":\"" + license_uri + "\"";
		return request.getRequestURL().toString() + "?query=" + query;

	} // getLicenseQueryLink

	public static String getTagQueryHref(HttpServletRequest request, String tag) {

		// starting with a request, add a tag filter to the search
		// and return the resulting HREF
		String query = request.getParameter("query");

		query = query + "+tag:\"" + tag + "\"";
		return request.getRequestURL().toString() + "?query=" + query;

	} // getTagQueryHref

	public static String[] getCuratorLinks(HttpServletRequest request, HitDetails result) {

		// get the curator(s) and resolve them to human readable name(s)
		String[] curators = result.getValues(Search.CURATOR_FIELD);
		if (curators == null) return new String[0];

                Vector<String> curator_links = new Vector<String>();
		
		for (int i = 0; i < curators.length; i++) {
		System.out.println(curators[i]);
			Curator c = Curator.byUrl(curators[i]);

			curator_links.add( "<a href=\"" + getCuratorQueryHref(request, curators[i]) + "\">" + c.getName() + "</a> <a href=\"" + curators[i] + "\"><img src=\"./img/house.png\" border=\"0\" /></a>" );
		}
	
		return (String[]) curator_links.toArray(new String[curator_links.size()]);
		
	}
	
	public static String getCuratorQueryHref(HttpServletRequest request, String curator) {

		// starting with a request, add a tag filter to the search
		// and return the resulting HREF
		String query = request.getParameter("query");

		query = query + "+" + Search.CURATOR_FIELD + ":'" + curator + "'";
		System.out.println(Search.CURATOR_FIELD);
		System.out.println(curator);
		return request.getRequestURL().toString() + "?query=" + query;

	} // getSourceQueryHref

} // ResultHelper
