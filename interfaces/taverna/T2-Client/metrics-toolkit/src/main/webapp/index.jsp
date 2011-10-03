<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="org.apache.commons.io.IOUtils"%>
<%@page import="eu.impact_project.iif.t2.client.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="http://www.impact-project.eu/fileadmin/css/iframe.css" media="screen" />


<script type="text/javascript">

var counterArray = new Array();

function addInputField(inputName){
	if(counterArray[inputName] == null){
		counterArray[inputName] = 0;
	}else{
		counterArray[inputName]++;
	}
	var location = document.getElementById(inputName);
	location.innerHTML += '<input type="text" name="' + inputName + counterArray[inputName] + '">' + "&nbsp;";
}

function changeInputType(oldObject, oType) {
	  var newObject = document.createElement('input');
	  newObject.type = oType;
	  if(oldObject.size) newObject.size = oldObject.size;
	  if(oldObject.value) newObject.value = oldObject.value;
	  if(oldObject.name) newObject.name = oldObject.name;
	  if(oldObject.id) newObject.id = oldObject.id;
	  if(oldObject.className) newObject.className = oldObject.className;
	  oldObject.parentNode.replaceChild(newObject,oldObject);
	  return newObject;
}

function switchInputField(span) {

	var field = span.getElementsByTagName("input")[0];

	var currentType = field.type;
	if (currentType == "text") {
		field = changeInputType(field, "file");
	} else {
		field = changeInputType(field, "text");
	}

	var inputName = field.name;
	var inputs = document.getElementById(inputName).getElementsByTagName("input");
	for (var i = 0; i <= inputs.length; i++) {
		field = inputs[i];
		if (currentType == "text") {
			field = changeInputType(field, "file");
		} else {
			field = changeInputType(field, "text");
		}
	}
	
}

function switchLocation(workflow, workflowCounter, switchTo) {
	if (switchTo == "remote") {
		insertHiddenInput("currentTab" + workflowCounter, "currentTab" + workflowCounter, "remote");
		spanToHideId = workflow + "local";
		spanToShowId = workflow + "remote";
	} else {
		insertHiddenInput("currentTab" + workflowCounter, "currentTab" + workflowCounter, "local");
		spanToHideId = workflow + "remote";
		spanToShowId = workflow + "local";
	}
	
	spanToHide = document.getElementById(spanToHideId);
	spanToShow = document.getElementById(spanToShowId);

	linkToGrayOut = document.getElementById(spanToHideId + "link");
	linkToBlacken = document.getElementById(spanToShowId + "link");

	linkToGrayOut.style.color = "gray";
	linkToBlacken.style.color = "black";
	
	spanToHide.style.display = "none";
	spanToShow.style.display = "inline";
}

function insertHiddenInput(spanId, name, value) {
	var span = document.getElementById(spanId);
	span.innerHTML = '<input type="hidden" value="' + value + '" name="' + name + '"></input>';
}

</script>

<title>IMPACT Metrics Toolkit</title>
</head>
<body>

<%
//request.setAttribute("indexFile", "index_mtk.jsp");

%>
<div class="csc-header csc-header-n1">

<a href="http://www.impact-project.eu/taa/dp/" target="_top">Demonstrator Platform</a>
<hr/>
<br/>

<h1 class="csc-firstHeader">Metrics Toolkit</h1>
</div>
<hr></hr>



<p><br></br>Please upload your workflow files. The tool parses the uploaded workflow description files (T2Flow) and generates a dynamic, interactive web form for each workflow. You 
can provide the input data required for each workflow. Any workflow supported by the IMPACT Interoperability Framework can be evaluated as long
as the following criteria are fulfilled:</p>
<ul>
<li>1. digitized full text (image) and corresponding ground-truth as workflow input</li>
<li>2. full text (OCR results or corrected results) as workflow output</li>
</ul>
<br></br>
<hr></hr>
<form action="WorkflowUploader" method="post" name="uploadWorkflows">
If you login to myExperiment, you can load your workflows from there:
<br><br>
	<table>
	<tr>
	
	<td>
	User:<br>
	<input name="myexp_user" type="text" size="10">
	</td>
	<td>
	Password:<br>
	<input name="myexp_password" type="password" size="10">
	</td>
	
	<td valign="bottom">
	<input type="submit" value="Login">
	</td>
	
	</tr>
	</table>
