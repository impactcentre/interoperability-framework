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

import java.net.URL;
import java.util.List;

/**
 * Provides a list of services and URLs to concrete services
 * 
 */
public interface ServiceProvider {
	/**
	 * 
	 * @return The list with all existing services
	 */
	abstract List<Service> getServiceList ();
	
	/**
	 * 
	 * @param id	ID of the service
	 * @return		URL to the service
	 */
	abstract URL getUrl (String id);

	/**
	 * Represents a service, e.g. a Web Service
	 */
	public interface Service {
		abstract URL getURL ();
		abstract String getTitle ();
		abstract String getDescription ();
		abstract int getIdentifier();
	}
}
