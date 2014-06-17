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

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * Sets the currently selected myExperiment groups
 */

public class GroupSelector extends HttpServlet {

	private static final long serialVersionUID = 1877630363617132110L;

	public GroupSelector() {
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * Sets the currently selected myExperiment groups
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(true);

		int i = 0;

		while (request.getParameter("MyExpGroup" + i) != null
				|| request.getAttribute("MyExpGroup" + i) != null) {
			
			String groupName = "";
			String groupParam = request.getParameter("MyExpGroup" + i);
			String groupAttrib = (String) request.getAttribute("MyExpGroup" + i);

			if (groupParam != null) {
				groupName = groupParam;
			} else if (groupAttrib != null) {
				groupName = groupAttrib;
			}
			session.setAttribute("selectedGroupName" + i, groupName);
			
			String workflowUrl = "";
			String workflowParam = request.getParameter("MyExpWorkflow" + i);
			String workflowAttrib = (String) request.getAttribute("MyExpWorkflow" + i);

			if (workflowParam != null) {
				workflowUrl = workflowParam;
			} else if (workflowAttrib != null) {
				workflowUrl = workflowAttrib;
			}
			session.setAttribute("currentWfId" + i, workflowUrl);
			i++;
		}

		RequestDispatcher rd = getServletContext().getRequestDispatcher("/");
		rd.forward(request, response);
	}
}
