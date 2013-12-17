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
import java.io.InputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

/**
 * Gets the groups and workflow infos from myExperiment
 */

public class WorkflowUploader extends HttpServlet {
	protected String redirect = "/";

	static final Comparator<WorkflowInfo> TITLE_ORDER = new Comparator<WorkflowInfo>() {
		public int compare(WorkflowInfo w1, WorkflowInfo w2) {
			return w1.getTitle().compareToIgnoreCase(w2.getTitle());
		}
	};

	public WorkflowUploader() {
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * Gets the groups and workflow infos from myExperiment
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// to find out own userID
		String urlStringWhoami = "http://www.myexperiment.org/whoami.xml";

		if (request.getParameter("myexp_user") != null
				&& request.getParameter("myexp_password") != null) {

			HttpSession session = request.getSession(true);

			String user = request.getParameter("myexp_user");
			String password = request.getParameter("myexp_password");

			// WorkflowParser also needs the credentials
			session.setAttribute("user", user);
			session.setAttribute("password", password);

			// session.setAttribute("selectedGroupName", null);

			// the client will be used to query myexperiment's REST api
			HttpClient client = Helper.createAuthenticatingClient("www.myexperiment.org", user, password);

			// GET method for retrieving the xml with the userID
			GetMethod get = new GetMethod(urlStringWhoami);
			// api needs basic authentication
			get.setDoAuthentication(true);

			try {
				int status = client.executeMethod(get);

				if (status == 200) {
					session.setAttribute("logged_in", "true");
					request.setAttribute("login_error", null);

					InputStream xmlResponse = get.getResponseBodyAsStream();

					// get the user uri which contains the userID,
					// userID does not need to be extracted,
					// the uri can be used directly to query the api for infos
					// about the user
					Attribute attr = (Attribute) Helper.applyXPathSingleNode(xmlResponse, "//user/@uri");
					String userUri = attr.getValue();

					// GET method to retrieve all groups of the user
					get = new GetMethod(userUri + "&elements=groups");
					client.executeMethod(get);
					xmlResponse = get.getResponseBodyAsStream();
					// get all groups from received xml
					List<Element> groupElements = Helper.applyXPathSeveralNodes(xmlResponse, "//group");

					// will be used in the jsp to construct selection lists
					Map<String, List<WorkflowInfo>> allWfInfos = new LinkedHashMap<String, List<WorkflowInfo>>();

					// if the user is in the impact group, then it will be the
					// default selection in the jsp, or else it will be the
					// first group
					boolean isFirstGroup = true;

					for (Element group : groupElements) {
						String groupUri = group.getAttributeValue("uri");
						String groupName = group.getTextTrim();

						// set the default selection
						if (isFirstGroup || groupName.startsWith("IMPACT")) {
							session.setAttribute("selectedGroupName0", groupName);
							session.setAttribute("selectedGroupName1", groupName);
							isFirstGroup = false;
						}

						// GET method for retrieving all workflow infos of a
						// group
						get = new GetMethod(groupUri + "&elements=shared-items");
						client.executeMethod(get);
						xmlResponse = get.getResponseBodyAsStream();

						// extract the necessary information from received xml
						List<WorkflowInfo> wfInfos = createWfInfos(xmlResponse);

						// map for jsp
						allWfInfos.put(groupName, wfInfos);

					}

					// GET method for retrieving all workflow infos of the user
					get = new GetMethod(userUri + "&elements=workflows");
					client.executeMethod(get);
					xmlResponse = get.getResponseBodyAsStream();

					// extract the necessary information from received xml
					List<WorkflowInfo> wfInfosUser = createWfInfos(xmlResponse);

					if (wfInfosUser != null && wfInfosUser.size() > 0) {
						// the select will show this under the other groups
						allWfInfos.put("Own workflows", wfInfosUser);
					}
					session.setAttribute("allWfInfos", allWfInfos);
					session.setAttribute("currentTab0", "remote");
					session.setAttribute("currentTab1", "remote");
				} else {
					session.setAttribute("logged_in", "false");
					request.setAttribute("login_error", "Login error");
					session.setAttribute("currentTab0", "local");
					session.setAttribute("currentTab1", "local");
				}
			} catch (HttpException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JDOMException e) {
				e.printStackTrace();
			} finally {
				// release any connection resources used by the method
				get.releaseConnection();
			}
		}
		RequestDispatcher rd = getServletContext().getRequestDispatcher(redirect);
		rd.forward(request, response);
	}

	private List<WorkflowInfo> createWfInfos(InputStream xmlResponse) {
		try {
			List<WorkflowInfo> wfInfos = new ArrayList<WorkflowInfo>();

			List<Element> workflowElements = Helper.applyXPathSeveralNodes(xmlResponse, "//workflow");

			for (Element wf : workflowElements) {
				String wfId = wf.getAttributeValue("resource").substring(38);
				String wfTitle = wf.getTextTrim();
				WorkflowInfo wfInfo = new WorkflowInfo(wfId);
				wfInfo.setTitle(wfTitle);
				wfInfos.add(wfInfo);
			}
			Collections.sort(wfInfos, TITLE_ORDER);

			return wfInfos;

		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
