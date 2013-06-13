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
import java.util.AbstractList;

import org.apache.commons.lang.text.StrBuilder;

/**
 * This serves as the abstract superclass of the output port data classes:
 * <ul>
 * <li>{@link PortDataValue} holds a single data value. For output ports of depth
 * zero it holds the entirety of the data in the port. For ports holding lists
 * it holds the data of each list entry.</li>
 * <li>{@link PortListValue} holds a list of items that make up the data of an output
 * port. These list items will be further instances of PortList until the depth
 * of the port is reached, when they will be instances of PortData (or
 * PortError) that hold the actual data values.</li>
 * <li>{@link PortErrorValue} holds error data if an error was returned from the
 * server for a particular data point.</li>
 * </ul>
 * 
 * @author Robert Haines
 */
public abstract class AbstractPortValue extends AbstractList<AbstractPortValue> {

	public static final String PORT_EMPTY_TYPE = "application/x-empty";
	public static final String PORT_ERROR_TYPE = "application/x-error";
	public static final String PORT_LIST_TYPE = "application/x-list";

	/**
	 * A reference back to the run to which this value belongs.
	 */
	protected final Run run;

	/**
	 * The URI reference to the actual data on the remote server.
	 */
	protected final URI reference;

	/**
	 * The content type (mime type) for the data held in this value.
	 */
	protected final String contentType;

	/**
	 * The size of the data, in bytes, held in this value.
	 */
	protected final long size;

	AbstractPortValue(Run run, URI reference, String type, long size) {
		this.run = run;
		this.reference = reference;
		this.contentType = type;
		this.size = size;
	}

	/**
	 * Get the content type (mime type) of the data held in this value.
	 * 
	 * @return the content type (mime type) of the data held in this value.
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Get all the data held in this port value.
	 * 
	 * This method can only be used on subclasses that actually hold data, such
	 * as {@link PortDataValue} or {@link PortErrorValue}.
	 * 
	 * @return all the data held in this port value.
	 * @throws UnsupportedOperationException
	 *             if called on an instance of {@link PortListValue}.
	 * @see #getData(int)
	 * @see #getDataStream()
	 * @see #getDataAsString()
	 */
	public abstract byte[] getData();

	/**
	 * Get all the data held at the specified index of this output value. If
	 * this value is a {@link PortListValue} then the value at the specified index is
	 * returned. If this value is a {@link PortDataValue} then only index 0 will
	 * return any data.
	 * 
	 * @param index
	 *            the index of the data to return.
	 * @return the data at the specified index.
	 * @throws IndexOutOfBoundsException
	 *             if the index specified does not fall within the bounds of the
	 *             underlying list of data values.
	 * @see #getData()
	 * @see #getDataStream()
	 * @see #getDataAsString()
	 */
	public abstract byte[] getData(int index);

	/**
	 * Get an input stream that can be used to stream the data held by this
	 * value from the server.
	 * 
	 * <b>Note:</b> You are responsible for closing the stream once you have
	 * finished with it. Not doing so may prevent further use of the underlying
	 * network connection.
	 * 
	 * @return the stream to read the data from.
	 * @throws UnsupportedOperationException
	 *             if called on an instance of {@link PortListValue}.
	 * @see #getData()
	 * @see #getData(int)
	 * @see #getDataAsString()
	 */
	public abstract InputStream getDataStream();

	/**
	 * Get all data held in this port value and save it to the specified file.
	 * 
	 * @param file
	 *            the file to write the data to.
	 * @throws UnsupportedOperationException
	 *             if called on an instance of {@link PortListValue}.
	 * @throws IOException
	 *             if the specified file cannot be found, opened or written to
	 *             for any reason.
	 */
	public abstract void writeDataToFile(File file) throws IOException;

	/**
	 * Get all the data held in this port value and return it as a String.
	 * 
	 * @return all the data held in this port value as a String.
	 * @throws UnsupportedOperationException
	 *             if called on an instance of {@link PortListValue}.
	 * @see #getData()
	 * @see #getData(int)
	 * @see #getDataStream()
	 */
	public String getDataAsString() {
		return new String(getData());
	}

	/**
	 * Get the size, in bytes, of the data held by this value. For lists this is
	 * the total of all the data sizes in the list.
	 * 
	 * @return the total size of the data in this value in bytes.
	 */
	public abstract long getDataSize();

	/**
	 * Get the URI reference to the actual data on the remote server.
	 * 
	 * @return the URI reference to the actual data on the remote server.
	 */
	public URI getReference() {
		return reference;
	}

	/**
	 * Get the run to which this value belongs.
	 * 
	 * @return the run to which this value belongs.
	 */
	public Run getRun() {
		return run;
	}

	/**
	 * Does this value hold an error? If this value is a list then each member
	 * of the list is checked for errors and true is returned if any error is
	 * found.
	 * 
	 * @return true if there is an error in this value, false otherwise.
	 */
	public abstract boolean isError();

	/**
	 * Produces a string representation of this value.
	 * 
	 * @return the String representation of this value.
	 */
	@Override
	public String toString() {
		return toString(0);
	}

	/**
	 * Produces a string representation of this value. An indent may be
	 * specified to aid readability when this value is part of a deep list of
	 * values.
	 * 
	 * @param indent
	 *            the amount of indent required.
	 * @return the String representation of this value.
	 */
	public String toString(int indent) {
		StrBuilder message = new StrBuilder();

		message.appendPadding(indent, ' ');
		message.appendln(reference.toASCIIString());
		message.appendPadding(indent, ' ');
		message.appendln(contentType);
		message.appendPadding(indent, ' ');
		message.append(getDataSize());

		return message.toString();
	}
}