</form>

<%if (request.getAttribute("login_error") != null) {%>
<div style="color: red"><%=request.getAttribute("login_error") %></div>

<%} 
boolean logged_in = session.getAttribute("logged_in") != null && session.getAttribute("logged_in").equals("true");
%>

<hr></hr>
<form action="WorkflowParser" method="post" enctype="multipart/form-data">
<br></br>

	<table>
			<% 
				String currentTab0 = (String) session.getAttribute("currentTab0");
				boolean tab0_is_local = currentTab0 != null && currentTab0.equals("local");
				boolean tab0_is_remote = currentTab0 != null && currentTab0.equals("remote");
			%>
	<%if (logged_in) { %>
	
	<tr>
		<td>
				
				<span id="currentTab0">
				<%if (currentTab0 != null) { %>
					<input type="hidden" name="currentTab0" value="<%=currentTab0%>"></input>
				<%} else {%>
					<input type="hidden" name="currentTab0" value="remote"></input>
				<%} %>
				</span>
		</td>
		<td>
			<a href="javascript:switchLocation('workflow0', 0, 'local')"
			 <%if (tab0_is_local) {%>style="color: black;"<%} else if (!tab0_is_local) {%>style="color: gray;"<%} %> id="workflow0locallink">
			 <u>local</u></a>
			&nbsp;&nbsp;
			<a href="javascript:switchLocation('workflow0', 0, 'remote')" 
			<%if (tab0_is_local) { %>style="color: gray"<%} else { %>style="color: black;"<%} %> id="workflow0remotelink"><u>myExperiment</u></a>
		</td>
	</tr>
	<%} %>
	<tr>
		<td>
			<b>Workflow&nbsp;1&nbsp;&nbsp;</b>
			
			
		</td>
		<td>
		 	<span id="workflow0local" <%if (tab0_is_remote) { %>style="display: none;"<%} %>><input type="file" name="file_workflow0" size="30"></input></span>
		 	
		 	<%if (logged_in) {
				String selectedGroupName = (String)session.getAttribute("selectedGroupName0");
				Map<String, List<WorkflowInfo>> allWfInfos = (Map<String, List<WorkflowInfo>>) session.getAttribute("allWfInfos");

		 		
		 	%>
			<span id="workflow0remote" <%if (tab0_is_local) { %>style="display: none;"<%} %>>
			<%
				if (allWfInfos != null && allWfInfos.size() == 1) {
				%>
					Group: "<%=selectedGroupName %>"
				<%
				} else if (allWfInfos != null && allWfInfos.size() > 1) {
			%>
				<span id="hiddenInputSelectGroup"></span>
				
				Group:
				<select name="MyExpGroup0" id="MyExpGroup0" style="width: 20em" onchange="insertHiddenInput('hiddenInputSelectGroup', 'selectGroup', 'true'); submit();">
				<%
				Set<String> set = allWfInfos.keySet();
				for (String key : set){
					%>
					<option value="<%=key %>" <%if (key.equals(selectedGroupName)) { %>selected="selected"<%} %>>
					<%=key %>
					</option>
					<%
				}
				%>
				</select>
				
			<%	} // else if (allWfInfos != null ...)
			
			List<WorkflowInfo> wfInfos = allWfInfos.get(selectedGroupName);
			if (wfInfos != null && wfInfos.size() > 0) {
			
			%>
				<br>Workflow:&nbsp;<select name="MyExpWorkflow0" id="MyExpWorkflow0" style="width: 20em">
		<% 
		
			
					for (WorkflowInfo info : wfInfos){
				%>
				<option value="<%=info.getWfId() %>" 
				<%if(session.getAttribute("currentWfId0") != null && session.getAttribute("currentWfId0").equals(info.getWfId())) { %> selected="selected" <%} %>>
				<%=info.getTitle()%>
				</option>
				<%
					}
		
				%>
				</select>
				<%
			} // if (wfInfos != null...
			else {
				%>
				&nbsp;&nbsp;There are no workflows in this group
				<%
			}
		%>
		
		
			</span>
			<%} // if (logged_in)%>
		</td>
	</tr>
	</table>
