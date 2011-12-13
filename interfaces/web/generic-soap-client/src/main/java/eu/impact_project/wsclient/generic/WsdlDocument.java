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
package eu.impact_project.wsclient.generic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.SoapUIException;
import com.ibm.wsdl.extensions.schema.SchemaImpl;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.parser.XSOMParser;

import eu.impact_project.wsclient.generic.util.AnnotationFactory;

/**
 * Allows low-level reading of a wsdl
 * 
 */
public class WsdlDocument {

	private URL wsdlUrl;

	// SoapUI
	private WsdlInterface wsdlInterface;

	// wsdl4j
	private Definition wsdlObject;

	// XSOM
	private XSSchema schemaObject;
	private Map<String, XSElementDecl> schemaElements = new HashMap<String, XSElementDecl>();

	/**
	 * Initializes libraries needed for accessing the wsdl and the inner xml
	 * schema
	 * 
	 * @param url
	 *            Reference pointing to the wsdl
	 * @throws IOException
	 *             if there is a problem reading the wsdl
	 */
	public WsdlDocument(URL url) throws IOException {
		try {
			wsdlUrl = url;

			initWsdl4j();
			initXsom();

			// initSoapui
			WsdlProject project = new WsdlProject();
			wsdlInterface = WsdlInterfaceFactory.importWsdl(project,
					wsdlUrl.toString(), false)[0];

		} catch (XmlException e) {
			throw new IOException("Exception in XmlBeans library", e);
		} catch (SoapUIException e) {
			throw new IOException("Exception in SoapUI library", e);
		} catch (WSDLException e) {
			throw new IOException("Exception in Wsdl4J library", e);
		}

	}

	/**
	 * Initializes libraries needed for accessing the wsdl and the inner xml
	 * schema
	 * 
	 * @param url
	 *            Reference pointing to the wsdl
	 * @throws IOException
	 *             if there is a problem reading the wsdl
	 */
	public WsdlDocument(String url) throws IOException {
		this(new URL(url));
	}

	private void initWsdl4j() throws WSDLException {
		WSDLFactory wfactory = WSDLFactory.newInstance();

		WSDLReader reader = wfactory.newWSDLReader();
		wsdlObject = reader.readWSDL(wsdlUrl.toString());
	}

	private void initXsom() throws IOException {
		try {
			SchemaImpl schema = (SchemaImpl) wsdlObject.getTypes()
					.getExtensibilityElements().get(0);

			Element el = schema.getElement();
			InputStream is = prepareSchema(el);
			XSOMParser parser = new XSOMParser();

			// is needed to read element documentations which are inside
			// annotations
			parser.setAnnotationParser(new AnnotationFactory());

			parser.parse(is);

			String targetNamespace = wsdlObject.getTargetNamespace();

			if (parser.getResult() == null)
				throw new IOException(
						"Could not initialize XML Schema");
			schemaObject = parser.getResult().getSchema(targetNamespace);

			addSchemaElements();

		} catch (TransformerConfigurationException e) {
			throw new IOException("Problem initializing XML Schema.", e);
		} catch (TransformerException e) {
			throw new IOException("Problem initializing XML Schema.", e);
		} catch (SAXException e) {
			throw new IOException("Problem initializing XML Schema.", e);
		}

	}

	// the schema document needs some of the namespaces from the wsdl. It
	// doesn't seem to work automatically
	private InputStream prepareSchema(Element el) throws TransformerException {
		@SuppressWarnings("unchecked")
		Map<String, String> namespaces = wsdlObject.getNamespaces();

		for (Map.Entry<String, String> ns : namespaces.entrySet()) {
			if (!ns.getKey().equals(""))
				el.setAttribute("xmlns:" + ns.getKey(), ns.getValue());
		}

		// in case that the schema is imported AND the path to it is relative
		String url = wsdlUrl.toString();
		String contextUrl = url.substring(0, url.lastIndexOf("/")+1);
		NodeList imports = el.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "import");
		for (int i = 0; i < imports.getLength(); i++) {
			Element currentImport = (Element)imports.item(i);
			String location = currentImport.getAttribute("schemaLocation");
			boolean isAbsolute = location.startsWith("http");
			if (!isAbsolute) {
				// make relative paths to absolute paths
				// or else the schema will not be found
				currentImport.setAttribute("schemaLocation", contextUrl + location);
			}
		}
		
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();

		OutputStream os = new ByteArrayOutputStream();

		DOMSource source = new DOMSource(el);
		StreamResult result = new StreamResult(os);
		transformer.transform(source, result);

