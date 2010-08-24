
<%@ page
  session="false"
  import="java.io.*"
  import="java.util.*"
  import="org.creativecommons.learn.RdfStoreFactory"
  import="org.creativecommons.learn.oercloud.Curator"
%>
<%@ include file="/header.jsp" %>

<div class="box">

<h1>Curators</h1>

<% Collection<Curator> curators = RdfStoreFactory.get().forDEd().load(Curator.class); %>


<ul>
	<% for (Curator c : curators) { %>
	<li><a href="<%=request.getContextPath()%>/browse/feeds.jsp?c=<%=c.getUrl()%>"><%=c.getName() %></a>  
		<a href="<%=c.getUrl() %>"><img src="<%=request.getContextPath()%>/img/house.png" border="0" /></a>
	</li>
	<% } %>
</ul>

</div>

<jsp:include page="/footer.jsp"/>

</body>
</html>
