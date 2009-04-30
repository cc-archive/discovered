<%--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at
  
  http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--%>
<%@ page
  session="false"
  import="java.io.*"
  import="java.util.*"
%>
<%@ include file="/header.jsp" %>

<div id="searchui" class="box">
<div id="search-base">
<form name="search" action="<%=request.getContextPath()%>/search.jsp" method="get">
  <input type="hidden" name="lang" value="en"/>
  <input id="q" name="query" size="44"/>&#160;
  <input id="qsubmit" type="submit" value="Search"/><br/>
  <a href="http://wiki.creativecommons.org/DiscoverEd_FAQ">learn more
  about DiscoverEd</a>
</form>
</div>
</div>

<jsp:include page="/footer.jsp"/>

</body>
</html>
