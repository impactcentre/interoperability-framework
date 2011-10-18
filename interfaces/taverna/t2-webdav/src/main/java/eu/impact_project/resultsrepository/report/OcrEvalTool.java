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

import java.util.ArrayList;
import java.util.List;

public class OcrEvalTool extends Tool {

	private String evaluationId;
	private List<Evaluation> evals = new ArrayList<Evaluation>();

	public OcrEvalTool(String name, String evaluationId) {
		this.name = name;
		this.evaluationId = evaluationId;
	}

	public String getName() {
		return name;
	}
	
	public String getEvaluationId() {
		return evaluationId;
	}
	
	public void addEvaluation(String chars, String errors, String accuracy,
			String words, String misrecognized, String wordAccuracy) {
		Evaluation eval = new Evaluation();
		eval.characters = chars;
		eval.errors = errors;
		eval.accuracy = accuracy;
		eval.words = words;
		eval.misrecognized = misrecognized;
		eval.wordAccuracy = wordAccuracy;
		evals.add(eval);
	}
	
	public List<Evaluation> getEvaluations() {
		return evals;
	}

	public class Evaluation {
		public String characters;
		public String errors;
		public String accuracy;
		public String words;
		public String misrecognized;
		public String wordAccuracy;
	}
}