<br><br>

	<table>
			<% 
				String currentTab1 = (String) session.getAttribute("currentTab1");
				boolean tab1_is_local = currentTab1 != null && currentTab1.equals("local");
				boolean tab1_is_remote = currentTab1 != null && currentTab1.equals("remote");
			%>
	<%if (logged_in) { %>
	<tr>
		<td>&nbsp;</td>
		<td>
			<a href="javascript:switchLocation('workflow1', 1, 'local')" 
			<%if (tab1_is_local) {%>style="color: black;"<%} else if (!tab1_is_local) {%>style="color: gray;"<%} %>  id="workflow1locallink"><u>local</u></a>
			&nbsp;&nbsp;
			<a href="javascript:switchLocation('workflow1', 1, 'remote')" 
			<%if (tab1_is_local) { %>style="color: gray"<%} else { %>style="color: black;"<%} %> id="workflow1remotelink"><u>myExperiment</u></a>
		</td>
	</tr>
	<%} %>
	<tr>
		<td>
			<b>Workflow&nbsp;2&nbsp;&nbsp;</b>
			
				<span id="currentTab1">
				<%if (currentTab1 != null) { %>
					<input type="hidden" name="currentTab1" value="<%=currentTab1%>"></input>
				<%} else {%>
					<input type="hidden" name="currentTab1" value="remote"></input>
				<%} %>
				</span>
			
		</td>
		<td>
		 	<span id="workflow1local" <%if (tab1_is_remote) { %>style="display: none;"<%} %>><input type="file" name="file_workflow1" size="30"></input></span>
		 	
		 	<%if (logged_in) {
				String selectedGroupName1 = (String)session.getAttribute("selectedGroupName1");
				Map<String, List<WorkflowInfo>> allWfInfos = (Map<String, List<WorkflowInfo>>) session.getAttribute("allWfInfos");
		 		
		 	%>
			<span id="workflow1remote" <%if (tab1_is_local) { %>style="display: none;"<%} %>>
			<%
				if (allWfInfos != null && allWfInfos.size() == 1) {
				%>
					Group: "<%=selectedGroupName1 %>"
				<%
				} else if (allWfInfos != null && allWfInfos.size() > 1) {
			%>
				Group:
				<select name="MyExpGroup1" id="MyExpGroup1" style="width: 20em" onchange="insertHiddenInput('hiddenInputSelectGroup', 'selectGroup', 'true'); submit();">
				<%
				Set<String> set = allWfInfos.keySet();
				for (String key : set){
					%>
					<option value="<%=key %>" <%if (key.equals(selectedGroupName1)) { %>selected="selected"<%} %>>
					<%=key %>
					</option>
					<%
				}
				%>
				</select>
				
			<%	} // else if (allWfInfos != null ...)

			List<WorkflowInfo> wfInfos = allWfInfos.get(selectedGroupName1);
			if (wfInfos != null && wfInfos.size() > 0) {
			
			%>
				<br></br>Workflow:
				<select name="MyExpWorkflow1" id="MyExpWorkflow1" style="width: 20em">
		<% 
		
			
					for (WorkflowInfo info : wfInfos){
				%>
				<option value="<%=info.getWfId() %>" 
				<%if(session.getAttribute("currentWfId1") != null && session.getAttribute("currentWfId1").equals(info.getWfId())) { %> selected="selected" <%} %>>
				<%=info.getTitle()%>
				</option>
				<%
					}
				%>
				</select>
				<%

			} // if (wfInfos != null...
			else {
				%>
				&nbsp;&nbsp;There are no workflows in this group
				<%
			}
		%>
		
			</span>
			<%} // if (logged_in)%>
		</td>
	</tr>
	</table>
	
	<%
	boolean printExamples = true;
	if (session.getAttribute("printExamples") != null) {
		printExamples = (Boolean) session.getAttribute("printExamples");
	}
	%>

	<br><br>
	<input type="checkbox" name="printExamples" <%if(printExamples){ %>checked="checked"<%} %>>Show input values, if available<br><br>
	<input type="submit" value="Show input fields"></input>


