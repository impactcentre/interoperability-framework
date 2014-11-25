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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.impact_project.iif.ws.generic.SoapAttachment;
import eu.impact_project.iif.ws.generic.SoapInput;
import eu.impact_project.iif.ws.generic.SoapOperation;
import eu.impact_project.iif.ws.generic.SoapOutput;
import eu.impact_project.iif.ws.generic.SoapService;

/**
 * Responsible for executing the chosen operation of the web service.
 */
public class SOAPresults extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String user="";
	private String pass="";
	private String hash="";
	final Logger logger = LoggerFactory.getLogger(SOAPresults.class);

	public SOAPresults() {
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		logger.info("Initializing Servlet SOAPresults");
		super.init(config);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * Loads the user values/files and sends them to the web service. Files are
	 * encoded to Base64. Stores the resulting message in the session and the
	 * resulting files on the server.
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		OutputStream outStream = null;
		BufferedInputStream bis = null;
		user = request.getParameter("user");
		pass = request.getParameter("pass");
		try {

			HttpSession session = request.getSession(true);

			String folder = session.getServletContext().getRealPath("/");
			if(!folder.endsWith("/")) {	
				folder = folder + "/";
			}

			Properties props = new Properties();
			InputStream stream = new URL("file:" + folder + "config.properties")
					.openStream();

			props.load(stream);
			stream.close();

			boolean loadDefault = Boolean.parseBoolean(props
					.getProperty("loadDefaultWebService"));
			boolean supportFileUpload = Boolean.parseBoolean(props
					.getProperty("supportFileUpload"));
			boolean oneResultFile = Boolean.parseBoolean(props
					.getProperty("oneResultFile"));
			String defaultFilePrefix = props.getProperty("defaultFilePrefix");

			SoapService serviceObject = (SoapService) session.getAttribute("serviceObject");
			session.setMaxInactiveInterval(300000);
			session.setAttribute("SO_TIMEOUT", 300000);
			SoapOperation operation = null;
			if (supportFileUpload) {
				
				// stores all the strings and encoded files from the html form
				Map<String, String> htmlFormItems = new HashMap<String, String>();
				
				FileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				List /* FileItem */items = upload.parseRequest(request);

				// Process the uploaded items
				Iterator iter = items.iterator();
				while (iter.hasNext()) {
					FileItem item = (FileItem) iter.next();

					// a normal string field
					if (item.isFormField()) {
						htmlFormItems
								.put(item.getFieldName(), item.getString());

						// uploaded file
					} else {

						// encode the uploaded file to base64
						String currentAttachment = new String(Base64
								.encode(item.get()));

						htmlFormItems.put(item.getFieldName(),
								currentAttachment);
					}
				}

				// get the chosen WSDL operation
				String operationName = htmlFormItems.get("operationName");
				
				operation = serviceObject.getOperation(operationName);
				for (SoapInput input : operation.getInputs()) {
					input.setValue(htmlFormItems.get(input.getName()));
				}
				
			} else {
				// get the chosen WSDL operation
				String operationName = request.getParameter("operationName");

				operation = serviceObject.getOperation(operationName);
				for (SoapInput input : operation.getInputs()) {
					String[] soapInputValues = request.getParameterValues(input.getName());
					input.clearValues();
					for (String value : soapInputValues) {						
						input.addValue(value);
					}
				}

			}
			
						
			List<SoapOutput> outs = operation.execute(user,pass);
			String soapResponse = operation.getResponse();

			
			
			String htmlResponse = useXslt(soapResponse, "/SoapToHtml.xsl");
			
			
			session.setAttribute("htmlResponse", htmlResponse);
			session.setAttribute("soapResponse", soapResponse);

			// for giving the file names back to the JSP
			List<String> fileNames = new ArrayList<String>();

			// process possible attachments in the response
			List<SoapAttachment> attachments = operation.getReceivedAttachments();
			int i = 0;
			for (SoapAttachment attachment : attachments) {

				// path to the server directory
				String serverPath = getServletContext().getRealPath("/");
				if(!serverPath.endsWith("/")) {	
					serverPath = folder + "/";
				}

				// construct the file name for the attachment
				String fileEnding = "";
				String contentType = attachment.getContentType();
				System.out.println("content type: " + contentType);
				if (contentType.equals("image/gif")) {
					fileEnding = ".gif";
				} else if (contentType.equals("image/jpeg")) {
					fileEnding = ".jpg";
				} else if (contentType.equals("image/tiff")) {
					fileEnding = ".tif";
				} else if (contentType.equals("application/vnd.ms-excel")) {
					fileEnding = ".xlsx";
				}
				
				String fileName = loadDefault? defaultFilePrefix: "attachedFile";
				
				String counter = oneResultFile? "": i+"";
				
				String attachedFileName = fileName + counter + fileEnding;

				// store the attachment into the file
				File file = new File(serverPath + attachedFileName);
				outStream = new FileOutputStream(file);

				InputStream inStream = attachment.getInputStream();

				bis = new BufferedInputStream(inStream);

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
				bis.close();

				fileNames.add(attachedFileName);
				i++;
			}

			// pass the file names to JSP
			request.setAttribute("fileNames", fileNames);

			request.setAttribute("round3", "round3");
			// get back to JSP
			RequestDispatcher rd = getServletContext().getRequestDispatcher(
					"/interface.jsp");
			rd.forward(request, response);

		} catch (Exception e) {
			logger.error("Exception", e);
			e.printStackTrace();
		} finally {
			if (outStream != null) {
				outStream.close();
			}
			if (bis != null) {
				bis.close();
			}
		}

	}
	
	private String useXslt(String xml, String xsltPath) throws TransformerException {
		InputStream soapStream = new ByteArrayInputStream(xml
				.getBytes());

		System.getProperty("javax.xml.parsers.DocumentBuilderFactory",
				"net.sf.saxon.TransformerFactoryImpl");

		Source xmlSource = new StreamSource(soapStream);

		URL urlxsl = getClass().getResource(xsltPath);
		File xsltFile = new File(urlxsl.getFile());

		Source xsltSource = new StreamSource(xsltFile);

		TransformerFactory transFact = TransformerFactory.newInstance();
		Transformer trans = transFact.newTransformer(xsltSource);

		ByteArrayOutputStream htmlOut = new ByteArrayOutputStream();
		Result res = new StreamResult(htmlOut);

		trans.transform(xmlSource, res);

		return htmlOut.toString();

	}

}
