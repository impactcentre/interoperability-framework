<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.io.File"%>
<%@page import="java.net.URL"%>
<%@page import="eu.impact_project.iif.t2.client.*"%>
<%
  String folder = application.getRealPath("/");
  if (!folder.endsWith("/")) folder = folder + "/";

  Properties props = new Properties();
  InputStream stream = new URL("file:" + folder + "config.properties").openStream();
  
  props.load(stream);
  stream.close();

  String style = props.getProperty("styleSheet");
  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" type="text/css" href="<%=style%>" media="screen">
	<link rel="stylesheet" type="text/css" href="css/bootstrap.min.css" media="screen" />
</head>
<body>
	<% 
	WorkflowDetails details = (WorkflowDetails)session.getAttribute("wfDetails");
	%>
	<div class="csc-header csc-header-n1">
		<h1 class="csc-firstHeader"><%=details.getTitle() %></h1>
	</div>
	<%=details.getDescription() %>
	<img alt="Workflow Preview" src="<%=details.getImageUrl() %>" width="750px">
</body>
</html>
