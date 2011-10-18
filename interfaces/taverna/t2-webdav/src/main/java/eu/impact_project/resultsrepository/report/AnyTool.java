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

package eu.impact_project.resultsrepository.report;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AnyTool extends Tool {
	private List<URL> inputUrl = new ArrayList<URL>();
	private double processingTime;
	
	public AnyTool(String name, List<URL> inputUrl, double processingTime) {
		this.name = name;
		this.inputUrl = inputUrl;
		this.processingTime = processingTime;
	}
	
	public List<URL> getInputUrl() {
		return inputUrl;
	}
	public double getProcessingTime() {
		return processingTime;
	}
}
