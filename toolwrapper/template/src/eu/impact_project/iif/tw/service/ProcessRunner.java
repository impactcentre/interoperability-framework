/*
 *  This class is based on the PLANETS class
 *  eu.planets_project.services.utils.ProcessRunner
 *
 *  Copyright (C) 2005-2008 The State and University Library
 *  Added to the Planets Project by the State and University Library
 *  Author Asger Blekinge-Rasmussen
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package #GLOBAL_PACKAGE_NAME#.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * ProcessRunner class.
 * <p>Native command executor. Based on ProcessBuilder.
 * <ul>
 * <li> Incorporates timeout for spawned processes.
 * <li> Handle automatic collection of bytes from the output and
 * error streams, to ensure that they dont block.
 * <li> Handles automatic feeding of input to the process.
 * <li> Blocking while executing
 * <li> Implements Runnable, to be wrapped in a Thread.
 * </ul>
 * </p>
 *
 * Use the Assessor methods to configure the Enviroment, input, collecting
 * behavoiur, timeout and startingDir.
 * Use the getters to get the output and error streams as strings, along with
 * the return code and if the process timed out.
 *
 * <p> This code is not yet entirely thread safe. Be sure to only call a given
 * processRunner from one thread, and do not reuse it. </p>
 *
 * This class is based on the PLANETS class
 * eu.planets_project.services.utils.ProcessRunner
 *
 * @author Asger Blekinge-Rasmussen
 * @version #GLOBAL_WRAPPER_VERSION#
 */
public class ProcessRunner implements Runnable {

    /** Maximum number of bytes to be collected from std out */
    public static final int MAX_BYTES_OUT = 2000000;
    /** Time to wait */
    public static final int WAIT = 100;
    //public static final long MAX_EXECUTION_TIME = (5 * 60 * 1000);
    public static final int MAX_WAIT_MILLIS = 1000;
    private static Logger logger = Logger.getLogger(ProcessRunner.class.getName());
    /** Based on ProcessBuilder which actually takes the command array */
    private final ProcessBuilder pb;
    /** The list of threads the process controller owns */
    private final List<Thread> threads =
            Collections.synchronizedList(new LinkedList<Thread>());
    /** Input stream to submit further input to the command line process */
    private InputStream processInputStream = null;
    /** Standard output stream of the process */
    private InputStream standardInputStream = null;
    /** Error stream of the process */
    private InputStream errorInputStream = null;

    private String message = "";
    /** Return code of the process */
    private int code; // -1 means "undefined", 0 success, >0 error

    {
        code = -1; // -1 means "undefined", 0 success, >0 error
    }

    /**
     * Constructor which initialises the process builder
     */
    public ProcessRunner() {
        pb = new ProcessBuilder();
    }

    /**
     * Constructor which takes the command list
     * @param commands
     */
    public ProcessRunner(List<String> commands) {
        this();
        setCommandList(commands);
    }

    /**
     * Set the command list that will be passed to the process builder
     * @param commands Command list
     */
    public void setCommandList(List<String> commands) {
        pb.command(commands);
    }

    /**
     * Set process input stream
     * @param processInputStream Process input stream
     */
    public synchronized void setProcessInputStream(InputStream processInputStream) {
        this.processInputStream = processInputStream;
    }

    /**
     * Get input stream of the process
     * @return Input stream
     */
    public InputStream getStdInputStream() {
        return standardInputStream;
    }

    /**
     * Get error stream of the process
     * @return Error stream
     */
    public InputStream getErrInputStream() {
        return errorInputStream;
    }

    /**
     * Get process return code
     * @return Return code
     */
    public int getCode() {
        return code;
    }

    /**
     * Run the process
     */
    public void run() {
        try {
            Process p = pb.start();


            try {
                code = execute(p);
            } catch (InterruptedException e) {
                logger.error("An interrupted exception occurred.", e);
                code = 1;
                message = e.getMessage()+"\n";
            }
            waitFor();
        } catch (IOException e) {
            logger.error("An I/O error occurred while running the process.", e);
            code = 1;
            message = e.getMessage()+"\n";
        }
    }
   /**
     * Initial dir of the process.
     * @param processDirName Name of initial dir
     */
    public void setProcessDir(String processDirName){
        File processDir = new File(processDirName);
        pb.directory(processDir);
    }
    /**
     * Execute process
     * @param p Process
     * @return int Process exit value
     * @throws java.lang.InterruptedException
     */
    private synchronized int execute(Process p) throws InterruptedException {
        int exitValue = -1;
        // endless loop
        while (true) {
            try {
                exitValue = p.exitValue();
                logger.info("Exit code of process is: "+exitValue);
                this.code = exitValue;
                break;
            } catch (IllegalThreadStateException ex) {
                logger.error("An IllegalThreadStateException exception occurred.", ex);
                code = 1;
                message = ex.getMessage()+"\n";
            }
        }
        return exitValue;
    }

    /**
     * Wait for the threads to terminate.
     */
    private synchronized void waitFor() {
        long maxWait = System.currentTimeMillis() + MAX_WAIT_MILLIS;
        while (System.currentTimeMillis() < maxWait && threads.size() > 0) {
            try {
                wait(WAIT);
            } catch (InterruptedException e) {
                logger.error("An InterruptedException exception occurred.", e);
                code = 1;
                message = e.getMessage()+"\n";
            }
        }
    }

    /**
     * @return the message
     */
    public String getMessage() {
        if(message == null)
            message = "";
        return message;
    }

}
