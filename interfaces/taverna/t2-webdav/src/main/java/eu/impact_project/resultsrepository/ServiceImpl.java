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

import static eu.impact_project.resultsrepository.XmlHandler.getElementContent;
import static eu.impact_project.resultsrepository.XmlHandler.getLayoutEvaluations;
import static eu.impact_project.resultsrepository.XmlHandler.getWordEvaluations;
import static eu.impact_project.resultsrepository.XmlHandler.isEvaluation;
import static eu.impact_project.resultsrepository.XmlHandler.isWordEvaluation;
import static eu.impact_project.resultsrepository.LogHandler.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jdom.JDOMException;

import eu.impact_project.resultsrepository.report.AnyTool;
import eu.impact_project.resultsrepository.report.LayoutEvalTool;
import eu.impact_project.resultsrepository.report.OcrEvalTool;
import eu.impact_project.resultsrepository.report.Report;

/**
 * Implementation that stores Taverna workflow results into a WebDAV repository.
 */
public class ServiceImpl implements Service {

	/**
	 * Workflow IDs may start with this prefix. In that case, it is removed
	 * before the ID is used as a Webdav folder name.
	 */
	private final String wfIdPrefix = "http://www.myexperiment.org/workflows/";

	/** Handles all the storage operations. */
	private ThreadLocal<DavHandler> dav = new ThreadLocal<DavHandler>();

	/** Total number of processed images. */
	private ThreadLocal<Double> imageCount = new ThreadLocal<Double>();

	/** Is used for logging. */
	private ThreadLocal<Writer> writer = new ThreadLocal<Writer>();

	private ThreadLocal<Logger> logger = new ThreadLocal<Logger>();

	/**
	 * Is true if there were errors that should be communicated to the service
	 * caller.
	 */
	private ThreadLocal<Boolean> hasErrors = new ThreadLocal<Boolean>();

	/**
	 * Container class for a simpler exchange of the input arguments.
	 */
	private class Inputs {

		public Inputs(Results wfResults, String wfId, long timer, String demId, long executionTime) {
			this.wfResults = wfResults;
			this.wfId = wfId;
			this.timer = timer;
			this.demId = demId;
			this.executionTime = executionTime;
		}

		public Results wfResults;
		public String wfId;
		public long timer;
		public String demId;
		public long executionTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.impact_project.resultsrepository.Service#storeData(eu.impact_project
	 * .resultsrepository.Results, java.lang.String, long, java.lang.String)
	 */
	@Override
	public String storeData(Results workflowResults, String wfId, long timer,
			String demId) {

		try {

			Inputs in = new Inputs(workflowResults, wfId, timer, demId, 0l);
			checkInputs(in);
			init(in);

			// is computed as soon as possible, because the storage time is not
			// part of the overall workflow processing time
			long workflowProcessingTime = getCurrentTimestamp() - timer;
			in.executionTime = workflowProcessingTime;

			Results fileLists = new Results();
			Results logLists = new Results();
			Results xmlEvalLists = new Results();
			Results layoutEvalLists = new Results();
			Results wordEvalLists = new Results();

			splitResults(in, fileLists, logLists, xmlEvalLists,
					layoutEvalLists, wordEvalLists);
			saveFiles(fileLists);
			saveToolLogs(logLists);
			saveReport(in, logLists, xmlEvalLists, layoutEvalLists,
					wordEvalLists);

			saveOwnLog();

		} catch (RuntimeException e) {
			clearThreadLocals();
			return "An error occurred:" + formatException(e);
		}
		String status = getStatusMessage();
		clearThreadLocals();
		return status;
	}

	/**
	 * Validates that the input arguments are correct.
	 */
	private void checkInputs(Inputs in) {
		String regexDemId = "[0-9a-zA-Z_-]+";
		// the workflow id may be a URL
		String regexWfId = "[:/?=0-9a-zA-Z_-]+";

		if (in.wfResults == null
				|| in.wfResults.getToolResultsList().size() < 1) {
			throw new RuntimeException("There were no results to be stored.");
		}
		if (in.wfId == null || !in.wfId.matches(regexWfId) || in.demId == null
				|| !in.demId.matches(regexDemId)) {
			throw new RuntimeException(
					"Invalid workflow ID or demonstrator ID.");
		}
	}

