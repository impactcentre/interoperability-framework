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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.LongRange;

import uk.org.taverna.server.client.connection.AttributeNotFoundException;
import uk.org.taverna.server.client.connection.MimeType;
import uk.org.taverna.server.client.connection.UserCredentials;
import uk.org.taverna.server.client.util.IOUtils;
import uk.org.taverna.server.client.util.URIUtils;
import uk.org.taverna.server.client.xml.ResourceLabel;
import uk.org.taverna.server.client.xml.RunResources;
import uk.org.taverna.server.client.xml.XMLReader;
import uk.org.taverna.server.client.xml.XMLWriter;

/**
 * The Run class represents a workflow run on a Taverna Server instance. It is
 * created by supplying a Server instance on which to create it and a workflow
 * to be run.
 * 
 * @author Robert Haines
 */
public final class Run {

	/*
	 * Internal names to use for storing various input and output in files on
	 * the server.
	 */
	private static final String BACLAVA_IN_FILE = "in.baclava";
	private static final String BACLAVA_OUT_FILE = "out.baclava";
	private static final String KEYPAIR_PREFIX = "keypair-";
	private static final String TRUST_PREFIX = "trust-";

	private static final String DEFAULT_CERTIFICATE_ALIAS = "Imported Certificate";
	private static final String DEFAULT_KEYPAIR_TYPE = "pkcs12";
	private static final String DEFAULT_CERTIFICATE_TYPE = "x509";

	private final URI uri;
	private final Server server;
	private final String id;
	private byte[] workflow;
	private boolean baclavaIn;
	private boolean baclavaOut;

	private boolean deleted;

	private RunResources resources;

	private final UserCredentials credentials;

	// Ports
	private Map<String, InputPort> inputPorts = null;
	private Map<String, OutputPort> outputPorts = null;

	/*
	 * Create a Run instance. This will already have been created on the remote
	 * server.
	 */
	private Run(URI uri, Server server, byte[] workflow,
			UserCredentials credentials) {
		this.uri = uri;
		this.server = server;
		this.id = URIUtils.extractFinalPathComponent(uri);
		this.workflow = workflow;
		this.baclavaIn = false;
		this.baclavaOut = false;

		this.credentials = credentials;
		resources = null;

		this.deleted = false;
	}

	/*
	 * Internal constructor for other classes within the package to create
	 * "lightweight" runs. Used when listing runs, etc.
	 */
	Run(URI uri, Server server, UserCredentials credentials) {
		this(uri, server, null, credentials);
	}

	/**
	 * 
	 * @param server
	 * @param workflow
	 * @param credentials
	 * @return
	 */
	public static Run create(Server server, byte[] workflow,
			UserCredentials credentials) {
		URI uri = server.initializeRun(workflow, credentials);

		return new Run(uri, server, workflow, credentials);
	}

	/**
	 * 
	 * @param server
	 * @param workflow
	 * @param credentials
	 * @return
	 * @throws IOException
	 */
	public static Run create(Server server, File workflow,
			UserCredentials credentials) throws IOException {
		return create(server, FileUtils.readFileToByteArray(workflow),
				credentials);
	}

	/**
	 * 
	 * @return
	 */
	public URI getURI() {
		return uri;
	}

