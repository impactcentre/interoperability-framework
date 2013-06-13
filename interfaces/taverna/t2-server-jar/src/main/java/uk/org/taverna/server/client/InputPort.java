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

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 
 * @author Robert Haines
 * 
 */
public final class InputPort extends Port {

	private String value;
	private String filename;
	private boolean remoteFile;

	InputPort(Run run, String name, int depth) {
		super(run, name, depth);

		value = null;
		filename = null;
		remoteFile = false;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		if (run.isInitialized()) {
			filename = null;
			remoteFile = false;
			this.value = value;
		}
	}

	public File getFile() {
		return new File(filename);
	}

	public String getFileName() {
		return filename;
	}

	public void setFile(File file) throws FileNotFoundException {
		if (run.isInitialized()) {
			if (file.isFile() && file.canRead()) {
				value = null;
				remoteFile = false;
				this.filename = file.getAbsolutePath();
			} else {
				throw new FileNotFoundException("File '"
						+ file.getAbsolutePath()
						+ "' either does not exist or is not readable.");
			}
		}
	}

	public void setRemoteFile(String filename) {
		if (run.isInitialized()) {
			value = null;
			this.filename = filename;
			remoteFile = true;
		}
	}

	public boolean isFile() {
		return !(filename == null);
	}

	public boolean isRemoteFile() {
		return isFile() && remoteFile;
	}

	public boolean isBaclava() {
		return run.isBaclavaInput();
	}

	public boolean isSet() {
		return !(value == null) || isFile() || isBaclava();
	}
}
