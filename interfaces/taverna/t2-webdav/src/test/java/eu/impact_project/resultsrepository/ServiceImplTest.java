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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class ServiceImplTest {

	String demonstratorId = "ugoe";
	String workflowId = "testWorkflow";
	long time = 0;

	@BeforeClass
	public static void setUpClass() throws Exception {
		ServerStarter.startWebServer(9001);
		ServerStarter.startDavServer(9002);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		ServerStarter.stopAll();
	}
	
	@Before
	public void setUp() {
		time = new Date().getTime();
	}

	@Test
	public void testStoreData() {
		ArrayList<String> resultsList = new ArrayList<String>();
		resultsList.add("http://localhost:9001/IMPACTTesseractV3Service/1234/outputFile/outputUrl_tmpfile1.tif.out.txt");
		resultsList.add("http://localhost:9001/IMPACTTesseractV3Service/1234/outputFile/outputUrl_tmpfile2.tif.out.txt");
		String message = executeWithList(resultsList);
		
		assertTrue(message.indexOf("Stored results at") >= 0);
		assertTrue(message.indexOf(demonstratorId + "/" + workflowId + "/") >= 0);
		assertFalse(message.indexOf("warnings or errors") >= 0);
	}
	
	@Test
	public void testStoreDataWordEval() {
		ArrayList<String> resultsList = new ArrayList<String>();
		resultsList.add("http://localhost:9001/IMPACTWordEvaluationService/1234/outputFile/outputFile_6922033601589215833.xml");
		String message = executeWithList(resultsList);
		
		assertTrue(message.indexOf("Stored results at") >= 0);
		assertTrue(message.indexOf(demonstratorId + "/" + workflowId + "/") >= 0);
		assertFalse(message.indexOf("warnings or errors") >= 0);
	}
	
	@Test
	public void testStoreDataLogs() throws MalformedURLException, IOException {
		String message = executeWithString(getText("http://localhost:9001/log_eval.txt"));	
		assertFalse(message.indexOf("warnings or errors") >= 0);

		message = executeWithString(getText("http://localhost:9001/log_tesseract.txt"));	
		assertTrue(message.indexOf("warnings or errors") >= 0);

	}

	@Test
	public void testStoreDataBadArguments() throws MalformedURLException, IOException {
		String demonstratorIdCopy = demonstratorId;
		String workflowIdCopy = workflowId;

		String expectedError = "Invalid workflow ID or demonstrator ID.";
		
		demonstratorId = null;
		String message = executeWithString(getText("http://localhost:9001/log_eval.txt"));
		assertTrue(message.contains(expectedError));

		demonstratorId = "";
		message = executeWithString(getText("http://localhost:9001/log_eval.txt"));
		assertTrue(message.contains(expectedError));

		demonstratorId = "?";
		message = executeWithString(getText("http://localhost:9001/log_eval.txt"));
		assertTrue(message.contains(expectedError));

		demonstratorId = demonstratorIdCopy;

		workflowId = null;
		message = executeWithString(getText("http://localhost:9001/log_eval.txt"));
		assertTrue(message.contains(expectedError));

		workflowId = "";
		message = executeWithString(getText("http://localhost:9001/log_eval.txt"));
		assertTrue(message.contains(expectedError));

		workflowId = ".,";
		message = executeWithString(getText("http://localhost:9001/log_eval.txt"));
		assertTrue(message.contains(expectedError));

		workflowId = workflowIdCopy;
	}
	
	@Test
	public void testStoreDataSpecialArguments() throws MalformedURLException, IOException {
		String demonstratorIdCopy = demonstratorId;
		String workflowIdCopy = workflowId;

		demonstratorId = "with-dash";
		String message = executeWithString(getText("http://localhost:9001/log_eval.txt"));
		assertTrue(message.indexOf("Stored results at") >= 0);

		demonstratorId = demonstratorIdCopy;

		workflowId = "with-dash";
		message = executeWithString(getText("http://localhost:9001/log_eval.txt"));
		assertTrue(message.indexOf("Stored results at") >= 0);

		workflowId = workflowIdCopy;
	}
	
	@Test
	public void testStoreDataEmptyResults() throws MalformedURLException, IOException {
		String expectedError = "There were no results to be stored.";
		
		Results results = new Results();
		Service ws = new ServiceImpl();
		String message =  ws.storeData(results, workflowId, time, demonstratorId);
		assertTrue(message.contains(expectedError));
		
		message =  ws.storeData(results, null, time, demonstratorId);
		assertTrue(message.contains(expectedError));
	}
	
	@Test
	public void testStoreDataNoOcrAccuracy() throws MalformedURLException, IOException {
		
		String noAccuracy = "http://localhost:9001/IMPACTOcrEvalService/1234/outputFile/xmlReportOutputUrl_IMPACT_tmpfilefromurl821755019209514659.tmp.report.xml";
		String message = executeWithString(noAccuracy);
		
		assertTrue(message.indexOf("Stored results at") >= 0);
		assertTrue(message.indexOf(demonstratorId + "/" + workflowId + "/") >= 0);
		assertFalse(message.indexOf("warnings or errors") >= 0);
	}
	
	
	
	
	private Results constructFromList(ArrayList<String> res) {
		ToolResults tr = new ToolResults();
		tr.setFields(res);
		Results results = new Results();
		results.addToolResults(tr);
		return results;
	}
	
	private String getText(String url) throws MalformedURLException, IOException {
		InputStream is = new URL(url).openStream();
		return IOUtils.toString(is);
	}
	
	private String executeWithString(String result) {
		ArrayList<String> resultsList = new ArrayList<String>();
		resultsList.add(result);
		Results results = constructFromList(resultsList);		
		Service ws = new ServiceImpl();
		return ws.storeData(results, workflowId, time, demonstratorId);
	}
	
	private String executeWithList(ArrayList<String> l) {
		Results results = constructFromList(l);		
		Service ws = new ServiceImpl();
		return ws.storeData(results, workflowId, time, demonstratorId);
	}


	
}
