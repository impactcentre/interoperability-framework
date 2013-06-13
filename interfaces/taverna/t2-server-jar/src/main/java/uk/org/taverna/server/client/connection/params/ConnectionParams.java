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

package uk.org.taverna.server.client.connection.params;

/**
 * This interface defines the API for connection parameter objects.
 * 
 * @author Robert Haines
 */
public interface ConnectionParams extends ConnectionPNames {

	/**
	 * Get the value of a parameter stored in this parameter set.
	 * 
	 * @param id
	 *            the parameter to get.
	 * @return the value of the requested parameter.
	 */
	public Object getParameter(String id);

	/**
	 * Remove and return a parameter from this parameter set.
	 * 
	 * @param id
	 *            the parameter to remove.
	 * @return the value of the removed parameter.
	 */
	public Object removeParameter(String id);

	/**
	 * Set a parameter value in this parameter set.
	 * 
	 * @param id
	 *            the parameter to set.
	 * @param value
	 *            the value of the parameter.
	 * @return the modified parameter set.
	 */
	public ConnectionParams setParameter(String id, Object value);

	/**
	 * Get the value of a boolean parameter stored in this parameter set. If the
	 * parameter is not present in this set then the default value is returned
	 * instead.
	 * 
	 * @param id
	 *            the parameter to get.
	 * @param defaultValue
	 *            the default value of the parameter if it not set.
	 * @return the value of the parameter.
	 */
	public boolean getBooleanParameter(String id, boolean defaultValue);

	/**
	 * Set a boolean parameter value in this parameter set.
	 * 
	 * @param id
	 *            the parameter to set.
	 * @param value
	 *            the value of the parameter.
	 * @return the modified parameter set.
	 */
	public ConnectionParams setBooleanParameter(String id, boolean value);

	/**
	 * Is the named parameter true?
	 * 
	 * @param id
	 *            the parameter to test.
	 * @return true if true, false if not or absent.
	 */
	public boolean isParameterTrue(String id);

	/**
	 * Is the named parameter false?
	 * 
	 * @param id
	 *            the parameter to test.
	 * @return true if false, false if not or absent.
	 */
	public boolean isParameterFalse(String id);
}