	/**
	 * Prepares the service for storing.
	 */
	private void init(Inputs in) {
		logger.set(Logger.getLogger(ServiceImpl.class + "."
				+ Thread.currentThread().getId()));
		writer.set(new StringWriter());
		hasErrors.set(false);
		imageCount.set(0d);

		// logger will write into a string which will be stored manually in a
		// file
		configureLogger();
		prepareInputs(in);

		logger.get().info("Workflow ID is: " + in.wfId);
		logger.get().info("Demonstrator ID is: " + in.demId);

		List<String> folderHierarchy = createFolderList(in);
		try {
			dav.set(new DavHandler(folderHierarchy));
		} catch (SSLHandshakeException e) {
			throw new RuntimeException(
					"You must import the SSL certificate for Results Repository using 'keytool'."
							+ formatException(e));
		} catch (IOException e2) {
			throw new RuntimeException("Problem while accessing repository."
					+ formatException(e2));
		}
		String folders = "";
		for (String folder : folderHierarchy) {
			folders += "/" + folder;
		}
		logger.get().info("Created folders in Repository: " + folders);

	}

	private void configureLogger() {
		Layout layout = new PatternLayout("%-5p %d %C.%M(%F:%L)%n        %m%n");
		WriterAppender appender = new WriterAppender(layout, writer.get());
		logger.get().addAppender(appender);
		// logger.setLevel(Level.INFO);
	}

	private void prepareInputs(Inputs in) {
		if (in.wfId.startsWith(wfIdPrefix)) {
			in.wfId = in.wfId.substring(wfIdPrefix.length());
		}
		in.wfId = in.wfId.replace("/", "~");
		in.wfId = in.wfId.replace("?", "*");
	}

	/**
	 * The list will be used to create folders on Webdav.
	 * 
	 * @return List containing folder names.
	 */
	private List<String> createFolderList(Inputs in) {
		List<String> folderHierarchy = new ArrayList<String>();

		folderHierarchy.add(in.demId);
		folderHierarchy.add(in.wfId);	

		String timestamp = "" + System.currentTimeMillis();
		folderHierarchy.add(timestamp);

		return folderHierarchy;
	}

	private long getCurrentTimestamp() {
		long stamp = 0l;
		try {
			Properties properties = new Properties();
			URL url = getClass().getResource("/report.properties");
			if (url == null)
				throw new IOException(
						"Property file for report generation not found.");
			InputStream is = url.openStream();
			properties.load(is);
			is.close();

			URL stampUrl = new URL(properties.getProperty("timestampUrl"));

			InputStream stampStream = stampUrl.openStream();
			String stampString = IOUtils.toString(stampStream);
			stamp = Long.parseLong(stampString);
		} catch (IOException e) {
			error("Could not compute the workflow execution time. ", e);
		}

		return stamp;
	}

	/**
	 * Iterates over all the results and sorts them into different lists. Values
	 * with an unknown format are ignored.
	 */
	private void splitResults(Inputs in, Results fileLists, Results logLists,
			Results xmlEvalLists, Results layoutEvalLists, Results wordEvalLists) {
		for (ToolResults resultList : in.wfResults.getToolResultsList()) {
			String sample = resultList.getFields().get(0);
			if (sample.equals("")) {
				error("One result value was empty. Some files might not have been stored.");
				continue;
			}
			boolean isUrlList = sample.startsWith("http");
			Set<String> endings = dav.get().getFileEndings();
			String currentEnding = sample
					.substring(sample.lastIndexOf(".") + 1);
			boolean isFileList = isUrlList && endings.contains(currentEnding);
			boolean isXmlEvalList = false;
			boolean isWordEvalList = false;
			try {
				isXmlEvalList = isUrlList && sample.endsWith(".xml")
						&& isEvaluation(dav.get().retrieveTextFile(sample));
				isWordEvalList = isUrlList && sample.endsWith(".xml")
						&& isWordEvaluation(dav.get().retrieveTextFile(sample));
			} catch (IOException e) {
				warn("Could not read file: " + sample);
			}

			boolean isLayoutEvalList = isUrlList && sample.endsWith(".evx");
			boolean isLogList = isLog(sample);
			if (isXmlEvalList) {
				xmlEvalLists.addToolResults(resultList);
			}
			if (isLayoutEvalList) {
				layoutEvalLists.addToolResults(resultList);
			}
			if (isWordEvalList) {
				wordEvalLists.addToolResults(resultList);
			}
			if (isFileList) {
				fileLists.addToolResults(resultList);
			} else if (isLogList) {
				logLists.addToolResults(resultList);
			} else {
				warn("Could not process some results. Sample value was neither a correctly formatted file URL, nor a log: "
						+ sample);
			}
		}
	}

