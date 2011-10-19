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

import java.util.ArrayList;
import java.util.List;

/**
 * Bean representing an input field of a WSDL operation.
 *
 */
public class SOAPinputField {

	private List<String> possibleValues;
	private List<String> multipleSelectValues = new ArrayList<String>();;
	public List<String> getMultipleSelectValues() {
		return multipleSelectValues;
	}
	public void addMultipleSelectValue(String value) {
		this.multipleSelectValues.add(value);
	}
	private String defaultValue;
	private String name;
	private String documentation;
	private boolean binary;

	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public SOAPinputField(String name) {
		super();
		this.name = name;
	}
	public SOAPinputField(String name, String documentation) {
		super();
		this.name = name;
		this.documentation = documentation;
	}
	public SOAPinputField(String name, String documentation, boolean binary) {
		super();
		this.name = name;
		this.documentation = documentation;
		this.binary = binary;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDocumentation() {
		return documentation;
	}
	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}
	public boolean isBinary() {
		return binary;
	}
	public void setBinary(boolean binary) {
		this.binary = binary;
	}
	public void setPossibleValues(List<String> possibleValues) {
		this.possibleValues = possibleValues;
	}
	public List<String> getPossibleValues() {
		return possibleValues;
	}
	
	
}
