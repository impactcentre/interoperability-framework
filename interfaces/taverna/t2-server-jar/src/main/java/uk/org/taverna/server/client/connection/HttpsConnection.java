/*
 * Copyright (c) 2010-2013 The University of Manchester, UK.
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

package uk.org.taverna.server.client.connection;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;

import uk.org.taverna.server.client.connection.params.ConnectionPNames;
import uk.org.taverna.server.client.connection.params.ConnectionParams;

/**
 * 
 * @author Robert Haines
 */
public class HttpsConnection extends HttpConnection implements ConnectionPNames {

	HttpsConnection(URI uri, ConnectionParams params) {
		super(uri, params);

		// get the remote port and default to 443 for https
		int remotePort = uri.getPort();
		remotePort = (remotePort != -1) ? remotePort : 443;

		try {
			TrustManager[] trustManagers = null;

			Certificate cert = (Certificate) params
					.getParameter(SSL_CLIENT_CERT);

			if (cert != null) {
				KeyStore ks = KeyStore.getInstance(KeyStore
						.getDefaultType());
				ks.load(null, null);

				ks.setCertificateEntry("peer", cert);

				TrustManagerFactory tmf = TrustManagerFactory
						.getInstance(TrustManagerFactory
								.getDefaultAlgorithm());
				tmf.init(ks);
				trustManagers = tmf.getTrustManagers();
			}

			if (params.getBooleanParameter(SSL_NO_AUTH, false)) {
				trustManagers = new TrustManager[] { new OpenTrustManager() };
			}

			if (trustManagers == null) {
				trustManagers = new TrustManager[] { getDefaultTrustManager() };
			}

			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(null, trustManagers, null);

			SSLSocketFactory sf;
			if (params.getBooleanParameter(SSL_NO_VERIFY_HOST, false)) {
				sf = new SSLSocketFactory(sslcontext,
						new AllowAllHostnameVerifier());
			} else {
				sf = new SSLSocketFactory(sslcontext);
			}

			Scheme httpsScheme = new Scheme("https", remotePort, sf);
			SchemeRegistry schemeRegistry = httpClient.getConnectionManager()
					.getSchemeRegistry();
			schemeRegistry.register(httpsScheme);
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private TrustManager getDefaultTrustManager()
			throws NoSuchAlgorithmException, KeyStoreException {
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());

		trustManagerFactory.init((KeyStore) null);

		for (TrustManager tm : trustManagerFactory.getTrustManagers()) {
			if (tm instanceof X509TrustManager) {
				return tm;
			}
		}

		return null;
	}

	private class OpenTrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}
}