	/**
	 * Saves the referenced files into the repository.
	 */
	private void saveFiles(Results fileLists) {
		if (fileLists.notEmpty()) {
			logger.get().info("Saving result files");

			for (ToolResults urls : fileLists.getToolResultsList()) {
				// one sample is enough, because all have the same urlParts
				// which are relevant here
				String sample = urls.getFields().get(0);
				UrlParts urlParts = new UrlParts();
				try {
					urlParts = splitUrl(sample);
					dav.get().saveFiles(urlParts, urls);
				} catch (MalformedURLException e1) {
					warn("URL format wrong, skipping: " + sample, e1);
				} catch (SSLHandshakeException e1) {
					warn("SSL certificate missing.", e1);
				} catch (IOException e) {
					warn("Some files could not be saved. The file set is "
							+ urlParts.service + "," + urlParts.port + ","
							+ urlParts.evalId + "," + urlParts.extension, e);
				}

				// assuming all lists have the same length
				if (imageCount.get() == 0)
					imageCount.set((double) urls.getFields().size());

			}
		}
	}

	/**
	 * Saves the tool processing logs into the repository, each one as a file.
	 */
	private void saveToolLogs(Results logLists) {
		if (logLists.notEmpty()) {
			logger.get().info("Saving tool logs");
			for (ToolResults logs : logLists.getToolResultsList()) {
				String sample = logs.getFields().get(0);
				try {
					dav.get().saveLogs(serviceName(sample),
							evaluationId(sample), logs.getFields());
				} catch (IOException e) {
					error("Problem while saving logs.", e);
				} catch (NoSuchFieldException e) {
					warn("Problem parsing log.", e);
				}
			}
		}
	}

	/**
	 * Generates the Excel report and stores it into the repository.
	 */
	private void saveReport(Inputs in, Results logLists, Results xmlEvalLists,
			Results layoutEvalLists, Results wordEvalLists) {
		List<AnyTool> tools = generateTools(logLists);
		List<OcrEvalTool> evalTools = generateEvalTools(xmlEvalLists,
				wordEvalLists);
		List<LayoutEvalTool> layoutEvalTools = generateLayoutEvalTools(layoutEvalLists);

		try {
			Report report = null;
			logger.get().info("Generating report");
			report = new Report(in.wfId, in.executionTime, in.demId, imageCount.get());
			report.setTools(tools);
			report.setOcrEvalTools(evalTools);
			report.setLayoutEvalTools(layoutEvalTools);
			logger.get().info("Saving report");
			InputStream excelStream = report.asExcel();
			String baseUrl = dav.get().getFolderHierarchyUrl();
			dav.get().saveStream(excelStream, baseUrl + "/report.xls");
		} catch (SSLHandshakeException e) {
			error("Problem reading the report template. You must import the SSL certificate using 'keytool'.",
					e);
		} catch (IOException e) {
			error("Problem generating report.", e);
		} catch (InvalidFormatException e) {
			error("Problem saving report.", e);
		}
	}

