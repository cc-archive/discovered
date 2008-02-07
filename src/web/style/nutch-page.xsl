<?xml version="1.0"?>
<!-- XSLT stylesheet that adds Nutch style, header, and footer
  elements.  This is used by Ant to generate static html pages. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"/>
  <xsl:template match="page">
    <html>
      <xsl:comment>This page is automatically generated.  Do not edit!</xsl:comment>
      <head>

	<meta http-equiv="Content-type" content="text/html; charset=utf-8" />
	<meta name="keywords" content="creativecommons, ccsearch, search, 
                                       engine, searchengine, license, find" />
	<meta name="description" content="A Creative Commons-based search
					  search engine of search engines." />
	<meta name="robots" content="index, follow" />
    

<!-- page title -->
        <title>
          <xsl:text>Open Education Search: </xsl:text>
          <xsl:value-of select="title" disable-output-escaping="yes"/>
        </title>
<!-- insert style -->
        <xsl:copy-of select="document('../include/style.html')"/>

<!-- specify icon file -->
      <link rel="icon" href="../img/favicon.ico" type="image/x-icon"/>
      <link rel="shortcut icon" href="../img/favicon.ico" type="image/x-icon"/>

      <script type="text/javascript">
      <xsl:comment>
function queryfocus() {
  search = document.search;
  if (search != null) { search.query.focus(); }
}
<xsl:text>// </xsl:text>
</xsl:comment>
      </script>
      </head>

      <body onLoad="queryfocus();">
<!-- insert localized header -->
        <xsl:copy-of select="document('include/header.html')"/>

	<div class="box"><div id="results">
              <xsl:call-template name="body"/>
	</div></div>

<!-- insert nutch footer -->
        <xsl:copy-of select="document('../include/footer.html')"/>
      </body>
    </html>
  </xsl:template>
<!-- included body -->
  <xsl:template name="body">
    <xsl:for-each select="body/node()">
      <xsl:choose>
<!-- orange intro -->
        <xsl:when test="name()='p' and position() &lt; 3">
            <xsl:copy-of select="."/>
        </xsl:when>
<!-- all other text -->
        <xsl:otherwise>
            <xsl:copy-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>
<!-- /included body -->
</xsl:stylesheet>
