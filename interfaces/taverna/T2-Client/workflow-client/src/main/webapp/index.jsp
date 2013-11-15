<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.io.File"%>
<%@page import="java.net.URL"%>
<%@page import="java.util.Properties"%>
<%@page import="eu.impact_project.iif.t2.client.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="css/style.css" media="screen" />

<%
String folder = application.getRealPath("/");
if(!folder.endsWith("/")) {
	folder = folder + "/";
}

Properties props = new Properties();
InputStream stream = new URL("file:" + folder + "config.properties").openStream();

props.load(stream);
stream.close();

String loadDefault = props.getProperty("loadDefaultWebService");
String serviceUser = props.getProperty("serviceUser");
String servicePass = props.getProperty("servicePassword");
String myexperimentUser = props.getProperty("myexperimentUser");
String myexperimentPass = props.getProperty("myexperimentPass");

boolean demoMode = Boolean.parseBoolean(props.getProperty("demoMode"));

boolean printExamples = true;
if (session.getAttribute("printExamples") != null) {
	printExamples = (Boolean) session.getAttribute("printExamples");
}
%>

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
		field.value = "";
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

function showDetails(){

	var selectedWorkflow = document.getElementById("MyExpWorkflow0").value;


	var detailsWindow = window.open("InfoGenerator?id="+selectedWorkflow,"WorkflowDetails",
			"resizable,width=800,height=700,scrollbars,left=200,top=100");

	detailsWindow.focus();
}
<% if (demoMode == false) {%>
function loginMyExperiment ()
{
	var frm = document.getElementById("uploadWorkflows");
	frm.submit();
}
<%}%>

</script>

<title>IMPACT Workflow Client</title>
</head>
<body>
<div class="csc-header csc-header-n1">

<a href="http://www.digitisation.eu/demonstrator-platform/" target="_top">Demonstrator Platform</a>
<hr/>
<br/>

<h1 class="csc-firstHeader">Workflow Client</h1>
</div>
Caution: Only execute workflows with strings as inputs and outputs (i.e. no binary files).
<br>
<br></br>
<hr></hr>
<p></p><br></br>


<table>

<tr>

<%
if (demoMode == false)
{
%>
<td valign="top">
<form action="WorkflowParser" method="post" enctype="multipart/form-data">
Please upload your workflow file:<br></br><br></br>
	<input type="file" name="file_workflow0" size="15"></input>
<br><br>
<!--
	Workflow 2: <input type="file" name="file_workflow1" size="30"></input>

	<br><br>
-->

	<input type="checkbox" name="printExamples" <%if(printExamples){ %>checked="checked"<%} %>>Show input values, if available<br><br>
	<input type="submit" value="Show input fields"></input>

</form>
</td>
<%
}
%>


<td valign="top" style="padding-left: 40px">
<h1>My Experiment</h1>
<% if (demoMode == true) {%>
<form action="WorkflowUploader" method="post" name="uploadWorkflows" id="uploadWorkflows">
	<input name="myexp_user" type="hidden" value="<%=myexperimentUser%>">
	<input name="myexp_password" type="hidden" value="<%=myexperimentPass%>">
</form>
<% } else {%>
<form action="WorkflowUploader" method="post" name="uploadWorkflows">
Or login to MyExperiment and choose a workflow:
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
<br>
<% } %>
<br>

<%if (request.getAttribute("login_error") != null) {%>
<div style="color: red"><%=request.getAttribute("login_error") %></div>

<%} else if (session.getAttribute("logged_in") != null && session.getAttribute("logged_in").equals("true")) {
		String selectedGroupName = (String)session.getAttribute("selectedGroupName0");
		Map<String, List<WorkflowInfo>> allWfInfos = (Map<String, List<WorkflowInfo>>) session.getAttribute("allWfInfos");

		if (allWfInfos != null && allWfInfos.size() == 1) {
			%>
			<div>Group: "<%=selectedGroupName %>"</div>
			<%
		} else if (allWfInfos != null && allWfInfos.size() > 1) {
			%>
			<form action="GroupSelector" method="post">
				Group:
				<select name="MyExpGroup0" id="MyExpGroup0" style="width: 20em" onchange="submit();">
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
				<!--input type="submit" value="Choose"-->
			</form>
			<%

		}


		List<WorkflowInfo> wfInfos = allWfInfos.get(selectedGroupName);
		if (wfInfos != null && wfInfos.size() > 0) {
%>

<form action="WorkflowParser" method="post" enctype="multipart/form-data">
	Workflow:
	<select name="MyExpWorkflow0" id="MyExpWorkflow0" style="width: 20em">
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

	<a href="javascript:showDetails()">Details</a>
	<br><br>

	<%
//	boolean printExamples2 = true;
//	if (session.getAttribute("printExamples2") != null) {
//		printExamples2 = (Boolean) session.getAttribute("printExamples2");
//	}
	%>

	<input type="checkbox" name="printExamples" <%if(printExamples){ %>checked="checked"<%} %>>Show input values, if available<br><br>
	<input type="submit" value="Show input fields"></input>



</form>
<%		} // if (wfInfos != null...
		else {
			%>
			<div>There are no workflows in this group</div>
			<%
		}
	} // else if (session.getAttribute("logged_in")...
	else
	{
		if (demoMode == true) {%>
		<script>
			loginMyExperiment();
		</script>
	<%  }
	}%>
