<!--
/*
	
	Copyright 2011 The IMPACT Project
	
	@author Dennis Neumann

	Licensed under the Apache License, Version 2.0 (the "License"); 
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
 
  		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

*/
-->

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@page import="eu.impact_project.wsclient.WSDLinfo"%><html>
<%@page import="eu.impact_project.wsclient.SOAPoperation"%>
<%@page import="eu.impact_project.wsclient.SOAPinputField"%>
<%@page import="java.util.*"%>
<%@page import="javax.xml.transform.*"%>
<%@page import="javax.xml.transform.stream.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.Properties"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.net.URL"%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="http://www.impact-project.eu/fileadmin/css/iframe.css" media="screen" />
<title>IMPACT Web Service Client</title>
<script language="JavaScript">
function getUrl(id) {
	
	var url = document.getElementById(id).value;
	var detailsWindow = window.open(url,"", "resizable,width=800,height=700,scrollbars,left=200,top=100");

	detailsWindow.focus();

}

function setStyle(element) {
	element.style.textDecoration = "underline";
	element.style.cursor = "pointer";
}
function removeStyle(element) {
	element.style.textDecoration = "none";
	element.style.cursor = "default";
}

</script>
</head>
<% 


String folder = application.getRealPath("/");

Properties props = new Properties();
InputStream stream = new URL("file:" + folder + "config.properties").openStream();

props.load(stream);
stream.close();

boolean loadDefault = Boolean.parseBoolean(props.getProperty("loadDefaultWebService"));
String defaultOperation = props.getProperty("defaultOperation");
String defaultTitle = props.getProperty("defaultTitle");
String defaultDescription = props.getProperty("defaultDescription");
String defaultButton = props.getProperty("defaultButton");
String defaultResultMessage = props.getProperty("defaultResultMessage");
boolean supportFileUpload = Boolean.parseBoolean(props.getProperty("supportFileUpload"));
boolean showResultFilesOnly = Boolean.parseBoolean(props.getProperty("showResultFilesOnly"));

%>
<body <%if(loadDefault && request.getAttribute("round2") == null && request.getAttribute("round3") == null){ %>onload="document.forms['defaultForm'].submit()"<%} %>>



<form name="defaultForm" action="SOAPinputs" method="post">

<input type="hidden" name="currentOperation" value="<%=defaultOperation%>">

</form>

<%if (loadDefault) { %>
	<br></br>
	<h1><%= defaultTitle %></h1>

<%}%>


