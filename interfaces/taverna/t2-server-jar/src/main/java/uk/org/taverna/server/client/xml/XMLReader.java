/*
 * Copyright (c) 2012, 2013 The University of Manchester, UK.
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

package uk.org.taverna.server.client.xml;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;

import uk.org.taverna.server.client.AbstractPortValue;
import uk.org.taverna.server.client.InputPort;
import uk.org.taverna.server.client.OutputPort;
import uk.org.taverna.server.client.PortFactory;
import uk.org.taverna.server.client.Run;
import uk.org.taverna.server.client.RunPermission;
import uk.org.taverna.server.client.connection.Connection;
import uk.org.taverna.server.client.connection.MimeType;
import uk.org.taverna.server.client.connection.UserCredentials;
import uk.org.taverna.server.client.util.URIUtils;
import uk.org.taverna.server.client.xml.port.AbsentValue;
import uk.org.taverna.server.client.xml.port.ErrorValue;
import uk.org.taverna.server.client.xml.port.InputDescription;
import uk.org.taverna.server.client.xml.port.LeafValue;
import uk.org.taverna.server.client.xml.port.ListValue;
import uk.org.taverna.server.client.xml.port.OutputDescription;
import uk.org.taverna.server.client.xml.port.Value;
import uk.org.taverna.server.client.xml.rest.Credential;
import uk.org.taverna.server.client.xml.rest.CredentialDescriptor;
import uk.org.taverna.server.client.xml.rest.CredentialList;
import uk.org.taverna.server.client.xml.rest.LinkedPermissionDescription;
import uk.org.taverna.server.client.xml.rest.ListenerDescription;
import uk.org.taverna.server.client.xml.rest.Location;
import uk.org.taverna.server.client.xml.rest.Permission;
import uk.org.taverna.server.client.xml.rest.PermissionsDescription;
import uk.org.taverna.server.client.xml.rest.PolicyDescription;
import uk.org.taverna.server.client.xml.rest.PropertyDescription;
import uk.org.taverna.server.client.xml.rest.RunDescription;
import uk.org.taverna.server.client.xml.rest.RunList;
import uk.org.taverna.server.client.xml.rest.SecurityDescriptor;
import uk.org.taverna.server.client.xml.rest.ServerDescription;
import uk.org.taverna.server.client.xml.rest.TavernaRun;
import uk.org.taverna.server.client.xml.rest.TavernaRunInputs;
import uk.org.taverna.server.client.xml.rest.TrustDescriptor;
import uk.org.taverna.server.client.xml.rest.TrustList;

public final class XMLReader {

	private final static String CTX_PATH = "uk.org.taverna.server.client.xml.rest:uk.org.taverna.server.client.xml.port";
	private final static URI NULL_URI = URI.create("");

	private final Connection connection;

	public XMLReader(Connection connection) {
		this.connection = connection;
	}

	public Object read(URI uri, UserCredentials credentials) {
		Object resources = null;
		InputStream is = connection.readStream(uri, MimeType.XML, credentials);

		try {
			JAXBContext context = JAXBContext.newInstance(CTX_PATH);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			resources = unmarshaller.unmarshal(is);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(is);
		}

		return resources;
	}

	public Object read(URI uri) {
		return read(uri, null);
	}

	public ServerResources readServerResources(URI uri) {
		Map<ResourceLabel, URI> links = new HashMap<ResourceLabel, URI>();

		// Read server top-level description.
		ServerDescription sd = (ServerDescription) read(uri);
		String version = sd.getServerVersion();
		String revision = sd.getServerRevision();
		String timestamp = sd.getServerBuildTimestamp();
		links.put(ResourceLabel.RUNS, sd.getRuns().getHref());
		links.put(ResourceLabel.POLICY, sd.getPolicy().getHref());
		links.put(ResourceLabel.FEED, sd.getFeed().getHref());

		// Read policy description and add links to server's set.
		PolicyDescription pd = (PolicyDescription) read(links
				.get(ResourceLabel.POLICY));
		links.put(ResourceLabel.RUNLIMIT, pd.getRunLimit().getHref());
		links.put(ResourceLabel.PERMITTED_WORKFLOWS, pd.getPermittedWorkflows()
				.getHref());
		links.put(ResourceLabel.PERMITTED_LISTENERS, pd
				.getPermittedListenerTypes().getHref());
		links.put(ResourceLabel.ENABLED_NOTIFICATIONS, pd
				.getEnabledNotificationFabrics().getHref());

		return new ServerResources(links, version, revision, timestamp);
	}

	public Map<String, URI> readRunList(URI uri, UserCredentials credentials) {
		RunList runList = (RunList) read(uri, credentials);
		List<TavernaRun> trs = runList.getRun();

		Map<String, URI> runs = new HashMap<String, URI>(trs.size());

		for (TavernaRun tr : trs) {
			runs.put(tr.getValue(), tr.getHref());
		}

		return runs;
	}

	public RunResources readRunResources(URI uri, UserCredentials credentials) {
		Map<ResourceLabel, URI> links = new HashMap<ResourceLabel, URI>();

		// Read run top-level description.
		RunDescription rd = (RunDescription) read(uri, credentials);
		String owner = rd.getOwner();
		links.put(ResourceLabel.WORKFLOW, rd.getCreationWorkflow().getHref());
		links.put(ResourceLabel.CREATE_TIME, rd.getCreateTime().getHref());
		links.put(ResourceLabel.START_TIME, rd.getStartTime().getHref());
		links.put(ResourceLabel.FINISH_TIME, rd.getFinishTime().getHref());
		links.put(ResourceLabel.STATUS, rd.getStatus().getHref());
		links.put(ResourceLabel.INPUT, rd.getInputs().getHref());
		links.put(ResourceLabel.OUTPUT, rd.getOutput().getHref());
		links.put(ResourceLabel.WDIR, rd.getWorkingDirectory().getHref());
		links.put(ResourceLabel.EXPIRY, rd.getExpiry().getHref());
		links.put(ResourceLabel.SECURITY_CTX, rd.getSecurityContext().getHref());

		// Read the inputs description.
		JAXBElement<?> root = (JAXBElement<?>) read(
				links.get(ResourceLabel.INPUT), credentials);
		TavernaRunInputs tri = (TavernaRunInputs) root.getValue();
		links.put(ResourceLabel.BACLAVA, tri.getBaclava().getHref());
		links.put(ResourceLabel.EXPECTED_INPUTS, tri.getExpected().getHref());

		// Read the special IO listeners - this is kind of hard-coded for now.
		for (Location loc : rd.getListeners().getListener()) {
			URI u = loc.getHref();
			if (URIUtils.extractFinalPathComponent(u).equalsIgnoreCase("io")) {
				root = (JAXBElement<?>) read(u, credentials);
				ListenerDescription ld = (ListenerDescription) root.getValue();

				for (PropertyDescription pd : ld.getProperties().getProperty()) {
					if (pd.getName().equalsIgnoreCase("stdout")) {
						links.put(ResourceLabel.STDOUT, pd.getHref());
					} else if (pd.getName().equalsIgnoreCase("stderr")) {
						links.put(ResourceLabel.STDERR, pd.getHref());
					} else if (pd.getName().equalsIgnoreCase("exitcode")) {
						links.put(ResourceLabel.EXITCODE, pd.getHref());
					}
				}
			}
		}

		// Read the security context iff we are the owner of the run
		if (credentials.getUsername().equals(owner)) {
			root = (JAXBElement<?>) read(links.get(ResourceLabel.SECURITY_CTX),
					credentials);
			SecurityDescriptor sd = (SecurityDescriptor) root.getValue();

			links.put(ResourceLabel.PERMISSIONS, sd.getPermissions().getHref());
			links.put(ResourceLabel.CREDENTIALS, sd.getCredentials().getHref());
			links.put(ResourceLabel.TRUSTS, sd.getTrusts().getHref());
		}

		return new RunResources(links, owner);
	}

	public Map<String, InputPort> readInputPortDescription(Run run, URI uri,
			UserCredentials credentials) {
		JAXBElement<?> root = (JAXBElement<?>) read(uri, credentials);
		InputDescription id = (InputDescription) root.getValue();

		Map<String, InputPort> ports = new HashMap<String, InputPort>();
		for (uk.org.taverna.server.client.xml.port.InputPort ip : id.getInput()) {
			InputPort port = PortFactory
					.newInputPort(run, ip.getName(), ip.getDepth());
			ports.put(port.getName(), port);
		}

		return ports;
	}

	public Map<String, OutputPort> readOutputPortDescription(Run run, URI uri,
			UserCredentials credentials) {
		JAXBElement<?> root = (JAXBElement<?>) read(uri, credentials);
		OutputDescription od = (OutputDescription) root.getValue();

		Map<String, OutputPort> ports = new HashMap<String, OutputPort>();
		for (uk.org.taverna.server.client.xml.port.OutputPort op : od
				.getOutput()) {

			LeafValue v = op.getValue();
			ListValue lv = op.getList();
			ErrorValue ev = op.getError();
			AbsentValue av = op.getAbsent();

			AbstractPortValue value = null;
			if (v != null) {
				value = PortFactory.newPortData(run, v.getHref(),
						v.getContentType(), v.getContentByteLength());
			} else if (lv != null) {
				value = parseOutputPortValueStructure(run, lv);
			} else if (ev != null) {
				value = PortFactory.newPortError(run, ev.getHref(),
						ev.getErrorByteLength());
			} else if (av != null) {
				value = PortFactory.newPortData(run, NULL_URI,
						AbstractPortValue.PORT_EMPTY_TYPE, 0);
			}

			// value should never still be null here
			assert (value != null);

			OutputPort port = PortFactory.newOutputPort(run, op.getName(),
					op.getDepth(), value);
			ports.put(port.getName(), port);
		}

		return ports;
	}

	/*
	 * This method has to parse the OutputPort structure by trying to cast to
	 * each type that a port can be. Not pretty.
	 * 
	 * Even though we know that first time through this method we must have a
	 * list we try to cast to a value first as this is what we will most often
	 * have.
	 */
	private AbstractPortValue parseOutputPortValueStructure(Run run, Value value) {
		if (LeafValue.class.isInstance(value)) {
			LeafValue lv = (LeafValue) value;

			return PortFactory.newPortData(run, lv.getHref(),
					lv.getContentType(),
					lv.getContentByteLength());
		} else if (ListValue.class.isInstance(value)) {
			ListValue lv = (ListValue) value;

			List<AbstractPortValue> list = new ArrayList<AbstractPortValue>();
			for (Value v : lv.getValueOrListOrError()) {
				list.add(parseOutputPortValueStructure(run, v));
			}

			return PortFactory.newPortList(run, lv.getHref(), list);
		} else if (ErrorValue.class.isInstance(value)) {
			ErrorValue ev = (ErrorValue) value;

			return PortFactory.newPortError(run, ev.getHref(),
					ev.getErrorByteLength());
		} else if (AbsentValue.class.isInstance(value)) {
			return PortFactory.newPortList(run, NULL_URI,
					new ArrayList<AbstractPortValue>());
		}

		// We should NOT get here!
		assert (false);
		return null;
	}

	public Map<String, RunPermission> readRunPermissions(URI uri,
			UserCredentials credentials) {
		JAXBElement<?> root = (JAXBElement<?>) read(uri, credentials);
		PermissionsDescription pd = (PermissionsDescription) root.getValue();

		Map<String, RunPermission> perms = new HashMap<String, RunPermission>();
		for (LinkedPermissionDescription lpd : pd.getPermission()) {
			Permission perm = lpd.getPermission();
			perms.put(lpd.getUserName(), RunPermission.fromString(perm.value()));
		}

		return perms;
	}

	public Map<URI, URI> readRunServiceCredentials(URI uri,
			UserCredentials credentials) {
		JAXBElement<?> root = (JAXBElement<?>) read(uri, credentials);
		CredentialList cl = (CredentialList) root.getValue();

		Map<URI, URI> creds = new HashMap<URI, URI>();
		for (Credential cred : cl.getCredential()) {
			CredentialDescriptor cd = cred.getUserpass();
			if (cd == null) {
				cd = cred.getKeypair();
			}

			creds.put(cd.getServiceURI(), cd.getHref());
		}

		return creds;
	}

	public List<URI> readRunTrustedIdentities(URI uri, UserCredentials credentials) {
		JAXBElement<?> root = (JAXBElement<?>) read(uri, credentials);
		TrustList tl = (TrustList) root.getValue();

		List<URI> trusts = new ArrayList<URI>();
		for (TrustDescriptor td : tl.getTrust()) {
			trusts.add(td.getHref());
		}

		return trusts;
	}
}
