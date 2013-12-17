<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.io.File"%>
<%@page import="java.net.URL"%>
<%@page import="java.util.Properties"%>
<%@page import="eu.impact_project.iif.t2.client.*"%>

<%
  String folder = application.getRealPath("/");
  if (!folder.endsWith("/")) folder = folder + "/";

  Properties props = new Properties();
  InputStream stream = new URL("file:" + folder + "config.properties").openStream();
  
  props.load(stream);
  stream.close();
  
  String loadDefault = props.getProperty("loadDefaultWebService");
  String serviceUser = props.getProperty("serviceUser");
  String servicePass = props.getProperty("servicePassword");
  String myexperimentUser = props.getProperty("myexperimentUser");
  String myexperimentPass = props.getProperty("myexperimentPass");
  
  boolean printExamples = true;

  if (session.getAttribute("printExamples") != null) printExamples = (Boolean) session.getAttribute("printExamples");
%>

<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="css/style.css" media="screen">
    <script type="text/javascript" src="js/main.js"></script>
    <title>IMPACT Workflow Client</title>
  </head>
  <body>
    <div class="csc-header csc-header-n1">
    <a href="http://www.digitisation.eu/demonstrator-platform/" target="_top">Demonstrator Platform</a>
    <hr><br/><h1 class="csc-firstHeader">Workflow Client</h1>
    </div>
    Caution: Only execute workflows with strings as inputs and outputs (i.e. no binary files).
    <br><br><br><hr><p></p><br><br> 
    <table>
      <tr>
      
      <td valign="top">
      <form action="WorkflowParser" method="post" enctype="multipart/form-data">
        Please upload your workflow file:<br><br><br><br>
        <input type="file" name="file_workflow0" size="15"></input>
        <br><br>
        <input type="checkbox" name="printExamples" checked="checked">Show input values, if available<br><br>
        <input type="submit" value="Show input fields"></input>
      </form>
      </td>

      <td valign="top" style="padding-left: 40px">
      <h1>My Experiment</h1>
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
      <br>
<%
      if (request.getAttribute("login_error") != null) {
%>
        <div style="color: red">
        <%=request.getAttribute("login_error") %>
        </div>

<%
      } else if (session.getAttribute("logged_in") != null && session.getAttribute("logged_in").equals("true")) { 
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
            <option value="<%=key %>" 
<%
              if (key.equals(selectedGroupName)) {
%>
                selected="selected"
<%            
            } 
%>          >
            <%=key %>
            </option>
<%
          }
%>
          </select>
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
          for (WorkflowInfo info : wfInfos) {
%>
            <option value="<%=info.getWfId() %>" 
<% 
            if (session.getAttribute("currentWfId0") != null && session.getAttribute("currentWfId0").equals(info.getWfId())) {
%> 
                selected="selected" 
<%  
            } 
%>          >
            <%=info.getTitle()%>
            </option>
<%
          }
%>
          </select>
          <a href="javascript:showDetails()">Details</a>
          <br><br>
          <input type="checkbox" name="printExamples" 
<%
          if (printExamples) {
%>
            checked = "checked"
<%        
          } 
%>        
          >Show input values, if available<br><br>
          <input type="submit" value="Show input fields"></input>
          </form>
<%      
        } else {
%>
          <div>There are no workflows in this group</div>
<%
        }
      }
%>
      </td>
      </tr>
    </table>
    <br>
    <hr>
    <br>
<%
    // is shown after submitting the form above
    if(request.getAttribute("round1") != null || request.getAttribute("round2") != null) {
        String wf = (String)session.getAttribute("currentWfId");
        if(wf != null && (wf.equals("1041") || wf.equals("1298"))) {
%>
          <div style="color: red">The execution of this workflow is currently blocked</div>
            
<%  
        } else {
%>
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
            if(noOfInputs > 0) {
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
                      <input type="text" size="40"
<%
                      if (printExamples) { 
%>
                          value="<%= inputExample %>"
<%
                      } 
%>                    name="workflow<%=i + inputName %>">
                    </span> 
<%
                    if(inputDepth > 0) {
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
            if(request.getAttribute("round2") == null) {
                
%>
              <input type="hidden" name="user" id="user" value="<%=serviceUser%>">
              <input type="hidden" name="pass" id="pass" value="<%=servicePass%>">
<%
              currentWorkflow.setWsdls(currentWorkflow.getStringVersion());
              for (Wsdl wsdl:currentWorkflow.getWsdls()) {
                if (!currentWorkflow.testUrl(wsdl.getUrl())) {
                    out.print("<em>" + wsdl.getUrl() + "</em> <em style=\"color:red\">Not avaliable</em><br>");
                    error = true;
                }
              }
            }
            if (error == false) {
%>
              <input type="submit" value="Execute workflow"></input>
              </form><br></br>
<%
            }
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
      if (errors != "") out.print(errors);
      // go through all workflows
      for (List<WorkflowOutputPort> currentPorts : allOutputs) {
        // go through all workflow ports
        for (WorkflowOutputPort port : currentPorts) {
%>
          <i><%=port.getName() %></i>: 
<%
          // go through all outputs of a port
          for (WorkflowOutput output : port.getOutputs()) {
            if (output.isBinary()) {
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
          } // for ... output
          out.print("<br><br>");
        } // for ... port
      } // for ... currentPorts
      out.print("<hr>");
    } // if ... round2
%>

  </body>
</html>
