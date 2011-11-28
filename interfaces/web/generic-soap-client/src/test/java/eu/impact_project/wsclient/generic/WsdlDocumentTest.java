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

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class WsdlDocumentTest {

	private static String wsdlUrl = "http://localhost:9002/IMPACTAbbyyFre10OcrProxy.wsdl";
	private static String wsdlUrlWrapper03 = "http://localhost:9002/IMPACTFineReader_wrapper0.3.wsdl";

	@BeforeClass
	public static void setUp() throws Exception {
		ServerStarter.startWebServer(9002);
	}

	
	@Test
	public void constructor_string() throws IOException {
		WsdlDocument wsdl = new WsdlDocument(wsdlUrl);
		assertNotNull(wsdl);
	}
	@Test(expected = IOException.class)
	public void constructor_noConnection() throws IOException {
		new WsdlDocument("http://localhost:9002/nonExistentService.wsdl");
	}
	@Test
	public void constructor_url() throws IOException {
		URL url = new URL(wsdlUrl);
		WsdlDocument wsdl = new WsdlDocument(url);
		assertNotNull(wsdl);
	}

	
	@Test
	public void getWsdlUrl() throws IOException {
		WsdlDocument wsdl = new WsdlDocument(wsdlUrl);
		String actual = wsdl.getWsdlUrl().toString();
		assertEquals(wsdlUrl, actual);
	}
	
	
	@Test
	public void generateRequest() throws IOException {
		WsdlDocument wsdl = new WsdlDocument(wsdlUrl);
		String request = wsdl.generateRequest("ocrImageFileByUrl");
		assertTrue(request.contains("inputTextType"));
	}
	@Test(expected=IOException.class)
	public void generateRequest_wrongOperation() throws IOException {
		WsdlDocument wsdl = new WsdlDocument(wsdlUrl);
		wsdl.generateRequest("nonExistentOperation");
	}
	
	
	@Test
	public void getOperationDocumentation() throws IOException {
		WsdlDocument wsdl = new WsdlDocument(wsdlUrl);
		String docu = wsdl.getOperationDocumentation("ocrImageFileByUrl");
		assertTrue(docu.contains("Apply text recognition"));
	}
	@Test(expected=IOException.class)
	public void getOperationDocumentation_wrongName() throws IOException {
		WsdlDocument wsdl = new WsdlDocument(wsdlUrl);
		wsdl.getOperationDocumentation("nonExistentOperation");
	}
	
	@Test
	public void getServiceDocumentation() throws IOException {
		WsdlDocument wsdl = new WsdlDocument(wsdlUrl);
		assertTrue(wsdl.getServiceDocumentation().contains("IMPACT Abbyy"));
	}
	@Test
	public void getServiceDocumentation_missing() throws IOException {
		WsdlDocument wsdl = new WsdlDocument(wsdlUrlWrapper03);
		assertEquals("", wsdl.getServiceDocumentation());
	}
	
	@Test
	public void getPossibleValues() throws IOException {
		WsdlDocument wsdl = new WsdlDocument(wsdlUrl);
		
		List<String> values = wsdl.getPossibleValues("recognitionLanguage");
		assertTrue(values.size() == 28);
		assertEquals("Bulgarian", values.get(0));
		
		values = wsdl.getPossibleValues("outputFormat");
		assertTrue(values.size() == 5);

		values = wsdl.getPossibleValues("inputUrl");
		assertTrue(values.size() == 0);

		values = wsdl.getPossibleValues("nonExistent");
		assertTrue(values.size() == 0);
	}
	@Test
	public void getPossibleValues_wrapper03() throws IOException {
		WsdlDocument wsdl = new WsdlDocument(wsdlUrlWrapper03);
		
		List<String> values = wsdl.getPossibleValues("dat");
		assertTrue(values.size() == 26);
	}
	
	
	@Test
	public void isMultiValued() throws IOException {
		WsdlDocument wsdl = new WsdlDocument(wsdlUrl);
		boolean isMulti = wsdl.isMultiValued("recognitionLanguage");
		assertTrue(isMulti);
		
		isMulti = wsdl.isMultiValued("inputUrl");
		assertFalse(isMulti);

		isMulti = wsdl.isMultiValued("inputTextType");
		assertFalse(isMulti);
	}
	@Test
	public void isMultiValued_wrapper03() throws IOException {
		WsdlDocument wsdl = new WsdlDocument(wsdlUrlWrapper03);
		boolean isMulti = wsdl.isMultiValued("languageItem");
		assertTrue(isMulti);
		
		isMulti = wsdl.isMultiValued("input");
		assertFalse(isMulti);
	}

	
	@AfterClass
	public static void tearDown() throws Exception {
		ServerStarter.stopAll();
	}

}
