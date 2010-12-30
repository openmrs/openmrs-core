/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.handler;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.User;
import org.openmrs.test.Verifies;

/**
 * Tests for {@link OpenmrsMetadataSaveHandler}
 */
public class OpenmrsMetadataSaveHandlerTest {
	
	/**
	 * @see {@link OpenmrsMetadataSaveHandler#handle(OpenmrsMetadata,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should trim whitespace from name", method = "handle(OpenmrsMetadata,User,Date,String)")
	public void handle_shouldTrimWhitespaceFromName() throws Exception {
		String NAME = "the location name";
		OpenmrsMetadataSaveHandler handler = new OpenmrsMetadataSaveHandler();
		Location loc = new Location();
		loc.setName(" " + NAME + " ");
		handler.handle(loc, null, null, null);
		Assert.assertEquals(NAME, loc.getName());
	}
	
	/**
	 * @see {@link OpenmrsMetadataSaveHandler#handle(OpenmrsMetadata,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should trim whitespace from description", method = "handle(OpenmrsMetadata,User,Date,String)")
	public void handle_shouldTrimWhitespaceFromDescription() throws Exception {
		String DESC = "the location desc";
		OpenmrsMetadataSaveHandler handler = new OpenmrsMetadataSaveHandler();
		Location loc = new Location();
		loc.setName("a name");
		loc.setDescription(" " + DESC + " ");
		handler.handle(loc, null, null, null);
		Assert.assertEquals(DESC, loc.getDescription());
	}
}
