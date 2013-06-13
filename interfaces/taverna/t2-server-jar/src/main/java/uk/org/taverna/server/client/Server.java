/*
 * Copyright (c) 2010-2012 The University of Manchester, UK.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the names of The University of Manchester nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package uk.org.taverna.server.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.LongRange;

import uk.org.taverna.server.client.connection.Connection;
import uk.org.taverna.server.client.connection.ConnectionFactory;
import uk.org.taverna.server.client.connection.MimeType;
import uk.org.taverna.server.client.connection.UserCredentials;
import uk.org.taverna.server.client.connection.params.ConnectionParams;
import uk.org.taverna.server.client.util.URIUtils;
import uk.org.taverna.server.client.xml.ResourceLabel;
import uk.org.taverna.server.client.xml.ServerResources;
import uk.org.taverna.server.client.xml.XMLReader;
import uk.org.taverna.server.client.xml.XMLWriter;

/**
 * The Server class represents a connection to a Taverna Server instance
 * somewhere on the Internet. Only one instance of this class is created for
 * each Taverna Server instance.
 * 
 * To make a connection to a server call {@link Server#connect(URI)} with it
 * full URL as the parameter. If there already exists a Server instance that is
 * connected to this Taverna Server then it will be returned, otherwise a new
 * Server instance is created and returned.
 * 
 * @author Robert Haines
 */
public final class Server {

	/*
	 * Where to find the REST endpoint in relation to the base URI of a Taverna
	 * Server.
	 * 
	 * Add a slash to the end of this address to work around this bug:
	 * http://dev.mygrid.org.uk/issues/browse/TAVSERV-113
	 */
	private final static String REST_ENDPOINT = "rest/";

	private final Connection connection;

	private final URI uri;
	private final Map<String, Map<String, Run>> runs;

	private final XMLReader reader;
	private ServerResources resources;

	/**
	 * 
	 * @param uri
	 * @param params
	 */
	public Server(URI uri, ConnectionParams params) {
		// strip out username and password if present in server URI
		this.uri = URIUtils.stripUserInfo(uri);

		connection = ConnectionFactory.getConnection(this.uri, params);

		reader = new XMLReader(connection);
		resources = null;

		// initialise run list
		runs = new HashMap<String, Map<String, Run>>();
	}

	/**
	 * 
	 * @param uri
	 */
	public Server(URI uri) {
		this(uri, null);
	}

	/**
	 * Get the version of the remote Taverna Server instance.
	 * 
	 * @return the Taverna Server version.
	 */
	public String getVersion() {
		return getServerResources().getVersion();
	}

	/**
	 * Get the three components (major.minor.patch) of the remote Taverna Server
	 * instance.
	 * 
	 * @return an int array containing the three version components.
	 */
	public int[] getVersionComponents() {
		return getServerResources().getVersionComponents();
	}

	/**
	 * Get the Run instance hosted by this Server by its id.
	 * 
	 * @param id
	 *            The id of the Run instance to get.
	 * @return the Run instance.
	 */
	public Run getRun(String id, UserCredentials credentials) {
		return getRunsFromServer(credentials).get(id);
	}

	/**
	 * Get all the Run instances hosted on this server.
	 * 
	 * @return all the Run instances hosted on this server.
	 */
	public Collection<Run> getRuns(UserCredentials credentials) {
		return getRunsFromServer(credentials).values();
	}

	/**
	 * Delete all runs on this server instance. Only the runs owned by the
	 * provided credentials will be deleted.
	 * 
	 * @param credentials
	 *            The credentials to authorize the deletion.
	 */
	public void deleteAllRuns(UserCredentials credentials) {
		// Calling getRuns() updates the user's run cache...
		for (Run run : getRuns(credentials)) {
			run.delete();
		}

		// ... so we can clear it here now we've deleted them all.
		runs.remove(credentials.getUsername());
	}

	private Map<String, Run> getRunsFromServer(UserCredentials credentials) {
		// Get this user's run list.
		URI uri = getLink(ResourceLabel.RUNS);
		Map<String, URI> runList = reader.readRunList(uri, credentials);

		// Get this user's run cache.
		Map<String, Run> userRuns = getUserRunCache(credentials.getUsername());

		// Add new runs to the user's run cache.
		for (String id : runList.keySet()) {
			if (!userRuns.containsKey(id)) {
				userRuns.put(id, new Run(runList.get(id), this, credentials));
			}
		}

		// Any ids in the runs list that aren't in the map we've just got from
		// the server are dead and can be removed. Note that we have to use an
		// iterator here because we may be removing things as we go.
		if (userRuns.size() > runList.size()) {
			Iterator<String> iterator = userRuns.keySet().iterator();
			while (iterator.hasNext()) {
				String i = iterator.next();
				if (!runList.containsKey(i)) {
					iterator.remove();
				}
			}
		}

		assert (userRuns.size() == runList.size());

		return userRuns;
	}

	private Map<String, Run> getUserRunCache(String user) {
		Map<String, Run> userRuns = runs.get(user);
		if (userRuns == null) {
			userRuns = new HashMap<String, Run>();
			runs.put(user, userRuns);
		}

		return userRuns;
	}

