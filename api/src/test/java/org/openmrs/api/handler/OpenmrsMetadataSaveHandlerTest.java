/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.handler;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.User;

/**
 * Tests for {@link OpenmrsMetadataSaveHandler}
 */
public class OpenmrsMetadataSaveHandlerTest {
	
	/**
	 * @see OpenmrsMetadataSaveHandler#handle(OpenmrsMetadata,User,Date,String)
	 */
	@Test
	public void handle_shouldTrimWhitespaceFromName() {
		String NAME = "the location name";
		OpenmrsMetadataSaveHandler handler = new OpenmrsMetadataSaveHandler();
		Location loc = new Location();
		loc.setName(" " + NAME + " ");
		handler.handle(loc, null, null, null);
		Assert.assertEquals(NAME, loc.getName());
	}
	
	/**
	 * @see OpenmrsMetadataSaveHandler#handle(OpenmrsMetadata,User,Date,String)
	 */
	@Test
	public void handle_shouldTrimWhitespaceFromDescription() {
		String DESC = "the location desc";
		OpenmrsMetadataSaveHandler handler = new OpenmrsMetadataSaveHandler();
		Location loc = new Location();
		loc.setName("a name");
		loc.setDescription(" " + DESC + " ");
		handler.handle(loc, null, null, null);
		Assert.assertEquals(DESC, loc.getDescription());
	}
}
