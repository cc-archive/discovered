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
<%
    // @author Nathan R. Yergler
    // show cclearn-related meta info for each hit.
    // information indexed by ./src/plugin/cclearn.

    // License
    String license_uri = detail.getValue("license");

    // Tags
    String[] tags = detail.getValues("tag");
%>

<% 
    if (license_uri != null) { %>
<br/><span class="metadata_label">License:</span>&nbsp;<%=license_uri%>
<%  }

    if (tags != null && tags.length > 0) { 
%>
       <br/><span class="metadata_label">Tags:</span>

<%     for (int i_tag = 0; i_tag < tags.length; i_tag++) { %>

          <%=tags[i_tag]%>&nbsp;
<%     } 

    }    %>