<%if (!loadDefault) { %>

<a href="http://www.impact-project.eu/taa/dp/" target="_top">Demonstrator Platform</a>
<hr/>
<br/>


<a href="index.jsp">&lt;- Back to Selection</a>


<br>
<br>
<!--form action="WSDLinfo" method="post"><input name="wsdlURL"
	type="text" size="100"
	value="<%if (session.getAttribute("wsdlURL") != null)
				out.print(session.getAttribute("wsdlURL"));%>">
<br>
<br>
<input type="submit" value="Get Operations"> <br>
<br>
<hr>
<br>

</form-->
<%

	List<SOAPoperation> soapOperations = null;
	if (session.getAttribute("soapOperations") != null) {
		soapOperations = (List<SOAPoperation>) session.getAttribute("soapOperations");
	}

	// display Web Service operations after committing the form above
	if (request.getAttribute("round1") != null
			|| request.getAttribute("round2") != null
			|| request.getAttribute("round3") != null) {

		//out.print(session.getAttribute("endpointURL"));
		out.print("<h3>" + session.getAttribute("wsName") + "</h3>");
		out.print("<br>");

		if (session.getAttribute("serviceDocumentation") != null
				&& !session.getAttribute("serviceDocumentation")
						.equals("")) {
			out.print(session.getAttribute("serviceDocumentation"));
		}

		out.print("<hr>");
		out.print("<br>");

		SOAPoperation currentOperation = null;
		if (session.getAttribute("currentOperation") != null) {
			currentOperation = (SOAPoperation) session
					.getAttribute("currentOperation");
		}
%>
<form action="SOAPinputs" method="post">Available Operations: <select
	name="currentOperation">
	<%
		for (SOAPoperation op : soapOperations) {
	%>
	<option value="<%=op.getName()%>"
		<%if (currentOperation != null
							&& op.getName().equals(currentOperation.getName())) {out.print(" selected");}%>><%=op.getName()%></option>
	<%
		}
	%>
</select> <br><br>
<% Boolean displayDefaults = (Boolean) session.getAttribute("displayDefaults"); %>
<input type="checkbox" name="displayDefaults" <% if (displayDefaults == null || displayDefaults) { %>checked="checked"<%} %>>Display available default values
<br><br>
<input type="submit" value="Show Operation Inputs"></form>
<br>

<%
	} // if(request... round1

} // if(!loadDefaults)
			
			
	if (request.getAttribute("round2") != null
			|| request.getAttribute("round3") != null) {

		SOAPoperation currentOperation = (SOAPoperation) session
				.getAttribute("currentOperation");
		String message = currentOperation.getDefaultMessage();

		String opName = currentOperation.getName();
		String opDocumentation = currentOperation.getDocumentation();
		out.print("<br>");
		if (!loadDefault) {
			out.print("<br>");
		out.print("<b>Operation: " + opName + "</b>");
		out.print("<br>");
		out.print(opDocumentation);
		} else {
			out.print("<b>" + defaultDescription + "</b>");

		}
		out.print("<br>");
		try {

			// XSLT transformer, transforms a SOAP message into an HTML form
			//			TransformerFactory tFactory = TransformerFactory
			//					.newInstance();
			//			String rootDir = application.getRealPath("/");
			//			Transformer transformer = tFactory
			//					.newTransformer(new StreamSource(
			//							new FileInputStream(rootDir
			//									+ "SoapToForm.xslt")));

			// convert the default message to StreamSource, because the transformer requires this type
			//			InputStream is = new ByteArrayInputStream(message
			//					.getBytes());
			//			StreamSource source = new StreamSource(is);

			// prepare an OutputStream for the transformation result
			//			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			//			StreamResult result = new StreamResult(bout);

			//			transformer.transform(source, result);

			// get the result from the stream
			//			String htmlFormBody = bout.toString();
%>
<form action="SOAPresults" method="post" <% if(supportFileUpload) { %> enctype="multipart/form-data"<%} %>>
<input type="hidden" name="operationName" value="<%=opName%>"> <br>

		<table>
<%

	for (SOAPinputField field : currentOperation.getInputs()) {
		%>
		<tr>
		<td valign="top">
		<%
		if (field.getDocumentation() != null && !field.getDocumentation().equals("")){
			out.print(field.getDocumentation() + ": ");
		} else {
			out.print(field.getName() + ": ");
		}
		%>
		</td>
		<%

		Boolean displayDefaults = (Boolean) session.getAttribute("displayDefaults"); 
		
		List<String> values = field.getPossibleValues();
		List<String> multipleSelectValues = field.getMultipleSelectValues();

		%>
		<td valign="top">
		<%

		if (values != null && values.size() > 0) { 
		%>
			<select name="<%= field.getName() %>">
				<%for (String value : values) { %>
					<option value="<%=value%>" 
					<% if (displayDefaults && field.getDefaultValue() != null && value.equals(field.getDefaultValue())) { %> selected="selected" <%} %>>
					<%=value%>
					</option>
				<%} %>
			</select>
			<br><br>
		<%} else if(multipleSelectValues != null && multipleSelectValues.size() > 0) { %>
			<select name="<%= field.getName() %>" size="<%= multipleSelectValues.size() %>" multiple="multiple">
				<%for (String value : multipleSelectValues) { %>
					<option value="<%=value%>" 
					<% if (displayDefaults && field.getDefaultValue() != null && value.equals(field.getDefaultValue())) { %> selected="selected" <%} %>>
					<%=value%>
					</option>
				<%} %>
			</select>
			<br><br>
			
		<%} else if (field.isBinary() && supportFileUpload == false){ %>
			<span style="color: red">Support for files is switched off.</span>
			<br><br>
			
		<%} else if (field.isBinary() && supportFileUpload == true){ %>
			<input 
				type="file" 
				name="<%= field.getName() %>"
				>
			<br><br>

		<%} else { %>
			<span style="white-space: nowrap;">
			<input
				type="text" 
				name="<%= field.getName() %>"
				id="<%= field.getName() %>"
				<% if (displayDefaults != null && displayDefaults) { %>value="<%=field.getDefaultValue()%>"<% } %>><% 
			if(field.getName().toLowerCase().contains("url")){
				String id = field.getName();
				%>&nbsp;<span style="color: #7979b2;"
					onmouseover="setStyle(this)"
					onmouseout="removeStyle(this)"
					onclick="getUrl('<%=id%>')">view</span>
			<%} %>
			</span>
			<br><br>
<%
		}
		%>
		</td>
		</tr>
		<%

	}

%>
		</table>
 <br>
<input type="submit" value="<% if(loadDefault) out.print(defaultButton); else out.print("Show Results");%>" /> <br>
<br>

</form>

<%
	} catch (Exception e) {
			e.printStackTrace();
		}

	} // if(request .. round2

	if (request.getAttribute("round3") != null) {
%>
<hr>
<br>

<% if(showResultFilesOnly == false) { %>
<b>Response messages:</b>
<br>
<br>

<%
	out.print(session.getAttribute("htmlResponse"));
}
%>
		
<%
	if (request.getAttribute("fileNames") != null) {
			List<String> fileNames = (List<String>) request.getAttribute("fileNames");
			for (String file : fileNames) {
%>

<% if(showResultFilesOnly == false) { %>
<br>
<br>
Attached file:
<%} else {%>
<br>
<br>
<%=defaultResultMessage + ": " %>
<%} %>
<a href="<%=file%>"><%=file%></a>
<%
			}
		}
%>

<%		if(loadDefault == false){  %>
<br><br>
<b>Received SOAP message:</b>
<br>
<textarea cols="100" rows="40" name="textarea">
<%
out.print(session.getAttribute("soapResponse"));
%>	
</textarea>
<%
		}
	} // if(request .. round3
%>




<br>
<br>



</body>
</html>