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
package org.openmrs.web.taglib;

import java.util.List;

import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;

import org.databene.commons.CollectionUtil;
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
	 */
	@Test
	@Verifies(value = "should sort encounters by encounterDatetime in descending order", method = "doStartTag()")
	public void doStartTag_shouldSortEncountersByEncounterDatetimeInDescendingOrder() throws Exception {
		executeDataSet("org/openmrs/web/taglib/include/ForEachEncounterTagTest.xml");
		Patient patient = Context.getPatientService().getPatient(7);
		List<Encounter> encounters = Context.getEncounterService().getEncountersByPatient(patient);
		ForEachEncounterTag tag = new ForEachEncounterTag();
		tag.setPageContext(new MockPageContext());
		tag.setDescending(true);
		tag.setEncounters(encounters);
		tag.setVar("enc");
		// the tag passes
		Assert.assertEquals(BodyTag.EVAL_BODY_BUFFERED, tag.doStartTag());
		//check the sorting
		Assert.assertEquals(7, encounters.get(0).getId().intValue());
		Assert.assertEquals(8, encounters.get(1).getId().intValue());
		Assert.assertEquals(6, encounters.get(2).getId().intValue());
	}
	
	/**
	 * @see {@link ForEachEncounterTag#doStartTag()}
	 */
	@Test
	@Verifies(value = "should pass for a patient with no encounters", method = "doStartTag()")
	public void doStartTag_shouldPassForAPatientWithNoEncounters() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		List<Encounter> encounters = Context.getEncounterService().getEncountersByPatient(patient);
		Assert.assertTrue(CollectionUtil.isEmpty(encounters));
		ForEachEncounterTag tag = new ForEachEncounterTag();
		tag.setPageContext(new MockPageContext());
		tag.setEncounters(encounters);
		// the tag passes
		Assert.assertEquals(Tag.SKIP_BODY, tag.doStartTag());
	}
}
