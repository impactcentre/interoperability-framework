<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<%@page import="eu.impact_project.iif.t2.client.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="css/style.css" media="screen" />

</head>

<body>
<% 

WorkflowDetails details = (WorkflowDetails)session.getAttribute("wfDetails");

%>


<div class="csc-header csc-header-n1">
<h1 class="csc-firstHeader"><%=details.getTitle() %></h1>
</div>
<hr></hr>

<%=details.getDescription() %>

<br><br>

<img alt="Workflow Preview" src="<%=details.getImageUrl() %>" width="750px">


</body>


</html>
