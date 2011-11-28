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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.Request.SubmitException;

import eu.impact_project.wsclient.generic.util.MyUtils;

/**
 * An executable operation with inputs and outputs
 * 
 */
public class SoapOperation {

	private Logger logger = LoggerFactory.getLogger(SoapOperation.class);
	private static final int SOAP_REQUEST = 0;
	private static final int SOAP_RESPONSE = 1;

	private String name;
	private Map<String, SoapInput> inputs = new HashMap<String, SoapInput>();
	private Map<String, SoapOutput> outputs = new HashMap<String, SoapOutput>();

	private WsdlDocument wsdlDoc;
	private WsdlOperation operation;
	private String soapResponse;

	private Response wsdlResponse;

	/**
	 * Creates a new operation by reading the wsdl document
	 * 
	 * @param name
	 *            of the operation
	 * @param wsdlDoc
	 *            contains info about the operation
	 * @throws IOException
	 */
	public SoapOperation(String name, WsdlDocument wsdlDoc) throws IOException {
		this.name = name;
		this.wsdlDoc = wsdlDoc;

		operation = wsdlDoc.getWsdlInterface().getOperationByName(name);

		Document defaultSoapDoc = MyUtils.toDocument(getDefaultRequest());
		parseSoap(defaultSoapDoc.getRootElement(), SOAP_REQUEST);
	}

	// creates either inputs or outputs while parsing the soap
	private void parseSoap(Element current, int soapType) throws IOException {
		if (current.getChildren().size() == 0 && !current.getText().equals("")) {
			if (soapType == SOAP_REQUEST) {
				SoapInput input = new SoapInput(current.getName(), wsdlDoc,
						this.name);
				inputs.put(current.getName(), input);
			} else if (soapType == SOAP_RESPONSE) {
				SoapOutput output = new SoapOutput(current.getName(),
						soapResponse);
				outputs.put(current.getName(), output);
			}
		} else {
			for (Object obj : current.getChildren()) {
				Element child = (Element) obj;
				parseSoap(child, soapType);
			}
		}
	}

	public String getName() {
		return name;
	}

	public List<SoapInput> getInputs() {
		return new ArrayList<SoapInput>(inputs.values());
	}

	public List<SoapOutput> getOutputs() {
		return new ArrayList<SoapOutput>(outputs.values());
	}

	public SoapInput getInput(String name) {
		return inputs.get(name);
	}

	/**
	 * Request message containing default values from the WSDL or question marks
	 * otherwise
	 * 
	 * @return
	 * @throws IOException
	 */
	public String getDefaultRequest() throws IOException {
		return wsdlDoc.generateRequest(name);
	}

	public String getDocumentation() throws IOException {
		String docu = wsdlDoc.getOperationDocumentation(name);
		return MyUtils.normalize(docu);
	}

	/**
	 * Sends the currently set inputs to the service
	 * 
	 * @return Received outputs
	 * @throws IOException
	 */
	public List<SoapOutput> execute() throws IOException {
		try {
			String request = getRequest();

			// prepare the request
			WsdlRequest wsdlRequest = operation.addNewRequest("req");
			wsdlRequest.setRequestContent(request);

			// send the request
			WsdlSubmit submit = (WsdlSubmit) wsdlRequest.submit(
					new WsdlSubmitContext(wsdlRequest), false);

			// get the response
			wsdlResponse = submit.getResponse();
			soapResponse = wsdlResponse.getContentAsString();

			InputStream soapStream = new ByteArrayInputStream(
					soapResponse.getBytes());
			SAXBuilder parser = new SAXBuilder();
			Document result = parser.build(soapStream);

			// create Output objects
			parseSoap(result.getRootElement(), SOAP_RESPONSE);

		} catch (SubmitException e) {
			throw new IOException("Problem while submitting the message.", e);
		} catch (JDOMException e) {
			throw new IOException(
					"Problem while parsing the SOAP request message.", e);
		}

		return new ArrayList<SoapOutput>(outputs.values());

	}

	/**
	 * Generates a SOAP message using the currently set input values
	 * 
	 * @return Request message
	 * @throws IOException
	 */
	public String getRequest() throws IOException {
		Document request = MyUtils.toDocument(getDefaultRequest());

		Map<Element, List<Element>> additionalInputs = new HashMap<Element, List<Element>>();
		insertValues(request.getRootElement(), additionalInputs);

		for (Element parent : additionalInputs.keySet()) {
			List<Element> childNodes = additionalInputs.get(parent);
			parent.addContent(childNodes);
		}

		// store the XML document in a String
		XMLOutputter serializer = new XMLOutputter();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();

		serializer.output(request, bout);
		String requestString = bout.toString();

		return requestString;
	}

	public String getResponse() {
		return soapResponse;
	}

	public List<SoapAttachment> getReceivedAttachments() {
		List<SoapAttachment> list = new ArrayList<SoapAttachment>();

		Attachment[] atts = wsdlResponse.getAttachments();
		for (Attachment att : atts) {
			try {
				SoapAttachment temp = new SoapAttachment(att.getInputStream(),
						att.getContentType());
				list.add(temp);
			} catch (Exception e) {
				logger.warn("Could not get the attachment '" + att.getName()
						+ "'");
			}
		}

		return list;
	}

	private void insertValues(Element current,
			Map<Element, List<Element>> additionalInputs) {

		if (inputs.get(current.getName()) != null) {
			SoapInput input = inputs.get(current.getName());
			String value = input.getValue();
			current.setText(value);

			// if there are several values
			List<Element> clones = new ArrayList<Element>();
			List<String> values = input.getValues();
			for (int i = 1; i < values.size(); i++) {
				Element clone = (Element) current.clone();
				clone.setText(values.get(i));
				clones.add(clone);
			}
			if (clones.size() > 0) {
				additionalInputs.put(current.getParentElement(), clones);
			}

			// recursive checking of all children
		} else {
			for (Object obj : current.getChildren()) {
				Element child = (Element) obj;
				insertValues(child, additionalInputs);
			}
		}

	}

}