	/**
	 * Generates tool objects containing information about each processed tool.
	 * 
	 * @return Tool list.
	 */
	private List<AnyTool> generateTools(Results logLists) {
		List<AnyTool> tools = new ArrayList<AnyTool>();

		for (ToolResults logs : logLists.getToolResultsList()) {
			String service = "";
			long time = 0;
			try {
				service = serviceName(logs.getFields().get(0));
				time = getTime(logs.getFields());
			} catch (NoSuchFieldException e2) {
				warn("Problem parsing log, skipping.", e2);
				continue;
			}
			List<URL> imageUrls = new ArrayList<URL>();

			if (hasInputUrl(logs.getFields().get(0))) {
				try {
					imageUrls = getUrls(logs);
				} catch (MalformedURLException e) {
					warn("Original image URL not found in log.", e);
				} catch (NoSuchFieldException e) {
					warn("Original image URL not found in log.", e);
				}
			}

			AnyTool tool = new AnyTool(service, imageUrls, time);
			tools.add(tool);
		}
		return tools;
	}

	/**
	 * Generates tool objects containing information about each processed
	 * evaluation tool.
	 * 
	 * @return Tool list.
	 */
	private List<OcrEvalTool> generateEvalTools(Results xmlEvalLists,
			Results wordEvalLists) {
		List<OcrEvalTool> evalTools = new ArrayList<OcrEvalTool>();

		for (ToolResults wordEvals : wordEvalLists.getToolResultsList()) {

			String sample = wordEvals.getFields().get(0);
			UrlParts urlParts = new UrlParts();
			try {
				urlParts = splitUrl(sample);
			} catch (MalformedURLException e1) {
				error("Could not process word evaluation URL, skipping: "
						+ sample, e1);
				continue;
			}

			String service = urlParts.service;
			String evalId = urlParts.evalId;

			OcrEvalTool tool = new OcrEvalTool(service, evalId);

			for (String url : wordEvals.getFields()) {
				if (url != null && !url.equals("")) {
					String[] evalArray = {};
					try {
						evalArray = getWordEvaluations(url);
						String words = evalArray[0];
						String misrecognzed = evalArray[1];
						String wordAccuracy = toPercent(evalArray[2]);
						tool.addEvaluation("", "", "", words, misrecognzed,
								wordAccuracy);
					} catch (JDOMException e) {
						error("Could not process word evaluation URL: " + url,
								e);
					} catch (IOException e) {
						error("Could not process word evaluation URL: " + url,
								e);
					}
				} else {
					error("Could not process a word evaluation file. The sample of the group was: "
							+ sample, new Exception());
				}

			}
			evalTools.add(tool);

		}

		for (ToolResults evals : xmlEvalLists.getToolResultsList()) {

			String sample = evals.getFields().get(0);
			UrlParts urlParts = new UrlParts();
			try {
				urlParts = splitUrl(sample);
			} catch (MalformedURLException e1) {
				error("Could not process OCR evaluation URL, skipping: "
						+ sample, e1);
				continue;
			}

			String service = urlParts.service;
			String evalId = urlParts.evalId;

			OcrEvalTool tool = new OcrEvalTool(service, evalId);

			for (String url : evals.getFields()) {
				if (url != null && !url.equals("")) {
					String xml = null;
					try {
						xml = dav.get().retrieveTextFile(url);
						String characters = getElementContent(xml, "Characters");
						String errors = getElementContent(xml, "Errors");
						String accuracy = getElementContent(xml, "Accuracy");
						String words = getElementContent(xml, "Words");
						String misrecognized = getElementContent(xml,
								"Misrecognized");
						String wordAccuracy = getElementContent(xml,
								"WordAccuracy");

						tool.addEvaluation(characters, errors, accuracy, words,
								misrecognized, wordAccuracy);
					} catch (IOException e) {
						error("Could not process evaluation file: " + url, e);
					} catch (NoSuchFieldException e) {
						error("Could not process evaluation file: " + url, e);
					}
				} else {
					error("Could not process an evaluation file. The sample of the group was: "
							+ sample, new Exception());
				}
			}
			evalTools.add(tool);
		}
		return evalTools;
	}

