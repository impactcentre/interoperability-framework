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

import uk.org.taverna.server.client.AbstractPortValue;
import uk.org.taverna.server.client.OutputPort;
import uk.org.taverna.server.client.PortListValue;
import uk.org.taverna.server.client.Run;

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
	
	public void setOutput(WorkflowOutput output) {
		this.outputs.add(output);
	}
	
	public void setOutput(String name, String url, String output) {
		if (output != null)
		{
			WorkflowOutput translate = new WorkflowOutput();
			
			this.name = name;
			translate.setUrl(url);
			translate.setValue(output);
			this.outputs.add(translate);
		}
		
	}
	
	public void setOutput(OutputPort output,boolean binary) {
		WorkflowOutput translate = new WorkflowOutput();
		String outputAsString = output.getDataAsString();
		String data = new String(outputAsString);
		
		this.name = output.getName();
		translate.setValue(data);
		translate.setUrl(output.getValue().toString());
		translate.setBinary(binary);
		
		if (outputs == null)
			outputs = new ArrayList<WorkflowOutput>();
		
		this.outputs.add(translate);

	}

	public void setOutput(OutputPort output, Run run, String name, int depth) {
		WorkflowOutput translate = new WorkflowOutput();
		
		PortListValue listPorts = (PortListValue) output.getValue();
		
		for (List<AbstractPortValue> port : listPorts)
		{
			//OutputPort currentOutput = OutputPort.newOutputPort(run, name, depth, port);
			OutputPort currentOutput = new OutputPort(run, name, depth, (AbstractPortValue) port);
			setOutput(currentOutput,true);
		}

	}
	
}
