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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.xmlbeans.impl.util.Base64;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

/**
 * Contains supporting functions.
 * 
 * @author dennis
 * 
 */
public class Helper {

	/**
	 * Creates an http client that uses basic authentication. The credentials
	 * will be sent preemptively with each request.
	 * 
	 * @param domain
	 *            Authentication domain
	 * @param user
	 *            User name for basic authentication
	 * @param password
	 *            Password for basic authentication
	 * @return Created client object
	 */
	public static HttpClient createAuthenticatingClient(String domain,
			String user, String password) {
		HttpClient client = new HttpClient();

		client.getParams().setAuthenticationPreemptive(true);
		client.getState().setCredentials(new AuthScope(domain, -1),
				new UsernamePasswordCredentials(user, password));
		return client;
	}

	public static Object applyXPathSingleNode(InputStream xmlStream,
			String xpathExpression) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(xmlStream);
		XPath xpath = XPath.newInstance(xpathExpression);
		return xpath.selectSingleNode(doc);
	}

	public static List<Element> applyXPathSeveralNodes(InputStream xmlStream,
			String xpathExpression) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(xmlStream);
		XPath xpath = XPath.newInstance(xpathExpression);
		return xpath.selectNodes(doc);
	}

	/**
	 * Extracts all the form input values from a request object. Taverna
	 * workflows are read as strings. Other files are converted to Base64
	 * strings.
	 * 
	 * @param request
	 *            The request object that will be analyzed
	 * @return Map containing all form values and files as strings. The name of
	 *         the form is used as the index
	 */
	public static Map<String, String> parseRequest(HttpServletRequest request) {

		// stores all the strings and encoded files from the html form
		Map<String, String> htmlFormItems = new HashMap<String, String>();
		try {

			// Create a factory for disk-based file items
			FileItemFactory factory = new DiskFileItemFactory();

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);

			// Parse the request
			List items;
			items = upload.parseRequest(request);

			// Process the uploaded items
			Iterator iter = items.iterator();
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();

				// a normal string field
				if (item.isFormField()) {

					// put the string items into the map
					htmlFormItems.put(item.getFieldName(), item.getString());

					// uploaded workflow file
				} else if (item.getFieldName().startsWith("file_workflow")) {

					htmlFormItems.put(item.getFieldName(), new String(item
							.get()));

					// uploaded file
				} else {

					// encode the uploaded file to base64
					String currentAttachment = new String(Base64.encode(item
							.get()));

					// put the encoded attachment into the map
					htmlFormItems.put(item.getFieldName(), currentAttachment);
				}
			}

		} catch (FileUploadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return htmlFormItems;
	}

}
