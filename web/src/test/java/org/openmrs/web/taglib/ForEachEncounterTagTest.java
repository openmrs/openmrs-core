/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockPageContext;

/**
 * Contains tests for the {@link ForEachEncounterTag}
 */
public class ForEachEncounterTagTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link ForEachEncounterTag#doStartTag()}
	 * @regression TRUNK-2465
	 */
	@Test
	@Verifies(value = "should sort encounters by encounterDatetime in descending order", method = "doStartTag()")
	public void doStartTag_shouldSortEncountersByEncounterDatetimeInDescendingOrder() throws Exception {
		int num = 3;
		executeDataSet("org/openmrs/web/taglib/include/ForEachEncounterTagTest.xml");
		Patient patient = Context.getPatientService().getPatient(7);
		List<Encounter> encounters = Context.getEncounterService().getEncountersByPatient(patient);
		ForEachEncounterTag tag = new ForEachEncounterTag();
		tag.setPageContext(new MockPageContext());
		tag.setDescending(true);
		tag.setEncounters(encounters);
		tag.setVar("enc");
		tag.setNum(num);
		// the tag passes
		Assert.assertEquals(BodyTag.EVAL_BODY_BUFFERED, tag.doStartTag());
		//the match count should not exceed the limit
		Assert.assertTrue(num >= tag.matchingEncs.size());
		//check the sorting
		Assert.assertEquals(11, tag.matchingEncs.get(0).getId().intValue());
		Assert.assertEquals(16, tag.matchingEncs.get(1).getId().intValue());
		Assert.assertEquals(7, tag.matchingEncs.get(2).getId().intValue());
	}
	
	/**
	 * @see {@link ForEachEncounterTag#doStartTag()}
	 */
	@Test
	@Verifies(value = "should pass for a patient with no encounters", method = "doStartTag()")
	public void doStartTag_shouldPassForAPatientWithNoEncounters() throws Exception {
		ForEachEncounterTag tag = new ForEachEncounterTag();
		tag.setPageContext(new MockPageContext());
		tag.setEncounters(new ArrayList<Encounter>());
		// the tag passes
		Assert.assertEquals(Tag.SKIP_BODY, tag.doStartTag());
	}
}
