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
import java.io.PrintWriter;
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

import org.apache.xmlbeans.impl.util.Base64;

import net.sf.taverna.t2.service.webservice.resource.DataResource;
import net.sf.taverna.t2.service.webservice.resource.DataValue;
import net.sf.taverna.t2.service.webservice.resource.JobResource;
import net.sf.taverna.t2.service.webservice.rest.TavernaRESTClient;

/**
 * Executes the chosen workflow by uploading it to taverna server
 */

public class WorkflowRunner extends HttpServlet {
	private static final long serialVersionUID = 1L;

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
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		try {

			// extract all the html form parameters from the request,
			// including files (files are encoded to base64)
			Map<String, String> htmlFormItems = Helper.parseRequest(request);

			HttpSession session = request.getSession(true);

			ArrayList<Workflow> workflows = (ArrayList<Workflow>) session
					.getAttribute("workflows");

			// connect to server
			TavernaRESTClient tavernaRESTClient = new TavernaRESTClient();
			tavernaRESTClient
					.setBaseURL("http://localhost:9080/tavernaserver/rest");

			Long jobID = 0L;

			// job ids will be used later to retrieve the outputs
			List<Long> jobIDs = new ArrayList<Long>();

			int j = 0;
			// put all the workflows and their inputs onto the server and
			// execute the corresponding jobs
			for (Workflow currentWorkflow : workflows) {

				// will contain all the inputs for the current workflow
				Map<String, DataValue> inputData = new HashMap<String, DataValue>();

				// convert input values from html form to taverna-specific
				// objects
				for (WorkflowInput currentInput : currentWorkflow.getInputs()) {

					String currentName = currentInput.getName();
					String currentNamePrefixed = "workflow" + j + currentName;
					int currentDepth = currentInput.getDepth();

					// get the current input value
					String currentValue = htmlFormItems
							.get(currentNamePrefixed);

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
						// the values in the html form are stored in format
						// name+index
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
				// upload the inputs to taverna server
				Long inputID = tavernaRESTClient.addData(new DataResource(
						inputData));

				// upload the workflow to server
				String workflowAsString = currentWorkflow.getStringVersion();
				Long workflowID = tavernaRESTClient
						.addWorkflow(workflowAsString);

				// create a job on server, which is automatically executed
				jobID = tavernaRESTClient.addJob(workflowID, inputID);
				// Long jobID = tavernaRESTClient.addJob(workflowID, null);

				jobIDs.add(jobID);

				System.err.println("-----------job id : " + jobID);

				j++;
			}

			// wait until all jobs are done
			long startTime = System.currentTimeMillis();
			long duration = 0;
			for (Long currentJobID : jobIDs) {

				while (!tavernaRESTClient.getJobStatus(currentJobID).equals(
						"COMPLETE")) {
					duration = System.currentTimeMillis() - startTime;
					System.out.println("Waiting for job [" + currentJobID
							+ "] to complete (" + (duration / 1000f) + ")");
					Thread.sleep(1000);
				}
			}

			// System.out.println(outputData);
			// tavernaRESTClient.deleteWorkflow(workflowID);
			// tavernaRESTClient.deleteJob(jobID);
			// tavernaRESTClient.deleteData(inputID);
			// tavernaRESTClient.deleteData(outputID);

			// process the outputs

			// will contain outputs from all ports of all workflows
			List<List<WorkflowOutputPort>> allOutputs = new ArrayList<List<WorkflowOutputPort>>();

			int workflowIndex = 0;
			for (Long currentJobID : jobIDs) {

				// get the outputs of the current job
				JobResource job = tavernaRESTClient.getJob(currentJobID);
				Long outputID = job.getOutputs();
				DataResource outputData = tavernaRESTClient.getData(outputID);

				// will contain outputs from all ports of the current workflow
				List<WorkflowOutputPort> workflowOutputPorts = new ArrayList<WorkflowOutputPort>();

				// taverna-specific result map
				Map<String, DataValue> resultMap = outputData.getDataMap();

				// go through the result map and create own objects which will
				// contain the results
				for (String key : resultMap.keySet()) {

					// will contain the output/outputs from one port of the
					// current workflow and the name of the port
					WorkflowOutputPort workflowPort = new WorkflowOutputPort(
							key);

					// taverna data value
					DataValue result = resultMap.get(key);

					// will contain the output/outputs from one port of the
					// current workflow
					List<WorkflowOutput> portOutputs = new ArrayList<WorkflowOutput>();

					// if the result is a nested list
					if (result.getValue() == null && result.getList() != null) {
						// then find the actual results recursively
						// files will be stored on the server
						transferOutputValues(workflowIndex, key, result
								.getList(), portOutputs);

					} else if (result.getErrorValue() != null) {
						System.out.println(result.getErrorValue());
						
						// if the result is just a value
					} else {
						// then create an output object and add it to the
						// outputs list
						String outputString = (String) result.getValue();
						WorkflowOutput output = processOutput(workflowIndex,
								outputString, key, 0);
						portOutputs.add(output);
					}

					// add the outputs list to the port object
					workflowPort.setOutputs(portOutputs);

					// add the port object to the ports list
					workflowOutputPorts.add(workflowPort);
				}

				// add the ports list of the current workflow to the outer
				// outputs list
				allOutputs.add(workflowOutputPorts);

				workflowIndex++;
			}

			session.setAttribute("allOutputs", allOutputs);
			request.setAttribute("round2", "round2");

			duration = System.currentTimeMillis() - startTime;
			System.out.println("Jobs took " + (duration / 1000f) + " seconds");

			// get back to JSP
			RequestDispatcher rd = getServletContext()
					.getRequestDispatcher("/");
			rd.forward(request, response);

		} catch (InterruptedException e) {
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
			List<DataValue> resultList, List<WorkflowOutput> outputs) {

		// go through all taverna data items
		for (DataValue resultItem : resultList) {
			// if the current item contains a list of values
			if (resultItem.getValue() == null && resultItem.getList() != null) {
				// process the list recursively
				transferOutputValues(workflowIndex, portName, resultItem
						.getList(), outputs);
				// if the item contains a value
			} else {
				String currentValue = (String) resultItem.getValue();
				String currentErrorValue = null;
				if (resultItem.getErrorValue() != null) {
					currentErrorValue = (String) resultItem.getErrorValue()
							.getMessage();
				}
				if (currentValue != null) {
					// create an output object
					WorkflowOutput wfOut = processOutput(workflowIndex,
							currentValue, portName, outputs.size());
					// add the object to the outputs
					outputs.add(wfOut);
				} else if (currentErrorValue != null) {
					WorkflowOutput wfOut = processOutput(workflowIndex,
							"ERROR MESSAGE :  " + currentErrorValue, portName,
							outputs.size());
					// add the object to the outputs
					outputs.add(wfOut);

				}

			}
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
