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

package uk.org.taverna.server.client.util;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;

/**
 * A small set of utility methods to ease working with URIs. URI objects are
 * immutable which can be annoying to work with when string manipulation-like
 * operations are required.
 * 
 * @author Robert Haines
 * @since 0.9.0
 */
public final class URIUtils {

	private URIUtils() {
	}

	/**
	 * Strip any user info from a {@link URI}.
	 * 
	 * @param uri
	 *            the {@link URI} to strip.
	 * @return the stripped {@link URI}.
	 * @throws IllegalArgumentException
	 *             if a mal-formed {@link URI} is passed in.
	 */
	public static URI stripUserInfo(final URI uri) {
		try {
			return new URIBuilder(uri).setUserInfo(null).build();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Bad URI passed in: " + uri);
		}
	}

	/**
	 * Append a path to the end of an existing {@link URI}'s path.
	 * 
	 * @param uri
	 *            the {@link URI} on which to append the extra path.
	 * @param extraPath
	 *            the extra path to be appended to the {@link URI}.
	 * @return the new {@link URI} with the extended path.
	 * @throws IllegalArgumentException
	 *             if a mal-formed {@link URI} or path is passed in.
	 */
	public static URI appendToPath(final URI uri, final String extraPath) {
		try {
			return new URIBuilder(uri).setPath(uri.getPath() + "/" + extraPath)
					.build().normalize();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(
					"Either a bad URI was passed in (" + uri
					+ ") or an illegal path was appended to it ("
					+ extraPath + ")");
		}
	}

	/**
	 * Extract the final component of the {@link URI}'s path.
	 * 
	 * If there is no path, or the path ends with <code>/</code> the empty
	 * {@link String} is returned. So:
	 * <ul>
	 * <li><code>http://example.org/this/is/a/path</code> would yield
	 * <code>path</code>,</li>
	 * <li><code>http://example.org</code> would yield the empty {@link String}
	 * and</li>
	 * <li><code>http://example.org/path/</code> would also yield the empty
	 * {@link String}.</li>
	 * </ul>
	 * 
	 * @param uri
	 *            the {@link URI} to get the final path component of.
	 * @return the final path component of the passed in {@link URI}.
	 */
	public static String extractFinalPathComponent(final URI uri) {
		String path = uri.getPath();
		return path.substring(path.lastIndexOf("/") + 1);
	}

	/**
	 * Set the query part of a {@link URI}.
	 * 
	 * @param uri
	 *            the {@link URI} to receive a new query part.
	 * @param query
	 *            the query to set (with or without the leading ?).
	 * @return the new {@link URI} with its query part set.
	 * @throws IllegalArgumentException
	 *             if a mal-formed {@link URI} or query is passed in.
	 */
	public static URI setQuery(final URI uri, String query) {
		String q = query;
		if (q.startsWith("?")) {
			q = q.substring(1);
		}

		try {
			return new URIBuilder(uri).setQuery(q).build();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(
					"Either a bad URI was passed in (" + uri
					+ ") or a bad query was appended to it (" + q + ")");
		}
	}
}
