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

<%@page import="eu.impact_project.ws.generic.*"%>
<%@page import="eu.impact_project.ws.WSDLinfo"%>
<%@page import="java.util.*"%>
<%@page import="javax.xml.transform.*"%>
<%@page import="javax.xml.transform.stream.*"%>
<%@page import="java.io.*"%>
<%@page import="java.util.Properties"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.net.URL"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="css/style.css" media="screen" />
<script src="js/jquery-1.7.2.min.js" type="text/javascript"></script>
<script src="js/index.js" type="text/javascript"></script>
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

if(!folder.endsWith("/")) {	
	folder = folder + "/";
}

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
boolean editableInputs = Boolean.parseBoolean(props.getProperty("editableInputs"));
boolean security = Boolean.parseBoolean(props.getProperty("security"));
String user = props.getProperty("user");
String pass = props.getProperty("pass");

SoapService serviceObject = null;
if(session.getAttribute("serviceObject") != null) {
	serviceObject = (SoapService)session.getAttribute("serviceObject");
}

%>
<body <%if(loadDefault && request.getAttribute("round2") == null && request.getAttribute("round3") == null)
{ %>onload="document.forms['defaultForm'].submit()"<%} %>>



<form name="defaultForm" action="SOAPinputs" method="post">

<input type="hidden" name="currentOperation" value="<%=defaultOperation%>">

</form>

<%if (loadDefault) { %>
	<br></br>
	<h1><%= defaultTitle %></h1>

<%}%>


