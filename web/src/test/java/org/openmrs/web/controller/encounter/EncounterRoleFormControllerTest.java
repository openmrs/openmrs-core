/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.encounter;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.EncounterRole;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;

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
		Assert.assertNotNull(encounterRole.getId());
		
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
		Assert.assertNull(encounterRole.getId());
		Assert.assertEquals(1, errors.getErrorCount());
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
		EncounterRole encounterRole = Context.getEncounterService().getEncounterRole(2);
		String roleName = "surgeon";
		String description = "person who did the operation";
		encounterRole.setName(roleName);
		encounterRole.setDescription(description);
		BindException errors = new BindException(encounterRole, "encounterRole");
		controller.save(session, encounterRole, errors);
		Assert.assertNotNull(encounterRole.getId());
		Assert.assertEquals(roleName, encounterRole.getName());
		Assert.assertEquals(description, encounterRole.getDescription());
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
		Assert.assertNotNull(encounterRole.getId());
		Assert.assertTrue(encounterRole.isRetired());
		Assert.assertEquals("EncounterRole.retiredSuccessfully", session.getAttribute(WebConstants.OPENMRS_MSG_ATTR));
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
		Assert.assertFalse(encounterRole.isRetired());
		Assert.assertEquals("EncounterRole.unretired", session.getAttribute(WebConstants.OPENMRS_MSG_ATTR));
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
		Assert.assertEquals("EncounterRole.purgedSuccessfully", session.getAttribute(WebConstants.OPENMRS_MSG_ATTR));
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
		Assert.assertEquals("admin/encounters/encounterRoleList", viewName);
		Assert.assertEquals(3, ((List) modelMap.get("encounterRoles")).size());
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
		Assert.assertEquals(1, errors.getErrorCount());
	}
	
}
