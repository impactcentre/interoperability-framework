<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" import="java.io.File,java.io.IOException,java.util.*,java.io.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Output files</title>
</head>
<body>
<% 

String urlApp = getServletContext().getRealPath("/");
urlApp+=request.getServletPath();
urlApp=urlApp.replaceAll("index.jsp","");
 
File f = new File(urlApp); // current directory
File[] files = f.listFiles();
for (File file : files) {
	if ((!file.isDirectory()) && (!file.getName().endsWith(".jsp")) )
    {    
   		%>
		<a href="<%=file.getName()%>" target="_blank"><br> <%=file.getName() %></a>       
    	<%
    }
}
%>
</body>
</html>
