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
import java.util.List;

import javax.xml.ws.Endpoint;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.impact_project.wsclient.generic.example.HelloImpl;


public class SoapOperationTest {

	private static WsdlDocument wsdlDoc;
	private static WsdlDocument wsdlDocImpact;
	private static SoapOperation operation;

	@BeforeClass
	public static void setUp() throws Exception {
		HelloImpl implementor = new HelloImpl();

		ServerStarter.startSoapServer(9001, "helloWorld", implementor);
		wsdlDoc = new WsdlDocument("http://localhost:9001/helloWorld?wsdl");
		
		ServerStarter.startWebServer(9002);
		wsdlDocImpact = new WsdlDocument("http://localhost:9002/IMPACTAbbyyFre10OcrProxy.wsdl");

		operation = new SoapOperation("sayHi", wsdlDoc);
	}


	@Test
	public void getInputs() {
		assertTrue(operation.getInputs().size() == 2);
	}

	@Test
	public void getInput() {
		SoapInput input = operation.getInput("text");
		assertNotNull(input);
	}

	@Test
	public void getDefaultRequest() throws IOException {
		String req = operation.getDefaultRequest();
		assertTrue(req.contains("<text>Bernhard</text>"));
	}

	@Test
	public void getDocumentation() throws IOException {
		// TODO ins beispiel einbauen
		//System.out.println(operation.getDocumentation());
	}

	@Test
	public void execute() throws IOException {
		List<SoapOutput> outputs = operation.execute("admin","admin");
		SoapOutput out = outputs.get(0);
		assertTrue(out.getName().equals("return"));
		assertTrue(out.getValue().equals("Hello Bernhard"));
	}

	@Test
	public void getRequest() throws IOException {
		operation.getInput("text").setValue("Ruediger");
		String req = operation.getRequest();
		assertTrue(req.contains("<text>Ruediger</text>"));
	}
	
	@Test
	public void getRequest_severalValues() throws IOException {
		SoapOperation op = new SoapOperation("ocrImageFileByUrl", wsdlDocImpact);
		op.getInput("recognitionLanguage").setValue("bla");
		op.getInput("recognitionLanguage").addValue("bla2");
		System.out.println(op.getRequest());
		
		for (String s : op.getInput("recognitionLanguage").getValues())
			System.out.println(s);
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		ServerStarter.stopAll();
	}

}
