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

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;

import javax.net.ssl.SSLHandshakeException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Is responsible for all operations on the Webdav repository.
 */
public class DavHandler {

	/**
	 * Contains a file that is downloaded from a URL. Used as an intermediary
	 * place for copying the file into the repository.
	 */
	private byte[] downloadedFile;

//	/** Make separator os dependant **/
	private final String separator = "/";

	/** URL to the repository. */
	private String davUrl;

	/**
	 * List of all folders that are created in the repository for the storage of
	 * the current results.
	 */
	private List<String> folderHierarchy;

	/** User name for access. */
	private String davUser;

	/** Access password. */
	private String davPassword;

	private HttpClient client;

	/** The endings are read from a config file. */
	private final Set<String> fileEndings = new HashSet<String>();

	/**
	 * Instantiates a new dav handler.
	 * 
	 * @param folderHierarchy
	 *            The folders are created in the repository.
	 * @throws HttpException
	 *             if there are problems with the connection.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public DavHandler(List<String> folderHierarchy) throws HttpException,
			IOException {
		this.folderHierarchy = new ArrayList<String>(folderHierarchy);

		Properties properties = new Properties();

		InputStream is = null;

		try {
			is = getClass().getResource("/dav.properties").openStream();
			properties.load(is);
		}

		finally {
			is.close();
		}

		String endings = properties.getProperty("fileEndingsToStore");
		String[] endingsArray = endings.split(",");
        Collections.addAll(fileEndings, endingsArray);

		this.davUrl = properties.getProperty("url");

		// remove the slash at the end if any
		if (davUrl.endsWith("/")) {
			davUrl = davUrl.substring(0, davUrl.length() - 1);
		}

		this.davUser = properties.getProperty("user");
		this.davPassword = properties.getProperty("password");

		client = new HttpClient();

		createFolders();
	}

	/**
	 * Creates folders in the repository.
	 * 
	 */
	private void createFolders() throws HttpException, IOException {
		Credentials defaultcreds = new UsernamePasswordCredentials(davUser,
				davPassword);
		client.getState().setCredentials(AuthScope.ANY, defaultcreds);
		client.getParams().setAuthenticationPreemptive(true);

		// sanity check
		checkUrl(davUrl);

		String currentBaseFolder = davUrl;
		for (String folderName : folderHierarchy) {
			createFolder(currentBaseFolder, folderName);
			currentBaseFolder = currentBaseFolder + separator + folderName;
		}
	}

	/**
	 * Creates the folder.
	 * 
	 * @param baseFolder
	 *            URL to the parent folder.
	 * @param folderName
	 *            Folder to be created.
	 * @return URL to the created folder.
	 */
	private String createFolder(String baseFolder, String folderName)
			throws HttpException, IOException {
		String folderUrl = baseFolder + separator + folderName;
		MkcolMethod meth = new MkcolMethod(folderUrl);
		int code = client.executeMethod(meth);
		meth.releaseConnection();
		if (code == 401)
			throw new HttpException(
					"Authorization for the Webdav repository failed.");
		return folderUrl;
	}

	/**
	 * Saves processing logs. Uses metadata of the tool to create a distinct
	 * folder in the repository.
	 * 
	 * @param service
	 *            The service name of the tool.
	 * @param evalId
	 *            Evaluation ID in case we evaluate several OCR tools.
	 * @param logs
	 *            the logs
	 */
	public void saveLogs(String service, String evalId, List<String> logs)
			throws HttpException, IOException {
		String baseUrl = getFolderHierarchyUrl();

		String serviceUrl = createFolder(baseUrl, service);
		if (!evalId.equals("")){
		    serviceUrl = createFolder(serviceUrl, evalId);
		}
		String logUrl = createFolder(serviceUrl, "log");

		int i = 1;
		for (String log : logs) {
			String targetFileName = format(i) + ".log";
			saveText(log, logUrl + separator + targetFileName);
			i++;
		}
	}

	/**
	 * Saves a string into a file in the repository.
	 * 
	 * @param text
	 *            Text to be stored.
	 * @param url
	 *            URL to the file that will be created.
	 */
	public void saveText(String text, String url) throws IOException {
		InputStream is = new ByteArrayInputStream(text.getBytes());
		saveStream(is, url);
	}

