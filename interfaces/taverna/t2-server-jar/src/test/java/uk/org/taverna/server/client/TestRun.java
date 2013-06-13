/*
 * Copyright (c) 2012, 2013 The University of Manchester, UK.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class TestRun extends TestRunsBase {

	// Some inputs.
	private final static String INPUT_XML = "<hello><yes>hello</yes><no>everybody</no><yes>world</yes></hello>";
	private final static String INPUT_XPATH = "//yes";

	// Some input files.
	private final static String INPUT_IN_FILE = "/inputs/in.txt";
	private final static String INPUT_STRINGS_FILE = "/inputs/strings.txt";
	private final static String INPUT_BACLAVA_FILE = "/inputs/empty_list_input.baclava";

	@Test
	public void testConnection() {
		byte[] workflow = loadResource(WKF_PASS_FILE);
		Run.create(server, workflow, user1);
	}

	@Test
	public void testInputSettingAndXMLUpload() {
		byte[] workflow = loadResource(WKF_XML_FILE);
		Run run = Run.create(server, workflow, user1);

		// Just set one input.
		run.getInputPort("xml").setValue(INPUT_XML);

		// Try to start the run - should fail.
		boolean caught = false;
		try {
			run.start();
		} catch (RunInputsNotSetException e) {
			caught = true;
		} catch (IOException e) {
			fail("Setting inputs failed.");
		}

		if (!caught) {
			fail("Inputs were not set properly but exception was not raised.");
		}

		// Set the other input.
		run.getInputPort("xpath").setValue(INPUT_XPATH);

		// Try to start the run - should not fail.
		try {
			run.start();
		} catch (RunInputsNotSetException e) {
			fail("Inputs were not set.");
		} catch (IOException e) {
			fail("Setting inputs failed.");
		}
		wait(run);

		assertEquals("Output OUT(0)", "hello",
				new String(run.getOutputPort("nodes").getData(0)));
		assertEquals("Output OUT(1)", "world",
				new String(run.getOutputPort("nodes").getData(1)));
	}

	@Test
	public void testFileInput() {
		byte[] workflow = loadResource(WKF_PASS_FILE);
		File inputFile = getResourceFile(INPUT_IN_FILE);
		Run run = Run.create(server, workflow, user1);

		try {
			run.getInputPort("IN").setFile(inputFile);
		} catch (FileNotFoundException e) {
			fail("Could not find input file: " + INPUT_IN_FILE);
		}

		try {
			run.start();
		} catch (Exception e) {
			fail("Failed to start run.");
		}

		assertTrue("Run is running", run.isRunning());
		wait(run);

		assertEquals("Output OUT", "Hello, World!", run.getOutputPort("OUT")
				.getDataAsString());
	}

	@Test
	public void testBaclavaInput() {
		File workflow = getResourceFile(WKF_LISTS_FILE);
		Run run = null;
		try {
			run = Run.create(server, workflow, user1);
		} catch (IOException e) {
			fail("Error loading workflow.");
		}

		byte[] baclava = loadResource(INPUT_BACLAVA_FILE);
		run.setBaclavaInput(baclava);

		// Do some tests on the input ports.
		Set<String> inputs = run.getInputPorts().keySet();
		assertTrue("Input SINGLE_IN present", inputs.contains("SINGLE_IN"));
		assertTrue("Input MANY_IN present", inputs.contains("MANY_IN"));
		assertTrue("Baclava inputs set", run.isBaclavaInput());
		assertTrue("Input SINGLE_IN uses baclava", run
				.getInputPort("SINGLE_IN").isBaclava());
		assertTrue("Input SINGLE_IN is set", run.getInputPort("SINGLE_IN")
				.isSet());

		try {
			run.start();
		} catch (Exception e) {
			fail("Failed to start run.");
		}

		assertTrue("Run is running", run.isRunning());
		wait(run);

		// Do some tests on the output ports.
		Set<String> outputs = run.getOutputPorts().keySet();
		assertTrue("Output SINGLE present", outputs.contains("SINGLE"));
		assertTrue("Output MANY present", outputs.contains("MANY"));
		OutputPort many = run.getOutputPort("MANY");
		assertEquals("Output SINGLE depth", 1, run.getOutputPort("SINGLE")
				.getDepth());
		assertEquals("Output MANY depth", 3, many.getDepth());
		assertEquals("Total length of MANY data", 12, many.getDataSize());
		String data = many.getValue().get(1).get(0).get(1).getDataAsString();
		assertEquals("MANY[1][0][1]", "Hello", data);

		// Check that trying to get data at the OutputPort level fails.
		boolean caught = false;
		try {
			run.getOutputPort("SINGLE").getData();
		} catch (UnsupportedOperationException e) {
			caught = true;
		}
		if (!caught) {
			fail("UnsupportedOperationException not caught when trying to get data from the root of a list output.");
		}

		// Check that trying to stream at the OutputPort level fails.
		caught = false;
		try {
			many.getDataStream();
		} catch (UnsupportedOperationException e) {
			caught = true;
		}
		if (!caught) {
			fail("UnsupportedOperationException not caught when trying to stream from a list output.");
		}

		// Check that we can stream from one of the leaves.
		byte[] buffer = new byte[5];
		int length = 0;
		InputStream is = many.getValue().get(1).get(0).get(1).getDataStream();
		try {
			length = is.read(buffer);
		} catch (IOException e) {
			fail("Could not read from output port data stream.");
		}

		assertEquals("5 bytes streamed", 5, length);
		assertEquals("Data is correct", "Hello", new String(buffer));
	}

	@Test
	public void testBaclavaOutput() {
		byte[] workflow = loadResource(WKF_PASS_FILE);
		Run run = Run.create(server, workflow, user1);

		run.getInputPort("IN").setValue("Some input...");
		run.requestBaclavaOutput();
		assertTrue("Baclava output set", run.isBaclavaOutput());

		try {
			run.start();
		} catch (Exception e) {
			fail("Failed to start run.");
		}

		assertTrue("Run is running", run.isRunning());
		wait(run);

		// Nothing raised here we hope.
		run.getBaclavaOutput();
	}

	@Test
	public void testResultStreaming() {
		byte[] workflow = loadResource(WKF_PASS_FILE);
		File inputFile = getResourceFile(INPUT_STRINGS_FILE);
		Run run = Run.create(server, workflow, user1);

		try {
			run.getInputPort("IN").setFile(inputFile);
		} catch (FileNotFoundException e) {
			fail("Could not find input file: " + INPUT_IN_FILE);
		}

		try {
			run.start();
		} catch (Exception e) {
			fail("Failed to start run.");
		}

		assertTrue("Run is running", run.isRunning());
		wait(run);

		// Check data size is correct.
		assertEquals("Data is 100 bytes", 100, run.getOutputPort("OUT")
				.getDataSize());

		// Stream just the first 10 bytes.
		byte[] buffer = new byte[100];
		int length = 0;
		InputStream is = run.getOutputPort("OUT").getDataStream();
		try {
			length = is.read(buffer, 0, 10);
		} catch (IOException e) {
			fail("Could not read from output port data stream.");
		} finally {
			IOUtils.closeQuietly(is);
		}

		assertEquals("10 bytes read", 10, length);
		assertEquals("Data is equal", "123456789\n", new String(buffer, 0, 10));

		// Stream the whole lot (also prove we can re-open streams).
		is = run.getOutputPort("OUT").getDataStream();
		try {
			length = is.read(buffer);
		} catch (IOException e) {
			fail("Could not read from output port data stream.");
		} finally {
			IOUtils.closeQuietly(is);
		}

		assertEquals("100 bytes read", 100, length);
		assertEquals("Last 10 chars are correct", "A23456789\n", new String(
				buffer, 90, 10));
	}

	@Test
	public void testAlwaysFail() {
		byte[] workflow = loadResource(WKF_FAIL_FILE);
		Run run = server.createRun(workflow, user1);

		try {
			run.start();
		} catch (Exception e) {
			fail("Failed to start run.");
		}
		wait(run);

		OutputPort out = run.getOutputPort("OUT");
		assertTrue("OUT is an error", out.isError());
	}

	@Test
	public void testWorkflowErrors() {
		byte[] workflow = loadResource(WKF_ERRORS_FILE);
		Run run = server.createRun(workflow, user1);

		try {
			run.start();
		} catch (Exception e) {
			fail("Failed to start run.");
		}
		wait(run);

		OutputPort out = run.getOutputPort("OUT");
		assertTrue("OUT is an error", out.isError());
	}
}
