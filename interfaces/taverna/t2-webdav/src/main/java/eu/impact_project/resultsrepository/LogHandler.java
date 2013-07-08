/*
	
	Copyright 2011 The IMPACT Project
	
	@author Dennis Neumann

	Licensed under the Apache License, Version 2.0 (the "License"); 
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
 
  		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

*/

package eu.impact_project.resultsrepository;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains functionality for dealing with tool processing logs.
 */
public class LogHandler {

	/**
	 * Extracts URLs of input images from logs.
	 * 
	 * @param logs
	 *            Tool processing logs.
	 * @return All extracted URLs.
	 * @throws NoSuchFieldException
	 *             if a log does not contain a URL.
	 * @throws MalformedURLException
	 *             if the found URL is malformed.
	 */
	public static List<URL> getUrls(ToolResults logs)
			throws NoSuchFieldException, MalformedURLException {
		List<URL> urls = new ArrayList<URL>();
		for (String log : logs.getFields()) {
			int offset = 20;
			int indexFrom = log.indexOf("URL of input image: ");
			if (indexFrom < 0) {
				offset = 19;
				indexFrom = log.indexOf("URL of input file: ");
			}
			
			if (indexFrom < 0)
				throw new NoSuchFieldException(
						"No input image URL found in log:\n\n" + log);

			int indexTo = log.indexOf(".\n", indexFrom);
			if (indexTo < indexFrom)
				throw new NoSuchFieldException(
						"Image URL must be bounded by .\\n:\n\n" + log);

			String origUrl = log.substring(indexFrom + offset, indexTo);
			try {
				urls.add(new URL(origUrl));
			} catch (MalformedURLException e) {
				throw new MalformedURLException("URL: " + origUrl);
			}
		}
		return urls;
	}

	/**
	 * Computes the total execution time of a tool.
	 * 
	 * @param logs
	 *            All the logs that a tool returned
	 * @return Overall tool processing time for all images.
	 * @throws NoSuchFieldException
	 *             if a log does not contain the processing time.
	 */
	public static long getTime(List<String> logs) throws NoSuchFieldException {
		long totalTime = 0;
		for (String log : logs) {
			int index = log.indexOf("Process finished successfully after ");
			if (index < 0)
				throw new NoSuchFieldException(
						"Processing time not found in log:\n\n" + log);

			String timeLine = log.substring(index, log.lastIndexOf(" "));
			String time = timeLine.split(" ")[4];
			long timeNumber = 0;
			try {
				timeNumber = Long.valueOf(time);
			} catch (NumberFormatException e) {
				throw new NoSuchFieldException("Invalid time (" + time
						+ ") in log:\n\n" + log);
			}
			totalTime += timeNumber;
		}

		return totalTime;
	}

	/**
	 * Checks if a log contains a URL of the input image.
	 * 
	 * @param log
	 *            the log
	 * @return true, if successful
	 */
	public static boolean hasInputUrl(String log) {
		return log.indexOf("URL of input image: ") >= 0 || log.indexOf("URL of input file: ") >= 0;
	}

	/**
	 * Checks a string is a tool log.
	 * 
	 * @param s
	 *            Arbitrary string.
	 * @return true, if is log
	 */
	public static boolean isLog(String s) {
		return s.indexOf("Using service:") >= 0;
	}

	/**
	 * Splits a specially formatted URL into data parts.
	 * 
	 * @param url
	 *            the url
	 * @return the url parts
	 * @throws MalformedURLException
	 *             if the URL is not formatted the right way.
	 */
	public static UrlParts splitUrl(String url) throws MalformedURLException {
		// provoke exception
		new URL(url);


		// http://domain.org/dir/<servicename>/<evalId>/outputFile/something.<fileextension>
		// evaluation IDs can contain underscores
		String regexWithEvalId = "http.+/(.+)/(.+)/outputFile/.+\\.(.+)$";

		UrlParts parts = new UrlParts();
		if (url.matches(regexWithEvalId)) {
			Matcher matcher = Pattern.compile(regexWithEvalId).matcher(url);
			matcher.find();

			parts.service = matcher.group(1);
			parts.evalId = matcher.group(2);
			parts.extension = matcher.group(3);

		} else {
			throw new MalformedURLException(
					"URL does not match the required pattern. Example: http://domain.org/MyService/timestamp/outputFile/output_tmp.txt");
		}

		return parts;
	}

	/**
	 * Extracts the name of the service encoded in the log.
	 * 
	 * @param log
	 *            the log
	 * @return Service name.
	 * @throws NoSuchFieldException
	 *             if the service name is missing.
	 */
	public static String serviceName(String log) throws NoSuchFieldException {
		String oneLinelog = log.replaceAll("\\s+", " ");

		// ... Using service: MyService. ...
		String regex = ".*Using service: ([^\\.\\s]+)\\..*";
		if (!oneLinelog.matches(regex))
			throw new NoSuchFieldException(
					"The log does not contain the service name or the format is wrong:\n"
							+ log);

		Matcher matcher = Pattern.compile(regex).matcher(oneLinelog);
		matcher.find();

		return matcher.group(1);
	}

	/**
	 * Extracts the evaluation ID which is only contained in evaluation logs.
	 * 
	 * @param log
	 *            the log
	 * @return Evaluation ID or an empty string.
	 */
	public static String evaluationId(String log) {
		String oneLinelog = log.replaceAll("\\s+", " ");

		// ... Evaluation-ID: MyId. ...
		String regex = ".*Evaluation-ID: ([^\\.\\s]+)\\..*";
		if (!oneLinelog.matches(regex))
			return "";

		Matcher matcher = Pattern.compile(regex).matcher(oneLinelog);
		matcher.find();

		return matcher.group(1);

	}

}
