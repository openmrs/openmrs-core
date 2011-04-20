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
package org.openmrs.api;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests methods in the {@link VisitService}
 * 
 * @since 1.9
 */
public class VisitServiceTest extends BaseContextSensitiveTest {
	
	@Test
	@Verifies(value = "should get all visit types", method = "getAllVisitTypes()")
	public void getAllVisitTypes_shouldGetAllVisitTypes() throws Exception {
		List<VisitType> visitTypes = Context.getVisitService().getAllVisitTypes();
		Assert.assertEquals(3, visitTypes.size());
	}
	
	@Test
	@Verifies(value = "should get correct visit type", method = "getVisitType(Integer)")
	public void getVisitType_shouldGetCorrentVisitType() throws Exception {
		VisitType visitType = Context.getVisitService().getVisitType(1);
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Initial HIV Clinic Visit", visitType.getName());
		
		visitType = Context.getVisitService().getVisitType(2);
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Return TB Clinic Visit", visitType.getName());
		
		visitType = Context.getVisitService().getVisitType(3);
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Hospitalization", visitType.getName());
		
		visitType = Context.getVisitService().getVisitType(4);
		Assert.assertNull(visitType);
	}
	
	@Test
	@Verifies(value = "should get correct visit type", method = "getVisitTypeByUuid(String)")
	public void getVisitTypeByUuid_shouldGetCorrentVisitType() throws Exception {
		VisitType visitType = Context.getVisitService().getVisitTypeByUuid("c0c579b0-8e59-401d-8a4a-976a0b183519");
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Initial HIV Clinic Visit", visitType.getName());
		
		visitType = Context.getVisitService().getVisitTypeByUuid("759799ab-c9a5-435e-b671-77773ada74e4");
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Return TB Clinic Visit", visitType.getName());
		
		visitType = Context.getVisitService().getVisitTypeByUuid("759799ab-c9a5-435e-b671-77773ada74e6");
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Hospitalization", visitType.getName());
		
		visitType = Context.getVisitService().getVisitTypeByUuid("759799ab-c9a5-435e-b671-77773ada74e1");
		Assert.assertNull(visitType);
	}
	
	@Test
	@Verifies(value = "should get correct visit types", method = "getVisitTypes(String)")
	public void getVisitTypes_shouldGetCorrentVisitTypes() throws Exception {
		List<VisitType> visitTypes = Context.getVisitService().getVisitTypes("HIV Clinic");
		Assert.assertNotNull(visitTypes);
		Assert.assertEquals(1, visitTypes.size());
		Assert.assertEquals("Initial HIV Clinic Visit", visitTypes.get(0).getName());
		
		visitTypes = Context.getVisitService().getVisitTypes("Clinic Visit");
		Assert.assertNotNull(visitTypes);
		Assert.assertEquals(2, visitTypes.size());
		Assert.assertEquals("Initial HIV Clinic Visit", visitTypes.get(0).getName());
		Assert.assertEquals("Return TB Clinic Visit", visitTypes.get(1).getName());
		
		visitTypes = Context.getVisitService().getVisitTypes("ClinicVisit");
		Assert.assertNotNull(visitTypes);
		Assert.assertEquals(0, visitTypes.size());
	}
	
	@Test
	@Verifies(value = "should save new visit type", method = "saveVisitType(VisitType)")
	public void saveVisitType_shouldSaveNewVisitType() throws Exception {
		List<VisitType> visitTypes = Context.getVisitService().getVisitTypes("Some Name");
		Assert.assertEquals(0, visitTypes.size());
		
		VisitType visitType = new VisitType("Some Name", "Description");
		Context.getVisitService().saveVisitType(visitType);
		
		visitTypes = Context.getVisitService().getVisitTypes("Some Name");
		Assert.assertEquals(1, visitTypes.size());
		
		//Should create a new visit type row.
		Assert.assertEquals(4, Context.getVisitService().getAllVisitTypes().size());
	}
	
	@Test
	@Verifies(value = "should save edited visit type", method = "saveVisitType(VisitType)")
	public void saveVisitType_shouldSaveEditedVisitType() throws Exception {
		VisitType visitType = Context.getVisitService().getVisitType(1);
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Initial HIV Clinic Visit", visitType.getName());
		
		visitType.setName("Edited Name");
		visitType.setDescription("Edited Description");
		Context.getVisitService().saveVisitType(visitType);
		
		visitType = Context.getVisitService().getVisitType(1);
		Assert.assertNotNull(visitType);
		Assert.assertEquals("Edited Name", visitType.getName());
		Assert.assertEquals("Edited Description", visitType.getDescription());
		
		//Should not change the number of visit types.
		Assert.assertEquals(3, Context.getVisitService().getAllVisitTypes().size());
	}
	
	@Test
	@Verifies(value = "should retire given visit type", method = "retireVisitType(VisitType, String)")
	public void retireVisitType_shouldRetireGivenVisitType() throws Exception {
		VisitType visitType = Context.getVisitService().getVisitType(1);
		Assert.assertNotNull(visitType);
		Assert.assertFalse(visitType.isRetired());
		Assert.assertNull(visitType.getRetireReason());
		
		Context.getVisitService().retireVisitType(visitType, "retire reason");
		
		visitType = Context.getVisitService().getVisitType(1);
		Assert.assertNotNull(visitType);
		Assert.assertTrue(visitType.isRetired());
		Assert.assertEquals("retire reason", visitType.getRetireReason());
		
		//Should not change the number of visit types.
		Assert.assertEquals(3, Context.getVisitService().getAllVisitTypes().size());
	}
	
	@Test
	@Verifies(value = "should unretire given visit type", method = "unretireVisitType(VisitType)")
	public void unretireVisitType_shouldUnretireGivenVisitType() throws Exception {
		VisitType visitType = Context.getVisitService().getVisitType(3);
		Assert.assertNotNull(visitType);
		Assert.assertTrue(visitType.isRetired());
		Assert.assertEquals("Some Retire Reason", visitType.getRetireReason());
		
		Context.getVisitService().unretireVisitType(visitType);
		
		visitType = Context.getVisitService().getVisitType(3);
		Assert.assertNotNull(visitType);
		Assert.assertFalse(visitType.isRetired());
		Assert.assertNull(visitType.getRetireReason());
		
		//Should not change the number of visit types.
		Assert.assertEquals(3, Context.getVisitService().getAllVisitTypes().size());
	}
	
	@Test
	@Verifies(value = "should delete given visit type", method = "purgeVisitType(VisitType)")
	public void purgeVisitType_shouldDeleteGivenVisitType() throws Exception {
		VisitType visitType = Context.getVisitService().getVisitType(1);
		Assert.assertNotNull(visitType);
		
		Context.getVisitService().purgeVisitType(visitType);
		
		visitType = Context.getVisitService().getVisitType(1);
		Assert.assertNull(visitType);
		
		//Should reduce the existing number of visit types.
		Assert.assertEquals(2, Context.getVisitService().getAllVisitTypes().size());
	}
}
