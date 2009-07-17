<html>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<head>
<title>DiscoverED</title>
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

   Ext.select(".more_tags").setVisibilityMode(Ext.Element.DISPLAY).hide();
   
   Ext.select(".show_tags").on("click", function(e) {

     var more_tags = Ext.get(e.getTarget()).parent().first(".more_tags");
     // more_tags.setVisibilityMode(Ext.Element.DISPLAY);
     more_tags.toggle();
     
     if (more_tags.isVisible())
     	e.getTarget().src="<%=request.getContextPath()%>/icons/bullet_toggle_minus.png";
     else {
     	e.getTarget().src="<%=request.getContextPath()%>/icons/bullet_toggle_plus.png";
	more_tags.dom.style.display = "none";
     }
     
     e.stopEvent();
   });
});

// -->
</script>
</head>

<body onLoad="queryfocus();">
  <div id="beta-box">
  </div>
  <div id="header-box">
    <div id="header" class="box">
      <div id="title">
	<h1><a href="<%=request.getContextPath()%>/">
	    <img src="<%=request.getContextPath()%>/img/oes-logo.png" border="0"/>
	</a></h1>
	<h2>Discover the Universe of Open Educational Resources</h2>
      </div>
    </div>
  </div>


