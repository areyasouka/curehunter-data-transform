<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="yes" />

<xsl:variable name="sep1" select="'&#9;'"/>
<xsl:variable name="nl" select="'&#10;'"/>

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template name="removeNewlinesTabs">
  <xsl:param name="text" select="."/>
  <xsl:choose>
    <xsl:when test="ancestor::sentence">
      <xsl:value-of select="translate($text, '&#xA;&#xD;&#x9;', ' ')" />
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="normalize-space(translate($text, '&#xA;&#xD;&#x9;', ' '))" />
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="text()">
  <xsl:call-template name="removeNewlinesTabs">
    <xsl:with-param name="text" select="."/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="MedlineCitation">
  <xsl:variable name="pmid" select="PMID" />
  <xsl:variable name="keywords" 
        select="Article/ArticleTitle/sentence[k/@type='S' and (k/@type='D' or k/@type='T')]/k
                |Article/Abstract/AbstractText/sentence[k/@type='S' and (k/@type='D' or k/@type='T')]/k" />
  <xsl:if test="$keywords">
    <xsl:variable name="dateCreated"><xsl:if test="DateCreated"><xsl:value-of select="concat(DateCreated/Year,'-',DateCreated/Month,'-',DateCreated/Day)" /></xsl:if></xsl:variable>
    <xsl:variable name="dateCompleted"><xsl:if test="DateCompleted"><xsl:value-of select="concat(DateCompleted/Year,'-',DateCompleted/Month,'-',DateCompleted/Day)" /></xsl:if></xsl:variable>
    <xsl:variable name="dateRevised"><xsl:if test="DateRevised"><xsl:value-of select="concat(DateRevised/Year,'-',DateRevised/Month,'-',DateRevised/Day)" /></xsl:if></xsl:variable>
    <xsl:value-of select="concat($pmid,$sep1,$dateRevised,$sep1)" /><MedlineCitation><xsl:apply-templates select="./*"/></MedlineCitation><xsl:value-of select="$nl" />
  </xsl:if>
</xsl:template>

<xsl:template match="PubmedArticleSet|PubmedArticle">
  <xsl:apply-templates select="@*|node()" />
</xsl:template>

<xsl:template match="DeleteCitation">
  <xsl:for-each select="PMID">
    <xsl:value-of select="concat(.,$sep1,$sep1,$nl)" />
  </xsl:for-each>
</xsl:template>

<xsl:template match="PubmedData" />

</xsl:stylesheet>
