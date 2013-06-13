/*
 * Copyright (c) 2013 The University of Manchester, UK.
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

import java.net.URI;
import java.util.List;

/**
 * This class is used to create input and output ports and various parts of the
 * output port data structures.
 * 
 * <b>Note:</b> For internal use only. Methods in this class are not intended
 * for use outside of this library.
 * 
 * @author Robert Haines
 */
public final class PortFactory {

	private PortFactory() {
	}

	/**
	 * Create a new InputPort.
	 * 
	 * <b>Note:</b> For internal use only.
	 * 
	 * @param run
	 *            the run that the port belongs to.
	 * @param name
	 *            the port name.
	 * @param depth
	 *            the depth of the data in the port.
	 * @return a new InputPort instance.
	 */
	public static InputPort newInputPort(Run run, String name, int depth) {
		return new InputPort(run, name, depth);
	}

	/**
	 * Create a new OutputPort.
	 * 
	 * <b>Note:</b> For internal use only.
	 * 
	 * @param run
	 *            the run that the port belongs to.
	 * @param name
	 *            the port name.
	 * @param depth
	 *            the depth of the data in the port.
	 * @param value
	 *            the port value data structure.
	 * @return a new OutputPort instance.
	 */
	public static OutputPort newOutputPort(Run run, String name, int depth,
			AbstractPortValue value) {
		return new OutputPort(run, name, depth, value);
	}

	/**
	 * Create a new instance of PortData.
	 * 
	 * <b>Note:</b> For internal use only.
	 * 
	 * @param run
	 *            the run that the data belongs to.
	 * @param reference
	 *            the URI reference to the data on the remote server.
	 * @param type
	 *            the mime type of the data.
	 * @param size
	 *            the size of the data in bytes.
	 * @return a new PortData instance.
	 */
	public static PortDataValue newPortData(Run run, URI reference, String type,
			long size) {
		return new PortDataValue(run, reference, type, size);
	}

	/**
	 * Create a new instance of PortList.
	 * 
	 * <b>Note:</b> For internal use only.
	 * 
	 * @param run
	 *            the run that the list belongs to.
	 * @param reference
	 *            the URI reference to the list on the remote server.
	 * @param list
	 *            the list of values that makes up this list.
	 * @return a new PortList instance.
	 */
	public static PortListValue newPortList(Run run, URI reference,
			List<AbstractPortValue> list) {
		return new PortListValue(run, reference, list);
	}

	/**
	 * Create a new instance of PortError.
	 * 
	 * <b>Note:</b> For internal use only.
	 * 
	 * @param run
	 *            the run that the error belongs to.
	 * @param reference
	 *            the URI reference to the error on the remote server.
	 * @param size
	 *            the size of the error data in bytes.
	 * @return a new PortError instance.
	 */
	public static PortErrorValue newPortError(Run run, URI reference, long size) {
		return new PortErrorValue(run, reference, size);
	}
}