</td>


</tr>

</table>

<br>
<hr></hr>
<br>
<%
	// is shown after submitting the form above
	if(request.getAttribute("round1") != null || request.getAttribute("round2") != null){
		String wf = (String)session.getAttribute("currentWfId");
		if(wf != null && (wf.equals("1041") || wf.equals("1298"))) {
%>
			<div style="color: red">The execution of this workflow is currently blocked</div>

	<%	} else { %>


		<form method="post" action="WorkflowRunner" enctype="multipart/form-data">
<%
		ArrayList<Workflow> workflows = (ArrayList<Workflow>) session.getAttribute("workflows");
		int i = 0;
		for (Workflow currentWorkflow : workflows) {
			boolean error = false;
%>
			<b>Workflow</b>
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
						<input type="text" size="40" <%if (printExamples) { %>value="<%= inputExample %>"<%} %> name="workflow<%=i + inputName %>">
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
			if(request.getAttribute("round2") == null)
			{

				%>
				<input type="hidden" name="user" id="user" value="<%=serviceUser%>">
				<input type="hidden" name="pass" id="pass" value="<%=servicePass%>">
				<%
				currentWorkflow.setWsdls(currentWorkflow.getStringVersion());
				for (Wsdl wsdl:currentWorkflow.getWsdls())
				{
					if (!currentWorkflow.testUrl(wsdl.getUrl())) // Check availability of wsdl
					{
						out.print("<em>" + wsdl.getUrl() + "</em> <em style=\"color:red\">Not avaliable</em><br>");
						error = true;
					}
				}
			}
			if (error == false)
			{%>
				<input type="submit" value="Execute workflow"></input>
				</form><br></br>
		  <%}
		} // for (Workflow ...)
%>
		<hr></hr>
		<br><br>
		<b>Workflow</b> Outputs
		<br><br>
<%
		} // if (wf != null ...) else
	} // if ... round1

	if(request.getAttribute("round2") != null) {
		List<List<WorkflowOutputPort>> allOutputs = (List<List<WorkflowOutputPort>>) session.getAttribute("allOutputs");
		String errors = session.getAttribute("errors").toString();

		if (errors != "")
			out.print(errors);

		int i = 1;
		// go through all workflows
		for (List<WorkflowOutputPort> currentPorts : allOutputs) {
			// go through all workflow ports
			for (WorkflowOutputPort port : currentPorts){
%>
				<i><%=port.getName() %></i>:
<%
				// go through all outputs of a port
				int j = 1;
				for (WorkflowOutput output : port.getOutputs()) {
					if (output.isBinary()){
						//<a href="FilePrinter?file=<%=output.getUrl() " target="_blank">file=i .=j </a>
%>
						<a href="<%=output.getUrl() %>" target="_blank">file</a>
<%
					} else {

						// check if the text results contain URL
						String result = output.getValue();
						String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

						if (result.matches(regex)) {

%>
						<a href="<%=output.getValue() %>" target="_blank"><%=output.getValue() %></a>
<%
						} else {
							out.print(output.getValue().replaceAll("\n", "<br>\n"));
						}


					}
					if (output != port.getOutputs().get(port.getOutputs().size()-1))
						out.print(",");
					j++;
				} // for ... output
				out.print("<br><br>");
			} // for ... port
			i++;
		} // for ... currentPorts
		out.print("<hr>");
	} // if ... round2
%>








</body>
</html>
