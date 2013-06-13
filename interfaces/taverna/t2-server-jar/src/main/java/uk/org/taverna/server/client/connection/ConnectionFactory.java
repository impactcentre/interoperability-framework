/*
 * Copyright (c) 2010, 2011 The University of Manchester, UK.
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

package uk.org.taverna.server.client.connection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import uk.org.taverna.server.client.connection.params.ConnectionParams;
import uk.org.taverna.server.client.connection.params.NullConnectionParams;

/**
 * 
 * @author Robert Haines
 */
public class ConnectionFactory {
	private static Map<URI, Connection> connections = new HashMap<URI, Connection>();

	private ConnectionFactory() {
	}

	public static Connection getConnection(URI uri, ConnectionParams params) {
		if (params == null) {
			params = new NullConnectionParams();
		}

		Connection c = connections.get(uri);

		if (c == null) {
			String scheme = uri.getScheme();
			if (scheme.equalsIgnoreCase("http")) {
				c = new HttpConnection(uri, params);
			} else if (scheme.equalsIgnoreCase("https")) {
				c = new HttpsConnection(uri, params);
			} else {
				throw new IllegalArgumentException(
						"Must specify a scheme, e.g. http or https");
			}

			connections.put(uri, c);
		}

		return c;
	}

	public static Connection getConnection(URI uri) {
		return getConnection(uri, null);
	}

	public static Connection getConnection(String uri, ConnectionParams params)
			throws URISyntaxException {
		return getConnection(new URI(uri), params);
	}

	public static Connection getConnection(String uri)
			throws URISyntaxException {
		return getConnection(uri, null);
	}
}
