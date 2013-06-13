/*
 * Copyright (c) 2012 The University of Manchester, UK.
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import uk.org.taverna.server.client.RunPermission;
import uk.org.taverna.server.client.xml.rest.Credential;
import uk.org.taverna.server.client.xml.rest.InputDescription;
import uk.org.taverna.server.client.xml.rest.KeyPairCredential;
import uk.org.taverna.server.client.xml.rest.MakeDirectory;
import uk.org.taverna.server.client.xml.rest.ObjectFactory;
import uk.org.taverna.server.client.xml.rest.PasswordCredential;
import uk.org.taverna.server.client.xml.rest.Permission;
import uk.org.taverna.server.client.xml.rest.PermissionDescription;
import uk.org.taverna.server.client.xml.rest.TrustDescriptor;

public final class XMLWriter {

	static byte[] write(JAXBElement<?> element) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			JAXBContext context = JAXBContext
					.newInstance("uk.org.taverna.server.client.xml.rest");
			Marshaller marshaller = context.createMarshaller();
			marshaller.marshal(element, os);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return os.toByteArray();
	}

	public static byte[] mkdir(String name) {
		MakeDirectory md = new MakeDirectory();
		md.setName(name);

		ObjectFactory factory = new ObjectFactory();
		JAXBElement<MakeDirectory> element = factory.createMkdir(md);

		return write(element);
	}

	public static byte[] inputValue(String value) {
		InputDescription.Value idv = new InputDescription.Value();
		idv.setValue(value);

		InputDescription id = new InputDescription();
		id.setValue(idv);

		ObjectFactory factory = new ObjectFactory();
		JAXBElement<InputDescription> element = factory.createRunInput(id);

		return write(element);
	}

	public static byte[] inputFile(File file) {
		InputDescription.File idf = new InputDescription.File();
		idf.setValue(file.getPath());

		InputDescription id = new InputDescription();
		id.setFile(idf);

		ObjectFactory factory = new ObjectFactory();
		JAXBElement<InputDescription> element = factory.createRunInput(id);

		return write(element);
	}

	public static byte[] runPermission(String username, RunPermission permission) {
		PermissionDescription pd = new PermissionDescription();
		pd.setUserName(username);
		pd.setPermission(Permission.fromValue(permission.permission));

		ObjectFactory factory = new ObjectFactory();
		JAXBElement<PermissionDescription> element = factory
				.createPermissionUpdate(pd);

		return write(element);
	}

	public static byte[] runServiceUserPassCredential(URI uri, String username,
			String password) {
		PasswordCredential pc = new PasswordCredential();
		pc.setServiceURI(uri);
		pc.setUsername(username);
		pc.setPassword(password);

		Credential cred = new Credential();
		cred.setUserpass(pc);

		ObjectFactory factory = new ObjectFactory();
		JAXBElement<Credential> element = factory.createCredential(cred);

		return write(element);
	}

	public static byte[] runServiceKeyPairCredential(URI uri, String filename,
			String type, String name, String password) {
		KeyPairCredential kpc = new KeyPairCredential();
		kpc.setServiceURI(uri);
		kpc.setCredentialFile(filename);
		kpc.setCredentialName(name);
		kpc.setFileType(type);
		kpc.setUnlockPassword(password);

		Credential cred = new Credential();
		cred.setKeypair(kpc);

		ObjectFactory factory = new ObjectFactory();
		JAXBElement<Credential> element = factory.createCredential(cred);

		return write(element);
	}

	public static byte[] runTrustedIdentity(String filename, String type) {
		TrustDescriptor td = new TrustDescriptor();
		td.setCertificateFile(filename);
		td.setFileType(type);

		ObjectFactory factory = new ObjectFactory();
		JAXBElement<TrustDescriptor> element = factory
				.createTrustedIdentity(td);

		return write(element);
	}
}
