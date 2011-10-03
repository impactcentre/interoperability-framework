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

import java.util.List;

/**
 * Bundles several Taverna workflow outputs
 * 
 * @author dennis
 * 
 */

public class WorkflowOutputPort {

	private String name;
	private List<WorkflowOutput> outputs;

	public WorkflowOutputPort(String name) {
		this.name = name;
	}

	public WorkflowOutputPort() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<WorkflowOutput> getOutputs() {
		return outputs;
	}

	public void setOutputs(List<WorkflowOutput> outputs) {
		this.outputs = outputs;
	}

}
