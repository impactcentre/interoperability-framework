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
import java.util.Enumeration;
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
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.bouncycastle.util.encoders.Base64;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.Request.SubmitException;

/**
 * Responsible for executing the chosen operation of the web service.
 */
public class SOAPresults extends HttpServlet {
	private static final long serialVersionUID = 1L;
	final Logger logger = LoggerFactory.getLogger(SOAPresults.class);

	private Map<Element, List<Element>> additionalXmlElements = new HashMap<Element, List<Element>>();

	public SOAPresults() {
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		logger.info("Initializing Servlet SOAPresults");
		super.init(config);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
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

		try {

			HttpSession session = request.getSession(true);

			String folder = session.getServletContext().getRealPath("/");

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

			
			WsdlOperation wsdlOperation = null;
			String messageToSend = "";
			Document doc = null;
			
			
			if (loadDefault && supportFileUpload) {
				
				// stores all the strings and encoded files from the html form
				Map<String, String> htmlFormItems = new HashMap<String, String>();

				// Create a factory for disk-based file items
				FileItemFactory factory = new DiskFileItemFactory();

				// Create a new file upload handler
				ServletFileUpload upload = new ServletFileUpload(factory);

				// Parse the request
				List /* FileItem */items = upload.parseRequest(request);

				// Process the uploaded items
				Iterator iter = items.iterator();
				while (iter.hasNext()) {
					FileItem item = (FileItem) iter.next();

					// a normal string field
					if (item.isFormField()) {

						// put the string items into the map
						htmlFormItems
								.put(item.getFieldName(), item.getString());

						// uploaded file
					} else {

						// encode the uploaded file to base64
						String currentAttachment = new String(Base64
								.encode(item.get()));

						// put the encoded attachment into the map
						htmlFormItems.put(item.getFieldName(),
								currentAttachment);
					}
				}

				WsdlInterface wsdlInterface = (WsdlInterface) session
						.getAttribute("wsdlInterface");

				// get the chosen WSDL operation
				String operationName = htmlFormItems.get("operationName");
				wsdlOperation = wsdlInterface
						.getOperationByName(operationName);

				// create a default SOAP message
				messageToSend = wsdlOperation.createRequest(true);

				// read SOAP message into an XML document
				SAXBuilder parser = new SAXBuilder();
				InputStream is = new ByteArrayInputStream(messageToSend
						.getBytes());
				doc = parser.build(is);

				// insert the form values and encoded files into the XML message
				Element root = doc.getRootElement();
				insertFormValues(root, htmlFormItems);

			} else {
				WsdlInterface wsdlInterface = (WsdlInterface) session
						.getAttribute("wsdlInterface");

				// get the chosen WSDL operation
				String operationName = request.getParameter("operationName");
				wsdlOperation = wsdlInterface
						.getOperationByName(operationName);

				// create a default SOAP message
				messageToSend = wsdlOperation.createRequest(true);

				// read SOAP message into an XML document
				SAXBuilder parser = new SAXBuilder();
				InputStream is = new ByteArrayInputStream(messageToSend
						.getBytes());
				doc = parser.build(is);

				// insert the form values and encoded files into the XML message
				Element root = doc.getRootElement();
				insertFormValues(root, request);

			}

			for (Element parent : additionalXmlElements.keySet()) {
				List<Element> childNodes = additionalXmlElements.get(parent);
				parent.addContent(childNodes);
			}
			additionalXmlElements.clear();

			// store the XML document in a String
			XMLOutputter serializer = new XMLOutputter();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			serializer.output(doc, bout);
			messageToSend = bout.toString();

			// prepare the request
			WsdlRequest wsdlRequest = wsdlOperation.addNewRequest("req");
			wsdlRequest.setRequestContent(messageToSend);
			// System.out.println(messageToSend);

			// send the request
			WsdlSubmit submit = (WsdlSubmit) wsdlRequest.submit(
					new WsdlSubmitContext(wsdlRequest), false);

			// get the response
			Response wsdlResponse = submit.getResponse();

			// get the response SOAP as string and store in the session
			String soapResponse = wsdlResponse.getContentAsString();

			InputStream soapStream = new ByteArrayInputStream(soapResponse
					.getBytes());

			System.getProperty("javax.xml.parsers.DocumentBuilderFactory",
					"net.sf.saxon.TransformerFactoryImpl");

			// JAXP liest Daten ueber die Source-Schnittstelle
			Source xmlSource = new StreamSource(soapStream);

			URL urlxsl = getClass().getResource("/SoapToHtml.xsl");
			File xsltFile = new File(urlxsl.getFile());

			Source xsltSource = new StreamSource(xsltFile);

			// das Factory-Pattern unterstuetzt verschiedene XSLT-Prozessoren
			TransformerFactory transFact = TransformerFactory.newInstance();
			// System.out.println(transFact.getClass().getName());
			Transformer trans = transFact.newTransformer(xsltSource);

			ByteArrayOutputStream htmlOut = new ByteArrayOutputStream();
			Result res = new StreamResult(htmlOut);

			trans.transform(xmlSource, res);

			String htmlResponse = htmlOut.toString();
			session.setAttribute("htmlResponse", htmlResponse);
			session.setAttribute("soapResponse", soapResponse);

			// for giving the file names back to the JSP
			List<String> fileNames = new ArrayList<String>();

			// process possible attachments in the response
			for (int i = 0; i < wsdlResponse.getAttachments().length; i++) {

				Attachment currentAttachment = wsdlResponse.getAttachments()[i];

				// path to the server directory
				String serverPath = getServletContext().getRealPath("/");

				// construct the file name for the attachment
				String fileEnding = "";
				String contentType = currentAttachment.getContentType();
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

				InputStream inStream = wsdlResponse.getAttachments()[0]
						.getInputStream();

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
			}

			// pass the file names to JSP
			request.setAttribute("fileNames", fileNames);

			request.setAttribute("round3", "round3");
			// get back to JSP
			RequestDispatcher rd = getServletContext().getRequestDispatcher(
					"/interface.jsp");
			rd.forward(request, response);

		} catch (JDOMException e) {
			logger.error("XML could not be parsed", e);
		} catch (SubmitException e) {
			logger.error("Submit exception", e);
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

	/**
	 * Extracts the user values and inserts them into the appropriate XML
	 * element in the SOAP request message
	 * 
	 * @param current
	 * @param htmlFormItems
	 */
	private void insertFormValues(Element current, HttpServletRequest request) {

		// check if the element should hold a base64 string (does currently the
		// same as for strings, may change for MTOM attachments)
		// if (request.getParameter(current.getName()) != null
		// && current.getAttribute("contentType") != null
		// && current.getAttribute("contentType").getValue().indexOf(
		// "application") > -1) {
		// String attachment = request.getParameter(current.getName());
		// current.setText(attachment);

		// or else an input string is assumed
		if (request.getParameter(current.getName()) != null) {
			String[] soapInputValues = request.getParameterValues(current
					.getName());
			List<Element> clones = new ArrayList<Element>();
			current.setText(soapInputValues[0]);
			for (int i = 1; i < soapInputValues.length; i++) {
				Element clone = (Element) current.clone();
				clone.setText(soapInputValues[i]);
				clones.add(clone);
				// current.getParentElement().addContent(clone);
			}
			if (clones.size() > 0) {
				additionalXmlElements.put(current.getParentElement(), clones);
			}

			// recursive checking of all children
		} else {
			List children = current.getChildren();
			Iterator iterator = children.iterator();
			while (iterator.hasNext()) {
				Element child = (Element) iterator.next();
				insertFormValues(child, request);
			}
		}

	}

	/**
	 * Extracts the user values and inserts them into the appropriate XML
	 * element in the SOAP request message
	 * 
	 * @param current
	 * @param htmlFormItems
	 */
	private void insertFormValues(Element current,
			Map<String, String> htmlFormItems) {

		boolean itemPresent = htmlFormItems.get(current.getName()) != null;
		// check if the element should hold a base64 string (does currently the
		// same as for strings, may change for MTOM attachments)
		if (itemPresent
				&& current.getAttribute("contentType") != null
				&& current.getAttribute("contentType").getValue().indexOf(
						"application") > -1 || itemPresent
				&& current.getTextTrim().startsWith("cid:")) {
			String attachment = htmlFormItems.get(current.getName());
			current.setText(attachment);

			// or else an input string is assumed
		} else if (htmlFormItems.get(current.getName()) != null) {
			String soapInputValue = htmlFormItems.get(current.getName());
			current.setText(soapInputValue);

			// recursive checking of all children
		} else {
			List children = current.getChildren();
			Iterator iterator = children.iterator();
			while (iterator.hasNext()) {
				Element child = (Element) iterator.next();
				insertFormValues(child, htmlFormItems);
			}
		}

	}

}
