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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

/**
 * Analyzes the uploaded workflow files for input names and input depths
 */

public class WorkflowParser extends HttpServlet {
	protected String redirect = "/";

	public WorkflowParser() {
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * Analyzes the uploaded workflow files for input names and input depths
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession(true);

		List<Workflow> workflows = new ArrayList<Workflow>();

		Map<String, String> htmlFormItems = Helper.parseRequest(request);

		int k = 0;
		while (htmlFormItems.get("currentTab" + k) != null) {

			session.setAttribute("currentTab" + k, htmlFormItems
					.get("currentTab" + k));

			k++;
		}

		// kind of a hack, because nested forms are not allowed.
		// some parameters are copied to attributes and the servlet for
		// selecting a group is called
		if (htmlFormItems.get("selectGroup") != null) {

			int i = 0;
			while (htmlFormItems.get("MyExpGroup" + i) != null) {

				request.setAttribute("MyExpGroup" + i, htmlFormItems
						.get("MyExpGroup" + i));
				i++;
			}

			int j = 0;
			while (htmlFormItems.get("MyExpWorkflow" + j) != null) {

				request.setAttribute("MyExpWorkflow" + j, htmlFormItems
						.get("MyExpWorkflow" + j));
				j++;
			}

			RequestDispatcher rd0 = getServletContext().getRequestDispatcher(
					"/GroupSelector");
			rd0.forward(request, response);
			return;

		// here is the original servlet functionality
		} else {

			int i = 0;
			while (htmlFormItems.get("file_workflow" + i) != null
					|| htmlFormItems.get("MyExpWorkflow" + i) != null) {
				String workflowFile = htmlFormItems.get("file_workflow" + i);
				String workflowUrl = htmlFormItems.get("MyExpWorkflow" + i);

				if (workflowFile != null && !workflowFile.equals("")) {

					Workflow currentWorkflow = parseWorkflow(workflowFile);
					workflows.add(currentWorkflow);

				} else if (workflowUrl != null && !workflowUrl.equals("")) {
					String urlString = "http://www.myexperiment.org/workflow.xml?id="
							+ workflowUrl + "&elements=content";

					String user = (String) session.getAttribute("user");
					String password = (String) session.getAttribute("password");

					HttpClient client = Helper.createAuthenticatingClient(
							"www.myexperiment.org", user, password);

					// GET method to retrieve the chosen workflow which is
					// base64
					// encoded and wrapped in xml
					GetMethod get = new GetMethod(urlString);
					get.setDoAuthentication(true);

					client.executeMethod(get);
					// get the xml
					InputStream responseBody = get.getResponseBodyAsStream();

					try {
						SAXBuilder builder = new SAXBuilder();
						Document doc = builder.build(responseBody);

						Element root = doc.getRootElement();
						// the content element contains the workflow
						String workflowBase64 = root.getChild("content")
								.getTextTrim();

						// decode the workflow and convert to string
						byte[] bytes = Base64.decodeBase64(workflowBase64
								.getBytes());
						String workflowString = new String(bytes);

						// make a Workflow instance which will be used in the
						// frontend
						Workflow currentWorkflow = parseWorkflow(workflowString);
						workflows.add(currentWorkflow);

						session.setAttribute("currentWfId" + i, workflowUrl);

					} catch (JDOMException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				i++;
			}

			// } else if (request.getParameter("uploadType") != null
			// && request.getParameter("uploadType").equals("myExperiment")) {
			//
			// String wfId = request.getParameter("MyExpWorkflow");
			//
			// String urlString = "http://www.myexperiment.org/workflow.xml?id="
			// + wfId + "&elements=content";
			//
			// String user = (String) session.getAttribute("user");
			// String password = (String) session.getAttribute("password");
			//
			// // prepare the httpclient for basic authentication
			// HttpClient client = new HttpClient();
			// client.getParams().setAuthenticationPreemptive(true);
			// client.getState().setCredentials(
			// new AuthScope("www.myexperiment.org", 80),
			// new UsernamePasswordCredentials(user, password));
			//
			// // GET method to retrieve the chosen workflow which is base64
			// // encoded and wrapped in xml
			// GetMethod get = new GetMethod(urlString);
			// get.setDoAuthentication(true);
			//
			// client.executeMethod(get);
			// // get the xml
			// InputStream responseBody = get.getResponseBodyAsStream();
			//
			// try {
			// SAXBuilder builder = new SAXBuilder();
			// Document doc = builder.build(responseBody);
			//
			// Element root = doc.getRootElement();
			// // the content element contains the workflow
			// String workflowBase64 = root.getChild("content").getTextTrim();
			//
			// // decode the workflow and convert to string
			// byte[] bytes = Base64.decodeBase64(workflowBase64.getBytes());
			// String workflowString = new String(bytes);
			//
			// // make a Workflow instance which will be used in the frontend
			// Workflow currentWorkflow = parseWorkflow(workflowString);
			// workflows.add(currentWorkflow);
			//
			// session.setAttribute("currentWfId", wfId);
			//
			// } catch (JDOMException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			//
			// // controls the "show examples" checkbox in the jsp
			// if (request.getParameter("printExamples") != null) {
			// session.setAttribute("printExamples", new Boolean(true));
			// } else {
			// session.setAttribute("printExamples", new Boolean(false));
			// }
			//
			// } else {
			//
			// // extract all the html form parameters from the request,
			// // including files (files are encoded to base64, except for the
			// // workflow file)
			// // Map<String, String> htmlFormItems =
			// Helper.parseRequest(request);
			//
			// int i = 0;
			// // for the case if there are several uploaded workflows.
			// // metrics toolkit has two
			// while (htmlFormItems.get("file_workflow" + i) != null
			// && !htmlFormItems.get("file_workflow" + i).equals("")) {
			// String currentWorkflowString = htmlFormItems
			// .get("file_workflow" + i);
			//
			// i++;
			// Workflow currentWorkflow = parseWorkflow(currentWorkflowString);
			// workflows.add(currentWorkflow);
			// }
			//
			// // controls the "show examples" checkbox in the jsp
			// if (htmlFormItems.get("printExamples") != null) {
			// session.setAttribute("printExamples", new Boolean(true));
			// } else {
			// session.setAttribute("printExamples", new Boolean(false));
			// }
		}

		// controls the "show examples" checkbox in the jsp
		if (htmlFormItems.get("printExamples") != null) {
			session.setAttribute("printExamples", new Boolean(true));
		} else {
			session.setAttribute("printExamples", new Boolean(false));
		}

		session.setAttribute("workflows", workflows);

		request.setAttribute("round1", "round1");

		// get back to JSP
		RequestDispatcher rd = getServletContext().getRequestDispatcher(
				redirect);
		rd.forward(request, response);

	}

	protected Workflow parseWorkflow(String workflowString) {

		// remove single-line java comments from beanshell scripts since they
		// might
		// produce corrupted java code after serialization
		workflowString = workflowString.replaceAll("[\\s]//[^\n]*", "");

		// <localworkerName> tags are not understood by taverna server
		// 0.2.x
		workflowString = workflowString.replaceAll(
				"<localworkerName>[^<]*</localworkerName>", "");

		Workflow workflow = new Workflow(workflowString);

		try {

			SAXBuilder builder = new SAXBuilder();

			InputStream stream = new ByteArrayInputStream(workflowString
					.getBytes());

			Document doc = builder.build(stream);

			// get all input ports from the top dataflow
			XPath xpath = XPath
					.newInstance("//wf:dataflow[@role='top']/wf:inputPorts/wf:port");
			Namespace ns = Namespace.getNamespace("wf",
					"http://taverna.sf.net/2008/xml/t2flow");
			xpath.addNamespace(ns);
			List<Element> results = xpath.selectNodes(doc);

			for (Element port : results) {

				// create workflow inputs using data from the port element
				WorkflowInput currentInput = new WorkflowInput("unknown_input");
				currentInput.setName(port.getChild("name", ns).getText());
				currentInput.setDepth(Integer.parseInt(port.getChild("depth",
						ns).getText()));

				// get all the example input values for the port
				// strangely, taverna does not delete old values if you enter a
				// new one
				XPath xpath2 = XPath
						.newInstance(".//net.sf.taverna.t2.annotation.AnnotationAssertionImpl[annotationBean/@class='net.sf.taverna.t2.annotation.annotationbeans.ExampleValue']");
				List<Element> results2 = xpath2.selectNodes(port);

				String value = "";
				String date = "";

				// luckily, taverna stores the date+time to each input example
				// so the most current one can be found
				for (Element el : results2) {
					if (el.getChildText("date").compareTo(date) > 0) {
						value = el.getChild("annotationBean").getChildText(
								"text");
						date = el.getChildText("date");
					}
				}
				currentInput.setExampleValue(value);

				// add the found input to the workflow instance
				workflow.getInputs().add(currentInput);

			}

		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return workflow;

	}

}
