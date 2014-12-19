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
import java.util.Iterator;
import java.util.List;

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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class HtmlServiceProvider implements ServiceProvider {

	final static Logger logger = LoggerFactory
			.getLogger(HtmlServiceProvider.class);

	private List<Service> services = new ArrayList<Service>();
	private URL configUrl;
	private URL serverUrl;
	private static final String NO_NAMESPACE = "";

	public HtmlServiceProvider(URL url) throws ConfigurationException {
		configUrl = url;
		try {
			serverUrl = new URL(configUrl.toString() + "/synapse/");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		loadServices();
	}

	private void loadServices() {
		InputStream htmlFile = getRemoteFile(serverUrl);
		Document htmlDoc = convertToDoc(htmlFile);
		NodeList anchors = applyXPath(htmlDoc, "//a", NO_NAMESPACE);

		for (int i = 0; i < anchors.getLength(); i++) {
			Node a = anchors.item(i);

			Service service = createServiceObjectFor(a, i);
			services.add(service);
		}
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

	private Document convertToDoc(InputStream htmlFile) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		Document doc = null;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(htmlFile);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc;
	}

	// private Document convertToDoc(InputStream htmlFile) {
	// SAXBuilder builder = new SAXBuilder();
	// Document doc = null;
	// try {
	// doc = builder.build(htmlFile);
	// } catch (JDOMException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// return doc;
	// }

	// private List<Element> applyXPath(Document doc,
	// String xpathExpression) {
	//		
	// List<Element> result = new ArrayList<Element>();
	//		
	// try {
	// XPath xpath = XPath.newInstance(xpathExpression);
	// result = xpath.selectNodes(doc);
	// } catch (JDOMException e) {
	// logger.error("-----Caught jdom exception--------");
	// }
	//
	// return result;
	// }

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

	private Service createServiceObjectFor(Node a, int id) {

		String title = formatTitle(a.getTextContent());

		String wsdlPath = a.getAttributes().getNamedItem("href")
				.getTextContent();
		URL wsdlUrl = null;
		try {
			wsdlUrl = new URL(configUrl + wsdlPath);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		String description = getWsdlDescription(wsdlUrl);

		Service result = new HtmlService(id, title, description, wsdlUrl);

		return result;
	}

	private String formatTitle(String title) { // IMPACTAbbyyOcrProxy

		if (title.startsWith("IMPACT"))
			title = title.substring(6, title.length()); // AbbyyOcrProxy

		if (title.endsWith("Proxy"))
			title = title.substring(0, title.length() - 5) + "Service"; // AbbyyOcrService

		String[] splitTitle = title.split("(?=[A-Z])"); // [Abbyy, Ocr, Service]

		title = "";
		for (String s : splitTitle) {
			title += s + " "; // Abbyy Ocr Service
		}

		return title;
	}

	String getWsdlDescription(URL wsdlUrl) {
		InputStream wsdl = getRemoteFile(wsdlUrl);
		Document wsdlDoc = convertToDoc(wsdl);
		NodeList descNodes = applyXPath(wsdlDoc,
				"/wsdl:definitions/wsdl:documentation/text()", "wsdl");

		if (descNodes != null && descNodes.item(0) != null)
			return descNodes.item(0).getTextContent().trim();
		else
			return "";
	}

	@Override
	public List<Service> getServiceList() {

		return services;
	}

	@Override
	public URL getUrl(String id) {

		return configUrl;
	}

	public class HtmlService implements Service {
		int id;
		String title;
		String description;
		URL url;

		public HtmlService(int id, String title, String description, URL url) {
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

	}

}
