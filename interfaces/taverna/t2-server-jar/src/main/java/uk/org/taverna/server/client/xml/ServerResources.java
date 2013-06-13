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

import java.net.URI;
import java.util.Map;

public final class ServerResources extends AbstractResources {

	private static final String SNAPSHOT = "-SNAPSHOT";

	private final String version;
	private final String revision;
	private final String timestamp;
	private final int[] versionComponents;

	ServerResources(Map<ResourceLabel, URI> links, String version,
			String revision, String timestamp) {
		super(links);

		this.revision = revision;
		this.timestamp = timestamp;

		// Parse out and rebuild the version string into a normalized
		// major.minor.patch format. This is so that all version strings have
		// three components - 2.4 will be converted to 2.4.0.
		versionComponents = new int[3];
		if (version.endsWith(SNAPSHOT)) {
			version = version.replaceAll(SNAPSHOT, "");
		}
		String[] vs = version.split("\\.", 3);
		versionComponents[0] = Integer.parseInt(vs[0]);
		versionComponents[1] = Integer.parseInt(vs[1]);

		try {
			// We might not have a third component!
			versionComponents[2] = Integer.parseInt(vs[2]);
		} catch (IndexOutOfBoundsException e) {
			versionComponents[2] = 0;
		}

		this.version = String.format("%d.%d.%d", versionComponents[0],
				versionComponents[1], versionComponents[2]);
	}

	public String getVersion() {
		return version;
	}

	public String getRevision() {
		return revision;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public int[] getVersionComponents() {
		return versionComponents;
	}
}
