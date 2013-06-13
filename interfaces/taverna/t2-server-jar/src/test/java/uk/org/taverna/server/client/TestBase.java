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

package uk.org.taverna.server.client;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;

import uk.org.taverna.server.client.connection.HttpBasicCredentials;
import uk.org.taverna.server.client.connection.UserCredentials;

public abstract class TestBase {

	// Workflow files.
	protected final static String WKF_PASS_FILE = "/workflows/pass_through.t2flow";
	protected final static String WKF_XML_FILE = "/workflows/xml_xpath.t2flow";
	protected final static String WKF_LISTS_FILE = "/workflows/empty_list.t2flow";
	protected final static String WKF_FAIL_FILE = "/workflows/always_fail.t2flow";
	protected final static String WKF_ERRORS_FILE = "/workflows/list_with_errors.t2flow";
	protected final static String WKF_MISS_OUT_FILE = "/workflows/missing_outputs.t2flow";

	// Common resources.
	protected static URI serverURI;
	protected static UserCredentials user1;
	protected static UserCredentials user2;

	@BeforeClass
	public static void getConfiguration() {
		String address = "http://localhost:9080/tavernaserver";
		String creds1 = System.getProperty("USER1", "taverna:taverna");
		String creds2 = System.getProperty("USER2");

		if (address == null) {
			fail("Invalid configuration. Make sure SERVER is set.");
		}

		try {
			serverURI = new URI(address);
		} catch (URISyntaxException e) {
			fail("Invalid configuration. SERVER is not a valid URI.");
		}

		user1 = new HttpBasicCredentials(creds1);
		if (creds2 != null) {
			user2 = new HttpBasicCredentials(creds2);
		}
	}

	protected InputStream getResourceStream(String filename) {
		InputStream is = getClass().getResourceAsStream(filename);

		if (is == null) {
			fail("Could not open resource: " + filename);
		}

		return is;
	}

	protected byte[] loadResource(String filename) {
		InputStream is = null;
		try {
			is = getResourceStream(filename);
			return IOUtils.toByteArray(is);
		} catch (Exception e) {
			fail("Could not open resource: " + filename);
		} finally {
			IOUtils.closeQuietly(is);
		}

		return null;
	}

	protected File getResourceFile(String filename) {
		try {
			URL fileURL = getClass().getResource(filename);
			return new File(fileURL.toURI());
		} catch (Exception e) {
			fail("Could not get file: " + filename);
		}

		return null;
	}
}
