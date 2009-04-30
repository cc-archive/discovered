<%
    // @author Nathan R. Yergler
    // show cclearn-related meta info for each hit.
    // information indexed by ./src/plugin/cclearn.

    // Resource result_item = ResultHelper.getResource(detail.getValue("url"));

    // curator information
    String[] curators = ResultHelper.getCuratorLinks(request, detail);

    // License
    String license_uri = detail.getValue("license");

    // Tags
    String[] tags = detail.getValues(Search.TAGS_INDEX_FIELD);
    
    // Education level(s)
    String[] ed_levels = detail.getValues(Search.ED_LEVEL_INDEX_FIELD);
    
    // Language(s)
    String[] languages = detail.getValues(Search.LANGUAGE_INDEX_FIELD);
    
    // Description
    if (detail.getValue("_dct_description") != null) summary = detail.getValue("_dct_description");
    /*
    if (result_item != null) {
    	summary = result_item.getDescription();
    }
    */
%>

<div class="result">
 
    <h2><a href="<%=url%>"><%=Entities.encode(title)%></a></h2>

        <p class="abstract"><%=summary%></p>

        <div class="meta">

	<table class="result-meta" width="100%" cellspacing="0">
		<tr valign="top" align="left">
			<td align="left" class="source" width="25%">
			
		<table align="left" width="100%" border="0">
		<tr><td width="1%" valign="top">
			<strong>Curator:</strong> 
			</td>
			<td>
		            <% if (curators != null) { 
		            	for (int i_cur = 0; i_cur < curators.length; i_cur++) { %>
			           <span><%=curators[i_cur]%></span><br/>
			        <% } 
		            } else { %>
						<a href="http://wiki.creativecommons.org/DiscoverEd:_Missing_Metadata">
							<img src="<%=request.getContextPath()%>/icons/help.png" alt="help" border="0"/>
						</a>
					<% } %>
					</td></tr></table>
			</td>
			<td align="left" class="education_level" width="25%"><strong>Education Level:</strong>
		            <% if (ed_levels != null) {
						for (String ed_level : ed_levels) { %>
		          		<a href="<%=ResultHelper.getRefinedQueryHref(request, Search.ED_LEVEL_QUERY_FIELD, ed_level)%>">
			     			<%=ed_level%>
			  			</a>
			        <% 	}
		            }else { %>
						<a href="http://wiki.creativecommons.org/DiscoverEd:_Missing_Metadata">
							<img src="<%=request.getContextPath()%>/icons/help.png" alt="help" border="0"/>
						</a>
					<% } %>
			</td>
			<td align="left" class="language" width="25%"><strong>Language:</strong>
					<% if (languages != null) { 
						for (String lang : languages) { %>
						<a href="<%=ResultHelper.getRefinedQueryHref(request, Search.LANGUAGE_QUERY_FIELD, lang) %>">
							<%=lang %>
						</a>
					<% } 
					} else { %>
						<a href="http://wiki.creativecommons.org/DiscoverEd:_Missing_Metadata">
							<img src="<%=request.getContextPath()%>/icons/help.png" alt="help" border="0" />
						</a>
					<% } %>
			</td>
			<td align="left" class="license" width="25%">
			<strong>License:</strong>
	<% if (license_uri != null) { %>
	    <a href="<%=license_uri%>">
	      <img src="<%=ResultHelper.getLicenseImage(license_uri)%>"
               title="<%=ResultHelper.getLicenseName(license_uri)%>" 
	       	   border=0 />
	    </a>
	    <a href="<%=ResultHelper.getLicenseQueryLink(request, license_uri)%>">
	      <img alt="more like this" src="<%=request.getContextPath()%>/img/magnifier.png" border="0" />
	    </a>
	<% } else { %>
		<a href="http://wiki.creativecommons.org/DiscoverEd:_Missing_Metadata">
		<img src="<%=request.getContextPath()%>/icons/help.png" alt="help" border="0" />
		</a>
	<% } %>
			</td>
		</tr>
<tr><td colspan="4" align="left" valign="top">
<%
    if (tags != null && tags.length > 0) {
		int initial_tags = tags.length;
		if ((tags.length - Search.MAX_TAGS) > Search.ORPHAN_TAG_LIMIT) initial_tags = Search.MAX_TAGS;

		if (initial_tags != tags.length) { %>
       		<img class="show_tags" src="<%=request.getContextPath()%>/icons/bullet_toggle_plus.png" />
		<% } %>
	<strong>Subject Tags:</strong>

<% 
   for (int i_tag = 0; i_tag < initial_tags; i_tag++) { %>

    <a href="<%=ResultHelper.getTagQueryHref(request, tags[i_tag])%>"><%=tags[i_tag]%></a>&nbsp;
<%     }
   
   // see if we need to show more tags
   if (tags.length > initial_tags) { %>
<div id="more_<%=i %>" class="more_tags">
    <% for (int i_tag=Search.MAX_TAGS; i_tag < tags.length; i_tag++) { %>
    <a href="<%=ResultHelper.getTagQueryHref(request, tags[i_tag])%>"><%=tags[i_tag]%></a>&nbsp;
<% } %>
</div>
<%    }
    }    %>
</td></tr>
	</table>

	  <p class="info-links">
    <%
      if (showCached) {
        %>(<a href="<%=request.getContextPath()%>/cached.jsp?<%=id%>"><i18n:message key="cached"/></a>) <%
    }
    %>
	  </p>

          <div class="clear"></div>

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
