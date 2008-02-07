<%
    // @author Nathan R. Yergler
    // show cclearn-related meta info for each hit.
    // information indexed by ./src/plugin/cclearn.

    // Curator
    String curator = detail.getValue("currator");

    // License
    String license_uri = detail.getValue("license");

    // Tags
    String[] tags = detail.getValues("tag");
%>

<div class="result">
 
    <h2><a href="<%=url%>"><%=Entities.encode(title)%></a></h2>

        <p class="abstract"><%=summary%></p>

        <div class="meta">
          <p class="license"><a href="<%=license_uri%>">
	    <img src="<%=ResultHelper.getLicenseImage(license_uri)%>" 
	    	 border=0 />
	  </a></p>

          <div class="primary">
            <p class="subject"><strong>Subject:</strong> 
	       <span></span></p>
            <p class="collection"><strong>Collection:</strong> 
	       <span></span></p>
          </div>
          <p class="source"><strong>Source:</strong> 
	       <span><%=curator%></span></p>

<% if (false) { %>
          <p><a
          href="http://www.oercommons.org/courses/applying-metal-sputtering/">More
          details</a></p>
<% } %>

          <div class="clear"></div>

    <%
      if (showCached) {
        %>(<a href="../cached.jsp?<%=id%>"><i18n:message key="cached"/></a>) <%
    }
    %>

    (<a href="../explain.jsp?<%=id%>&query=<%=URLEncoder.encode(queryString, "UTF-8")%>&lang=<%=queryLang%>"><i18n:message key="explain"/></a>)
    (<a href="../anchors.jsp?<%=id%>"><i18n:message key="anchors"/></a>)

    <% if (hit.moreFromDupExcluded()) {
    String more =
    "query="+URLEncoder.encode("site:"+hit.getDedupValue()+" "+queryString, "UTF8")
    +params+"&hitsPerSite="+0
    +"&lang="+queryLang
    +"&clustering="+clustering;%>
    (<a href="../search.jsp?<%=more%>"><i18n:message key="moreFrom"/>
     <%=hit.getDedupValue()%></a>)
    <% } %>
    <br><br>
    </div>
<%
    if (tags != null && tags.length > 0) { 
%>
       <br/><span class="metadata_label">Tags:</span>

<%     for (int i_tag = 0; i_tag < tags.length; i_tag++) { %>

          <a href="<%=ResultHelper.getTagQueryHref(request,
	  tags[i_tag])%>"><%=tags[i_tag]%></a>&nbsp;
<%     } 

    }    %>
</div>