		String temp = os.toString();
		return new ByteArrayInputStream(temp.getBytes());
	}

	// finds all the element definitions in the xml schema
	private void addSchemaElements() {
		for (Map.Entry<String, XSComplexType> entry : schemaObject
				.getComplexTypes().entrySet()) {
			XSContentType content = entry.getValue().getContentType();
			XSParticle particle = content.asParticle();
			if (particle != null) {
				XSTerm term = particle.getTerm();
				if (term.isModelGroup()) {
					XSParticle[] particles = term.asModelGroup().getChildren();
					for (XSParticle p : particles) {
						XSTerm pterm = p.getTerm();
						if (pterm.isElementDecl()) {
							XSElementDecl e = pterm.asElementDecl();
							schemaElements.put(e.getName(), e);
						}
					}
				}
			}
		}
	}

	/**
	 * Retrieves allowed values for a restricted type
	 * 
	 * @param inputName
	 *            SOAP input
	 * @return allowed values for the input or an empty list if the type of the
	 *         input is not a string restriction
	 */
	public List<String> getPossibleValues(String inputName) {
		List<String> values = new ArrayList<String>();

		XSElementDecl element = schemaElements.get(inputName);
		if (element == null)
			return values;

		XSType type = element.getType();
		if (type == null || !(type instanceof XSRestrictionSimpleType))
			return values;

		XSRestrictionSimpleType res = (XSRestrictionSimpleType) type;

		String baseType = res.getBaseType().getName();
		// allow only xsd:string "enumerations"
		if (!baseType.equals("string"))
			return values;

		for (XSFacet facet : res.getDeclaredFacets()) {
			String value = facet.getValue().toString();
			values.add(value);
		}

		return values;
	}

	/**
	 * Checks if "maxOccurs" of the element definition is greater than 1, i.e.,
	 * if the SOAP message can contain several values for one input
	 * 
	 * @param inputName
	 *            The element name
	 * @return
	 */
	public boolean isMultiValued(String inputName) {
		for (Map.Entry<String, XSComplexType> entry : schemaObject
				.getComplexTypes().entrySet()) {
			XSContentType content = entry.getValue().getContentType();
			XSParticle particle = content.asParticle();
			if (particle != null) {
				XSTerm term = particle.getTerm();
				if (term.isModelGroup()) {
					XSParticle[] particles = term.asModelGroup().getChildren();
					for (XSParticle p : particles) {
						XSTerm pterm = p.getTerm();
						if (pterm.isElementDecl()) {
							XSElementDecl e = pterm.asElementDecl();
							if (e.getName().equals(inputName)) {
								return p.isRepeated();
							}
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * Returns the documentation of the whole wsdl
	 * 
	 * @return
	 */
	public String getServiceDocumentation() {
		Element el = wsdlObject.getDocumentationElement();
		if (el == null)
			return "";
		return el.getTextContent();
	}

	/**
	 * Returns the documentation of a specific operation
	 * 
	 * @param operationName
	 * @return
	 * @throws IOException
	 *             if there is no such operation
	 */
	public String getOperationDocumentation(String operationName)
			throws IOException {
		Operation op = getOperation(operationName);
		Element docu = op.getDocumentationElement();
		if (docu == null)
			return "";
		return docu.getTextContent();
	}

	private Operation getOperation(String operationName) throws IOException {
		// get the first PortType
		PortType p = (PortType) wsdlObject.getPortTypes().values().iterator()
				.next();

		Operation op = p.getOperation(operationName, null, null);
		if (op == null)
			throw new IOException("Could not find operation " + operationName);
		return op;
	}

	/**
	 * Returns the documentation of a specific element
	 * 
	 * @param inputName
	 * @return
	 */
	public String getInputDocumentation(String inputName) {
		XSElementDecl element = schemaElements.get(inputName);

		XSAnnotation anno = element.getAnnotation();
		if (anno != null)
			return anno.getAnnotation().toString();
		return "";
	}

	/**
	 * Returns the documentation of an input element (which is normally of a
	 * complex type). The documentations for the specific simple type inputs
	 * must be encoded inside.
	 * 
	 * @param operationName
	 *            Operation in which the input is located
	 * @return
	 * @throws IOException
	 *             if there is no such operation
	 */
	public String getInputDocumentationsFromWsdl(String operationName)
			throws IOException {
		Operation op = getOperation(operationName);
		Element docu = op.getInput().getDocumentationElement();
		if (docu == null || docu.getTextContent().equals(""))
			return "";
		return docu.getTextContent();
	}

	/**
	 * Creates a request message for the given operation. Default values from
	 * WSDL will be filled in automatically. Otherwise, the values will be
	 * question marks.
	 * 
	 * @param operationName
	 * @return SOAP request
	 * @throws IOException
	 *             if there is no such operation
	 */
	public String generateRequest(String operationName) throws IOException {
		WsdlOperation operation = wsdlInterface
				.getOperationByName(operationName);
		if (operation == null)
			throw new IOException("Operation not found: " + operationName);

		String message = operation.createRequest(true);

		return message;
	}

	protected WsdlInterface getWsdlInterface() {
		return wsdlInterface;
	}

	public URL getWsdlUrl() {
		return wsdlUrl;
	}

}
