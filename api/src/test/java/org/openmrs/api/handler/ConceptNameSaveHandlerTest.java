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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.User;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Tests the {@link ConceptNameSaveHandler} class.
 */
public class ConceptNameSaveHandlerTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ConceptNameSaveHandler#handle(ConceptName,User,Date,String)
	 */
	@Test
	public void handle_shouldNotFailIfTagsIsNull() {
		ConceptNameSaveHandler handler = new ConceptNameSaveHandler();
		ConceptName name = new ConceptName();
		name.setTags(null);
		handler.handle(name, null, null, null);
	}
	
	/**
	 * @see ConceptNameSaveHandler#handle(ConceptName,User,Date,String)
	 */
	@Test
	public void handle_shouldReplaceTagsWithoutIdsWithDatabaseFetchedTag() {
		ConceptNameSaveHandler handler = new ConceptNameSaveHandler();
		ConceptName name = new ConceptName();
		name.addTag("preferred"); // this tag has a null id
		name.addTag("short"); // this tag has a null id
		handler.handle(name, null, null, null);
		for (ConceptNameTag tag : name.getTags()) {
			if (tag.getTag().equals("preferred")) {
				assertEquals(4, tag.getConceptNameTagId().intValue());
			} else if (tag.getTag().equals("short")) {
				assertEquals(2, tag.getConceptNameTagId().intValue());
			}
		}
	}
	
	/**
	 * @see ConceptNameSaveHandler#handle(ConceptName,User,Date,String)
	 */
	@Test
	public void handle_shouldNotReplaceTagsWithoutIdsThatAreNotInTheDatabase() {
		ConceptNameSaveHandler handler = new ConceptNameSaveHandler();
		ConceptName name = new ConceptName();
		name.addTag(new ConceptNameTag("Some randome tag name", "")); // this tag has a null id
		handler.handle(name, null, null, null);
		ConceptNameTag newTag = name.getTags().iterator().next();
		assertNull(newTag.getConceptNameTagId());
	}
	
	/**
	 * @see ConceptNameSaveHandler#handle(ConceptName,User,Date,String)
	 */
	@Test
	public void handle_shouldNotReplaceTagsThatHaveIds() {
		ConceptNameSaveHandler handler = new ConceptNameSaveHandler();
		ConceptName name = new ConceptName();
		ConceptNameTag tag = new ConceptNameTag("some randome tag name with an id", "");
		tag.setConceptNameTagId(34); // this tag has an id
		name.addTag(tag);
		handler.handle(name, null, null, null);
		ConceptNameTag newTag = name.getTags().iterator().next();
		assertEquals(34, newTag.getConceptNameTagId().intValue());
	}
}
