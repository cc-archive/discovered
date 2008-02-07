package org.creativecommons.learn;

import javax.servlet.http.HttpServletRequest;

public class ResultHelper {

    public static String getLicenseImage (String license_url) {
	// return the URL to the license image for the specied license

	if (license_url.indexOf("http://creativecommons.org/licenses") != 0) 
	    // not a CC license
	    return null;

	String result = license_url.replace(
				     "http://creativecommons.org/licenses/",
				     "http://i.creativecommons.org/l/");
	result += "80x15.png";

	return result;

    } // getLicenseImage

    public static String getTagQueryHref (HttpServletRequest request,
					  String tag) {

	// starting with a request, add a tag filter to the search
	// and return the resulting HREF
	String query = request.getParameter("query");

	query = query + "+tag:\"" + tag + "\"";
	return request.getRequestURL().toString() + "?query=" + query;

    } // getTagQueryHref

    public static String getSourceQueryHref (HttpServletRequest request,
					     String source) {

	// starting with a request, add a tag filter to the search
	// and return the resulting HREF
	String query = request.getParameter("query");

	query = query + "+source:\"" + source + "\"";
	return request.getRequestURL().toString() + "?query=" + query;

    } // getSourceQueryHref

} // ResultHelper
