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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
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

/**
 * Servlet for analyzing a WSDL file
 */
public class WSDLinfo extends HttpServlet {
	final Logger logger = LoggerFactory.getLogger(WSDLinfo.class);
	private static final long serialVersionUID = 1L;
	public static String configLocation = "./src/main/resources/services.xml";

	public WSDLinfo() {
		super();
		// TODO Auto-generated constructor stub
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
		try {

			HttpSession session = request.getSession(true);

			WsdlProject project = new WsdlProject();

			String wsdlURL = null;
			if (request.getParameter("wsId") != null) {

				wsdlURL = request.getParameter("wsId");
				// try {
				//
				// ServletContext sc = getServletConfig().getServletContext();
				// FileServiceProvider.setServletContext(sc);
				// ServiceProvider sp = new FileServiceProvider(
				// FileServiceProvider.findConfig());
				// wsdlURL = sp.getUrl(request.getParameter("wsId"))
				// .toString();
				// } catch (ConfigurationException e) {
				// logger.error("Couldn't load configuration", e);
				// } catch (URISyntaxException e) {
				// logger.error("Couldn't load configuration", e);
				// }

			} else {
				// get WSDL file
				wsdlURL = request.getParameter("wsdlURL");
				logger.trace("Importing WSDL from URL " + wsdlURL);

			}
			WsdlInterface wsdlInterface = WsdlInterfaceFactory.importWsdl(
					project, wsdlURL, false)[0];
			// get endpoint URL
			String endpointURL = wsdlInterface.getEndpoints()[0];

			// pass the WS name to interface.jsp
			String wsName = null;
			if (request.getParameter("wsName") != null) {
				wsName = request.getParameter("wsName");
				session.setAttribute("wsName", wsName);
			}

			// get the plain WSDL file as string
			// will be used to retrieve documentations
			URL url = new URL(wsdlURL);
			logger.trace("Opening connection");
			URLConnection urlConn = url.openConnection();
			urlConn.setDoInput(true);
			urlConn.setUseCaches(false);

			String wsdlString = IOUtils.toString(urlConn.getInputStream());

			// convert the WSDL stream into a dom document
			DocumentBuilderFactory domFactory = DocumentBuilderFactory
					.newInstance();
			domFactory.setNamespaceAware(true); // never forget this!
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			org.w3c.dom.Document wsdlDoc = builder
					.parse(new ByteArrayInputStream(wsdlString.getBytes()));

			// use xpath on the document to get the ws documentation
			javax.xml.xpath.XPathFactory xfactory = javax.xml.xpath.XPathFactory
					.newInstance();
			javax.xml.xpath.XPath xpath = xfactory.newXPath();
			xpath.setNamespaceContext(new WSDLNamespaceContext());
			javax.xml.xpath.XPathExpression expr = xpath
					.compile("/wsdl:definitions/wsdl:documentation/text()");
			String serviceDocumentation = (String) expr.evaluate(wsdlDoc,
					javax.xml.xpath.XPathConstants.STRING);

			serviceDocumentation = serviceDocumentation.trim();
			serviceDocumentation = serviceDocumentation.replaceAll("\\s+", " ");

			// get names and corresponding SOAP message templates of all
			// operations

			// Soap UI operations
			List<Operation> operations = wsdlInterface.getOperationList();
			// for own operation objects containing info which will be used
			// later
			List<SOAPoperation> soapOperations = new ArrayList<SOAPoperation>();

			for (Operation operation : operations) {
				WsdlOperation op = (WsdlOperation) operation;

				String opName = op.getName();
				logger.trace("Found operation " + opName);
				// new own operation object
				SOAPoperation soapOp = new SOAPoperation(opName);

				// get the documentation of the operation
				expr = xpath.compile("//wsdl:portType/wsdl:operation[@name='"
						+ opName + "']/wsdl:documentation/text()");
				String operationDocumentation = (String) expr.evaluate(wsdlDoc,
						javax.xml.xpath.XPathConstants.STRING);
				operationDocumentation = operationDocumentation.trim();
				operationDocumentation = operationDocumentation.replaceAll(
						"\\s+", " ");

				// insert the documentation into own operation object
				soapOp.setDocumentation(operationDocumentation);

				String defaultMessage = op.createRequest(true);
				// will maybe need it later
				soapOp.setDefaultMessage(defaultMessage);

				// will contain documentations of input values
				Map<String, String> inputNamesToDocumentations = new HashMap<String, String>();

				// get the combined input documentation string from wsdl
				expr = xpath.compile("//wsdl:portType/wsdl:operation[@name='"
						+ opName + "']/wsdl:input/wsdl:documentation/text()");
				String inputDocumentations = (String) expr.evaluate(wsdlDoc,
						javax.xml.xpath.XPathConstants.STRING);
				inputDocumentations = inputDocumentations.trim();
				inputDocumentations = inputDocumentations.replaceAll("\\s+",
						" ");

				// split the documentation string into "input name" ->
				// "input docu" pairs
				if (inputDocumentations != null
						&& !inputDocumentations.equals("")) {
					String[] splitInputs = inputDocumentations.split(";");

					for (String in : splitInputs) {
						in = in.trim();
						String[] namesAndDocus = in.split(":");
						if (namesAndDocus != null && namesAndDocus.length == 2) {
							inputNamesToDocumentations.put(namesAndDocus[0]
									.trim(), namesAndDocus[1].trim());
						}
					}
				}

				// convert the default message into a dom document
				org.w3c.dom.Document messageDoc = builder
						.parse(new ByteArrayInputStream(defaultMessage
								.getBytes()));

				// parse the default message for operation input names
				// and create corresponding input field objects
				org.w3c.dom.Element soapBody = (org.w3c.dom.Element) messageDoc
						.getDocumentElement().getElementsByTagNameNS(
								"http://schemas.xmlsoap.org/soap/envelope/",
								"Body").item(0);
				List<SOAPinputField> inputFieldObjects = getAllInputFields(
						soapBody.getChildNodes(),
						new ArrayList<SOAPinputField>());

				// attach a previously retrieved documentation if any to each
				// input object
				for (SOAPinputField inputObject : inputFieldObjects) {
					String inputName = inputObject.getName();

					expr = xpath.compile("//wsdl:operation[@name='" + opName
							+ "']/wsdl:input[1]/@message");
					String messageName = (String) expr.evaluate(wsdlDoc,
							javax.xml.xpath.XPathConstants.STRING);
					// <operation name="convertByUrl">
					// <input message="tns:ByUrlRequest"

					expr = xpath.compile("//wsdl:message[@name='"
							+ removeNamespace(messageName)
							+ "']/wsdl:part[1]/@element");
					String elementName = (String) expr.evaluate(wsdlDoc,
							javax.xml.xpath.XPathConstants.STRING);
					// <message name="ByUrlRequest">
					// <part name="part1" element="tns:ByUrlRequest">

					expr = xpath.compile("//xsd:element[@name='"
							+ removeNamespace(elementName) + "']/@type");
					String typeName = (String) expr.evaluate(wsdlDoc,
							javax.xml.xpath.XPathConstants.STRING);
					// <xsd:element name="ByUrlRequest"
					// type="tns:ByUrlRequestType"/>

					expr = xpath.compile("//xsd:complexType[@name='"
							+ removeNamespace(typeName)
							+ "']//xsd:element[@name='" + inputName
							+ "']/@default");
					String defaultValue = (String) expr.evaluate(wsdlDoc,
							javax.xml.xpath.XPathConstants.STRING);

					inputObject.setDefaultValue(defaultValue);

					// get the list of enumerations if present

					expr = xpath.compile("//xsd:complexType[@name='"
							+ removeNamespace(typeName)
							+ "']//xsd:element[@name='" + inputName
							+ "']/@type");
					String simpleType = (String) expr.evaluate(wsdlDoc,
							javax.xml.xpath.XPathConstants.STRING);
					// <xsd:complexType name="ByUrlRequestType">
					// ... <xsd:element type="tns:fileFormatType"/>

					expr = xpath.compile("//xsd:simpleType[@name='"
							+ removeNamespace(simpleType)
							+ "']//xsd:enumeration");
					org.w3c.dom.NodeList enums = (org.w3c.dom.NodeList) expr
							.evaluate(wsdlDoc,
									javax.xml.xpath.XPathConstants.NODESET);
					// <xsd:simpleType name="fileFormatType">
					// <xsd:restriction base="xsd:string">
					// <xsd:enumeration value="tiff"/>

					List<String> possibleValues = new ArrayList<String>();
					for (int l = 0; l < enums.getLength(); l++) {
						String value = enums.item(l).getAttributes()
								.getNamedItem("value").getTextContent();
						possibleValues.add(value);
					}
					inputObject.setPossibleValues(possibleValues);

					// special case for enum list inputs -> multiple selects

					expr = xpath.compile("//xsd:complexType[not(@name='"
							+ removeNamespace(typeName)
							+ "')]//xsd:element[@name='" + inputName + "']");
					org.w3c.dom.NodeList enumListInput = (org.w3c.dom.NodeList) expr
							.evaluate(wsdlDoc,
									javax.xml.xpath.XPathConstants.NODESET);

					if (enumListInput != null && enumListInput.getLength() != 0) {

						// default value
						expr = xpath.compile("//xsd:complexType[not(@name='"
								+ removeNamespace(typeName)
								+ "')]//xsd:element[@name='" + inputName
								+ "']/@default");
						String def = (String) expr.evaluate(wsdlDoc,
								javax.xml.xpath.XPathConstants.STRING);
						if (def != null && !(def.equals(""))) {
							inputObject.setDefaultValue(def);
						}
						
						// type
						expr = xpath.compile("//xsd:complexType[not(@name='"
								+ removeNamespace(typeName)
								+ "')]//xsd:element[@name='" + inputName
								+ "']/@type");
						String type = (String) expr.evaluate(wsdlDoc,
								javax.xml.xpath.XPathConstants.STRING);

						// enum list for multiple select
						expr = xpath.compile("//xsd:simpleType[@name='"
								+ removeNamespace(type)
								+ "']//xsd:enumeration");
						org.w3c.dom.NodeList enumList = (org.w3c.dom.NodeList) expr
								.evaluate(wsdlDoc,
										javax.xml.xpath.XPathConstants.NODESET);
						
						// insert into input object
						for (int l = 0; l < enumList.getLength(); l++) {
							String value = enumList.item(l).getAttributes()
									.getNamedItem("value").getTextContent();
							inputObject.addMultipleSelectValue(value);
						}


					}
					
					
					
					

					if (inputNamesToDocumentations.get(inputName) != null) {
						String documentation = inputNamesToDocumentations
								.get(inputName);
						inputObject.setDocumentation(documentation);
					}
				}

				// attach the input objects to the corresponding operation
				// object
				soapOp.setInputs(inputFieldObjects);

				// and add the operation object to the global operations list
				soapOperations.add(soapOp);
			}

			// transfer values to JSP through session and request
			logger.trace("Adding operation to session");
			request.setAttribute("round1", "round1");
			session.setAttribute("endpointURL", endpointURL);
			session.setAttribute("wsdlURL", wsdlURL);
			session.setAttribute("wsdlInterface", wsdlInterface);
			session.setAttribute("serviceDocumentation", serviceDocumentation);
			session.setAttribute("soapOperations", soapOperations);

			// session
			// .setAttribute("operationsAndMessages",
			// operationsAndMessages);

			// get back to JSP
			RequestDispatcher rd = getServletContext().getRequestDispatcher(
					"/interface.jsp");
			rd.forward(request, response);

		} catch (XmlException e) {
			logger.error("Caught XmlException", e);
		} catch (SoapUIException e) {
			logger.error("Caught SoapUIException", e);
		} catch (ParserConfigurationException e) {
			logger.error("Caught ParserConfigurationException", e);
		} catch (SAXException e) {
			logger.error("Caught SaxException", e);
		} catch (javax.xml.xpath.XPathExpressionException e) {
			logger.error("Caught XPathExpressionException", e);
		}

	}

