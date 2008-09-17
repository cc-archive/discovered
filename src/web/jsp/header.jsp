<html>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<head>
<title>ccLearn Universal Education Search</title>
<link rel="icon" href="<%=request.getContextPath()%>/img/favicon.ico" type="image/x-icon"/>
<link rel="shortcut icon" href="<%=request.getContextPath()%>/img/favicon.ico" type="image/x-icon"/>

<script src="<%=request.getContextPath()%>/ext/adapter/ext/ext-base.js" 
	type="text/javascript"> </script>
<script src="<%=request.getContextPath()%>/ext/ext-core.js" 
	type="text/javascript"> </script>

<style type="text/css" media="screen">
    @import "<%=request.getContextPath()%>/css/oesearch.css";
</style>

<script type="text/javascript">
<!--
function queryfocus() { document.search.query.focus(); }


Ext.onReady(function() {

   Ext.select("a.show_tags").on("click", function(e) {
     Ext.get(e.getTarget()).parent().first(".more_tags").show();
     Ext.get(e.getTarget()).hide();
     
     e.stopEvent();
   });
});

// -->
</script>
</head>

<body onLoad="queryfocus();">

  <div id="header-box">
    <div id="header" class="box">
      <div id="title">
	<h1><a href="<%=request.getContextPath()%>/">
	    <img src="<%=request.getContextPath()%>/img/oes-logo.png" border="0"/>
	</a></h1>
      </div>
    </div>
  </div>