</form>

<br>
<hr></hr>
<br>
<%
	// is shown after submitting the form above
	if(request.getAttribute("round1") != null || request.getAttribute("round2") != null){
%>
		<form method="post" action="WorkflowRunner" enctype="multipart/form-data">
<%
		ArrayList<Workflow> workflows = (ArrayList<Workflow>) session.getAttribute("workflows");
		int i = 0;
		for (Workflow currentWorkflow : workflows) {
%>
			<b>Workflow <%=i + 1 %></b>
<%
			int noOfInputs = currentWorkflow.getInputs().size();
			if(noOfInputs > 0){
				out.println("input field" + (noOfInputs > 1? "s": ""));
				// construct a form input field for each workflow input
				for(WorkflowInput currentInput : currentWorkflow.getInputs()) {
					String inputName = currentInput.getName();
					int inputDepth = currentInput.getDepth();
					String inputExample = currentInput.getExampleValue();
%>
					<br><%= inputName %>: 
					<span>
						<img onclick="switchInputField(this.parentNode)" alt="switch input type" src="graphics/switch.gif">
						<input type="text" <%if (printExamples) { %>value="<%= inputExample %>"<%} %> name="workflow<%=i + inputName %>">
					</span> 
<%
					if(inputDepth > 0){
%>
						<span id="workflow<%=i + inputName %>"></span>
						<span onclick="addInputField('workflow<%=i + inputName %>')" style="cursor: pointer">+</span>
<%	
					}
				}
			} else {
				out.print("The workflow has no input fields");
			}
			i++;
			out.print("<br><br>");
		} // for (Workflow ...)
%>
		<br>
		<input type="submit" value="Execute workflows"></input>
		</form><br></br>
		<hr></hr>
<%
	} // if ... round1

	if(request.getAttribute("round2") != null) {
		List<List<WorkflowOutputPort>> allOutputs = (List<List<WorkflowOutputPort>>) session.getAttribute("allOutputs");
		
%>
	<table border="0">
	<tr>
<%
		
		int i = 1;
		// go through all workflows
		for (List<WorkflowOutputPort> currentPorts : allOutputs) {
%>
		<td>
		<br><br>
		<b>Workflow <%=i %></b> Outputs
		<br><br>
<%
			// go through all workflow ports
			for (WorkflowOutputPort port : currentPorts){
%>
				<h3><%=port.getName() %>:&nbsp;&nbsp;&nbsp;&nbsp;</h3> 
<%
				// go through all outputs of a port
				int j = 1;
				for (WorkflowOutput output : port.getOutputs()) {
					if (output.isBinary()){
%>
						<a href="FilePrinter?file=<%=output.getUrl() %>" target="_blank">file<%=i %>.<%=j %></a>
<%
					} else {

						// check if the text results contain URL
						String result = output.getValue();
						String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

						if (result.matches(regex)) {
						URL evalUrl = new URL(output.getValue());
						InputStream evalStream = evalUrl.openStream();
						String evalText = IOUtils.toString(evalStream);
						String[] evalSplit = evalText.split("\n");
						out.print("<br>" + evalSplit[0] + "<br>" + evalSplit[1] + "<br>" + evalSplit[2]);
						
%>							
						<br></br><a href="<%=output.getValue() %>" target="_blank">Details</a>
<%
						} else {
							out.print(output.getValue());
						}
						

					}
					if (output != port.getOutputs().get(port.getOutputs().size()-1))
						out.print(",");
					j++;
				} // for ... output
				out.print("<br><br>");
			} // for ... port
			i++;
			out.print("</td>");
		} // for ... currentPorts
		out.print("</tr></table>");
		out.print("<hr>");
	} // if ... round2
%>

</body>
</html>
