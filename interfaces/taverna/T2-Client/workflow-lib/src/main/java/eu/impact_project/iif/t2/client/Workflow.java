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

import java.util.ArrayList;
import java.util.List;

/**
 * Bean representing a Taverna workflow
 * 
 * @author dennis
 * 
 */

public class Workflow {

	private String stringVersion;
	private List<WorkflowInput> inputs = new ArrayList<WorkflowInput>();

	public Workflow(String stringVersion) {
		this.stringVersion = stringVersion;
	}

	public Workflow() {
	}

	public String getStringVersion() {
		return stringVersion;
	}

	public void setStringVersion(String stringVersion) {
		this.stringVersion = stringVersion;
	}

	public List<WorkflowInput> getInputs() {
		return inputs;
	}

	public void setInputs(List<WorkflowInput> inputs) {
		this.inputs = inputs;
	}

}
