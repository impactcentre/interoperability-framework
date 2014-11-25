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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import eu.impact_project.wsclient.ServiceProvider.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlServiceProvider implements ServiceProvider {

	final static Logger logger = LoggerFactory
			.getLogger(HtmlServiceProvider.class);

	private List<Service> services = new ArrayList<Service>();
	private URL xmlUrl;
	private static final String NO_NAMESPACE = null;

	public XmlServiceProvider(URL url) throws ConfigurationException {
		xmlUrl = url;
		loadServices();
	}

	private void loadServices() {
		InputStream xmlFile = getRemoteFile(xmlUrl);
		if (xmlFile == null)
			throw new RuntimeException("The file " + xmlUrl.toString() + " not reachable or the server certificate is invalid");
		Document xmlDoc = convertToDoc(xmlFile);
		NodeList serviceNodes = applyXPath(xmlDoc, "//service", NO_NAMESPACE);

		SortedSet<Service> sortedSet = new TreeSet<Service>();
		for (int i = 0; i < serviceNodes.getLength(); i++) {
			Node srv = serviceNodes.item(i);

			Service service = createServiceObjectFor(srv);
			sortedSet.add(service);
		}
		services = new ArrayList<Service>(sortedSet);
	}

	private InputStream getRemoteFile(URL url) {
		HttpClient client = new HttpClient();
		GetMethod getMethod = new GetMethod(url.toString());
		try {
			client.executeMethod(getMethod);
			return getMethod.getResponseBodyAsStream();
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Document convertToDoc(InputStream xmlFile) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		Document doc = null;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(xmlFile);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc;
	}

	private NodeList applyXPath(Document doc, String xpathExpression,
			String namespace) {

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		if (!namespace.equals(NO_NAMESPACE))
			xpath.setNamespaceContext(new WSDLNamespaceContext());

		XPathExpression expr;

		Object result = null;
		try {
			expr = xpath.compile(xpathExpression);
			result = expr.evaluate(doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return (NodeList) result;

	}

	private Service createServiceObjectFor(Node service) {

		int id = Integer.valueOf(service.getAttributes().getNamedItem("id")
				.getTextContent());
		
		String urlString = "";
		String title = "";
		String description = "";
		
		NodeList nodes = service.getChildNodes();
		for(int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String nodeName = node.getLocalName();
			if(nodeName != null && nodeName.equals("url"))
				urlString = node.getTextContent();
			else if(nodeName != null && nodeName.equals("title"))
				title = node.getTextContent();
			else if(nodeName != null && nodeName.equals("description"))
				description = node.getTextContent().trim();
		}
				
		URL wsdlUrl = null;
		try {
			wsdlUrl = new URL(urlString);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Service result = new XmlService(id, title, description, wsdlUrl);

		return result;
	}

	@Override
	public List<Service> getServiceList() {

		return services;
	}

	@Override
	public URL getUrl(String id) {

		return xmlUrl;
	}
	

	public class XmlService implements Service, Comparable<XmlService> {
		int id;
		String title;
		String description;
		URL url;

		public XmlService(int id, String title, String description, URL url) {
			this.id = id;
			this.title = title;
			this.description = description;
			this.url = url;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public int getIdentifier() {
			return id;
		}

		@Override
		public URL getURL() {
			return url;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public int compareTo(XmlService arg0) {
			return this.id - arg0.getIdentifier();
		}

	}

}