	private String removeNamespace(String name) {
		name = name.substring(name.indexOf(":") + 1);
		return name;
	}

	private HttpClient createAuthenticatingClient(String domain, String user,
			String password) {
		HttpClient client = new HttpClient();

		client.getParams().setAuthenticationPreemptive(true);
		client.getState().setCredentials(new AuthScope(domain, 80),
				new UsernamePasswordCredentials(user, password));
		return client;
	}

	private Object applyXPathSingleNode(InputStream xmlStream,
			String xpathExpression) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(xmlStream);
		XPath xpath = XPath.newInstance(xpathExpression);
		return xpath.selectSingleNode(doc);
	}

	private List<Element> applyXPathSeveralNodes(InputStream xmlStream,
			String xpathExpression) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(xmlStream);
		XPath xpath = XPath.newInstance(xpathExpression);
		return xpath.selectNodes(doc);
	}

	/**
	 * Goes through a nodelist recursively and stores all XML leaves as inputs
	 * 
	 * @param nodes
	 *            DOM nodes to be inspected
	 * @param resultList
	 *            list to be filled with ws inputs
	 * @return list containing all inputs of the web service
	 */
	private List<SOAPinputField> getAllInputFields(org.w3c.dom.NodeList nodes,
			List<SOAPinputField> resultList) {
		for (int i = 0; i < nodes.getLength(); i++) {
			org.w3c.dom.Node currentItem = nodes.item(i);
			if (currentItem.getChildNodes().getLength() == 1
					&& currentItem.getChildNodes().item(0).getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
				SOAPinputField field = new SOAPinputField(currentItem
						.getLocalName());

				boolean isBinary = false;
				org.w3c.dom.NamedNodeMap currentAttributes = currentItem
						.getAttributes();
				for (int j = 0; j < currentAttributes.getLength(); j++) {
					org.w3c.dom.Node currentAttr = currentAttributes.item(j);
					if (currentAttr.getNodeName().indexOf("contentType") > -1
							&& currentAttr.getNodeValue().indexOf(
									"application/") > -1) {
						isBinary = true;
					}
				}
				
				String itemValue = currentItem.getChildNodes().item(0).getNodeValue().trim();
				if(itemValue.startsWith("cid:")) {
					isBinary = true;
				}

				field.setBinary(isBinary);
				resultList.add(field);
			} else {
				getAllInputFields(currentItem.getChildNodes(), resultList);
			}
		}
		return resultList;
	}

}
