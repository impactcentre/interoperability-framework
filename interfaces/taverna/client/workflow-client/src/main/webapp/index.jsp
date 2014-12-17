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

  String style = props.getProperty("styleSheet");
  
  boolean printExamples = true;

  if (session.getAttribute("printExamples") != null) printExamples = (Boolean) session.getAttribute("printExamples");
%>

<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="<%=style%>" media="screen">
    <link rel="stylesheet" type="text/css" href="css/bootstrap.min.css" media="screen" />
    <script type="text/javascript" src="js/main.js"></script>
    <title>IMPACT Workflow Client</title>
  </head>
  <body>
    <div class="csc-header csc-header-n1">
    <a href="http://www.digitisation.eu/demonstrator-platform/" target="_top">Demonstrator Platform</a>
        <h1 class="csc-firstHeader">Workflow Client</h1>
    </div>
    Caution: Only execute workflows with strings as inputs and outputs (i.e. no binary files).

    <form action="WorkflowParser" method="post" enctype="multipart/form-data">
        Please upload your workflow file:
        <input type="file" name="file_workflow0"></input>
        <input type="checkbox" name="printExamples" checked="checked">Show input values, if available
        <input type="submit" value="Show input fields"></input>
    </form>

    <h1>My Experiment</h1>
    <form action="WorkflowUploader" method="post" name="uploadWorkflows">
        Or login to MyExperiment and choose a workflow:
        <label for="myexp_user">User:</label>
        <input name="myexp_user" type="text" size="10">
        <label for="myexp_password">Password:</label>
        <input name="myexp_password" type="password">
        <input type="submit" value="Login">
    </form>
    <%
    if (request.getAttribute("login_error") != null)
    {
        %>
        <%=request.getAttribute("login_error") %>
        <%
    }
    else if (session.getAttribute("logged_in") != null && session.getAttribute("logged_in").equals("true"))
    {
        String selectedGroupName = (String)session.getAttribute("selectedGroupName0");
        Map<String, List<WorkflowInfo>> allWfInfos = (Map<String, List<WorkflowInfo>>) session.getAttribute("allWfInfos");
        if (allWfInfos != null && allWfInfos.size() == 1)
        {
            %>
            <div>Group: "<%=selectedGroupName %>"</div>
            <%
        } else if (allWfInfos != null && allWfInfos.size() > 1)
        {
            %>
            <form action="GroupSelector" method="post">
                <label for="MyExpGroup0">Group:</label>
                <select name="MyExpGroup0" id="MyExpGroup0" onchange="submit();">
                    <%
                    Set<String> set = allWfInfos.keySet();
                    for (String key : set)
                    {
                        %>
                        <option value="<%=key %>"
                        <%
                        if (key.equals(selectedGroupName))
                        {
                            %>
                            selected="selected"
                            <%
                        }
                        %>
                        ><%=key %>
                        </option>
                        <%
                    }
                    %>
                </select>
            </form>
            <%
        }
        List<WorkflowInfo> wfInfos = allWfInfos.get(selectedGroupName);
        if (wfInfos != null && wfInfos.size() > 0)
        {
            %>
            <form action="WorkflowParser" method="post" enctype="multipart/form-data">
            <label for="MyExpWorkflow0">Workflow:</label>
            <select name="MyExpWorkflow0" id="MyExpWorkflow0">
            <%
            for (WorkflowInfo info : wfInfos)
            {
                %>
                <option value="<%=info.getWfId() %>"
                <%
                if (session.getAttribute("currentWfId0") != null && session.getAttribute("currentWfId0").equals(info.getWfId()))
                {
                    %>
                    selected="selected"
                    <%
                }
                %>
                >
                <%=info.getTitle()%>
                </option>
                <%
            }
            %>
             </select>
             <a href="javascript:showDetails()">Details</a>
             <input type="checkbox" name="printExamples"
             <%
             if (printExamples)
             {
                %>
                checked = "checked"
                <%
             }
             %>
             >Show input values, if available
             <input type="submit" value="Show input fields"></input>
             </form>
             <%
        } else
        {
            %>
            <div>There are no workflows in this group</div>
            <%
        }
    }// end logged_in
    // is shown after submitting the form above
    if(request.getAttribute("round1") != null || request.getAttribute("round2") != null)
    {
        String wf = (String)session.getAttribute("currentWfId");
        if(wf != null && (wf.equals("1041") || wf.equals("1298")))
        {
            %>
            <div style="color: red">The execution of this workflow is currently blocked</div>
            <%
        } else
        {
            %>
            <form method="post" action="WorkflowRunner" enctype="multipart/form-data">
                <%
                ArrayList<Workflow> workflows = (ArrayList<Workflow>) session.getAttribute("workflows");
                int i = 0;
                for (Workflow currentWorkflow : workflows)
                {
                    boolean error = false;
                    %>
                    <b>Workflow</b>
                    <%
                    int noOfInputs = currentWorkflow.getInputs().size();

                    if(noOfInputs > 0)
                    {
                        out.println("input field" + (noOfInputs > 1? "s": ""));
                        // construct a form input field for each workflow input
                        for(WorkflowInput currentInput : currentWorkflow.getInputs())
                        {
                            String inputName = currentInput.getName();
                            int inputDepth = currentInput.getDepth();
                            String inputExample = currentInput.getExampleValue();
                            %>
                            <%= inputName %>:
                            <span>
                                <img onclick="switchInputField(this.parentNode)" alt="switch input type" src="graphics/switch.gif">
                                <input type="text" size="40"
                                <%
                                if (printExamples)
                                {
                                    %>
                                    value="<%= inputExample %>"
                                    <%
                                }
                                %>
                                name="workflow<%=i + inputName %>">
                            </span>
                            <%
                            if(inputDepth > 0)
                            {
                                %>
                                <span id="workflow<%=i + inputName %>"></span>
                                <span onclick="addInputField('workflow<%=i + inputName %>')" style="cursor: pointer">+</span>
                                <%
                            }
                        }//end for Inputs
                    }
                    else
                    {
                        out.print("The workflow has no input fields");
                    }
                    i++;

                    if(request.getAttribute("round2") == null)
                    {
                        %>
                        <input type="hidden" name="user" id="user" value="<%=serviceUser%>">
                        <input type="hidden" name="pass" id="pass" value="<%=servicePass%>">
                        <%
                        currentWorkflow.setWsdls(currentWorkflow.getStringVersion());
                        for (Wsdl wsdl:currentWorkflow.getWsdls())
                        {
                            if (!currentWorkflow.testUrl(wsdl.getUrl()))
                            {
                                out.print("<em>" + wsdl.getUrl() + "</em> <em style=\"color:red\">Not avaliable</em><br>");
                                error = true;
                            }
                        }
                    }
                    if (error == false)
                    {
                        %>
                        <input type="submit" value="Execute workflow"></input>
                    </form>
                    <%
                    }
                } // for (Workflow ...)
            %>
            <b>Workflow</b> Outputs
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