	/**
	 * 
	 * @return
	 */
	public Server getServer() {
		return server;
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, InputPort> getInputPorts() {
		if (inputPorts == null) {
			inputPorts = getInputPortInfo();
		}

		return inputPorts;
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public InputPort getInputPort(String name) {
		return getInputPorts().get(name);
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, OutputPort> getOutputPorts() {
		if (outputPorts == null) {
			outputPorts = getOutputPortInfo();
		}

		return outputPorts;
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public OutputPort getOutputPort(String name) {
		return getOutputPorts().get(name);
	}

	/**
	 * Upload data to a file in this Run instance's workspace on the server.
	 * 
	 * @param data
	 *            The data to upload.
	 * @param remoteName
	 *            The name of the file to save the data in on the server.
	 * @param remoteDirectory
	 *            The directory within the workspace in which to save the data.
	 *            This directory must already exist.
	 * @return the URI of the uploaded data on the remote server.
	 */
	public URI uploadData(byte[] data, String remoteName,
			String remoteDirectory) {

		return uploadData(new ByteArrayInputStream(data), remoteName,
				remoteDirectory);
	}

	/**
	 * Upload data to a file in this Run instance's workspace on the server.
	 * 
	 * @param data
	 *            The data to upload.
	 * @param remoteName
	 *            The name of the file to save the data in on the server.
	 * @return the URI of the uploaded data on the remote server.
	 */
	public URI uploadData(byte[] data, String remoteName) {

		return uploadData(data, remoteName, null);
	}

	/**
	 * Upload data to a file in this Run instance's workspace on the server.
	 * 
	 * @param stream
	 *            The stream with the data to be uploaded.
	 * @param remoteName
	 *            The name of the file to save the data in on the server.
	 * @param remoteDirectory
	 *            The directory within the workspace in which to save the data.
	 *            This directory must already exist.
	 * @return the URI of the uploaded data on the remote server.
	 */
	public URI uploadData(InputStream stream, String remoteName,
			String remoteDirectory) {
		URI uploadLocation = getLink(ResourceLabel.WDIR);
		if (remoteDirectory != null) {
			uploadLocation = URIUtils.appendToPath(uploadLocation,
					remoteDirectory);
		}

		return server.uploadData(uploadLocation, stream, remoteName,
				credentials);
	}

	/**
	 * Upload data to a file in this Run instance's workspace on the server.
	 * 
	 * @param stream
	 *            The stream with the data to be uploaded.
	 * @param remoteName
	 *            The name of the file to save the data in on the server.
	 * @return the URI of the uploaded data on the remote server.
	 */
	public URI uploadData(InputStream stream, String remoteName) {

		return uploadData(stream, remoteName, null);
	}

	/**
	 * Upload a file to this Run instance's workspace on the server.
	 * 
	 * @param file
	 *            The file to upload.
	 * @param remoteDirectory
	 *            The directory within the workspace to upload the file to.
	 * @param rename
	 *            The name to use for the file when saving it in the workspace.
	 * @return the name of the file as used on the server.
	 * @throws FileNotFoundException
	 *             if the file does not exist or cannot be read.
	 */
	public String uploadFile(File file, String remoteDirectory, String rename)
			throws FileNotFoundException {

		URI uploadLocation = getLink(ResourceLabel.WDIR);
		if (remoteDirectory != null) {
			uploadLocation = URIUtils.appendToPath(uploadLocation,
					remoteDirectory);
		}

		return server.uploadFile(uploadLocation, file, rename, credentials);
	}

	/**
	 * Upload a file to this Run instance's workspace on the server.
	 * 
	 * @param file
	 *            The file to upload.
	 * @param remoteDirectory
	 *            The directory within the workspace to upload the file to.
	 * @return the name of the file as used on the server.
	 * @throws IOException
	 */
	public String uploadFile(File file, String remoteDirectory)
			throws IOException {
		return uploadFile(file, remoteDirectory, null);
	}

	/**
	 * Upload a file to this Run instance's workspace on the server.
	 * 
	 * @param file
	 *            The file to upload.
	 * @return the name of the file as used on the server.
	 * @throws IOException
	 */
	public String uploadFile(File file) throws IOException {
		return uploadFile(file, null, null);
	}

	/**
	 * Upload baclava data to specify all input port value.
	 * 
	 * @param data
	 *            The data to upload.
	 */
	public void setBaclavaInput(byte[] data) {
		RunStatus rs = getStatus();
		if (rs == RunStatus.INITIALIZED) {
			uploadData(data, BACLAVA_IN_FILE);
			server.updateResource(getLink(ResourceLabel.BACLAVA),
					BACLAVA_IN_FILE, credentials);

			baclavaIn = true;
		} else {
			throw new RunStateException(rs, RunStatus.INITIALIZED);
		}
	}

	/**
	 * Upload a baclava file to specify all input port values.
	 * 
	 * @param file
	 *            The file to upload.
	 * @throws IOException
	 */
	public void setBaclavaInput(File file) throws IOException {
		RunStatus rs = getStatus();
		if (rs == RunStatus.INITIALIZED) {
			uploadFile(file, null, BACLAVA_IN_FILE);
			server.updateResource(getLink(ResourceLabel.BACLAVA),
					BACLAVA_IN_FILE, credentials);

			baclavaIn = true;
		} else {
			throw new RunStateException(rs, RunStatus.INITIALIZED);
		}
	}

	/**
	 * Is this run using baclava to set all its input ports?
	 * 
	 * @return true if yes, false if not.
	 */
	public boolean isBaclavaInput() {
		// if baclavaIn is true then we know that is correct, else we check.
		if (baclavaIn) {
			return true;
		} else {
			String test = server.readResourceAsString(
					getLink(ResourceLabel.BACLAVA), credentials);

			// if we get back the baclava input file name we are using it.
			if (test.equals(BACLAVA_IN_FILE)) {
				baclavaIn = true;
			}

			return baclavaIn;
		}
	}

	/**
	 * Is this run using baclava to return all its output port data?
	 * 
	 * @return true if yes, false if not.
	 */
	public boolean isBaclavaOutput() {
		// if baclavaOut is true then we know that is correct, else we check.
		if (baclavaOut) {
			return true;
		} else {
			String test = server.readResourceAsString(
					getLink(ResourceLabel.OUTPUT), credentials);

			// if we get back the baclava output file name we are using it.
			if (test.equals(BACLAVA_OUT_FILE)) {
				baclavaOut = true;
			}

			return baclavaOut;
		}
	}

	/**
	 * Set the server to return outputs for this Run in baclava format. This
	 * must be set before the Run is started.
	 */
	public void requestBaclavaOutput() {
		// don't try and request it again!
		if (baclavaOut) {
			return;
		}

		RunStatus rs = getStatus();
		if (rs == RunStatus.INITIALIZED) {
			server.updateResource(getLink(ResourceLabel.OUTPUT),
					BACLAVA_OUT_FILE, credentials);

			baclavaOut = true;
		} else {
			throw new RunStateException(rs, RunStatus.INITIALIZED);
		}
	}

	/**
	 * Get the outputs of this Run as a baclava formatted document. The Run must
	 * have been set to output in baclava format before it is started.
	 * 
	 * @return The baclava formatted document contents as a byte array.
	 * @see #requestBaclavaOutput()
	 * @see #getBaclavaOutputStream()
	 * @see #writeBaclavaOutputToFile(File)
	 */
	public byte[] getBaclavaOutput() {
		RunStatus rs = getStatus();
		if (rs == RunStatus.FINISHED) {
			URI baclavaLink = URIUtils.appendToPath(
					getLink(ResourceLabel.WDIR), BACLAVA_OUT_FILE);
			if (!baclavaOut) {
				throw new AttributeNotFoundException(baclavaLink);
			}

			return server.readResourceAsBytes(baclavaLink, MimeType.BYTES,
					credentials);
		} else {
			throw new RunStateException(rs, RunStatus.FINISHED);
		}
	}

	/**
	 * Get an input stream that can be used to stream the baclava output data of
	 * this run. The Run must have been set to output in baclava format before
	 * it is started.
	 * 
	 * <b>Note:</b> You are responsible for closing the stream once you have
	 * finished with it. Not doing so may prevent further use of the underlying
	 * network connection.
	 * 
	 * @return The stream to read the baclava data from.
	 * @see #getBaclavaOutput()
	 * @see #writeBaclavaOutputToFile(File)
	 * @see #requestBaclavaOutput()
	 */
	public InputStream getBaclavaOutputStream() {
		RunStatus rs = getStatus();
		if (rs == RunStatus.FINISHED) {
			URI baclavaLink = URIUtils.appendToPath(
					getLink(ResourceLabel.WDIR), BACLAVA_OUT_FILE);
			if (!baclavaOut) {
				throw new AttributeNotFoundException(baclavaLink);
			}

			return server.readResourceAsStream(baclavaLink, MimeType.BYTES,
					null, credentials);
		} else {
			throw new RunStateException(rs, RunStatus.FINISHED);
		}
	}

	/**
	 * Writes the baclava output data of this run directly to a file. The data
	 * is not loaded into memory, it is streamed directly to the file. The file
	 * is created if it does not already exist and will overwrite existing data
	 * if it does.
	 * 
	 * The Run must have been set to output in baclava format before it is
	 * started.
	 * 
	 * @param file
	 *            the file to write to.
	 * @throws FileNotFoundException
	 *             if the file exists but is a directory rather than a regular
	 *             file, does not exist but cannot be created, or cannot be
	 *             opened for any other reason.
	 * @throws IOException
	 *             if there is any I/O error.
	 * @see #getBaclavaOutput()
	 * @see #getBaclavaOutputStream()
	 * @see #requestBaclavaOutput()
	 */
	public void writeBaclavaOutputToFile(File file) throws IOException {
		InputStream is = getBaclavaOutputStream();
		try {
			IOUtils.writeStreamToFile(is, file);
		} finally {
			org.apache.commons.io.IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Get the id of this run.
	 * 
	 * @return the id of this run.
	 */
	public String getIdentifier() {
		return id;
	}

	/**
	 * Get the owner of this run.
	 * 
	 * @return the username of the owner of this run.
	 */
	public String getOwner() {
		return getRunResources().getOwner();
	}

	/**
	 * Are the credentials being used to access this run those of the owner? The
	 * owner of the run can give other users certain access rights to their runs
	 * but only the owner can change these rights - or even see what they are.
	 * Sometimes it is useful to know if the user accessing the run is actually
	 * the owner of it or not.
	 * 
	 * @return whether the user credentials of this Run instance are those of
	 *         the owner of the run as the remote server understands it.
	 */
	public boolean isOwner() {
		return credentials.getUsername().equals(getOwner());
	}

	/**
	 * Get the status of this Run.
	 * 
	 * @return the status of this Run.
	 */
	public RunStatus getStatus() {
		if (deleted) {
			return RunStatus.DELETED;
		} else {
			return RunStatus.fromString(server.readResourceAsString(
					getLink(ResourceLabel.STATUS), credentials));
		}
	}

	/**
	 * Is this Run initialized?
	 * 
	 * @return true if the Run is initialized, false otherwise.
	 */
	public boolean isInitialized() {
		return getStatus() == RunStatus.INITIALIZED;
	}

	/**
	 * Is this Run running?
	 * 
	 * @return true if the Run is running, false otherwise.
	 */
	public boolean isRunning() {
		return getStatus() == RunStatus.RUNNING;
	}

	/**
	 * Is this Run finished?
	 * 
	 * @return true if the Run is finished, false otherwise.
	 */
	public boolean isFinished() {
		return getStatus() == RunStatus.FINISHED;
	}

	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * Start this Run running on the server. The Run must not be already
	 * running, or finished.
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException {
		RunStatus rs = getStatus();
		if (rs != RunStatus.INITIALIZED) {
			throw new RunStateException(rs, RunStatus.INITIALIZED);
		}

		// set all the inputs
		if (!isBaclavaInput()) {
			setAllInputs();
		}

		server.updateResource(getLink(ResourceLabel.STATUS),
				RunStatus.RUNNING.status, credentials);
	}

	/**
	 * Get the workflow of this Run as a String.
	 * 
	 * @return the workflow of this Run as a String.
	 */
	public byte[] getWorkflow() {
		if (workflow == null) {
			workflow = server.readResourceAsBytes(
					getLink(ResourceLabel.WORKFLOW), MimeType.XML, credentials);
		}

		return workflow;
	}

	/**
	 * Get the expiry time of this Run as a Date object.
	 * 
	 * @return the expiry time of this Run as a Date object.
	 */
	public Date getExpiry() {
		return getTime(ResourceLabel.EXPIRY);
	}

	/**
	 * Set the expiry time of this Run.
	 * 
	 * @param time
	 *            the new expiry time of this Run.
	 */
	public void setExpiry(Date time) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		String expiry = DatatypeConverter.printDateTime(cal);
		server.updateResource(getLink(ResourceLabel.EXPIRY), expiry,
				credentials);
	}

	/**
	 * Delete this Run.
	 */
	public boolean delete() {
		try {
			server.deleteResource(uri, credentials);
		} catch (AttributeNotFoundException e) {
			// Ignore this. Delete is idempotent so deleting a run that has
			// already been deleted or is for some other reason not there should
			// happen silently.
		} finally {
			deleted = true;
		}

		return true;
	}

	/**
	 * Get the return code of the underlying Taverna Server process. A zero
	 * value indicates success.
	 * 
	 * @return the return code of the underlying Taverna Server process.
	 */
	public int getExitCode() {
		return new Integer(server.readResourceAsString(
				getLink(ResourceLabel.EXITCODE), credentials));
	}

	/**
	 * Get the console output of the underlying Taverna Server process.
	 * 
	 * @return the console output of the underlying Taverna Server process.
	 */
	public String getConsoleOutput() {
		return server.readResourceAsString(getLink(ResourceLabel.STDOUT),
				credentials);
	}

	/**
	 * Get the console errors of the underlying Taverna Server process.
	 * 
	 * @return the console errors of the underlying Taverna Server process.
	 */
	public String getConsoleError() {
		return server.readResourceAsString(getLink(ResourceLabel.STDERR),
				credentials);
	}

	/**
	 * Get the time that this Run was created as a Date object.
	 * 
	 * @return the time that this Run was created.
	 */
	public Date getCreateTime() {
		return getTime(ResourceLabel.CREATE_TIME);
	}

	/**
	 * Get the time that this Run was started as a Date object.
	 * 
	 * @return the time that this Run was started.
	 */
	public Date getStartTime() {
		return getTime(ResourceLabel.START_TIME);
	}

	/**
	 * Get the time that this Run finished as a Date object.
	 * 
	 * @return the time that this Run finished.
	 */
	public Date getFinishTime() {
		return getTime(ResourceLabel.FINISH_TIME);
	}

	private Date getTime(ResourceLabel time) {
		String dateTime = server.readResourceAsString(getLink(time),
				credentials);
		Calendar cal = DatatypeConverter.parseDateTime(dateTime);

		return cal.getTime();
	}

	/**
	 * Get all the permissions set for this run. Only the owner of a run may
	 * query its permissions.
	 * 
	 * @return a map of username to permission for each user.
	 * @throws IllegalUserAccessException
	 *             if the credentials in this Run are not the owner's.
	 * @see {@link RunPermission}
	 */
	public Map<String, RunPermission> getPermissions() {
		if (!isOwner()) {
			throw new IllegalUserAccessException(credentials.getUsername());
		}

		XMLReader reader = server.getXMLReader();

		Map<String, RunPermission> perms = reader.readRunPermissions(
				getLink(ResourceLabel.PERMISSIONS), credentials);

		return perms;
	}

	/**
	 * Return the permission granted to user for this run, if any. Only the
	 * owner of a run may query its permissions.
	 * 
	 * @param username
	 *            The username of the user to query.
	 * @return the permission the user has been granted, if any.
	 * @throws IllegalUserAccessException
	 *             if the credentials in this Run are not the owner's.
	 * @see {@link RunPermission}
	 */
	public RunPermission getPermission(String username) {
		if (!isOwner()) {
			throw new IllegalUserAccessException(credentials.getUsername());
		}

		return getPermissions().get(username);
	}

	/**
	 * Grant the specified user the stated permission.
	 * 
	 * To revoke a permission for a user, simply set the permission for that
	 * user to <code>RunPermission.NONE</code> or <code>null</code>.
	 * 
	 * Only the owner of a run may grant or revoke permissions on it.
	 * 
	 * @param username
	 *            The user to grant permissions to.
	 * @param permission
	 *            The permission to grant.
	 * @return the URI of the created permission resource on the server.
	 * @throws IllegalUserAccessException
	 *             if the credentials in this Run are not the owner's.
	 * @see {@link RunPermission}
	 */
	public URI setPermission(String username, RunPermission permission) {
		if (!isOwner()) {
			throw new IllegalUserAccessException(credentials.getUsername());
		}

		if (permission == null) {
			permission = RunPermission.NONE;
		}

		byte[] content = XMLWriter.runPermission(username, permission);

		return server.createResource(getLink(ResourceLabel.PERMISSIONS),
				content, credentials);
	}

	/**
	 * Get all the credentials that have been provided for this run.
	 * 
	 * Only the owner of a run may query credentials that have been provided for
	 * it.
	 * 
	 * @return A {@link Map} of service URI to credential resource URI.
	 * @throws IllegalUserAccessException
	 *             if the credentials in this Run are not the owner's.
	 */
	public Map<URI, URI> getServiceCredentials() {
		if (!isOwner()) {
			throw new IllegalUserAccessException(credentials.getUsername());
		}

		XMLReader reader = server.getXMLReader();

		Map<URI, URI> creds = reader.readRunServiceCredentials(
				getLink(ResourceLabel.CREDENTIALS), credentials);

		return creds;
	}

	/**
	 * Get the credential resource URI that Taverna Server is using to provide
	 * credentials for the supplied service URI.
	 * 
	 * @param serviceURI
	 *            The service URI to query.
	 * @return The credential resource that is being used for the specified
	 *         service URI.
	 * @throws IllegalUserAccessException
	 *             if the credentials in this Run are not the owner's.
	 */
	public URI getServiceCredential(URI serviceURI) {
		return getServiceCredentials().get(serviceURI);
	}

	/**
	 * Provide a username and password credential for the secure service at the
	 * specified URI.
	 * 
	 * Only the owner of a run may supply credentials for it.
	 * 
	 * @param serviceURI
	 *            The URI of the service that this credential is for.
	 * @param username
	 *            The username of this credential.
	 * @param password
	 *            The password of this credential.
	 * @return The URI to the credential resource on the remote Taverna Server.
	 * @throws IllegalUserAccessException
	 *             if the credentials in this Run are not the owner's.
	 */
	public URI setServiceCredential(URI serviceURI, String username,
			String password) {
		if (!isOwner()) {
			throw new IllegalUserAccessException(credentials.getUsername());
		}

		// Is this a new credential or an update to an existing one?
		URI credURI = getServiceCredential(serviceURI);

		byte[] content = XMLWriter.runServiceUserPassCredential(serviceURI,
				username, password);

		if (credURI == null) {
			return server.createResource(getLink(ResourceLabel.CREDENTIALS),
					content, credentials);
		} else {
			return server.updateResource(credURI, content, credentials);
		}
	}

	/**
	 * Provide a client certificate credential for the secure service at the
	 * specified URI. You will need to provide the password to unlock the
	 * private key.
	 * 
	 * Only the owner of a run may supply credentials for it.
	 * 
	 * @param serviceURI
	 *            The URI of the service that this credential is for.
	 * @param keypair
	 *            The file with the credential in it.
	 * @param password
	 *            The password to unlock the credential.
	 * @return The URI to the credential resource on the remote Taverna Server.
	 * @throws IOException
	 *             if there was a problem reading the credential file.
	 * @throws IllegalUserAccessException
	 *             if the credentials in this Run are not the owner's.
	 */
	public URI setServiceCredential(URI serviceURI, File keypair,
			String password) throws IOException {
		if (!isOwner()) {
			throw new IllegalUserAccessException(credentials.getUsername());
		}

		String remoteName = uploadFile(keypair);

		return setServiceKeyPairCredential(serviceURI, remoteName, password,
				DEFAULT_CERTIFICATE_ALIAS);
	}

	/**
	 * Provide a client certificate credential for the secure service at the
	 * specified URI. You will need to provide the password to unlock the
	 * private key.
	 * 
	 * @param serviceURI
	 *            The URI of the service that this credential is for.
	 * @param keypair
	 *            The stream with the credential in it.
	 * @param password
	 *            The password to unlock the credential.
	 * @return The URI to the credential resource on the remote Taverna Server.
	 * @throws IllegalUserAccessException
	 *             if the credentials in this Run are not the owner's.
	 */
	public URI setServiceCredential(URI serviceURI, InputStream keypair,
			String password) {
		if (!isOwner()) {
			throw new IllegalUserAccessException(credentials.getUsername());
		}

		String remoteFile = KEYPAIR_PREFIX + UUID.randomUUID();

		uploadData(keypair, remoteFile);

		return setServiceKeyPairCredential(serviceURI, remoteFile, password,
				DEFAULT_CERTIFICATE_ALIAS);

	}

	private URI setServiceKeyPairCredential(URI serviceURI, String remoteFile,
			String password, String alias) {
		// Is this a new credential or an update to an existing one?
		URI credURI = getServiceCredential(serviceURI);

		byte[] content = XMLWriter.runServiceKeyPairCredential(serviceURI,
				remoteFile, DEFAULT_KEYPAIR_TYPE, alias, password);

		if (credURI == null) {
			return server.createResource(getLink(ResourceLabel.CREDENTIALS),
					content, credentials);
		} else {
			return server.updateResource(credURI, content, credentials);
		}
	}

	/**
	 * Delete the credential that has been provided for the specified service.
	 * 
	 * Only the owner of a run may delete its credentials.
	 * 
	 * @param serviceURI
	 *            The service for which to delete the credential.
	 * @return true if the operation succeeded, false otherwise.
	 * @throws IllegalUserAccessException
	 *             if the credentials in this Run are not the owner's.
	 */
	public boolean deleteServiceCredential(URI serviceURI) {
		if (!isOwner()) {
			throw new IllegalUserAccessException(credentials.getUsername());
		}

		return server.deleteResource(getServiceCredential(serviceURI),
				credentials);
	}

	/**
	 * Delete all credentials associated with this workflow run.
	 * 
	 * Only the owner of a run may delete its credentials.
	 * 
	 * @return true if the operation succeeded, false otherwise.
	 * @throws IllegalUserAccessException
	 *             if the credentials in this Run are not the owner's.
	 */
	public boolean deleteAllServiceCredentials() {
		if (!isOwner()) {
			throw new IllegalUserAccessException(credentials.getUsername());
		}

		return server.deleteResource(getLink(ResourceLabel.CREDENTIALS),
				credentials);
	}

	/**
	 * Return a list of all the URIs of trusted identities (for example, a
	 * server public key for peer verification) that have been registered for
	 * this run. At present there is no way to differentiate between trusted
	 * identities without noting the URI returned when originally uploaded.
	 * 
	 * Only the owner of a run may query its trusted identities.
	 * 
	 * @return the list of trusted identities registered for this run.
	 * @throws IllegalUserAccessException
	 *             if the credentials in this Run are not the owner's.
	 */
	public List<URI> getTrustedIdentities() {
		if (!isOwner()) {
			throw new IllegalUserAccessException(credentials.getUsername());
		}

		XMLReader reader = server.getXMLReader();

		List<URI> trusts = reader.readRunTrustedIdentities(
				getLink(ResourceLabel.TRUSTS), credentials);

		return trusts;
	}

	/**
	 * Add a trusted identity (server public key) to verify peers when using
	 * https connections to Web Services. The URI of the trusted identity on the
	 * server is returned.
	 * 
	 * Only the owner of a run may add a trusted identity.
	 * 
	 * @param certificate
	 *            The public key file to use as a trusted identity.
	 * @return The URI of the uploaded trusted identity resource.
	 * @throws IOException
	 *             if the specified file cannot be opened for any reason.
	 * @throws IllegalUserAccessException
	 *             if the credentials in this Run are not the owner's.
	 */
	public URI setTrustedIdentity(File certificate) throws IOException {
		if (!isOwner()) {
			throw new IllegalUserAccessException(credentials.getUsername());
		}

		String remoteFile = uploadFile(certificate);

		return setTrustedIdentity(remoteFile);
	}

	/**
	 * Add a trusted identity (server public key) to verify peers when using
	 * https connections to Web Services. The URI of the trusted identity on the
	 * server is returned.
	 * 
	 * Only the owner of a run may add a trusted identity.
	 * 
	 * @param certificate
	 *            The public key to use as a trusted identity.
	 * @return The URI of the uploaded trusted identity resource.
	 * @throws IllegalUserAccessException
	 *             if the credentials in this Run are not the owner's.
	 */
	public URI setTrustedIdentity(InputStream certificate) {
		if (!isOwner()) {
			throw new IllegalUserAccessException(credentials.getUsername());
		}

		String remoteFile = TRUST_PREFIX + UUID.randomUUID();

		uploadData(certificate, remoteFile);

		return setTrustedIdentity(remoteFile);
	}

	private URI setTrustedIdentity(String remoteFile) {
		byte[] content = XMLWriter.runTrustedIdentity(remoteFile,
				DEFAULT_CERTIFICATE_TYPE);

		return server.createResource(getLink(ResourceLabel.TRUSTS), content,
				credentials);
	}

	/**
	 * Delete the trusted identity resource at the specified URI from the remote
	 * server.
	 * 
	 * Only the owner of a run may delete a trusted identity.
	 * 
	 * @param uri
	 *            The URI of the trusted identity to delete.
	 * @return true on success, false otherwise.
	 * @throws IllegalUserAccessException
	 *             if the credentials in this Run are not the owner's.
	 */
	public boolean deleteTrustedIdentity(URI uri) {
		if (!isOwner()) {
			throw new IllegalUserAccessException(credentials.getUsername());
		}

		return server.deleteResource(uri, credentials);
	}

	/**
	 * Delete all of the trusted identities that have been registered for use by
	 * this run.
	 * 
	 * Only the owner of a run may delete trusted identities.
	 * 
	 * @return true on success, false otherwise.
	 * @throws IllegalUserAccessException
	 *             if the credentials in this Run are not the owner's.
	 */
	public boolean deleteAllTrustedIdentities() {
		if (!isOwner()) {
			throw new IllegalUserAccessException(credentials.getUsername());
		}

		return server
				.deleteResource(getLink(ResourceLabel.TRUSTS), credentials);
	}

	/**
	 * Get an input stream that can be used to stream all the output data of
	 * this run in zip format.
	 * 
	 * <b>Note:</b> You are responsible for closing the stream once you have
	 * finished with it. Not doing so may prevent further use of the underlying
	 * network connection.
	 * 
	 * @return The stream to read the zip data from.
	 * @see #writeOutputToZipFile(File)
	 */
	public InputStream getOutputZipStream() {
		RunStatus rs = getStatus();
		if (rs == RunStatus.FINISHED) {
			URI uri = URIUtils.appendToPath(getLink(ResourceLabel.WDIR), "out");

			return server.readResourceAsStream(uri, MimeType.ZIP, null,
					credentials);
		} else {
			throw new RunStateException(rs, RunStatus.FINISHED);
		}
	}

	/**
	 * Writes all the output data of this run directly to a file in zip format.
	 * The data is not loaded into memory, it is streamed directly to the file.
	 * The file is created if it does not already exist and will overwrite
	 * existing data if it does.
	 * 
	 * @param file
	 *            the file to write to.
	 * @throws FileNotFoundException
	 *             if the file exists but is a directory rather than a regular
	 *             file, does not exist but cannot be created, or cannot be
	 *             opened for any other reason.
	 * @throws IOException
	 *             if there is any I/O error.
	 * @see #getOutputZipStream()
	 */
	public void writeOutputToZipFile(File file) throws IOException {
		InputStream is = getOutputZipStream();
		try {
			IOUtils.writeStreamToFile(is, file);
		} finally {
			org.apache.commons.io.IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Create a directory in the workspace of this Run. At present you can only
	 * create a directory one level deep.
	 * 
	 * @param dir
	 *            the name of the directory to create.
	 * @return the {@link URI} of the created directory.
	 * @throws IllegalArgumentException
	 *             if an attempt to create a directory more than one level deep
	 *             is made.
	 */
	public URI mkdir(String dir) {
		if (dir.contains("/")) {
			throw new IllegalArgumentException(
					"Directories can only be created one level deep.");
		}

		return server.mkdir(getLink(ResourceLabel.WDIR), dir, credentials);
	}

	/*
	 * Set all the inputs on the server. The inputs must have been set prior to
	 * this call using the InputPort API or a runtime exception is thrown.
	 */
	private void setAllInputs() throws IOException {
		List<String> missingPorts = new ArrayList<String>();

		for (InputPort port : getInputPorts().values()) {
			if (!port.isSet()) {
				missingPorts.add(port.getName());
			} else {
				if (port.isFile()) {
					// If we're using a local file upload it first then set the
					// port to use a remote file.
					if (!port.isRemoteFile()) {
						String file = uploadFile(port.getFile());
						port.setRemoteFile(file);
					}
				}

				setInputPort(port);
			}
		}

		if (!missingPorts.isEmpty()) {
			throw new RunInputsNotSetException(id, missingPorts);
		}
	}

	private void setInputPort(InputPort port) {
		URI path = URIUtils.appendToPath(getLink(ResourceLabel.INPUT),
				"/input/" + port.getName());
		byte[] value;

		if (port.isFile()) {
			value = XMLWriter.inputFile(port.getFile());
		} else {
			value = XMLWriter.inputValue(port.getValue());
		}

		server.updateResource(path, value, credentials);
	}

	private RunResources getRunResources() {
		if (resources == null) {
			resources = server.getXMLReader()
					.readRunResources(uri, credentials);
		}

		return resources;
	}

	private Map<String, InputPort> getInputPortInfo() {
		XMLReader reader = server.getXMLReader();

		return reader.readInputPortDescription(this,
				getLink(ResourceLabel.EXPECTED_INPUTS), credentials);
	}

	private Map<String, OutputPort> getOutputPortInfo() {
		XMLReader reader = server.getXMLReader();

		return reader.readOutputPortDescription(this,
				getLink(ResourceLabel.OUTPUT), credentials);
	}

	byte[] getOutputData(URI uri, LongRange range) {
		return server.readResourceAsBytes(uri, MimeType.BYTES, range,
				credentials);
	}

	InputStream getOutputDataStream(URI uri, LongRange range) {
		return server.readResourceAsStream(uri, MimeType.BYTES, range,
				credentials);
	}

	private URI getLink(ResourceLabel key) {
		return getRunResources().get(key);
	}
}
