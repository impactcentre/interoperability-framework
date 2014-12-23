/*
	
	Copyright 2011 The IMPACT Project
	
	@author Dennis Neumann

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

package eu.impact_project.wsclient;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.impact_project.iif.ws.generic.SoapService;

/**
 * Servlet for analyzing a WSDL file
 */
public class WSDLinfo extends HttpServlet {
	final Logger logger = LoggerFactory.getLogger(WSDLinfo.class);
	private static final long serialVersionUID = 1L;
	public static String configLocation = "./src/main/resources/services.xml";

	public WSDLinfo() {
		super();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		logger.info("Initializing Servlet WSDLinfo");
		super.init(config);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession(true);

		String wsdlURL = null;
		if (request.getParameter("wsId") != null) {
			wsdlURL = request.getParameter("wsId");
		} else {
			// get WSDL file
			wsdlURL = request.getParameter("wsdlURL");
			logger.trace("Importing WSDL from URL " + wsdlURL);
		}

		// pass the WS name to interface.jsp
		String wsName = null;
		if (request.getParameter("wsName") != null) {
			wsName = request.getParameter("wsName");
			session.setAttribute("wsName", wsName);
		}
		
                //pasar user y pass decodificados por sesion
                
                String key = "1234567891234567";                
                
                String wsUser = "";
                if(request.getParameter("user") != null)
                {
                    if(request.getParameter("user") != "")
                    {
                        wsUser = Security.decrypt(request.getParameter("user"),key);
                        session.setAttribute("wsUser", wsUser);
                    }
                }
                
                String wsPass = "";
                if(request.getParameter("pass") != null)
                {
                    if(request.getParameter("pass") != "")
                    {
                        wsPass = Security.decrypt(request.getParameter("pass"),key);
                        session.setAttribute("wsPass", wsPass);
                    }
                }
                
		logger.info("URL WSDL: " + wsdlURL);
		SoapService serviceObject = new SoapService(wsdlURL);
		
		

		// transfer values to JSP through session and request
		request.setAttribute("round1", "round1");
		//session.setAttribute("endpointURL", endpointURL);
		session.setAttribute("wsdlURL", wsdlURL);
		//session.setAttribute("wsdlInterface", wsdlInterface);
		session.setAttribute("serviceObject", serviceObject);
		//session.setAttribute("soapOperations", soapOperations);
		logger.info("WSDL STRING: " + serviceObject.toString());

		// get back to JSP
		RequestDispatcher rd = getServletContext().getRequestDispatcher(
				"/interface.jsp");
		rd.forward(request, response);

	}

	/**
	 * Uses the passed WSDL URL to analyze the WSDL for operations and
	 * documentation. Retrieves empty SOAP messages for the operations using
	 * SOAP UI libraries. Passes the information further to the displaying JSP.
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

			HttpSession session = request.getSession(true);

			String wsdlURL = null;
			if (request.getParameter("wsId") != null) {
				wsdlURL = request.getParameter("wsId");
			} else {
				// get WSDL file
				wsdlURL = request.getParameter("wsdlURL");
				logger.trace("Importing WSDL from URL " + wsdlURL);
			}

			// pass the WS name to interface.jsp
			String wsName = null;
			if (request.getParameter("wsName") != null) {
				wsName = request.getParameter("wsName");
				session.setAttribute("wsName", wsName);
			}
			
                        //pasar user y pass decodificados por sesion
                
                        String key = "1234567891234567";                

                        String wsUser = "";
                        if(request.getParameter("user")!=null)
                        {
                            if(request.getParameter("user") != "")
                            {
                                wsUser = Security.decrypt(request.getParameter("user"),key);
                                session.setAttribute("wsUser", wsUser);
                            }
                        }

                        String wsPass = "";
                        if(request.getParameter("pass")!=null)
                        {
                            if(request.getParameter("pass") != "")
                            {
                                wsPass = Security.decrypt(request.getParameter("pass"),key);
                                session.setAttribute("wsPass", wsPass);
                            }
                        }
                        
                        
			logger.info("URL WSDL: " + wsdlURL);
			SoapService serviceObject = new SoapService(wsdlURL);
			
			

			// transfer values to JSP through session and request
			request.setAttribute("round1", "round1");
			//session.setAttribute("endpointURL", endpointURL);
			session.setAttribute("wsdlURL", wsdlURL);
			//session.setAttribute("wsdlInterface", wsdlInterface);
			session.setAttribute("serviceObject", serviceObject);
			//session.setAttribute("soapOperations", soapOperations);

			// get back to JSP
			RequestDispatcher rd = getServletContext().getRequestDispatcher(
					"/interface.jsp");
			rd.forward(request, response);

	}

}
