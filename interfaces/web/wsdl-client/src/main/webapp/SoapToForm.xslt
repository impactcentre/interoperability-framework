<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:myns="http://webServices">

<xsl:output method="text"/>

    <xsl:template match="/">
        <xsl:apply-templates select="//*[count(./*)=0 and ancestor::node()[local-name()='Body']]"/>
    </xsl:template>
    
    <xsl:template match="*[attribute::*[local-name()='contentType']]">
            <xsl:value-of select="local-name()"/>: 
        &lt;input type="file" name="<xsl:value-of select="local-name()"/>"/&gt;
        &lt;br&gt;
    </xsl:template>

    <xsl:template match="*">
        <xsl:value-of select="local-name()"/>: 
        &lt;input type="text" name="<xsl:value-of select="local-name()"/>"/&gt;
        &lt;br&gt;
    </xsl:template>
    
</xsl:stylesheet>
