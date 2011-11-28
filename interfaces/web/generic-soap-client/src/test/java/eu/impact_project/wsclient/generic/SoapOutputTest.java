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
package eu.impact_project.wsclient.generic;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SoapOutputTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetValue() {
		String request = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
				+ "<soap:Body>"
				+ "<ns2:sayHiResponse xmlns:ns2=\"http://example.webservice.commons.sub.unigoettingen.de/\">"
				+ "<return>Hello Bernhard</return>"
				+ "</ns2:sayHiResponse>"
				+ "</soap:Body>" + "</soap:Envelope>";
		SoapOutput output = new SoapOutput("return", request);
		assertTrue(output.getValue().equals("Hello Bernhard"));

		output = new SoapOutput("wrongname", request);
		assertTrue(output.getValue().equals(""));

	}

}
