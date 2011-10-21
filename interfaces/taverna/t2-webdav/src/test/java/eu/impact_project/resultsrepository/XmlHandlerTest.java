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

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.MalformedURLException;

import org.jdom.JDOMException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class XmlHandlerTest {

	@BeforeClass
	public static void setUp() throws Exception {
		ServerStarter.startWebServer(9001);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		ServerStarter.stopAll();
	}

	@Test
	public void testIsEvaluation() {
		assertTrue(XmlHandler
				.isEvaluation("<bla><Character_Evaluation>123</Character_Evaluation></bla>"));
		assertFalse(XmlHandler
				.isEvaluation("<bla><Character_Eval>123</Character_Eval></bla>"));
	}

	@Test
	public void testGetElementContent() throws NoSuchFieldException {
		String value = XmlHandler.getElementContent("\n<bla>123</bla>", "bla");
		assertEquals("123", value);
	}

	@Test(expected=NoSuchFieldException.class)
	public void testGetElementContentNoElement() throws NoSuchFieldException {
		XmlHandler.getElementContent("\n<bla>123</bla>", "bla2");
	}

	@Test
	public void testGetLayoutEvaluations() throws MalformedURLException, JDOMException, IOException {
		String url = "http://localhost:9001/IMPACTLayoutEvalService_outputUrl_Default_Evaluation_ID_tmp.tmp.out.evx";
		String[] evals = XmlHandler.getLayoutEvaluations(url);
		
		assertEquals(2, evals.length);
		assertEquals("1.00000", evals[0]);
		assertEquals("1.00000", evals[1]);
	}

	@Test(expected=JDOMException.class)
	public void testGetLayoutEvaluationsWrongXML() throws MalformedURLException, JDOMException, IOException {
		String url = "http://localhost:9001/IMPACTOcrEvalService_xmlReportOutputUrl_Tesseract3_tmpfilefromurl3426034968434706595.tmp.report.xml";
		XmlHandler.getLayoutEvaluations(url);
	}

	@Test(expected=JDOMException.class)
	public void testGetLayoutEvaluationsText() throws MalformedURLException, JDOMException, IOException {
		String url = "http://localhost:9001/IMPACTTesseractV3Service_outputUrl_tmpfile1.tif.out.txt";
		XmlHandler.getLayoutEvaluations(url);
	}

	@Test
	public void testGetWordEvaluations() throws MalformedURLException, JDOMException, IOException {
		String url = "http://localhost:9001/IMPACTWordEvaluationService_outputFile_6922033601589215833.xml";
		String[] evals = XmlHandler.getWordEvaluations(url);
		
		assertEquals(3, evals.length);
		assertEquals("273", evals[0]);
		assertEquals("207", evals[1]);
		assertEquals("0.24175824175824176", evals[2]);
	}

}
