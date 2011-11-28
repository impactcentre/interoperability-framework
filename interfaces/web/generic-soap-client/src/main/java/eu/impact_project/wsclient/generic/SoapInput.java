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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.xpath.XPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.impact_project.wsclient.generic.util.MyUtils;

/**
 * Bean representing an input field of a WSDL operation.
 * 
 */
public class SoapInput {
	private Logger logger = LoggerFactory.getLogger(SoapInput.class);

	private String name;
	private WsdlDocument wsdlDoc;
	private String parentOperation;
	private String defaultValue;
	private boolean binary = false;
	private List<String> values;
	private String documentation = null;

	public SoapInput(String name, WsdlDocument wsdlDoc, String parentOperation)
			throws IOException {
		this.name = name;
		this.parentOperation = parentOperation;
		this.wsdlDoc = wsdlDoc;
		values = new ArrayList<String>();
		init();
	}

	private void init() throws IOException {
		Document request = MyUtils.toDocument(wsdlDoc
				.generateRequest(parentOperation));
		setDocumentation();
		String expression = "//*[local-name()='" + name + "'][1]/text()";
		try {
			XPath xp = XPath.newInstance(expression);
			Text t = (Text) xp.selectSingleNode(request);
			String value = t.getText();
			if (value.equals("?")) {
				defaultValue = "";
			} else if (value.startsWith("cid:")) {
				binary = true;
				defaultValue = "";
			} else {
				defaultValue = value;
			}

		} catch (JDOMException e) {
			throw new IOException("Could not read the generated SOAP using "
					+ expression, e);
		}
	}

	private void setDocumentation() {
		try {
			// documentation from the xsd annotation in the element
			documentation = wsdlDoc.getInputDocumentation(name);
			if (documentation != null && !documentation.equals(""))
				return;

			// coded inside documentation of a wsdl input
			// eg "input1: doc for input1; input2: second input documentation"
			String completeInputDocu = wsdlDoc
					.getInputDocumentationsFromWsdl(parentOperation);
			if (completeInputDocu.indexOf(name + ":") < 0)
				throw new IOException();

			int from = completeInputDocu.indexOf(name + ":") + name.length()
					+ 1;
			int to = completeInputDocu.indexOf(";", from);
			if (to == -1)
				to = completeInputDocu.length();
			documentation = completeInputDocu.substring(from, to);
			documentation = MyUtils.normalize(documentation);
		} catch (IOException e) {
			documentation = "";
			logger.info("No documentation found for input " + name);
		}
	}

	public String getDocumentation() throws IOException {
		if (documentation == null)
			throw new IOException("No documentation for input: " + name);
		return documentation;
	}

	public String getValue() {
		if (values.size() > 0)
			return values.get(0);
		else
			return "";
	}

	public void setValue(String value) {
		values.clear();
		values.add(value);
	}

	public List<String> getValues() {
		return values;
	}

	public void addValue(String value) {
		this.values.add(value);
	}

	public void clearValues() {
		values.clear();
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return true if a file is expected as input
	 */
	public boolean isBinary() {
		return binary;
	}

	/**
	 * 
	 * @return all allowed values for this input
	 */
	public List<String> getPossibleValues() {
		return wsdlDoc.getPossibleValues(name);
	}

	/**
	 * 
	 * @return true if the input can take several values
	 */
	public boolean isMultiValued() {
		return wsdlDoc.isMultiValued(name);
	}

}
