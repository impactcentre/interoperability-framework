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

/**
 * Bean representing an input to a Taverna workflow
 * 
 * @author dennis
 * 
 */

public class WorkflowInput {

	private String name;
	private int depth;
	private boolean binary;
	private String exampleValue;

	public String getExampleValue() {
		return exampleValue;
	}

	public void setExampleValue(String exampleValue) {
		this.exampleValue = exampleValue;
	}

	public WorkflowInput(String name) {
		this.name = name;
	}

	public WorkflowInput() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public boolean isBinary() {
		return binary;
	}

	public void setBinary(boolean binary) {
		this.binary = binary;
	}

}
