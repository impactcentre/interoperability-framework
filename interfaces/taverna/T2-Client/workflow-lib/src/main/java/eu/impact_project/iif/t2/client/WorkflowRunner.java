/*
	
	Copyright 2011 The IMPACT Project
	
	@author Dennis
	@version 0.1

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

package eu.impact_project.iif.t2.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.t2.service.webservice.resource.DataValue;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.xmlbeans.impl.util.Base64;
import org.xml.sax.SAXException;


import uk.org.taverna.server.client.*;
import uk.org.taverna.server.client.connection.HttpBasicCredentials;
import uk.org.taverna.server.client.connection.UserCredentials;

import uk.org.taverna.server.client.InputPort;
import uk.org.taverna.server.client.OutputPort;
import uk.org.taverna.server.client.PortListValue;
import uk.org.taverna.server.client.PortDataValue;
import uk.org.taverna.server.client.Run;
import uk.org.taverna.server.client.RunStatus;
import uk.org.taverna.server.client.Server;
import uk.org.taverna.server.client.connection.HttpBasicCredentials;
import uk.org.taverna.server.client.connection.UserCredentials;
import uk.org.taverna.server.client.connection.params.ConnectionParams;

/**
 * Executes the chosen workflow by uploading it to taverna server
 */

public class WorkflowRunner extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static Server tavernaRESTClient;

	public WorkflowRunner() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * Executes the chosen workflow by uploading it to taverna server
	 */
	@SuppressWarnings("deprecation")
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// extract all the html form parameters from the request,
		// including files (files are encoded to base64)
		Map<String, String> htmlFormItems = Helper.parseRequest(request);
		// will contain outputs from all ports of all workflows
		List<List<WorkflowOutputPort>> allOutputs = new ArrayList<List<WorkflowOutputPort>>();
		HttpSession session = request.getSession(true);
		ArrayList<Workflow> workflows = (ArrayList<Workflow>) session.getAttribute("workflows");
		
		try {
			String address = null;
			long duration = 0;
			long startTime = System.currentTimeMillis();
            address = "https://taverna.taverna@localhost:8443/taverna-server";
            URI serverURI = new URI(address);
            // connect to server
            Server tavernaRESTClient = new Server(serverURI);
				
			if (tavernaRESTClient != null)
			{
				Run runID = null;
				boolean urlFault = false;
				List<String> invalidUrls = new ArrayList<String>();
				List<Run> runIDs = new ArrayList<Run>();
				UserCredentials user = null;
				String cred = "taverna:taverna";
				
				user = new HttpBasicCredentials(cred);
				int j = 0;
				// put all the workflows and their inputs onto the server and
				// execute the corresponding jobs
				for (Workflow currentWorkflow : workflows) {
					// upload the workflow to server
					String workflowAsString = currentWorkflow.getStringVersion();
					byte[] currentWorkflowBytes = workflowAsString.getBytes();
					String userForm = htmlFormItems.get("user");
					String passForm = htmlFormItems.get("pass");
					
					runID = tavernaRESTClient.createRun(currentWorkflowBytes, user);
										
					int cont = 0;
					for (Wsdl currentWsdl : currentWorkflow.getWsdls()) {
						currentWsdl.setUser(userForm);
						currentWsdl.setPass(passForm);
						
						System.out.print("\n Runner USER : " +  currentWsdl.getUser() + "\n");
						System.out.print("\n Runner USER : " +  currentWsdl.getPass() + "\n");
						
						System.out.print("\n WSDL : " +  currentWsdl.getUrl() + "\n");
						// Set credentials for each web service
						//if ((currentWsdl.getUser() != null) && (currentWsdl.getPass() != null))
							//	if ((currentWsdl.getUser().length() >0) && (currentWsdl.getPass().length() >0))
								//	runID.setServiceCredential(new URI(currentWsdl.getUrl()), currentWsdl.getUser(), currentWsdl.getPass());
						cont++;
					}
					
					String inputsText = "";
					for (String currentInput : htmlFormItems.keySet()) {
						inputsText += htmlFormItems.get(currentInput) + " ";
					}
					System.out.print("Input form: " + inputsText );
					
					currentWorkflow.setUrls(inputsText);
					// currentWorkflow.setUrls(workflowAsString);
					for (String currentUrl : currentWorkflow.getUrls()) {
					      if (!currentWorkflow.testUrl(currentUrl))
					      {
					    	  urlFault = true;
					    	  if (!invalidUrls.contains(currentUrl))
					    		  invalidUrls.add(currentUrl);
					    	  System.out.println("Url not available: " + currentUrl);
					      }
					      // else System.out.println("Url OK: " + currentUrl);
					}
					
					if (!urlFault)
					{
						// will contain all the inputs for the current workflow
						Map<String, DataValue> inputData = new HashMap<String, DataValue>();
						
						for (WorkflowInput currentInput : currentWorkflow.getInputs()) {
	
							String currentName = currentInput.getName();
							String currentNamePrefixed = "workflow" + j + currentName;
							int currentDepth = currentInput.getDepth();
	
							// get the current input value
							String currentValue = htmlFormItems.get(currentNamePrefixed);
	
							// if the inputs are just simple values
							if (currentDepth == 0) {
								// put the value into taverna-specific map
								inputData.put(currentName, new DataValue(currentValue));
	
								// if the inputs are a list of values
							} else if (currentDepth > 0) {
								// then the values must be nested in a list
								List<DataValue> valueList = new ArrayList<DataValue>();
	
								// add the current value
								valueList.add(new DataValue(currentValue));
	
								// add all the additional values
								int i = 0;
								// the values in the html form are stored in format name+index
								while (htmlFormItems.get(currentNamePrefixed + i) != null
										&& !((String) htmlFormItems
												.get(currentNamePrefixed + i))
												.equals("")) {
									String additionalValue = htmlFormItems
											.get(currentNamePrefixed + i);
									valueList.add(new DataValue(additionalValue));
									i++;
								}
								// store the list in the map
								inputData.put(currentName, new DataValue(valueList));
							}
						}
	
						Map<String, InputPort> inputPorts = runID.getInputPorts();
						
						// convert input values from html form to taverna-specific objects
						for (Map.Entry<String, DataValue> inputWorkflow : inputData.entrySet())
						{
							runID.getInputPort(inputWorkflow.getKey()).setValue(inputWorkflow.getValue().toString());
							//System.out.println("INPUT: " +  inputWorkflow.getValue().toString());
						}
						
						runID.start();
						//System.out.println("Run URL: "+ runID.getURI() );
						runIDs.add(runID);
						//System.err.print("Run UUID: "+ runID.getIdentifier() + " STATUS:" + runID.getStatus() );
						
						j++;
					}
			
					// wait until all jobs are done
					for (Run currentRunID : runIDs)
					{
						while (currentRunID.isRunning())
						{
							try {
								duration = System.currentTimeMillis() - startTime;
								System.out.println("Waiting for job [" + currentRunID.getIdentifier()
										+ "] to complete (" + (duration / 1000f) + ")" + " STATUS:" + runID.getStatus());
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								System.out.println("HOPELESS");
							}
						}
					}

					// process the outputs
					int workflowIndex = 0;
					for (Run currentRunID : runIDs) {
						// will contain outputs from all ports of the current workflow
						List<WorkflowOutputPort> workflowOutputPorts = new ArrayList<WorkflowOutputPort>();
	
						if (currentRunID.isFinished())
						{
							System.out.println("Owner: " + currentRunID.isOwner());
							// get the outputs of the current job
							if (currentRunID.isOwner())
							{
								System.out.println("Output state: " + currentRunID.getExitCode());
								Map<String, OutputPort> outputPorts = null;
								if (currentRunID.getOutputPorts() != null)
										outputPorts = currentRunID.getOutputPorts();
								for (Map.Entry<String, OutputPort> outputPort : outputPorts.entrySet())
								{
									WorkflowOutputPort workflowOutPortCurrent = new WorkflowOutputPort();
		
									if (outputPort != null)
									{
										if (outputPort.getValue().getDepth() == 0)
										{
											workflowOutPortCurrent.setOutput(outputPort.getValue(),false);
											workflowOutputPorts.add(workflowOutPortCurrent);
										}
										else
										{
											System.out.println("outputName : " + outputPort.getKey());
											workflowOutPortCurrent.setOutput(outputPort.getValue(), currentRunID, outputPort.getKey(), outputPort.getValue().getDepth());
											workflowOutputPorts.add(workflowOutPortCurrent);
										}
									}
								}
								currentRunID.delete();
							}
						}
						// else System.out.println("[" + currentRunID.getIdentifier() + "] Still not finished. SKIP");
					
						allOutputs.add(workflowOutputPorts);
						workflowIndex++;
						
					}
				}		
				//if (allOutputs.isEmpty())
				if (urlFault)
				{
					String error = "";
					for (String url:invalidUrls)
						error += url + "<br>";
					System.out.println("ERRORS: " + error);
					session.setAttribute("errors", "<em style=\"color:red\">Resources not available:</em> <br>" + error);
				}
				else
				{
					session.setAttribute("errors", "");
				}
				
				session.setAttribute("allOutputs", allOutputs);
				request.setAttribute("round2", "round2");
		
				duration = System.currentTimeMillis() - startTime;
				System.out.println("Jobs took " + (duration / 1000f) + " seconds");
		
				// get back to JSP
				RequestDispatcher rd = getServletContext()
						.getRequestDispatcher("/");
				rd.forward(request, response);
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}

	/**
	 * Analyzes the taverna output data structure and stores the output values
	 * in output objects
	 * 
	 */

    private void transferOutputValues(int workflowIndex, String portName,
    		PortDataValue result, List<WorkflowOutput> outputs) {
            //PortValue result, List<WorkflowOutput> outputs) {

    // go through all taverna data items
            // if the current item contains a list of values
            if (result != null) 
            {
                    String currentValue = (String) result.getDataAsString();
                    // create an output object
                    WorkflowOutput wfOut = processOutput(workflowIndex,currentValue, portName, outputs.size());
                    // add the object to the outputs
                    outputs.add(wfOut);
            }
}


	/**
	 * Creates an output object. Strings that "look like" base64 will be decoded
	 * and stored into files.
	 * 
	 */
	private WorkflowOutput processOutput(int workflowIndex, String output,
			String portName, int listIndex) {

		WorkflowOutput wfOutput = new WorkflowOutput();
		try {

			// the length of the analyzed part of the string to find out if
			// it
			// is base64
			int headerLength = 100;

			boolean longString = false;
			String header = "";

			// find out if the string is that long at all
			if (output.length() > headerLength) {
				header = output.substring(0, headerLength);
				longString = true;
			}

			// check if the string contains only base64 characters
			// if yes, then decode it and store in a file
			if (longString && header.matches("[a-zA-Z0-9+/=]+")) {

				// path to the server directory
				String serverPath = getServletContext().getRealPath("/");
				String tmpPath = ((File) getServletContext().getAttribute(
						"javax.servlet.context.tempdir")).getAbsolutePath();
				// the actual path to store the file
				String fileName = "wf" + workflowIndex + portName + listIndex;
				File file = new File(tmpPath + "/" + fileName);
				OutputStream outStream = new FileOutputStream(file);

				// decode the base64 string
				InputStream inStream = new ByteArrayInputStream(Base64
						.decode(output.getBytes()));

				// save the file
				BufferedInputStream bis = new BufferedInputStream(inStream);
				int bufSize = 1024 * 8;
				byte[] bytes = new byte[bufSize];
				int count = bis.read(bytes);
				while (count != -1 && count <= bufSize) {
					outStream.write(bytes, 0, count);
					count = bis.read(bytes);
				}
				if (count != -1) {
					outStream.write(bytes, 0, count);
				}
				outStream.close();

				// set the output object properties
				wfOutput.setBinary(true);
				wfOutput.setUrl(fileName);

			} else {
				// or else just store the string in the object
				wfOutput.setBinary(false);
				wfOutput.setValue(output);
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return wfOutput;
	}
}