	private String toPercent(String decimal) {
		Float percent = Float.parseFloat(decimal) * 100;
		// String beforePoint = percent.intValue() + "";
		//
		// String percentString = percent.toString();
		// int pointIndex = percentString.indexOf(".");
		// String afterPoint = percentString.substring(pointIndex+1,
		// pointIndex+3);

		return percent.toString();
	}

	/**
	 * Generates tool objects containing information about each processed layout
	 * evaluation tool.
	 * 
	 * @return Tool list.
	 */
	private List<LayoutEvalTool> generateLayoutEvalTools(Results layoutEvalLists) {
		List<LayoutEvalTool> tools = new ArrayList<LayoutEvalTool>();
		for (ToolResults evals : layoutEvalLists.getToolResultsList()) {
			String sample = evals.getFields().get(0);
			UrlParts urlParts = new UrlParts();
			try {
				urlParts = splitUrl(sample);
			} catch (MalformedURLException e) {
				error("Could not process layout evaluation URL, skipping: "
						+ sample, e);
				continue;
			}
			String service = urlParts.service;
			String evalId = urlParts.evalId;

			LayoutEvalTool tool = new LayoutEvalTool(service, evalId);
			for (String url : evals.getFields()) {
				if (url != null && !url.equals("")) {
					String[] evalArray = {};
					try {
						evalArray = getLayoutEvaluations(url);
						String area = evalArray[0];
						String count = evalArray[1];
						tool.addEvaluation(area, count);
					} catch (JDOMException e) {
						error("Could not process layout evaluation URL: " + url,
								e);
					} catch (IOException e) {
						error("Could not process layout evaluation URL: " + url,
								e);
					}
				} else {
					error("Could not process a layout evaluation file. The sample of the group was: "
							+ sample, new Exception());
				}
			}
			tools.add(tool);
		}
		return tools;
	}

	/**
	 * Saves the logged messages into a file in the repository.
	 */
	private void saveOwnLog() {
		try {
			dav.get().saveText(writer.get().toString(),
					dav.get().getFolderHierarchyUrl() + "/log.txt");
		} catch (IOException e) {
			throw new RuntimeException(
					"Error while saving the log file. The results might still have been stored at "
							+ dav.get().getFolderHierarchyUrl()
							+ formatException(e));
		}
	}

	/**
	 * Constructs the message that is returned to the user of this service.
	 * 
	 * @return Status message.
	 */
	private String getStatusMessage() {
		String htmlPrefix = "<html><head></head><body>";

		String repoUrl = dav.get().getFolderHierarchyUrl();
		// https link is not clickable in Taverna 2.2
		if (repoUrl.startsWith("https://")) {
			repoUrl = "http://" + repoUrl.substring(8);
		}
		String repoLink = "Stored results at <a href='" + repoUrl
				+ "'>repository</a>";
		String errorLink = "";
		if (hasErrors.get()) {
			errorLink = "<br>There were warnings or errors during execution, see <a href='";
			errorLink += repoUrl;
			errorLink += "/log.txt'>Log file</a>";
		}

		String htmlPostfix = "</body></html>";

		return htmlPrefix + repoLink + errorLink + htmlPostfix;

	}

	/**
	 * Formats the given exception so that it can be returned to the service
	 * user instead of or along with the status message.
	 * 
	 */
	private String formatException(Exception e) {
		String s = "\n" + e.toString() + "\n        ";

		for (StackTraceElement elem : e.getStackTrace()) {
			s += elem + "\n        ";
		}
		return s;
	}

	private void warn(String message, Throwable exception) {
		hasErrors.set(true);
		logger.get().warn(message, exception);
	}

	private void warn(String message) {
		hasErrors.set(true);
		logger.get().warn(message);
	}

	private void error(String message, Throwable exception) {
		hasErrors.set(true);
		logger.get().error(message, exception);
	}

	private void error(String message) {
		hasErrors.set(true);
		logger.get().error(message);
	}

	private void clearThreadLocals() {
		Logger l = logger.get();
		if (l != null)
			l.removeAllAppenders(); // or else we get a huge memory leak
		logger.remove();
		writer.remove();
		dav.remove();
		hasErrors.remove();
		imageCount.remove();
	}

}
