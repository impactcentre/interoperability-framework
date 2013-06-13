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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.lang.math.LongRange;

import uk.org.taverna.server.client.util.IOUtils;

/**
 * 
 * @author Robert Haines
 */
public class PortDataValue extends AbstractPortValue {

	// If there is no data...
	private static final byte[] EMPTY_DATA = new byte[0];

	PortDataValue(Run run, URI reference, String type, long size) {
		super(run, reference, type, size);
	}

	@Override
	public boolean isError() {
		return false;
	}

	/**
	 * Get the data value at the specified index. As PortData only ever holds a
	 * single data point specifying an index greater than zero makes no sense.
	 * This method is included for completeness and to satisfy the List
	 * interface.
	 * 
	 * @param index
	 *            the index of the data value to return.
	 * @return the data value at the specified index.
	 * @throws IndexOutOfBoundsException
	 *             if anything other than zero is given as the value for index.
	 */
	@Override
	public AbstractPortValue get(int index) {
		if (index != 0) {
			throw new IndexOutOfBoundsException();
		}

		return this;
	}

	/**
	 * Get the number of data values in this list. This method just returns 1 as
	 * PortData only ever holds a single data item. This method is included for
	 * completeness and to satisfy the List interface.
	 * 
	 * @return 1.
	 */
	@Override
	public int size() {
		return 1;
	}

	@Override
	public long getDataSize() {
		return size;
	}

	@Override
	public InputStream getDataStream() {
		return run.getOutputDataStream(reference, null);
	}

	@Override
	public void writeDataToFile(File file) throws IOException {
		InputStream is = run.getOutputDataStream(reference, null);
		try {
			IOUtils.writeStreamToFile(is, file);
		} finally {
			org.apache.commons.io.IOUtils.closeQuietly(is);
		}
	}

	@Override
	public byte[] getData() {
		// LongRange is inclusive so size is too long by one.
		return getData(new LongRange(0, (size - 1)));
	}

	@Override
	public byte[] getData(int index) {
		return getData();
	}

	public byte[] getData(int start, int length) {
		// If length is zero then there is nothing to return.
		if (length == 0) {
			return EMPTY_DATA;
		}

		// LongRange is inclusive so (start + length) is too long by one.
		return getData(new LongRange(start, (start + length - 1)));
	}

	private byte[] getData(LongRange range) {

		// Return empty data if this value is empty.
		if (getDataSize() == 0
				|| contentType.equalsIgnoreCase("application/x-empty")) {
			return EMPTY_DATA;
		}

		// Check the range provided is sensible. LongRange is inclusive so size
		// is too long by one.
		if (range.getMinimumLong() < 0) {
			range = new LongRange(0, range.getMaximumLong());
		}
		if (range.getMaximumLong() >= getDataSize()) {
			range = new LongRange(range.getMinimumLong(),
					(getDataSize() - 1));
		}

		return run.getOutputData(reference, range);
	}
}
