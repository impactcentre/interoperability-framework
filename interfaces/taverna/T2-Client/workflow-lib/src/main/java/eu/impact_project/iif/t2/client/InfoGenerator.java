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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Gets information about a workflow.
 */
public class InfoGenerator extends HttpServlet {
	protected String redirect = "/";

	public InfoGenerator() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	/**
	 * Gets information about a workflow.
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession(true);

		String wfId = request.getParameter("id");


		String urlString = "http://www.myexperiment.org/workflow.xml?id="
				+ wfId;

		String user = (String) session.getAttribute("user");
		String password = (String) session.getAttribute("password");

		// prepare httpclient for basic authentication
		HttpClient client = new HttpClient();
		client.getParams().setAuthenticationPreemptive(true);
		client.getState().setCredentials(
				new AuthScope("www.myexperiment.org", 80),
				new UsernamePasswordCredentials(user, password));

		GetMethod get = new GetMethod(urlString);
		get.setDoAuthentication(true);

		client.executeMethod(get);
		InputStream responseBody = get.getResponseBodyAsStream();

		try {

			SAXBuilder builder = new SAXBuilder();

			Document doc = builder.build(responseBody);
			
			WorkflowDetails details = new WorkflowDetails(); 
			
			Element root = doc.getRootElement();
			details.setTitle(root.getChild("title").getTextTrim());
			details.setDescription(root.getChild("description").getTextTrim());
			details.setImageUrl(root.getChild("preview").getTextTrim());
			
			session.setAttribute("wfDetails", details);
			
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		RequestDispatcher rd = getServletContext().getRequestDispatcher(
				"/info.jsp");
		rd.forward(request, response);

	}

	/**
	 * 
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

	}

}