	/**
	 * Saves a stream into a file in the repository.
	 * 
	 * @param stream
	 *            Stream to be stored.
	 * @param url
	 *            URL to the file that will be created.
	 */
	public void saveStream(InputStream stream, String url) throws IOException {

		PutMethod putMethod = new PutMethod(url);

		putMethod.setRequestEntity(new InputStreamRequestEntity(stream));
		try {
			client.executeMethod(putMethod);
			putMethod.releaseConnection();

		} catch (IOException e) {
			throw new IOException(e);
		}
		finally { 
			stream.close();
			
		}

	}

	/**
	 * Copies one file.
	 * 
	 * @param sourceUrl
	 *            The remote URL of the file.
	 * @param targetUrl
	 *            URL in the repository.
	 */
	private void copyFile(String sourceUrl, String targetUrl)
			throws HttpException, IOException {

		if (sourceUrl != null && !sourceUrl.equals("")) {
			GetMethod getMethod = new GetMethod(sourceUrl);
			PutMethod putMethod = new PutMethod(targetUrl);

			client.executeMethod(getMethod);
			downloadedFile = getMethod.getResponseBody();
			InputStream stream = null;

			try {
				stream = new ByteArrayInputStream(downloadedFile);

				putMethod.setRequestEntity(new InputStreamRequestEntity(stream));
				client.executeMethod(putMethod);
			}
			finally {
	 			stream.close();
			}
			getMethod.releaseConnection();
			putMethod.releaseConnection();

		} else {
			throw new IOException(
					"A tool did not return a URL. The target URL would have been: "
							+ targetUrl);
		}
	}

	/**
	 * Saves files into folders which are constructed.
	 * 
	 * @param urlParts
	 *            Special information that is coded in the URL of each result
	 *            file.
	 * @param urls
	 *            Source URLs.
	 */
	public void saveFiles(UrlParts urlParts, ToolResults urls)
			throws HttpException, IOException {
		String serviceDir = urlParts.service;
		String port = urlParts.port;
		String extension = urlParts.extension;
		String evaluationId = urlParts.evalId;

		String baseUrl = getFolderHierarchyUrl();
		String serviceUrl = createFolder(baseUrl, serviceDir);
		serviceUrl = createFolder(serviceUrl, evaluationId);
		// serviceUrl = createFolder(serviceUrl, "testing123");
		String portUrl = createFolder(serviceUrl, port);

		int i = 1;
		for (String sourceUrl : urls.getFields()) {
			try {
				String targetFileName = format(i) + "." + extension;
				copyFile(sourceUrl, portUrl + separator + targetFileName);
				i++;
			} catch (SSLHandshakeException e) {
				throw new SSLHandshakeException("URL: " + sourceUrl);
			}
		}
	}

	/**
	 * Gets the folder hierarchy url.
	 * 
	 * @return the folder hierarchy url
	 */
	public String getFolderHierarchyUrl() {

		String hierarchy = davUrl;
		for (String folderName : folderHierarchy) {
			hierarchy = hierarchy + separator + folderName;
		}
		return hierarchy;
	}

	/**
	 * Formats a number into a sortable string.
	 * 
	 * @param i
	 *            The number
	 * @return String that is sortable in a file system. Used for generating
	 *         file names.
	 */
	private String format(int i) {
		String str = i + "";
		switch (str.length()) {
		case 1:
			str = "0000" + i;
			break;
		case 2:
			str = "000" + i;
			break;
		case 3:
			str = "00" + i;
			break;
		case 4:
			str = "0" + i;
		}
		return str;
	}

	/**
	 * Reads a text file from a URL.
	 * 
	 * @param url
	 *            URL to the file.
	 * @return String with the file contents.
	 */
	public String retrieveTextFile(String url) throws IOException {
		GetMethod getMethod = new GetMethod(url);
		client.executeMethod(getMethod);
		downloadedFile = getMethod.getResponseBody();
		getMethod.releaseConnection();
		return new String(downloadedFile);
	}

	/**
	 * Checks if a URL is valid and reachable.
	 * 
	 * @param url
	 *            the url
	 */
	private void checkUrl(String url) throws IOException {
		URL u = new URL(url);
		u.toString();
		GetMethod method = new GetMethod(davUrl);
		int code = client.executeMethod(method);
		method.releaseConnection();
		if (code == 404)
			throw new HttpException("URL of the repository is not reachable.");
	}

	/**
	 * Gets the file endings that will be saved into the repository using this
	 * object.
	 * 
	 * @return the file endings
	 */
	public Set<String> getFileEndings() {
		return fileEndings;
	}

}
