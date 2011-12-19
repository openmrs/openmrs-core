package org.openmrs.web.controller.encounter;

import org.junit.Test;
import org.openmrs.EncounterRole;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpSession;
import java.util.List;

import static org.junit.Assert.*;

/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p/>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p/>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
public class EncounterRoleFormControllerTest extends BaseWebContextSensitiveTest {
	
	protected static final String ENC_INITIAL_DATA_XML = "org/openmrs/api/include/EncounterServiceTest-initialData.xml";
	
	/**
	 * @verifies save a new encounter role object
	 * @see EncounterRoleFormController#save(javax.servlet.http.HttpSession, org.openmrs.EncounterRole, org.springframework.validation.BindingResult)
	 */
	@Test
	public void saveEncounterRole_shouldSaveANewEncounterRoleObject() throws Exception {
		EncounterRoleFormController controller = new EncounterRoleFormController();
		MockHttpServletRequest request = new MockHttpServletRequest();
		HttpSession session = request.getSession();
		EncounterRole encounterRole = new EncounterRole();
		encounterRole.setName("attending physician");
		encounterRole.setDescription("person in charge");
		BindException errors = new BindException(encounterRole, "encounterRole");
		controller.save(session, encounterRole, errors);
		assertNotNull(encounterRole.getId());
		
	}
	
	/**
	 * @verifies raise an error if validation of encounter role fails
	 * @see EncounterRoleFormController#save(javax.servlet.http.HttpSession, org.openmrs.EncounterRole, org.springframework.validation.BindingResult)
	 */
	@Test
	public void saveEncounterRole_shouldRaiseAnErrorIfValidationOfEncounterRoleFails() throws Exception {
		EncounterRoleFormController controller = new EncounterRoleFormController();
		MockHttpServletRequest request = new MockHttpServletRequest();
		HttpSession session = request.getSession();
		EncounterRole encounterRole = new EncounterRole();
		encounterRole.setDescription("person in charge");
		BindException errors = new BindException(encounterRole, "encounterRole");
		controller.save(session, encounterRole, errors);
		assertNull(encounterRole.getId());
		assertEquals(1, errors.getErrorCount());
	}
	
	/**
	 * @verifies edit and save an existing encounter
	 * @see EncounterRoleFormController#save(javax.servlet.http.HttpSession, org.openmrs.EncounterRole, org.springframework.validation.BindingResult)
	 */
	@Test
	public void saveEncounterRole_shouldEditAndSaveAnExistingEncounter() throws Exception {
		executeDataSet(ENC_INITIAL_DATA_XML);
		EncounterRoleFormController controller = new EncounterRoleFormController();
		MockHttpServletRequest request = new MockHttpServletRequest();
		HttpSession session = request.getSession();
		EncounterRole encounterRole = Context.getEncounterService().getEncounterRole(1);
		String roleName = "surgeon";
		String description = "person who did the operation";
		encounterRole.setName(roleName);
		encounterRole.setDescription(description);
		BindException errors = new BindException(encounterRole, "encounterRole");
		controller.save(session, encounterRole, errors);
		assertNotNull(encounterRole.getId());
		assertEquals(roleName, encounterRole.getName());
		assertEquals(description, encounterRole.getDescription());
	}
	
	/**
	 * @verifies retire an existing encounter
	 * @see EncounterRoleFormController#retire(javax.servlet.http.HttpSession, org.openmrs.EncounterRole, org.springframework.validation.BindingResult)
	 */
	@Test
	public void retire_shouldRetireAnExistingEncounter() throws Exception {
		executeDataSet(ENC_INITIAL_DATA_XML);
		EncounterRoleFormController controller = new EncounterRoleFormController();
		MockHttpServletRequest request = new MockHttpServletRequest();
		HttpSession session = request.getSession();
		EncounterRole encounterRole = Context.getEncounterService().getEncounterRole(1);
		encounterRole.setRetireReason("this role is no more existing");
		BindException errors = new BindException(encounterRole, "encounterRole");
		controller.retire(session, encounterRole, errors);
		assertNotNull(encounterRole.getId());
		assertTrue(encounterRole.isRetired());
		assertEquals("EncounterRole.retiredSuccessfully", session.getAttribute(WebConstants.OPENMRS_MSG_ATTR));
	}
	
