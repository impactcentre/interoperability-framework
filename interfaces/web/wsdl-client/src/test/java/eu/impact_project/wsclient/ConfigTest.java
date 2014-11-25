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

import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import eu.impact_project.wsclient.ServiceProvider.Service;

public class ConfigTest {

	@Test
	public void testConfig () {
		try {
			ServiceProvider sp = new FileServiceProvider("src/main/resources/services.xml");
			List<Service> services = sp.getServiceList();
			for (Service s: services) {
				System.out.println(s.getDescription());
			}
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
