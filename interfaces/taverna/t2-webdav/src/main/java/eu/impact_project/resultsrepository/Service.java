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

package eu.impact_project.resultsrepository;

import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * 
 * @author dennis
 * 
 *         Service that can store the results of a workflow into a repository.
 * 
 */
@WebService
public interface Service {

	/**
	 * Stores the given results into a repository.
	 * 
	 * @param allResults
	 *            The results of the complete workflow that should be stored.
	 *            Contains lists of individual output ports.
	 * @param workflowID
	 *            A string that identifies the run workflow. Should be the URL
	 *            to the workflow on MyExperiment.
	 * @param timer
	 *            Timestamp of the workflow start. Is used to compute the
	 *            overall execution time of the workflow.
	 * @param demonstratorID
	 *            Arbitrary string that should identify the executor of the
	 *            workflow.
	 * @return A message that informs the user of the storage success and/or of
	 *         the occurred errors.
	 */
	public String storeData(@WebParam(name = "allResults") Results allResults,
			@WebParam(name = "workflowID") String workflowID,
			@WebParam(name = "timer") long timer,
			@WebParam(name = "demonstratorID") String demonstratorID);
}
