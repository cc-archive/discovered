<%
    // @author Nathan R. Yergler
    // show cclearn-related meta info for each hit.
    // information indexed by ./src/plugin/cclearn.

    Resource result_item = ResultHelper.getResource(detail.getValue("url"));

    // curator information
    String[] curators = ResultHelper.getCuratorLinks(request, detail);

    // License
    String license_uri = detail.getValue("license");

    // Tags
    String[] tags = detail.getValues("tag");
    
    // Description
    if (result_item != null) {
    	summary = result_item.getDescription();
    }
%>

<div class="result">
 
    <h2><a href="<%=url%>"><%=Entities.encode(title)%></a></h2>

        <p class="abstract"><%=summary%></p>

        <div class="meta">
	<% if (license_uri != null) { %>
          <p class="license">
	    <a href="<%=license_uri%>">
	      <img src="<%=ResultHelper.getLicenseImage(license_uri)%>"
               title="<%=ResultHelper.getLicenseName(license_uri)%>" 
	       	   border=0 />
	    </a>
	    <a href="<%=ResultHelper.getLicenseQueryLink(request, license_uri)%>">
	      <img alt="more like this" src="<%=request.getContextPath()%>/img/magnifier.png" border="0" />
	    </a>
	  </p>
	<% } %>

          <div class="primary">
            <p class="source"><strong>Curator:</strong> 
            <% for (int i_cur = 0; i_cur < curators.length; i_cur++) { %>
	           <span><%=curators[i_cur]%></span>
	        <% } %>
	    </p>
<!--            <p class="subject"><strong>Subject:</strong> 
	       <span></span></p>
            <p class="collection"><strong>Collection:</strong> 
	       <span></span></p>
-->
          </div>

	  <p class="info-links">
    <%
      if (showCached) {
        %>(<a href="<%=request.getContextPath()%>/cached.jsp?<%=id%>"><i18n:message key="cached"/></a>) <%
    }
    %>
	  </p>

          <div class="clear"></div>
<p>
<%
    if (tags != null && tags.length > 0) { 
%>
       <strong>Tags:</strong>

<%     for (int i_tag = 0; i_tag < tags.length; i_tag++) { %>

          <a href="<%=ResultHelper.getTagQueryHref(request, tags[i_tag])%>">
	     <%=tags[i_tag]%>
	  </a>&nbsp;
<%     } 

    }    %>
</p>

    <% if (hit.moreFromDupExcluded()) {
    String more =
    "query="+URLEncoder.encode("site:"+hit.getDedupValue()+" "+queryString, "UTF8")
    +params+"&hitsPerSite="+0
    +"&lang="+queryLang
    +"&clustering="+clustering;%>
    (<a href="<%=request.getContextPath()%>/search.jsp?<%=more%>"><i18n:message key="moreFrom"/>
     <%=hit.getDedupValue()%></a>)
    <% } %>
    (<a href="<%=request.getContextPath()%>/explain.jsp?<%=id%>&query=<%=URLEncoder.encode(queryString, "UTF-8")%>&lang=<%=queryLang%>"><i18n:message key="explain"/></a>)
    (<a href="<%=request.getContextPath()%>/anchors.jsp?<%=id%>"><i18n:message key="anchors"/></a>)

    </div>
</div>
