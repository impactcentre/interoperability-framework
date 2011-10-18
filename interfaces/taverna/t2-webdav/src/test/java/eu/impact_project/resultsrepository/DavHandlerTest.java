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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DavHandlerTest {

	private List<String> folders = Arrays.asList(new String[] { "parent",
			"child" });

	@BeforeClass
	public static void setUp() throws Exception {
		ServerStarter.startWebServer(9001);
		ServerStarter.startDavServer(9002);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		ServerStarter.stopAll();
	}

	@Test
	public void testDavHandler() throws HttpException, IOException {
		new DavHandler(folders);

		new URL("http://localhost:9002/parent").getContent();
		new URL("http://localhost:9002/parent/child")
				.getContent();

		// there were no exceptions
		assertTrue(true);

	}

	@Test
	public void testDavHandlerEmptyFolders() throws HttpException, IOException {
		new DavHandler(new ArrayList<String>());

		// there were no exceptions
		assertTrue(true);

	}

	@Test(expected = java.net.ConnectException.class)
	public void testDavHandlerNoDav() throws Exception {

		try {
			ServerStarter.stopAll();
			new DavHandler(folders);
		} catch (Exception e) {
			throw e;
		} finally {
			ServerStarter.startWebServer(9001);
			ServerStarter.startDavServer(9002);
		}

	}

	@Test
	public void testSaveLogs() throws HttpException, IOException {
		DavHandler dav = new DavHandler(folders);

		List<String> logs = Arrays.asList(new String[] { "log1", "log2" });
		dav.saveLogs("myservice", "myevalid", logs);
		InputStream log1 = new URL(
				"http://localhost:9002/parent/child/myservice_myevalid/log/00001.log")
				.openStream();
		// just checking
		new URL(
				"http://localhost:9002/parent/child/myservice_myevalid/log/00002.log")
				.openStream();
		String log1String = IOUtils.toString(log1);
		assertEquals("log1", log1String);

	}

	@Test
	public void testSaveText() throws HttpException, IOException {
		DavHandler dav = new DavHandler(folders);
		dav
				.saveText("some text",
						"http://localhost:9002/parent/child/file.txt");
		InputStream stream = new URL(
				"http://localhost:9002/parent/child/file.txt").openStream();
		String text = IOUtils.toString(stream);
		assertEquals("some text", text);
	}

	@Test(expected = java.io.FileNotFoundException.class)
	public void testSaveStream() throws HttpException, IOException {
		DavHandler dav = new DavHandler(folders);

		InputStream stream = new ByteArrayInputStream("mystream".getBytes());
		dav.saveStream(stream, "http://localhost:9002/parent/child/stream.txt");

		new URL("http://localhost:9002/parent/child/stream.txt").openStream();
		// no exception
		assertTrue(true);

		// expecting exception
		new URL("http://localhost:9002/parent/child/stream2.txt").openStream();

	}

	@Test
	public void testSaveFiles() throws HttpException, IOException {
		DavHandler dav = new DavHandler(folders);

		UrlParts urlParts = new UrlParts();
		urlParts.service = "IMPACTTesseractV3Service";
		urlParts.port = "outputUrl";
		urlParts.extension = "txt";
		String url1 = "http://localhost:9001/IMPACTTesseractV3Service_outputUrl_tmpfile1.tif.out.txt";
		String url2 = "http://localhost:9001/IMPACTTesseractV3Service_outputUrl_tmpfile2.tif.out.txt";
		ArrayList<String> urls = new ArrayList<String>();
		urls.add(url1);
		urls.add(url2);
		ToolResults results = new ToolResults();
		results.setFields(urls);
		dav.saveFiles(urlParts, results);
		
		new URL("http://localhost:9002/parent/child/IMPACTTesseractV3Service/outputUrl/00001.txt").openStream();
		new URL("http://localhost:9002/parent/child/IMPACTTesseractV3Service/outputUrl/00002.txt").openStream();
		
	}

	@Test
	public void testGetFolderHierarchyUrl() throws HttpException, IOException {
		DavHandler dav = new DavHandler(folders);
		String url = dav.getFolderHierarchyUrl();
		assertEquals("http://localhost:9002/parent/child", url);
	}

	@Test
	public void testRetrieveTextFile() throws HttpException, IOException {
		DavHandler dav = new DavHandler(folders);

		InputStream stream = new ByteArrayInputStream("some text".getBytes());
		PutMethod putMethod = new PutMethod(
				"http://localhost:9002/parent/child/textToRetrieve.txt");
		putMethod.setRequestEntity(new InputStreamRequestEntity(stream));
		HttpClient client = new HttpClient();
		client.executeMethod(putMethod);
		stream.close();
		putMethod.releaseConnection();

		String retrieved = dav.retrieveTextFile("http://localhost:9002/parent/child/textToRetrieve.txt");
		assertEquals("some text", retrieved);
	}

	@Test
	public void testGetFileEndings() throws HttpException, IOException {
		DavHandler dav = new DavHandler(folders);
		Set<String> endings = dav.getFileEndings();
		assertNotNull(endings);
		assertTrue(endings.contains("tif"));
	}

}