	/**
	 * @verifies unretire an existing encounter
	 * @see EncounterRoleFormController#unretire(javax.servlet.http.HttpSession, org.openmrs.EncounterRole, org.springframework.validation.BindingResult)
	 */
	@Test
	public void unretire_shouldRetireAnExistingEncounter() throws Exception {
		executeDataSet(ENC_INITIAL_DATA_XML);
		EncounterRoleFormController controller = new EncounterRoleFormController();
		MockHttpServletRequest request = new MockHttpServletRequest();
		HttpSession session = request.getSession();
		EncounterRole encounterRole = Context.getEncounterService().getEncounterRole(2);
		BindException errors = new BindException(encounterRole, "encounterRole");
		controller.unretire(session, encounterRole, errors);
		assertFalse(encounterRole.isRetired());
		assertEquals("EncounterRole.unretired", session.getAttribute(WebConstants.OPENMRS_MSG_ATTR));
	}
	
	/**
	 * @verifies purge an existing encounter
	 * @see EncounterRoleFormController#purge(javax.servlet.http.HttpSession, org.openmrs.EncounterRole, org.springframework.validation.BindingResult)
	 */
	@Test
	public void purge_shouldPurgeAnExistingEncounter() throws Exception {
		executeDataSet(ENC_INITIAL_DATA_XML);
		EncounterRoleFormController controller = new EncounterRoleFormController();
		MockHttpServletRequest request = new MockHttpServletRequest();
		HttpSession session = request.getSession();
		EncounterRole encounterRole = Context.getEncounterService().getEncounterRole(1);
		BindException errors = new BindException(encounterRole, "encounterRole");
		controller.purge(session, encounterRole, errors);
		assertEquals("EncounterRole.purgedSuccessfully", session.getAttribute(WebConstants.OPENMRS_MSG_ATTR));
	}
	
	/**
	 * @verifies add list of encounter role objects to the model
	 * @see EncounterRoleFormController#getEncounterList(org.springframework.ui.ModelMap)
	 */
	@Test
	public void showEncounterList_shouldAddListOfEncounterRoleObjectsToTheModel() throws Exception {
		ModelMap modelMap = new ModelMap();
		executeDataSet(ENC_INITIAL_DATA_XML);
		EncounterRoleFormController controller = new EncounterRoleFormController();
		String viewName = controller.getEncounterList(modelMap);
		assertEquals("admin/encounters/encounterRoleList", viewName);
		assertEquals(3, ((List) modelMap.get("encounterRoles")).size());
	}
	
	/**
	 * @verifies raise an error if retire reason is not filled
	 * @see EncounterRoleFormController#retire(javax.servlet.http.HttpSession, org.openmrs.EncounterRole, org.springframework.validation.BindingResult)
	 */
	@Test
	public void retire_shouldRaiseAnErrorIfRetireReasonIsNotFilled() throws Exception {
		executeDataSet(ENC_INITIAL_DATA_XML);
		EncounterRoleFormController controller = new EncounterRoleFormController();
		MockHttpServletRequest request = new MockHttpServletRequest();
		HttpSession session = request.getSession();
		EncounterRole encounterRole = Context.getEncounterService().getEncounterRole(1);
		encounterRole.setRetireReason(""); //setting empty retire reason so that it will raise an error.
		BindException errors = new BindException(encounterRole, "encounterRole");
		controller.retire(session, encounterRole, errors);
		assertEquals(1, errors.getErrorCount());
	}
	
}
