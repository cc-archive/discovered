
<%@ page
  session="false"
  import="java.io.*"
  import="java.util.*"
  import="org.creativecommons.learn.TripleStore"
  import="org.creativecommons.learn.oercloud.Curator"
%>
<%@ include file="../header.jsp" %>

<div class="box">

<h1>Curators</h1>

<% Collection<Curator> curators = TripleStore.get().load(Curator.class); %>


<ul>
	<% for (Curator c : curators) { %>
	<li><%=c.getName() %> 
		<a href="<%=c.getUrl() %>"><img src="./img/house.png" border="0" /></a>
	</li>
	<% } %>
</ul>

</div>

<jsp:include page="/include/footer.html"/>

</body>
</html>
