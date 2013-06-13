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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/**
 * This class provides easy configuration of an SSL connection to a Taverna
 * Server. Peer verification can be toggled and a client certificate can be
 * provided for authentication to servers with non-standard, or self-signed,
 * certificates.
 * 
 * @author Robert Haines
 */
public final class CustomSSLConnectionParams extends AbstractConnectionParams {

	/**
	 * Create a custom SSL configuration with a client certificate and optional
	 * peer verification.
	 * 
	 * @param certificate
	 *            the client certificate as an InputStream.
	 * @param noVerify
	 *            switch to turn off peer verification.
	 * @throws CertificateException
	 *             if there is a problem with the provided certificate.
	 */
	public CustomSSLConnectionParams(InputStream certificate, boolean noVerify)
			throws CertificateException {
		super();

		if (certificate != null) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Certificate cert = cf.generateCertificate(certificate);

			params.put(SSL_CLIENT_CERT, cert);
		}

		setBooleanParameter(SSL_NO_VERIFY_HOST, noVerify);
	}

	/**
	 * Create a custom SSL configuration with just a client certificate.
	 * 
	 * @param certificate
	 *            the client certificate as an InputStream.
	 * @throws CertificateException
	 *             if there is a problem with the provided certificate.
	 */
	public CustomSSLConnectionParams(InputStream certificate)
			throws CertificateException {
		this(certificate, false);
	}

	/**
	 * Create a custom SSL configuration with a client certificate and optional
	 * peer verification.
	 * 
	 * @param certificate
	 *            the client certificate as a File object.
	 * @param noVerify
	 *            switch to turn off peer verification.
	 * @throws FileNotFoundException
	 *             if the certificate provided could not be found.
	 * @throws CertificateException
	 *             if there is a problem with the provided certificate.
	 */
	public CustomSSLConnectionParams(File certificate, boolean noVerify)
			throws FileNotFoundException, CertificateException {
		this(new FileInputStream(certificate), noVerify);
	}

	/**
	 * Create a custom SSL configuration with just a client certificate.
	 * 
	 * @param certificate
	 *            the client certificate as a File object.
	 * @throws FileNotFoundException
	 *             if the certificate provided could not be found.
	 * @throws CertificateException
	 *             if there is a problem with the provided certificate.
	 */
	public CustomSSLConnectionParams(File certificate)
			throws FileNotFoundException, CertificateException {
		this(certificate, false);
	}

	/**
	 * Create a custom SSL configuration with a client certificate and optional
	 * peer verification.
	 * 
	 * @param certificate
	 *            the client certificate.
	 * @param noVerify
	 *            switch to turn off peer verification.
	 */
	public CustomSSLConnectionParams(Certificate certificate, boolean noVerify) {
		params.put(SSL_CLIENT_CERT, certificate);
		setBooleanParameter(SSL_NO_VERIFY_HOST, noVerify);
	}

	/**
	 * Create a custom SSL configuration with just a client certificate.
	 * 
	 * @param certificate
	 *            the client certificate.
	 */
	public CustomSSLConnectionParams(Certificate certificate) {
		this(certificate, false);
	}
}
