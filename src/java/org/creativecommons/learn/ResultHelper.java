package org.creativecommons.learn;

import javax.servlet.http.HttpServletRequest;

public class ResultHelper {

    public static String getLicenseCSS (String license_url) {
	// return the name of a specific CSS class based on the license

	if (license_url.equals("http://creativecommons.org/licenses/by/3.0/"))
	    return "green";

	return "red";
	
    } // getLicenseCSS

    public static String getTagQueryHref (HttpServletRequest request,
					  String tag) {

	// starting with a request, add a tag filter to the search
	// and return the resulting HREF
	String query = request.getParameter("query");

	query = query + "+tag:" + tag;
	return request.getRequestURL().toString() + "?query=" + query;

    } // getTagQueryHref

} // ResultHelper
