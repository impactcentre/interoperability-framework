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

import java.io.InputStream;
import java.net.URI;

import org.apache.commons.lang.text.StrBuilder;

/**
 * 
 * @author Robert Haines
 */
public final class OutputPort extends Port {

	private final AbstractPortValue value;

	public OutputPort(Run run, String name, int depth, AbstractPortValue value) {
		super(run, name, depth);

		this.value = value;
	}

	/**
	 * 
	 * @return
	 */
	public AbstractPortValue getValue() {
		return value;
	}

	/**
	 * Does this port contain error?
	 * 
	 * @return <code>true</code> if this port contains an error,
	 *         <code>false</code> otherwise.
	 */
	public boolean isError() {
		return value.isError();
	}

	/**
	 * Get the total size of all the data on this OutputPort. For a singleton
	 * port this is simply the size of the single value but for any other depth
	 * port it is the addition of all values in the port.
	 * 
	 * @return The total data size of this OutputPort.
	 */
	public long getDataSize() {
		return value.getDataSize();
	}

	public String getContentType() {
		return value.getContentType();
	}

	public byte[] getData() {
		return value.getData();
	}

	public byte[] getData(int index) {
		return value.getData(index);
	}

	public String getDataAsString() {
		return value.getDataAsString();
	}

	public InputStream getDataStream() {
		return value.getDataStream();
	}

	public URI getReference() {
		return value.getReference();
	}

	@Override
	public String toString() {
		return toString(0);
	}

	public String toString(int indent) {
		StrBuilder message = new StrBuilder();

		message.appendPadding(indent, ' ');
		message.appendln(name + " (depth " + depth + ") {");
		message.appendPadding(indent, ' ');
		message.appendln(value.toString(indent + 1));
		message.append("}");

		return message.toString();
	}
}
