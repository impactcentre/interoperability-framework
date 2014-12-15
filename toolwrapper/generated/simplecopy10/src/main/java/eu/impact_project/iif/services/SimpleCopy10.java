/*
 * Copyright 2014 The IMPACT Project Consortium
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package eu.impact_project.iif.services;

import eu.impact_project.iif.services.proc.CommandLineProcess;
import eu.impact_project.iif.services.util.FileUtils;
import eu.impact_project.iif.services.util.StringUtils;
import eu.impact_project.iif.services.util.DownloaderUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.FileAppender;
import org.apache.log4j.BasicConfigurator;

/**
 * Simple copy version 10 service class.
 *
 * @author IMPACT Project Consortium
 * @version 0.7
 */
public class SimpleCopy10 {
    /**
     * Static mime types / file extentions.
     */

    HashMap mimeTypeExtensions;
    /**
     * Static logger variable.
     */
    private static Logger logger = Logger.getLogger(SimpleCopy10.class.getName());

    /*
     * These variables can be mapped to an output port using the OutputMapping
     * property in the output configuration file (data types must be the same).
     *
     */
    private boolean processing_success;
    private int processing_returncode;
    private String processing_message;
    private String processing_log;
    private int processing_time;
    private String processing_unitid;

    {
        processing_success = false;
        processing_returncode = -1;
        processing_message = "";
        processing_log = "";
        processing_time = 0;
        processing_unitid = "http://null";
    }

    /**
     * Apply process to the input object.
     * @param inImgFile Input image file.
     * @throws IOException
     */
    private boolean process(HashMap<String, String> cliCmdKeyValPairs, int opid, String outputstream) {

	mimeTypeExtensions = new HashMap<String,String>();
        mimeTypeExtensions.put("text/plain", "txt");
        mimeTypeExtensions.put("image/tiff", "tif");
        mimeTypeExtensions.put("image/png", "png");
        mimeTypeExtensions.put("image/jpeg", "jpg");
        mimeTypeExtensions.put("application/xml", "xml");

        // Assigning values to the variables used in the command pattern.
        // If additional variables are required, they must be added in the
        // eu.impact_project.iif.tw.service.CommandPatternVariables class.
        String cliCmdPattern = getValueOfServiceParameter("cliCommand" + opid);

        // Command line process
        CommandLineProcess clp =
                new CommandLineProcess(cliCmdPattern, cliCmdKeyValPairs, false, outputstream);
        try {
            clp.init();
        } catch (IOException ex) {
            processing_message = "I/O Exception. " + ex.getMessage();
            processing_success = false;
            return false;
        }
        clp.execute();
        processing_success = (clp.getCode() == 0);
        processing_returncode = clp.getCode();
        processing_log += clp.getProcessingLog();

        // The return codes of the tool should be documented here by
        // creating a case for each code and assigning the corresponding
        // message.
        switch (processing_returncode) {
            case 0:
                processing_message = "Process finished successfully with code 0";
                infolog(processing_message);
                break;
            case -1:
                processing_message = "Process result is undefined with code -1";
                errorlog(processing_message);
                break;
            default:
                processing_message = "Process failed with error code " + processing_returncode + ". ";
                errorlog(processing_message);
                break;
        }

        return processing_success;
    }

