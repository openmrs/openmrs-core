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
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.User;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests the {@link ConceptNameSaveHandler} class.
 */
public class ConceptNameSaveHandlerTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link ConceptNameSaveHandler#handle(ConceptName,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not fail if tags is null", method = "handle(ConceptName,User,Date,String)")
	public void handle_shouldNotFailIfTagsIsNull() throws Exception {
		ConceptNameSaveHandler handler = new ConceptNameSaveHandler();
		ConceptName name = new ConceptName();
		name.setTags(null);
		handler.handle(name, null, null, null);
	}
	
	/**
	 * @see {@link ConceptNameSaveHandler#handle(ConceptName,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should replace tags without ids with database fetched tag", method = "handle(ConceptName,User,Date,String)")
	public void handle_shouldReplaceTagsWithoutIdsWithDatabaseFetchedTag() throws Exception {
		ConceptNameSaveHandler handler = new ConceptNameSaveHandler();
		ConceptName name = new ConceptName();
		name.addTag(ConceptNameTag.PREFERRED); // this tag has a null id
		name.addTag(ConceptNameTag.SHORT); // this tag has a null id
		handler.handle(name, null, null, null);
		for (ConceptNameTag tag : name.getTags()) {
			if (tag.getTag().equals(ConceptNameTag.PREFERRED)) {
				Assert.assertEquals(4, tag.getConceptNameTagId().intValue());
			} else if (tag.getTag().equals(ConceptNameTag.SHORT)) {
				Assert.assertEquals(2, tag.getConceptNameTagId().intValue());
			}
		}
	}
	
	/**
	 * @see {@link ConceptNameSaveHandler#handle(ConceptName,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not replace tags without ids that are not in the database", method = "handle(ConceptName,User,Date,String)")
	public void handle_shouldNotReplaceTagsWithoutIdsThatAreNotInTheDatabase() throws Exception {
		ConceptNameSaveHandler handler = new ConceptNameSaveHandler();
		ConceptName name = new ConceptName();
		name.addTag(new ConceptNameTag("Some randome tag name", "")); // this tag has a null id
		handler.handle(name, null, null, null);
		ConceptNameTag newTag = name.getTags().iterator().next();
		Assert.assertNull(newTag.getConceptNameTagId());
	}
	
	/**
	 * @see {@link ConceptNameSaveHandler#handle(ConceptName,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not replace tags that have ids", method = "handle(ConceptName,User,Date,String)")
	public void handle_shouldNotReplaceTagsThatHaveIds() throws Exception {
		ConceptNameSaveHandler handler = new ConceptNameSaveHandler();
		ConceptName name = new ConceptName();
		ConceptNameTag tag = new ConceptNameTag("some randome tag name with an id", "");
		tag.setConceptNameTagId(34); // this tag has an id
		name.addTag(tag);
		handler.handle(name, null, null, null);
		ConceptNameTag newTag = name.getTags().iterator().next();
		Assert.assertEquals(34, newTag.getConceptNameTagId().intValue());
	}
}
