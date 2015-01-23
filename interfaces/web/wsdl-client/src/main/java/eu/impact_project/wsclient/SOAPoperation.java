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

import java.util.List;

/**
 * Bean representing a web service operation.
 * @author dennis
 *
 */
public class SOAPoperation {
	
	private String name;
	private String defaultMessage;
	private List<SOAPinputField> inputs;
	private String documentation;
	
	public SOAPoperation(String name, List<SOAPinputField> inputs) {
		super();
		this.name = name;
		this.inputs = inputs;
	}
	public SOAPoperation(String name) {
		super();
		this.name = name;
		this.inputs = null;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<SOAPinputField> getInputs() {
		return inputs;
	}
	public void setInputs(List<SOAPinputField> inputs) {
		this.inputs = inputs;
	}
	public String getDefaultMessage() {
		return defaultMessage;
	}
	public void setDefaultMessage(String defaultMessage) {
		this.defaultMessage = defaultMessage;
	}
	public String getDocumentation() {
		return documentation;
	}
	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	
}
