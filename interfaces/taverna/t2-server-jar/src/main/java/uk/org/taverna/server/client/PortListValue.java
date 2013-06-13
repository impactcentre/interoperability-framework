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

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.apache.commons.lang.text.StrBuilder;

/**
 * 
 * @author Robert Haines
 */
public final class PortListValue extends AbstractPortValue {

	private final List<AbstractPortValue> list;
	private long dataSize;

	PortListValue(Run run, URI reference, List<AbstractPortValue> list) {
		super(run, reference, PORT_LIST_TYPE, 0);

		this.list = list;
		this.dataSize = -1;
	}

	/**
	 * Get the data value at the specified index.
	 * 
	 * @param index
	 *            the index of the data value to return.
	 * @return the data value at the specified index.
	 */
	@Override
	public AbstractPortValue get(int index) {
		return list.get(index);
	}

	/**
	 * Get the number of data values in this list.
	 * 
	 * @return the number of data values in this list.
	 */
	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isError() {
		for (AbstractPortValue p : list) {
			if (p.isError()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public byte[] getData() {
		throw new UnsupportedOperationException(
				"This operation is not supported for list output ports.");
	}

	@Override
	public byte[] getData(int index) {
		return list.get(index).getData();
	}

	@Override
	public InputStream getDataStream() {
		throw new UnsupportedOperationException(
				"This operation is not supported for list output ports.");
	}

	@Override
	public void writeDataToFile(File file) {
		throw new UnsupportedOperationException(
				"This operation is not supported for list output ports.");
	}

	@Override
	public long getDataSize() {
		if (dataSize == -1) {
			dataSize = 0;

			for (AbstractPortValue p : list) {
				dataSize += p.getDataSize();
			}
		}

		return dataSize;
	}

	@Override
	public String toString(int indent) {
		StrBuilder message = new StrBuilder();

		message.appendPadding(indent, ' ');
		message.appendln(reference.toASCIIString());
		message.appendPadding(indent, ' ');
		message.appendln("[");
		for (AbstractPortValue p : list) {
			message.appendln(p.toString(indent + 1));
		}
		message.appendPadding(indent, ' ');
		message.append("]");

		return message.toString();
	}
}
