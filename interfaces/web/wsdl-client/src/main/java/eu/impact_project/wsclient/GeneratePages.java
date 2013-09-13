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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.SoapUIException;

import eu.impact_project.iif.ws.generic.SoapService;

/**
 * Servlet for analyzing a WSDL file
 */
public class GeneratePages extends HttpServlet {
	final Logger logger = LoggerFactory.getLogger(GeneratePages.class);
	private static final long serialVersionUID = 1L;
	public static String configLocation = "./src/main/resources/services.xml";

	public GeneratePages() {
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
			String fileName = null;
			String path = null;
			FileWriter file = null;
			File f;
			PrintWriter pw = null;
			InputStream in = null;
			OutputStream out = null;
			byte[] buf = new byte[1024];
			int len;
			 
			fileName = request.getParameter("wsId");
			fileName = fileName.substring(fileName.lastIndexOf("/"));
			fileName = fileName.substring(fileName.lastIndexOf("/")+1,fileName.lastIndexOf("?"));
			fileName = fileName+".jsp";
			path = request.getSession().getServletContext().getRealPath("/");
			in = new FileInputStream(path + "interface.jsp");
			out =new FileOutputStream(path+fileName);
			
			System.out.println("This is the input file: " + path + "interface.jsp");
			System.out.println("This is the output file: " + path + fileName);

			while ((len = in.read(buf)) > 0) 
			{
				  out.write(buf, 0, len);
			}
			
			String wsdlURL = null;
			if (request.getParameter("wsId") != null) {
				wsdlURL = request.getParameter("wsId");
			} else {
				// get WSDL file
				wsdlURL = request.getParameter("wsdlURL");
				logger.trace("Importing WSDL from URL " + wsdlURL);
			}
			
			// get back to JSP
			RequestDispatcher rd = getServletContext().getRequestDispatcher(
					"/"+fileName);
			rd.forward(request, response);

	}

}
