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

package uk.org.taverna.server.client.connection;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

/**
 * 
 * @author Robert Haines
 */
public abstract class AbstractConnection implements Connection {

	@Override
	public URI create(URI uri, byte[] content, MimeType type,
			UserCredentials credentials) {
		return create(uri, new ByteArrayInputStream(content), content.length,
				type, credentials);
	}

	@Override
	public URI create(URI uri, InputStream content, MimeType type,
			UserCredentials credentials) {
		return create(uri, content, -1, type, credentials);
	}

	@Override
	public InputStream readStream(URI uri, MimeType type,
			UserCredentials credentials) {
		return readStream(uri, type, null, credentials);
	}

	@Override
	public byte[] read(URI uri, MimeType type, UserCredentials credentials) {
		return read(uri, type, null, credentials);
	}

	@Override
	public URI update(URI uri, byte[] content, MimeType type,
			UserCredentials credentials) {
		return update(uri, new ByteArrayInputStream(content), content.length,
				type, credentials);
	}

	@Override
	public URI update(URI uri, InputStream content, MimeType type,
			UserCredentials credentials) {
		return update(uri, content, -1, type, credentials);
	}
}
