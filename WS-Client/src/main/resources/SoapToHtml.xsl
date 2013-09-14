<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
 xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

 <xsl:output method="html" indent="yes" encoding="UTF-8"/>

 <xsl:template match="/">
  <xsl:apply-templates select="//soapenv:Body"/>
 </xsl:template>

 <xsl:template match="soapenv:Body">
  <xsl:apply-templates select=".//*[not(./child::*)]"/>
 </xsl:template>

 <xsl:template match="*">
  <h3>
   <xsl:value-of select="local-name()"/>
  </h3>
  <xsl:choose>
   <xsl:when test="starts-with(text(), 'http://')">
    <br/>
    <div>
     <xsl:element name="a">
      <xsl:attribute name="href">
       <xsl:value-of select="text()"/>
      </xsl:attribute>
      <xsl:value-of select="text()"/>
     </xsl:element>
    </div>
   </xsl:when>
   <xsl:otherwise>
    <pre>
     <xsl:value-of select="text()"/>
    </pre>
   </xsl:otherwise>
  </xsl:choose>
  <br/>
 </xsl:template>

</xsl:stylesheet>
