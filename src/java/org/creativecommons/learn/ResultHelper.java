package org.creativecommons.learn;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.nutch.searcher.HitDetails;
import org.creativecommons.learn.oercloud.Curator;

import thewebsemantic.NotFoundException;

public class ResultHelper {

	private static String _getFullUrl(HttpServletRequest request) {
		
		StringBuffer url = request.getRequestURL();
		url.append(request.getQueryString());
		
		return url.toString();
	}

	private static String addQueryParameter(HttpServletRequest request, 
			String query_key, String query_value) {
		
		// starting with a request, add a tag filter to the search
		// and return the resulting HREF
		try {
			String query = URLDecoder.decode(request.getParameter("query"), "UTF-8").trim();
			query = query + " " + query_key + ":\"" + query_value + "\"";
			query = URLEncoder.encode(query, "UTF-8");
			
			return request.getRequestURL().append("?query=" + query).toString();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		// fall back to just returning the existing URI
		return _getFullUrl(request);
		
	} // addQueryParameter
	
	public static String getLicenseQueryLink(HttpServletRequest request,
			String license_uri) {

		return addQueryParameter(request, "cc", "license=" + license_uri);

	} // getLicenseQueryLink

	public static String getTagQueryHref(HttpServletRequest request, String tag) {

		return addQueryParameter(request, Search.TAGS_FIELD, tag);

	} // getTagQueryHref

	public static String getCuratorQueryHref(HttpServletRequest request,
			String curator) {

		return addQueryParameter(request, Search.CURATOR_FIELD, curator);

	} // getCuratorQueryHref

	public static String[] getCuratorLinks(HttpServletRequest request,
			HitDetails result) {

		// get the curator(s) and resolve them to human readable name(s)
		String[] curators = result.getValues(Search.CURATOR_FIELD);
		if (curators == null)
			return new String[0];

		Vector<String> curator_links = new Vector<String>();

		for (int i = 0; i < curators.length; i++) {
			System.out.println(curators[i]);
			Curator c;
			try {
				c = (Curator)TripleStore.get().load(Curator.class, curators[i]);
			} catch (NotFoundException e) {
				c = null;
			}
			
			if (c != null) {
				curator_links.add("<a href=\""
						+ getCuratorQueryHref(request, curators[i]) + "\">"
						+ c.getName() + "</a> <a href=\"" + curators[i]
						+ "\"><img src=\"./img/house.png\" border=\"0\" /></a>");
			}
		}

		return (String[]) curator_links
				.toArray(new String[curator_links.size()]);

	}

	public static String getLicenseImage(String license_url) {
		// return the URL to the license image for the specified license
		try {
			if ((license_url == null)
					|| (license_url
							.indexOf("http://creativecommons.org/licenses") != 0))
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

} // ResultHelper
