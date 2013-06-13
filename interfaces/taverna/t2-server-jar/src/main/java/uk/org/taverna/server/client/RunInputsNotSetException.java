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

package uk.org.taverna.server.client;

import java.util.List;

/**
 * This exception is used to indicate that one or more inputs to a {@link Run}
 * were not set before attempting to start it running.
 * 
 * @author Robert Haines
 * @since 0.9.0
 */
public class RunInputsNotSetException extends ServerException {
	private static final long serialVersionUID = 1L;

	private final List<String> missing;
	private final String id;

	/**
	 * Construct a new input not set exception specifying the identifier of the
	 * run and the missing inputs.
	 * 
	 * @param id
	 *            the identifier of the run with the missing input.
	 * @param missingInputs
	 *            a list of the names of the missing inputs.
	 */
	public RunInputsNotSetException(String id, List<String> missingInputs) {
		super("One or more inputs of the run with id '" + id + "' are not set.");

		this.id = id;
		this.missing = missingInputs;
	}

	/**
	 * Get the list of input port names that are not set.
	 * 
	 * @return the list of input port names that are not set.
	 */
	public List<String> getInputNames() {
		return missing;
	}

	/**
	 * Get the identifier of the run with the unset inputs.
	 * 
	 * @return the run identifier.
	 */
	public String getRunIdentifier() {
		return id;
	}
}
