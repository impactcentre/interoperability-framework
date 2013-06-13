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

package uk.org.taverna.server.client;

/**
 * An enum to represent the status of a Run on a Taverna Server.
 * 
 * @author Robert Haines
 */
public enum RunStatus {

	/**
	 * Used to represent a Run in the Initialized state.
	 */
	INITIALIZED("Initialized"),

	/**
	 * Used to represent a Run in the Running state.
	 */
	RUNNING("Operating"),

	/**
	 * Used to represent a Run in the Finished state.
	 */
	FINISHED("Finished"),

	/**
	 * Used to represent a Run in the Stopped state. This state is currently
	 * unused.
	 */
	STOPPED("Stopped"),

	/**
	 * Used to represent a Run that has been deleted from the server.
	 */
	DELETED("Deleted"),

	/**
	 * Used to represent a Run in an undefined, or unknown state. This state is
	 * currently unused.
	 */
	UNDEFINED("unknown");

	/**
	 * The String representation of this status.
	 */
	public final String status;

	private RunStatus(String status) {
		this.status = status;
	}

	/**
	 * Convert a String representation of a status to its RunStatus equivalent.
	 * 
	 * @param status
	 *            the status to convert.
	 * @return the converted status.
	 * @throws IllegalArgumentException
	 *             if there is no such status.
	 */
	public static RunStatus fromString(String status) {
		for (RunStatus rs : RunStatus.values()) {
			if (rs.status.equals(status)) {
				return rs;
			}
		}

		throw new IllegalArgumentException("There is no such status as '"
				+ status + "'");
	}
}
