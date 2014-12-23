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

<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ page import="eu.impact_project.iif.ws.generic.*" %>
<%@ page import="eu.impact_project.wsclient.WSDLinfo" %>
<%@ page import="java.util.*" %>
<%@ page import="javax.xml.transform.*" %>
<%@ page import="javax.xml.transform.stream.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLEncoder" %>
<%
	String folder = application.getRealPath("/");

	if(!folder.endsWith("/"))
	{
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

	String user = (String) session.getAttribute("wsUser");
	String pass = (String) session.getAttribute("wsPass");

	String style = props.getProperty("styleSheet");

	//filter null
	user = user!=null?user:"";
	pass = pass!=null?pass:"";

	SoapService serviceObject = null;
	if(session.getAttribute("serviceObject") != null) {
		serviceObject = (SoapService)session.getAttribute("serviceObject");
	}
%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<link rel="stylesheet" type="text/css" href="<%=style%>" media="screen" />
		<link rel="stylesheet" type="text/css" href="css/bootstrap.min.css" media="screen" />
		<script src="js/jquery-1.7.2.min.js" type="text/javascript"></script>
		<script src="js/index.js" type="text/javascript"></script>
		<script src="js/dragdrop.js" type="text/javascript"></script>
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
	<body
		<%
		if(loadDefault && request.getAttribute("round2") == null && request.getAttribute("round3") == null)
		{
			%>
			onload="document.forms['defaultForm'].submit()"
			<%
		}
		%>
	>

	<form role="form" name="defaultForm" action="SOAPinputs" method="post" class="searchform form-group">
		<input type="hidden" name="currentOperation" value="<%=defaultOperation%>">
	</form>
	<%
	if (loadDefault)
	{
		%>
		<h1><%= defaultTitle %></h1>
		<%
	}
	else
	{
		List<SoapOperation> soapOperations = serviceObject.getOperations();

		if (request.getAttribute("round1") != null
				|| request.getAttribute("round2") != null
				|| request.getAttribute("round3") != null)
		{
			out.print("<h1>" + session.getAttribute("wsName") + "</h1>");
			out.print("<p>" + serviceObject.getDocumentation() + "</p>");

			SoapOperation currentOperation = null;
			if (session.getAttribute("currentOperation") != null)
			{
				currentOperation = (SoapOperation) session.getAttribute("currentOperation");
			}
			%>
			<form role="form" action="SOAPinputs" method="post" class="formSoapInputs searchform form-group">
				<div class="form-group soapinputs">
					<label for="currentOperation">Available Operations:</label>
					<select	name="currentOperation" class="search-field form-control">
					<%
					for (SoapOperation op : soapOperations)
					{
						%>
						<option value="<%=op.getName()%>"
							<%
							if (currentOperation != null && op.getName().equals(currentOperation.getName()))
							{
								out.print(" selected");
							}
							%>
						>
						<%=op.getName()%>
						</option>
					<%
					}
					%>
					</select>
					<%
					Boolean displayDefaults = (Boolean) session.getAttribute("displayDefaults");
					%>
					<label class="checkbox-inline">
					<input type="checkbox" name="displayDefaults" id="displayDefaults"
						<%
						if (displayDefaults == null || displayDefaults)
						{
							%>
							checked="checked"
							<%
						}
						%>
					/>
					Display available default values
					</label>
					<input class="btn btn-default" type="submit" value="Show Operation Inputs" class="link">
				</div>
			</form>
		<%
		} // if(request... round1
	} // if(!loadDefaults)

	if (request.getAttribute("round2") != null
			|| request.getAttribute("round3") != null)
	{
		SoapOperation currentOperation = (SoapOperation) session
				.getAttribute("currentOperation");
		String message = currentOperation.getDefaultRequest();
		String opName = currentOperation.getName();
		String opDocumentation = "";

		try {
			opDocumentation = currentOperation.getDocumentation();
		} catch (IOException e)
		{
			opDocumentation = "";
		}
		if (!loadDefault)
		{
			out.print("<p>Operation: " + opName + "</p>");
			out.print("<span>" + opDocumentation + "</span>");
		} else {
			out.print("<p>" + defaultDescription + "</p>");
		}
		try
		{
		%>
		<form role="form" action="SOAPresults" method="post" <% if(supportFileUpload) { %> enctype="multipart/form-data"<%} %> class="formSoapResults searchform form-group">
			<input type="hidden" name="operationName" value="<%=opName%>">
			<%
			if (security)
			{%>
				<div class="form-group login">
					<label for="user">User:</label>
					<input class="search-field form-control" type="text" name="user" value="<%=user%>">
					<label for="pass">Password:</label>
					<input class="search-field form-control" type="password" name="pass" value="<%=pass%>">
				</div>
			<%
			} else { %>
				<input type="<% if (!security) {out.print("hidden"); } else {out.print("text");} %>" name="user" value="<%=user%>">
				<input type="<% if (!security) {out.print("hidden"); } else {out.print("password");} %>" name="pass" value="<%=pass%>">
			<%
			}
			for (SoapInput field : currentOperation.getInputs())
			{
				Boolean displayDefaults = (Boolean) session.getAttribute("displayDefaults");
				List<String> values = field.getPossibleValues();


				if (field.getDocumentation() != null && !field.getDocumentation().equals(""))
				{
					%>
						<label for="<%= field.getName() %>">
					<%
						out.print(field.getDocumentation());
					%>
						</label>
					<%
				}
				if (values != null && values.size() > 0 && !field.isMultiValued())
				{
					if (editableInputs == false)
					{
						%>
						<select name="<%= field.getName() %>" class="search-field form-control">
								<option value="<%=field.getDefaultValue()%>">
								<%=field.getDefaultValue()%>
								</option>
						</select>
						<%
					}else
					{
						%>
						<select name="<%= field.getName() %>" class="search-field form-control">
							<%
							for (String value : values)
							{%>
								<option value="<%=value%>"
									<%
									if (displayDefaults && field.getDefaultValue() != null && value.equals(field.getDefaultValue()))
									{%>
										selected="selected" <%
									}%>
									><%=value%>
								</option>
							<%
							}%>
						</select>
						<%
					}
				}
				else if(field.isMultiValued())
				{
				%>
					<select name="<%= field.getName() %>" size="<%= values.size() %>" multiple="multiple" class="search-field form-control">
						<%for (String value : values) { %>
							<option value="<%=value%>"
							<% if (displayDefaults && field.getDefaultValue() != null && value.equals(field.getDefaultValue())) { %> selected="selected" <%} %>>
							<%=value%>
							</option>
						<%} %>
					</select>
				<%
				} else if (field.isBinary() && supportFileUpload == false)
				{
					%>
					<span style="color: red">Support for files is switched off.</span>
					<%
				} else if (field.isBinary() && supportFileUpload == true)
				{
					%>
					<input class="search-field form-control" type="file" name="<%= field.getName() %>">
					<%
				} else
				{
					if (editableInputs == false)
					{
						%>
						<input type="hidden" name="<%= field.getName() %>" id="<%= field.getName() %>"
							<%
							if (displayDefaults != null && displayDefaults)
							{
								%>
								value="<%=field.getDefaultValue()%>"
								<%
							}
							%>
						/>
						<%
						if (field.getDefaultValue().startsWith("http"))
						{
							%>
							<a href="<%=field.getDefaultValue()%>"><%=field.getDefaultValue()%></a>
							<%
						}
						else
						{
							out.print(field.getDefaultValue());
						}
					}
					else // Editable inputs
					{
						%>
                    	<input
                    	<%
						if (field.getDefaultValue().toLowerCase().contains("http"))
								out.print("class=\"search-field form-control inputurl\"");
						else	out.print("class=\"search-field form-control\"");

						if (displayDefaults != null && displayDefaults)
						{
							%>
							value="<%=field.getDefaultValue()%>"
							<%
						}
						%>
						type="text" name="<%= field.getName() %>" id="<%= field.getName() %>"/>
						<%
						if (field.getDefaultValue().toLowerCase().contains("http"))
						{
							String id = field.getName();
							if (editableInputs == true)
							{
								%>
								<span style="color: #7979b2;" onmouseover="setStyle(this)" onmouseout="removeStyle(this)" onclick="getUrl('<%=id%>')">view</span>
								<p class="dragandrophandler upload-drop-zone" id="<%=field.getName()%>_dragandrop">Drag and drop a file here...</p>
								<%
							}
						}
					}
				}
			}
			%>
			<script>loadDragAndDrop();</script>
			<input class="btn btn-default" type="submit" value="<% if(loadDefault) out.print(defaultButton); else out.print("Show Results");%>" class="link"/>
		</form>
		<%
		} catch (Exception e)
		{
				e.printStackTrace();
		}

	} // if(request .. round2
	if (request.getAttribute("round3") != null)
	{
		SoapOperation currentOperation = (SoapOperation) session.getAttribute("currentOperation");

		if(showResultFilesOnly == false)
		{
			String time = "";
			String message = "";
			String log = "";
			String outputMessage = "";
			String otherMessages = "";

			for (SoapOutput output : currentOperation.getOutputs())
			{
				String name = output.getName();
				String value = output.getValue();

				if (name == "log")
				{
					log = "<h3 id=\"mostrarLog\">Log</h3>";
					log += "<div id=\"msgid1Log\">";
					log += "<h3 id=\"ocultarLog\">" + name + "</h3>";
					log += value;
					log += "</div>";
				}
				else if (value.startsWith("http")) {
					String url = "";
					if (value.indexOf("?") > -1) 
					{
						url = value.substring(0, value.indexOf("?"));
						url += URLEncoder.encode(value.substring(value.indexOf("?")));
					}
					else
						url = value;
								
					
					outputMessage = "<h3>Output</h3>";
					outputMessage += "<div>";
					outputMessage += "<a target=\"_blank\" href='" + url + "'>";
					outputMessage += value;
					outputMessage += "</a>";
					outputMessage += "</div>";
				}
				else if (name == "message")
				{
					message = "<h3>Message</h3>";
					message += value;
				}
				else if (name == "time")
					time = value + " ms";
				else
				{
					otherMessages += "<div>";
					otherMessages += "<h3>" + name + "</h3>";
					otherMessages += value;
					otherMessages += "</div>";
				}
			}
			if (outputMessage.length() > 0)
			{
				out.println( outputMessage );
				out.println( message + ".  " + time + "." );
				out.println( log );
			}
			else
			{
				out.println(otherMessages);
			}
		}
		if (request.getAttribute("fileNames") != null)
		{
				List<String> fileNames = (List<String>) request.getAttribute("fileNames");
				for (String file : fileNames)
				{
					if(showResultFilesOnly == false)
					{
						%>
						Attached file:
						<%
					} else
					{
						%>
						<%=defaultResultMessage + ": " %>
						<%
					}
					%>
					<a href="<%=file%>"><%=file%></a>
					<%
				}
		}
	} // if(request round3
	%>
	</body>
</html>