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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LogHandlerTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	private static ToolResults wrap(String log1, String log2) {
		ToolResults logs = new ToolResults();
		ArrayList<String> list = new ArrayList<String>();
		list.add(log1);
		list.add(log2);
		logs.setFields(list);
		return logs;
	}

	@Test
	public void testGetUrls() throws MalformedURLException,
			NoSuchFieldException {
		String log1 = "bla\nURL of input image: http://test.com/test1.txt.\n";
		String log2 = "bla\nURL of input image: http://test.com/test2.txt.\n";
		ToolResults logs = wrap(log1, log2);

		List<URL> urls = LogHandler.getUrls(logs);

		assertTrue(urls.size() == 2);
		assertEquals("http://test.com/test1.txt", urls.get(0).toString());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testGetUrlsWrongLog() throws MalformedURLException,
			NoSuchFieldException {
		String log1 = "bla\nURL of input image: http://test.com/test1.txt.\n";
		String log2 = "bla\ninput image: http://test.com/test2.txt.\n";
		ToolResults logs = wrap(log1, log2);

		LogHandler.getUrls(logs);
	}

	@Test(expected = NoSuchFieldException.class)
	public void testGetUrlsWrongLog2() throws MalformedURLException,
			NoSuchFieldException {
		String log1 = "bla\nURL of input image: http://test.com/test1.txt.\n";
		String log2 = "bla\nURL of input image: http://test.com/test2.txt";
		ToolResults logs = wrap(log1, log2);

		LogHandler.getUrls(logs);
	}

	@Test(expected = MalformedURLException.class)
	public void testGetUrlsBadUrl() throws MalformedURLException,
			NoSuchFieldException {
		String log1 = "bla\nURL of input image: http://test.com/test1.txt.\n";
		String log2 = "bla\nURL of input image: /test.com/test2.txt.\n";
		ToolResults logs = wrap(log1, log2);

		LogHandler.getUrls(logs);
	}

	@Test
	public void testGetTime() throws NoSuchFieldException {
		String log1 = "bla\nProcess finished successfully after 1111 milliseconds..";
		String log2 = "bla\nProcess finished successfully after 2222 milliseconds..";
		List<String> logs = Arrays.asList(new String[] { log1, log2 });
		long totalTime = LogHandler.getTime(logs);
		assertTrue(3333 == totalTime);
	}

	@Test(expected = NoSuchFieldException.class)
	public void testGetTimeWrongLog() throws NoSuchFieldException {
		String log1 = "bla\nProcess finished successfully after 1111 milliseconds..";
		String log2 = "bla\nsome stuff..";
		List<String> logs = Arrays.asList(new String[] { log1, log2 });
		LogHandler.getTime(logs);
	}

	@Test(expected = NoSuchFieldException.class)
	public void testGetTimeNaN() throws NoSuchFieldException {
		String log1 = "bla\nProcess finished successfully after 1111 milliseconds..";
		String log2 = "bla\nProcess finished successfully after abc milliseconds..";
		List<String> logs = Arrays.asList(new String[] { log1, log2 });
		LogHandler.getTime(logs);
	}

	@Test
	public void testSplitUrl() throws MalformedURLException {
		UrlParts parts = LogHandler
				.splitUrl("http://domain.org/MyService/1234/outputFile/MyService_myport_tmp.txt");
		assertEquals("MyService", parts.service);
		assertEquals("outputFile", parts.port);
		assertEquals("txt", parts.extension);
		assertEquals("", parts.evalId);

		UrlParts partsEval = LogHandler
				.splitUrl("http://domain.org/MyService/myEvalId/outputFile/tmp.txt");
		assertEquals("MyService", partsEval.service);
		assertEquals("", partsEval.evalId);
		
		UrlParts partsEvalWithUnderscores = LogHandler
				.splitUrl("http://domain.org/dir/MyService/myEvalId_With_Underscores/outputFile/MyService_myport_myEvalId_With_Underscores_tmp.txt");
		assertEquals("", partsEvalWithUnderscores.evalId);
                
                UrlParts partsEval2 = LogHandler
				.splitUrl("http://domain.org/MyService/Id/myEvalId/outputFile/tmp.txt");
	}

	@Test(expected = MalformedURLException.class)
	public void testSplitUrlMalformed() throws MalformedURLException {
		LogHandler.splitUrl("http//domain.org/dir/1234/MyService_myport_tmp.txt");
	}

	@Test(expected = MalformedURLException.class)
	public void testSplitUrlBadFormat() throws MalformedURLException {
		LogHandler.splitUrl("http://domain.org/dir123MyService.txt");
	}

	@Test
	public void testServiceName() throws NoSuchFieldException {
		String service = LogHandler
				.serviceName("...\nUsing service: MyService.\n bla\n blub");
		assertEquals("MyService", service);
	}

	@Test(expected = NoSuchFieldException.class)
	public void testServiceNameNoName() throws NoSuchFieldException {
		LogHandler.serviceName("...\nUsing service: .\n bla\n blub");
	}

	@Test(expected = NoSuchFieldException.class)
	public void testServiceWrongFormat() throws NoSuchFieldException {
		LogHandler.serviceName("...\nUsing service = MyService.\n bla\n blub");
	}

	@Test(expected = NoSuchFieldException.class)
	public void testServiceWrongFormat2() throws NoSuchFieldException {
		LogHandler.serviceName("...\nUsing service: MyService \n bla.\n blub");
	}

	@Test
	public void testEvaluationId() throws NoSuchFieldException {
		String id = LogHandler
				.evaluationId("...\nEvaluation-ID: MyEvalId.\n bla\n blub");
		assertEquals("MyEvalId", id);
	}

	@Test
	public void testEvaluationIdNoId() throws NoSuchFieldException {
		String id = LogHandler.evaluationId("...\nEvaluation-ID: .\n bla\n blub");
		assertEquals("", id);
	}

	@Test
	public void testEvaluationIdWrongFormat() throws NoSuchFieldException {
		String id = LogHandler.evaluationId("...\nEvaluation-ID = MyEvalId.\n bla\n blub");
		assertEquals("", id);
		id = LogHandler.evaluationId("...\nEvaluation-ID: MyEvalId \n bla.\n blub");
		assertEquals("", id);
	}

}
