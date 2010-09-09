<%@ page 
  session="false"
  contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"

  import="java.io.*"
  import="java.util.*"
  import="java.net.*"

  import="org.apache.nutch.html.Entities"
  import="org.apache.nutch.metadata.Nutch"
  import="org.apache.nutch.searcher.*"
  import="org.apache.nutch.plugin.*"
  import="org.apache.nutch.clustering.*"
  import="org.apache.hadoop.conf.*"
  import="org.apache.nutch.util.NutchConfiguration"

  import="org.creativecommons.learn.RdfStoreFactory"
  import="org.creativecommons.learn.oercloud.*"

%>
<%@ include file="/header.jsp" %>


<div class="box">

<% Collection<Feed> feeds = null;

	if (request.getParameter("c") != null) { 
    	Curator c = RdfStoreFactory.get().forDEd().load(Curator.class, request.getParameter("c")); 
    	feeds = c.getFeeds(); 
    %>
<h1>Feeds for <%=c.getName() %></h1>
<% } else { 
		feeds = RdfStoreFactory.get().forDEd().loadDeep(Feed.class);%>
<h1>Feeds</h1>
<% } %>

<ul>
	<% for (Feed f : feeds) { %>
	<li><%=f.getUri().toString() %> (<%=f.getCurator().getName()%>)
	<ul><li>Last aggregated on: <%=f.getLastImport().toString() %></li></ul>
	</li>
	<% } %>
</ul>

</div>

<jsp:include page="/footer.jsp"/>

</body>
</html>
