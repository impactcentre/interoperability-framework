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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import uk.org.taverna.server.client.connection.AccessForbiddenException;

public class TestRunPermissions extends TestRunsBase {

	private Run run;

	@Before
	public void createRun() {
		// Catch the case where USER2 is not passed in. All tests here will be
		// skipped silently if user2 is null.
		assumeNotNull(user2);

		byte[] workflow = loadResource(WKF_PASS_FILE);
		run = server.createRun(workflow, user1);
	}

	@Test
	public void testOwnership() {
		assertTrue("Correct run owner", run.isOwner());
		assertEquals("Correct run owner", user1.getUsername(), run.getOwner());

		String id = run.getIdentifier();
		Run run1 = server.getRun(id, user2);
		assertNull("Can't list runs without permission", run1);

		run.setPermission(user2.getUsername(), RunPermission.READ);
		run1 = server.getRun(id, user2);

		assertFalse("Not the owner", run1.isOwner());
		assertEquals("Still the same owner", user1.getUsername(),
				run1.getOwner());

		boolean caught = false;
		try {
			run1.getPermissions();
		} catch (IllegalUserAccessException e) {
			caught = true;
		}
		assertTrue("Only the owner can list permissions", caught);
	}

	@Test
	public void testReadPermission() {
		run.setPermission(user2.getUsername(), RunPermission.READ);
		String id = run.getIdentifier();
		Run run1 = server.getRun(id, user2);

		boolean caught = false;
		try {
			run1.getInputPort("IN").setValue("Hello");
			run1.start();
		} catch (AccessForbiddenException e) {
			caught = true;
		} catch (IOException e) {
			fail("AccessForbiddenException not caught");
		}
		assertTrue("AccessForbiddenException not caught", caught);

		run.getInputPort("IN").setValue("Hello, World!");
		try {
			run.start();
		} catch (IOException e) {
			fail("Could not start run");
		}
		wait(run);

		run1.getOutputPort("OUT").getData();

		caught = false;
		try {
			run1.delete();
		} catch (AccessForbiddenException e) {
			caught = true;
		}
		assertTrue("Cannot delete run", caught);
	}

	@Test
	public void testUpdatePermission() {
		run.setPermission(user2.getUsername(), RunPermission.UPDATE);
		String id = run.getIdentifier();
		Run run1 = server.getRun(id, user2);

		run1.getInputPort("IN").setValue("Hello");

		try {
			run1.start();
		} catch (IOException e) {
			fail("Could not start run");
		}
		wait(run1);

		run1.getOutputPort("OUT").getData();

		boolean caught = false;
		try {
			run1.delete();
		} catch (AccessForbiddenException e) {
			caught = true;
		}
		assertTrue("Cannot delete run", caught);
	}

	@Test
	public void testDeletePermission() {
		run.setPermission(user2.getUsername(), RunPermission.DELETE);
		String id = run.getIdentifier();
		Run run1 = server.getRun(id, user2);

		run1.getInputPort("IN").setValue("Hello");

		try {
			run1.start();
		} catch (IOException e) {
			fail("Could not start run");
		}
		wait(run1);

		run1.getOutputPort("OUT").getData();

		run1.delete();
	}
}