    /**
     * Service operation
     * @param inputUrl Input url
     * @return Response message subtree
     */
    public OMElement simpleCopy(String inputUrl) {

        infolog("========= PROCESSING REQUEST =========");
	infolog("Using toolwrapper version 0.7.0");
        infolog("Using service: IMPACTSimpleCopy10Service");

	infolog("Using outputdir: " + "" );

        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        msgCtx.setProperty("ConfigContextTimeoutInterval", "300000");

	String bits[];
	String fileName;
	String outputdir = "";
	Path path = null;

	Boolean is_inputdir = false;
	Boolean input_is_url = true;
	String type;

	infolog("No evaluation-id");

        String processingUnit = this.getValueOfServiceParameter("processingUnit");
        if (processingUnit != null && !processingUnit.isEmpty()) {
            infolog("Parameter processingUnit from services.xml: "+processingUnit);
            processing_unitid = processingUnit;
        }

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(
                "http://impact-project.eu/iif/services", "tns");

        // Required for copying output files to a public web server directory
        String publicHttpAccessDir = getValueOfServiceParameter("publicHttpAccessDir");

	// Check if the public web server directory exists
        if (!new File(publicHttpAccessDir).isDirectory()) {
            errorlog("The output directory " + publicHttpAccessDir + " does not exist. Check "
                    + "publicHttpAccessDir parameter in the service configuration");
            return simpleCopyResult(fac, omNs);
        }

	// Check if the public output (file)dir exists, otherwise create it
	String timeStamp = Long.toString(System.currentTimeMillis() / 1000L);

	String publicOutputBaseFileDir = publicHttpAccessDir + 
					"IMPACTSimpleCopy10Service" + File.separator +
					 File.separator +
					timeStamp + File.separator;

	// Check if the public output (log)dir exists, otherwise create it
	String publicOutputLogDir = publicHttpAccessDir + 
			    "IMPACTSimpleCopy10Service" + File.separator +
			     File.separator +
			    timeStamp + File.separator + 
			    "processLog" + File.separator;
	if (!new File(publicOutputLogDir).isDirectory()) {
	    File publicOutputDirLog = new File(publicOutputLogDir);
	    try {
		if (publicOutputDirLog.mkdirs()) {
		    infolog("Created directory: " + publicOutputLogDir);
		} else {
		    errorlog("Could not create the public (log)output directory " + publicOutputLogDir);
		    return simpleCopyResult(fac, omNs);
		}
	    } catch(Exception e){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		errorlog(sw.toString());
		return simpleCopyResult(fac, omNs);
	    }
	}

	try {
	        logger.addAppender(new FileAppender(new SimpleLayout(), publicOutputLogDir + "result.log"));
	} catch (Exception e) {
	        e.printStackTrace();
	}

        // Required for providing access to output files in a public web server directory
        String publicHttpAccessUrl = getValueOfServiceParameter("publicHttpAccessUrl");
	publicHttpAccessUrl = publicHttpAccessUrl + 
			      "IMPACTSimpleCopy10Service" + "/" +  "/" +
			      timeStamp + "/";

        // Command pattern variables key value pairs
        HashMap cliCmdKeyValPairs = new HashMap<String, String>();


    // Input: inputUrl
    File inputUrlFile = null;
    String inputUrlExt = "tmp";
    String inputUrlFileName = "";
    URL inputUrlUrl = null;

    try {
        // Input is a well-formated URL
        inputUrlUrl = new URL(inputUrl);
        String inputUrlUrlStr = inputUrlUrl.toString();
        if(!inputUrlUrlStr.matches(getValueOfServiceParameter("serviceUrlFilter"))) {
            errorlog("Format of input URL \"" + inputUrl + "\" does not comply with security policy");
            return simpleCopyResult(fac, omNs);
        }
        infolog("URL of input file: " + inputUrlUrlStr);
    } catch (MalformedURLException ex) {
        // If the input is not an URL, it might be a file on the filesystem..
        inputUrlFileName = inputUrl;
        path = Paths.get(inputUrlFileName);
        if (Files.notExists(path)) {
            errorlog("\"" + inputUrl + "\" is not a valid value for the "
                            + "parameter \"inputUrlUrl\"");
            return simpleCopyResult(fac, omNs);
        }
        input_is_url = false;
    }

	if (input_is_url) {
	    // Determine the file name by taking the last part of the URL (and strip the extension)
	    bits = inputUrlUrl.toString().split("/");
	    fileName = bits[bits.length - 1];

	    if(fileName.contains(".")) {
	       bits = fileName.split("\\.");
	       fileName = bits[0];
	    };

	    if (is_inputdir) {
		    inputUrlExt = "tmp";
	    } else {
            inputUrlExt = StringUtils.getFileExtension(inputUrlUrl.toString());
            if (inputUrlExt == null) {
                    inputUrlExt = "tmp";
		    }
	    }
	} else {
        input_is_url = false;
        bits = inputUrlFileName.split(File.separator);
        fileName = bits[bits.length - 1];

        if (fileName.contains(".")) {
            bits = fileName.split("\\.");
            fileName = bits[0];
        };

	    // Try to determine the file extention by
	    // probing the file's content.
	    try {
            type = Files.probeContentType(path);
            if(mimeTypeExtensions.containsKey(type)) {
		        inputUrlExt = (String) mimeTypeExtensions.get(type);
            } else {
		        inputUrlExt = "tmp";
	        }
	    } catch(IOException e) {
		    type = "";
	    }
	}

    try {
        if (input_is_url) {
            if (is_inputdir) { 
                inputUrlFile = DownloaderUtils.mirror(inputUrlUrl.toString());
            } else {
                inputUrlFile = FileUtils.urlToFile(inputUrlUrl, inputUrlExt);
            }
        } else {
            inputUrlFile = new File(inputUrlFileName);
        }

        if (!inputUrlFile.exists()) {
            errorlog("Input file " + inputUrlFile.toString() + " is not available");
            return simpleCopyResult(fac, omNs);
        }
        infolog("Input file created: " + inputUrlFile.getAbsolutePath());

        // Mapping to command line pattern variable
        cliCmdKeyValPairs.put("input", inputUrlFile.getAbsolutePath());
    } catch(IOException ex) {
        errorlog("Unable to read from URL \"" + inputUrlUrl + "\"");
        return simpleCopyResult(fac, omNs);
    }


        // definition of output file name(s)
        String outputPrefix = FileUtils.getFileNameFromUrl(inputUrl);
        String outputFileName = FileUtils.getTmpFile(outputPrefix+"_IMPACTSimpleCopy10Service_outputFile_", "txt").getAbsolutePath();
        cliCmdKeyValPairs.put("output", outputFileName);

        // Command execution
        long startMillis = System.currentTimeMillis();
        process(cliCmdKeyValPairs, 1, "");
        long timeMillis = System.currentTimeMillis() - startMillis;
        processing_time = (int) timeMillis;

    // Output: output
    File outputFile = new File(outputFileName);
    boolean isCreatedoutput = false;

    if (!outputFile.exists() || outputFile.length() == 0) {
        infolog("The expected output file " + outputFile.getName() + " is empty or has not been created");
    } else {
        infolog("Output file of size " + outputFile.length() + " has been created successfully");
        isCreatedoutput = true;
    }

    // path to the public output directory
    String outputpublicOutputFileDir = publicOutputBaseFileDir + "output" + File.separator;

    if (!new File(outputpublicOutputFileDir).isDirectory()) {
        File publicOutputDirFile = new File(outputpublicOutputFileDir);
        try {
            if (publicOutputDirFile.mkdirs()) {
                infolog("Created directory: " + outputpublicOutputFileDir);
            } else {
                errorlog("Could not create the public (file)output directory " + outputpublicOutputFileDir);
                return simpleCopyResult(fac, omNs);
            }
        } catch(Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            e.printStackTrace(pw);
            errorlog(sw.toString());

            return simpleCopyResult(fac, omNs);
        }
    }

    // url pointer to the public output url
    // this url will end up as the result url
    URL outputFileUrl = null;

    // file pointer to the new file created
    File outputPublicFile;

    // this will override the default filename
    String outputOutFileName = "";

    if (outputdir.equals("")) {
        String outputextension = outputFile.getName().split("\\.")[1];

        if (outputOutFileName.equals("")) {
            outputPublicFile = new File(outputpublicOutputFileDir + fileName + "." + outputextension);
        } else {
            outputPublicFile = new File(outputpublicOutputFileDir + outputOutFileName);
        }
        infolog("Public output file: " + outputPublicFile.toString());
        try {
            if (isCreatedoutput) {
                org.apache.commons.io.FileUtils.copyFile(outputFile, outputPublicFile);
            }
        } catch (IOException _) {
            infolog("No outputfile created in the public access directory for 'output'");
        }

        if( "true" == "true" )
        {
            try {
                outputFile.delete();
                infolog("Removing temp file " + outputFile.toString());
            } catch (SecurityException _) {
                infolog("Error while removing file " + outputFile.toString());
            }
        }
        

        try {
            if (isCreatedoutput) {
                if (outputOutFileName.equals("")) {
                    outputFileUrl = new URL(publicHttpAccessUrl + "output" + "/" + fileName + "." + outputextension);
                } else {
                    outputFileUrl = new URL(publicHttpAccessUrl + "output" + "/" + outputOutFileName);
                }
            } else {
                outputFileUrl = new URL("http://null");
            }
        } catch (MalformedURLException ex) {
            errorlog("Malformed URL for binary result resource. Verify"
                + "publicHttpAccessUrl parameter in the "
                + "resources/services.xml. Exception message: "
                + ex.getMessage());
            return simpleCopyResult(fac, omNs);
        }

        if (isCreatedoutput) {
            infolog("Output URL: " + outputFileUrl.toString());
        }

    } else {
        if (outputOutFileName.equals("")) {
                outputPublicFile = new File(outputpublicOutputFileDir);
        } else {
                outputPublicFile = new File(outputpublicOutputFileDir + outputOutFileName);
        }

        try {
            org.apache.commons.io.FileUtils.copyDirectory(outputFile, outputPublicFile);
        } catch (IOException _) {
            infolog("No outputfile created in the public access directory for 'output'");
        }

        try {
            infolog("Removing " + outputFile.toString());
            org.apache.commons.io.FileUtils.deleteDirectory(outputFile);
        } catch (IOException _) {
            infolog("Unable to remove tmp dir " + outputFile.toString());
        }

        try {
            outputFileUrl = new URL(publicHttpAccessUrl + "outputdir");
        } catch (MalformedURLException ex) {
            return simpleCopyResult(fac, omNs);
        }

        infolog("Output URL: " + outputFileUrl.toString());
    }


        if(!processing_success) {
            errorlog("An error occurred while executing the process");
            return simpleCopyResult(fac, omNs);
        }

        // success
        processing_success = true;
        processing_returncode = 0;
        infolog("Process finished successfully after "
                    + timeMillis + " milliseconds.");
        infolog("Service request has been processed successfully");
        OMElement result = simpleCopyResult(fac, omNs);
        OMElement outputElm = fac.createOMElement("output", omNs);
        outputElm.setText(String.valueOf(outputFileUrl.toString()));
        result.addChild(outputElm);
        return result;
    }

