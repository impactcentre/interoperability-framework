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

import org.apache.commons.httpclient.methods.PutMethod;

/**
 * 
 * An extension for the Apache HttpClient to create Webdav folders.
 *
 */
public class MkcolMethod extends PutMethod {
	public MkcolMethod() {
		super();
	}
	public MkcolMethod(String path){
		super(path);
	}

	public String getName() {
		return "MKCOL";
	}

}
