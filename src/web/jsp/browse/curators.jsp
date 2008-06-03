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

  import="org.creativecommons.learn.TripleStore"
  import="org.creativecommons.learn.oercloud.*"

%>
<%@ include file="/header.jsp" %>

<% TripleStore store = TripleStore.get();
%>

<h2>Curators</h2>

<ul>
<% for (Curator c : store.load(Curator.class)) { %>

	<li><%=c.getName() %></li>
<% } %>
</ul>
<jsp:include page="/include/footer.html"/>

</body>
</html>