<%if (!loadDefault) { %>

<!-- a href="http://www.digitisation.eu/tools/browse/interoperability-framework/demonstrator-platform/" target="_top">Demonstrator Platform</a-->

<%


	
	List<SoapOperation> soapOperations = serviceObject.getOperations();

	if (request.getAttribute("round1") != null
			|| request.getAttribute("round2") != null
			|| request.getAttribute("round3") != null) {
		
		out.print("<h1>" + session.getAttribute("wsName") + "</h1>");
		out.print("<br>");
		out.print("<span class=\"btn1\">");
		out.print("<a id=\"back\" href=\"http://www.digitisation.eu/tools/browse/interoperability-framework/demonstrator-platform/ \" ONCLICK=\"window.parent.location='http://www.digitisation.eu/tools/interoperability-framework/demonstrator-platform/'\">");
		out.print("<span>Back to Selection</span>");
		out.print("</a></span>");

		out.print("<p>" + serviceObject.getDocumentation() + "</p>");

		SoapOperation currentOperation = null;
		if (session.getAttribute("currentOperation") != null) {
			currentOperation = (SoapOperation) session
					.getAttribute("currentOperation");
		}
%>
<form action="SOAPinputs" method="post">
	<span id="availableOps">
		<label for="currentOperation">Available Operations:</label>
	</span>
	<select	name="currentOperation">
	<%
		for (SoapOperation op : soapOperations) {
	%>
	<option value="<%=op.getName()%>"
		<%if (currentOperation != null
							&& op.getName().equals(currentOperation.getName())) {out.print(" selected");}%>><%=op.getName()%></option>
	<%
		}
	%>
</select> <br><br>
<% 
   Boolean displayDefaults = (Boolean) session.getAttribute("displayDefaults");
   out.print("<br>");
   out.print("<br>");
%>
<input type="checkbox" name="displayDefaults" id="displayDefaults" <% if (displayDefaults == null || displayDefaults) { %>checked="checked"<%} %>>
<label for="displayDefaults">
	Display available default values
</label>
<br><br>
<input type="submit" value="Show Operation Inputs" class="link"></form>

<%
	} // if(request... round1

} // if(!loadDefaults)
			
		
	if (request.getAttribute("round2") != null
			|| request.getAttribute("round3") != null) {


		SoapOperation currentOperation = (SoapOperation) session
				.getAttribute("currentOperation");

		String message = currentOperation.getDefaultRequest();

		String opName = currentOperation.getName();
		String opDocumentation = "";
		try {
			opDocumentation = currentOperation.getDocumentation();
		} catch (IOException e) {
			opDocumentation = "";
		}
		out.print("<br>");
		if (!loadDefault) {
			out.print("<br>");
		out.print("<p>Operation: " + opName + "</p>");
		out.print("<br>");
		out.print(opDocumentation);
		} else {
			out.print("<p>" + defaultDescription + "</p>");

		}
		out.print("<br>");
		try {

%>
<form action="SOAPresults" method="post" <% if(supportFileUpload) { %> enctype="multipart/form-data"<%} %>>
<input type="hidden" name="operationName" value="<%=opName%>"> <br>
        <table>
	<% if (security) { %>

        <tr>
        <td>
	        User:<br>
	        <input type="text" name="user" value="">
        </td>
        <td>
        	Password:<br>  <input type="password" name="pass" value="">
        </td>
        </tr>
        <td>
        <tr>
        <br/>
        </td>
        </tr>
		</table>
	<%} else { %>
		<input type="<% if (!security) {out.print("hidden"); } else {out.print("text");} %>" name="user" value="<%=user%>">
		<input type="<% if (!security) {out.print("hidden"); } else {out.print("password");} %>" name="pass" value="<%=pass%>">
	<%} %>
<%

	for (SoapInput field : currentOperation.getInputs()) {
		%>
		<tr>
		<td valign="top" class="description">
		<%
		out.print(field.getName());
		if (field.getDocumentation() != null && !field.getDocumentation().equals("")){
			out.print(" (" + field.getDocumentation() + ")");
		}
		%>
		</td>
		<%

		Boolean displayDefaults = (Boolean) session.getAttribute("displayDefaults"); 
		
		List<String> values = field.getPossibleValues();

		%>
		<td valign="top">
		<label>
		<%
		if (values != null && values.size() > 0 && !field.isMultiValued()) {
			if (editableInputs == false) 
			{%>
				<select name="<%= field.getName() %>">
						<option value="<%=field.getDefaultValue()%>">
						<%=field.getDefaultValue()%>
						</option>
				</select>
		  <%}
			else
			{%>
				<select name="<%= field.getName() %>">
					<%for (String value : values) { %>
						<option value="<%=value%>" 
						<% if (displayDefaults && field.getDefaultValue() != null && value.equals(field.getDefaultValue())) { %> selected="selected" <%} %>>
						<%=value%>
						</option>
					<%} %>
				</select>
		  <%}%>
		</label>
		<br/>
		<%	
		} else if(field.isMultiValued()) { %>
			<select name="<%= field.getName() %>" size="<%= values.size() %>" multiple="multiple">
				<%for (String value : values) { %>
					<option value="<%=value%>" 
					<% if (displayDefaults && field.getDefaultValue() != null && value.equals(field.getDefaultValue())) { %> selected="selected" <%} %>>
					<%=value%>
					</option>
				<%} %>
			</select>
		<%} else if (field.isBinary() && supportFileUpload == false){ %>
			<span style="color: red">Support for files is switched off.</span>
		<%} else if (field.isBinary() && supportFileUpload == true){ %>
			<input 
				type="file" 
				name="<%= field.getName() %>">
		<%} else { %>
			<span style="white-space: nowrap;">
			<%if (editableInputs == false) 
			  {%>
			     <input
					type="hidden"
					name="<%= field.getName() %>"
					id="<%= field.getName() %>"
					<% if (displayDefaults != null && displayDefaults) { %>value="<%=field.getDefaultValue()%>"<% }%>>
					</br>
					<% if (field.getDefaultValue().startsWith("http")) {%>
					<a href="<%=field.getDefaultValue()%>"><%=field.getDefaultValue()%></a>
					<%} else {
						out.print(field.getDefaultValue());
						out.println();
						}%>
			  <%} else{%>
			     <input
					type="text"
					name="<%= field.getName() %>"
					id="<%= field.getName() %>"
					<% if (displayDefaults != null && displayDefaults) { %>value="<%=field.getDefaultValue()%>"<% }%>>
			  <%}%>
		   <%
			if(field.getName().toLowerCase().contains("url") || field.getDocumentation().toLowerCase().contains("url"))
			{
				String id = field.getName();
				if (editableInputs == true)
				{
				%>&nbsp;<span style="color: #7979b2;"
					onmouseover="setStyle(this)"
					onmouseout="removeStyle(this)"
					onclick="getUrl('<%=id%>')">view</span>
			<%}
			}
			%>
			</span>
			<br><br>
			</label>
<%
		}
		%>
		</td>
		</tr>
		<%

	}

%>
		</table>
		<br/>
<input type="submit" value="<% if(loadDefault) out.print(defaultButton); else out.print("Show Results");%>" class="link" />
</form>

<%
	} catch (Exception e) {
			e.printStackTrace();
		}

	} // if(request .. round2

	if (request.getAttribute("round3") != null) {
		
		SoapOperation currentOperation = (SoapOperation) session
				.getAttribute("currentOperation");

%>

<% if(showResultFilesOnly == false) { %>
<br>

<%
	for (SoapOutput output : currentOperation.getOutputs()) 
	{
		String name = output.getName();
		String value = output.getValue();
		if ((name == "processingUnit") || (name == "returncode") || (name == "Created") ||
			 (name == "toolProcessingTime") || (name == "success")) 
		{}
		else if (((name == "processingLog")) || (name == "log"))
		{
			out.println("<h3 id=\"mostrarLog\">" + name + "</h3>");
			out.println("<div id=\"msgid1Log\">");
			out.println("<h3 id=\"ocultarLog\">" + name + "</h3>");
			out.println(value);
			out.println("</div>");
		}
		else if (value.startsWith("http")) {
			out.println("<h3>" + output.getName() + "</h3>");
			out.println("<div>");
			out.println("<a target=\"_blank\" href='" + value + "'>");
			out.println(value);
			out.println("</a>");
			out.println("</div>");	
		}
		else if ((name == "time"))
		{
			out.println("<h3>" + output.getName() + "</h3>");
			out.println(value + " ms");			
		}
		else {
			out.println("<h3>" + output.getName() + "</h3>");
			out.println(value);
		}
	}
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
<h3 id="mostrar">Received SOAP message</h3>
<br>
<div id="msgid1">
	<h3 id="ocultar">Received SOAP message</h3>
	</br>
	<textarea cols="100" rows="40" name="textarea">
</div>
<%
out.print(currentOperation.getResponse());
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