	private ServerResources getServerResources() {
		if (resources == null) {
			URI restURI = URIUtils.appendToPath(uri, REST_ENDPOINT);
			resources = reader.readServerResources(restURI);
		}

		return resources;
	}

	/**
	 * Connect to a Taverna Server.
	 * 
	 * @param uri
	 *            The address of the server to connect to in the form
	 *            http://server:port/location
	 * @return a Server instance representing the connection to the specified
	 *         Taverna Server.
	 * @throws URISyntaxException
	 *             if the provided URI is badly formed.
	 */
	@Deprecated
	public static Server connect(String uri) throws URISyntaxException {
		return new Server(new URI(uri));
	}

	/**
	 * Connect to a Taverna Server.
	 * 
	 * @param uri
	 *            The URI of the server to connect to.
	 * @return a Server instance representing the connection to the specified
	 *         Taverna Server.
	 */
	@Deprecated
	public static Server connect(URI uri) {
		return new Server(uri);
	}

	/**
	 * Get the URI of this server instance.
	 * 
	 * @return the URI of this server instance.
	 */
	public URI getURI() {
		return uri;
	}

	/**
	 * Get the maximum number of run that this server can host concurrently.
	 * 
	 * @return the maximum number of run that this server can host concurrently.
	 */
	public int getRunLimit(UserCredentials credentials) {
		byte[] limit = connection.read(getLink(ResourceLabel.RUNLIMIT),
				MimeType.TEXT, credentials);

		return Integer.parseInt(new String(limit).trim());
	}

	/**
	 * Initialize a Run on this server instance.
	 * 
	 * @param workflow
	 *            the workflow to be run.
	 * @return the id of the new run as returned by the server.
	 */
	URI initializeRun(byte[] workflow, UserCredentials credentials) {
		URI location = connection.create(getLink(ResourceLabel.RUNS), workflow,
				MimeType.T2FLOW, credentials);

		return location;
	}

	/**
	 * Create a new Run on this server with the supplied workflow.
	 * 
	 * @param workflow
	 *            the workflow to be run.
	 * @return a new Run instance.
	 */
	public Run createRun(byte[] workflow, UserCredentials credentials) {
		Run run = Run.create(this, workflow, credentials);

		getUserRunCache(credentials.getUsername())
		.put(run.getIdentifier(), run);

		return run;
	}

	/**
	 * Create a new Run on this server with the supplied workflow file.
	 * 
	 * @param workflow
	 *            the workflow file to be run.
	 * @return a new Run instance.
	 * @throws IOException
	 *             on IO errors.
	 */
	public Run createRun(File workflow, UserCredentials credentials)
			throws IOException {

		return createRun(FileUtils.readFileToByteArray(workflow), credentials);
	}

	URI createResource(URI uri, byte[] content, UserCredentials credentials) {
		return connection.create(uri, content, MimeType.XML, credentials);
	}

	URI updateResource(URI uri, byte[] content, UserCredentials credentials) {
		return connection.update(uri, content, MimeType.XML, credentials);
	}

	URI updateResource(URI uri, String content, UserCredentials credentials) {
		return connection.update(uri, content.getBytes(), MimeType.TEXT,
				credentials);
	}

	boolean deleteResource(URI uri, UserCredentials credentials) {
		return connection.delete(uri, credentials);
	}

	byte[] readResourceAsBytes(URI uri, MimeType type, LongRange range,
			UserCredentials credentials) {
		return connection.read(uri, type, range, credentials);
	}

	byte[] readResourceAsBytes(URI uri, MimeType type,
			UserCredentials credentials) {
		return connection.read(uri, type, credentials);
	}

	String readResourceAsString(URI uri, UserCredentials credentials) {
		return new String(connection.read(uri, MimeType.TEXT, credentials));
	}

	InputStream readResourceAsStream(URI uri, MimeType type, LongRange range,
			UserCredentials credentials) {
		return connection.readStream(uri, type, range, credentials);
	}

	URI uploadData(URI uri, InputStream stream, String remoteName,
			UserCredentials credentials) {
		uri = URIUtils.appendToPath(uri, remoteName);

		return connection.update(uri, stream, MimeType.BYTES, credentials);
	}

	String uploadFile(URI uri, File file, String rename,
			UserCredentials credentials) throws FileNotFoundException {
		if (file.isDirectory()) {
			throw new FileNotFoundException("File passed in is a directory.");
		}

		if (rename == null || rename.equals("")) {
			rename = file.getName();
		}

		uri = URIUtils.appendToPath(uri, rename);

		InputStream is = new FileInputStream(file);
		try {
			connection.update(uri, is, file.length(), MimeType.BYTES,
					credentials);
		} finally {
			IOUtils.closeQuietly(is);
		}

		return rename;
	}

	URI mkdir(URI root, String name, UserCredentials credentials) {
		if (name.contains("/")) {
			throw new IllegalArgumentException(
					"creation of subdirectories directly (" + name + ")");
		}

		return connection.create(root, XMLWriter.mkdir(name), MimeType.XML,
				credentials);
	}

	XMLReader getXMLReader() {
		return reader;
	}

	private URI getLink(ResourceLabel key) {
		return getServerResources().get(key);
	}
}