    /**
     *
     * @param fac OMElement factory
     * @param omNs OMNamespace Namespace of the elements created
     * @return Subtree of the result message
     */
    OMElement simpleCopyResult(OMFactory fac, OMNamespace omNs) {
        OMElement result = fac.createOMElement("result", omNs);
        // processing_success
        OMElement successElm = fac.createOMElement("success", omNs);
        successElm.setText(String.valueOf(processing_success));
        result.addChild(successElm);
        // processing_returncode
        OMElement returncodeElm = fac.createOMElement("returncode", omNs);
        returncodeElm.setText(String.valueOf(processing_returncode));
        result.addChild(returncodeElm);
        // processing_time
        OMElement proctimeElm = fac.createOMElement("time", omNs);
        proctimeElm.setText(String.valueOf(processing_time));
        result.addChild(proctimeElm);
        // processing_log
        OMElement processingLogElm = fac.createOMElement("log", omNs);
        processingLogElm.setText(String.valueOf(processing_log));
        result.addChild(processingLogElm);
        // processing_message
        OMElement processingMsgElm = fac.createOMElement("message", omNs);
        processingMsgElm.setText(String.valueOf(processing_message));
        result.addChild(processingMsgElm);
        return result;
    }


    /**
     * Get the value of a service parameter defined in the
     * resources/services.xml.
     * @param parm Parameter defined in the services.xml
     * @return Value of the parameter
     */
    private String getValueOfServiceParameter(String parm) {
        MessageContext msgCtx = MessageContext.getCurrentMessageContext();
        Parameter patternParameter = msgCtx.getParameter(parm);
        String ptn = (String) patternParameter.getValue();
        return ptn;
    }

    /**
     * Informative log
     * @param msg Message
     */
    private void infolog(String msg) {
        processing_log += msg + ".\n";
        processing_message = msg;
        logger.info(msg);
    }

    /**
     * Error log
     * @param msg Message
     */
    private void errorlog(String msg) {
        if(processing_returncode == 0)
            processing_returncode = 1;
        processing_success = false;
        logger.info("Service assigned error code 1");
        processing_log += "ERROR: " + msg + ".\n";
        processing_message = "Service request processing ended with an error";
        logger.error(msg);
    }
}
