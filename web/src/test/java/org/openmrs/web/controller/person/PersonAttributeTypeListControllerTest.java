/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.person;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ModelMap;

/**
 * Tests the {@link PersonAttributeTypeListController} controller
 */
public class PersonAttributeTypeListControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see PersonAttributeTypeListController#displayPage(ModelMap)
	 */
	@Test
	@Verifies(value = "should not fail if not authenticated", method = "displayPage(ModelMap)")
	public void displayPage_shouldNotFailIfNotAuthenticated() throws Exception {
		Context.logout();
		new PersonAttributeTypeListController().displayPage(new ModelMap());
	}
	
	/**
	 * @see PersonAttributeTypeListController#displayPage(ModelMap)
	 */
	@SuppressWarnings("unchecked")
	@Test
	@Verifies(value = "should put all attribute types into map", method = "displayPage(ModelMap)")
	public void displayPage_shouldPutAllAttributeTypesIntoMap() throws Exception {
		ModelMap map = new ModelMap();
		new PersonAttributeTypeListController().displayPage(map);
		List<PersonAttributeType> alltypes = (List<PersonAttributeType>) map.get("personAttributeTypeList");
		Assert.assertEquals(3, alltypes.size());
	}
	
	/**
	 * @see PersonAttributeTypeListController#moveDown(null,HttpSession)
	 */
	@Test
	@Verifies(value = "should move selected ids down in the list", method = "moveDown(null,HttpSession)")
	public void moveDown_shouldMoveSelectedIdsDownInTheList() throws Exception {
		// sanity check
		List<PersonAttributeType> allTypes = Context.getPersonService().getAllPersonAttributeTypes();
		Assert.assertEquals(1, allTypes.get(0).getId().intValue());
		
		// the test
		Integer[] ids = new Integer[] { 1 };
		new PersonAttributeTypeListController().moveDown(ids, new MockHttpSession());
		allTypes = Context.getPersonService().getAllPersonAttributeTypes();
		Assert.assertEquals("The types didn't move correctly", 8, allTypes.get(0).getId().intValue());
	}
	
	/**
	 * @see PersonAttributeTypeListController#moveDown(null,HttpSession)
	 */
	@Test
	@Verifies(value = "should not fail if given last id", method = "moveDown(null,HttpSession)")
	public void moveDown_shouldNotFailIfGivenLastId() throws Exception {
		// sanity check
		List<PersonAttributeType> allTypes = Context.getPersonService().getAllPersonAttributeTypes();
		Assert.assertEquals(2, allTypes.get(allTypes.size() - 1).getId().intValue());
		
		// the test
		Integer[] ids = new Integer[] { 2 };
		new PersonAttributeTypeListController().moveDown(ids, new MockHttpSession());
		allTypes = Context.getPersonService().getAllPersonAttributeTypes();
		Assert.assertEquals(2, allTypes.get(allTypes.size() - 1).getId().intValue());
	}
	
	/**
	 * @see PersonAttributeTypeListController#moveDown(null,HttpSession)
	 */
	@Test
	@Verifies(value = "should not fail if not given any ids", method = "moveDown(null,HttpSession)")
	public void moveDown_shouldNotFailIfNotGivenAnyIds() throws Exception {
		new PersonAttributeTypeListController().moveDown(new Integer[] {}, new MockHttpSession());
		new PersonAttributeTypeListController().moveDown(null, new MockHttpSession());
	}
	
	/**
	 * @see PersonAttributeTypeListController#moveUp(null,HttpSession)
	 */
	@Test
	@Verifies(value = "should move selected ids up one in the list", method = "moveUp(null,HttpSession)")
	public void moveUp_shouldMoveSelectedIdsUpOneInTheList() throws Exception {
		
		// sanity check
		List<PersonAttributeType> allTypes = Context.getPersonService().getAllPersonAttributeTypes();
		Assert.assertEquals(8, allTypes.get(1).getId().intValue());
		
		// the test
		Integer[] ids = new Integer[] { 8 };
		new PersonAttributeTypeListController().moveUp(ids, new MockHttpSession());
		allTypes = Context.getPersonService().getAllPersonAttributeTypes();
		Assert.assertEquals("The types didn't move correctly", 8, allTypes.get(0).getId().intValue());
	}
	
	/**
	 * @see PersonAttributeTypeListController#moveUp(null,HttpSession)
	 */
	@Test
	@Verifies(value = "should not fail if given first id", method = "moveUp(null,HttpSession)")
	public void moveUp_shouldNotFailIfGivenFirstId() throws Exception {
		// sanity check
		List<PersonAttributeType> allTypes = Context.getPersonService().getAllPersonAttributeTypes();
		Assert.assertEquals(1, allTypes.get(0).getId().intValue());
		
		// the test
		new PersonAttributeTypeListController().moveUp(new Integer[] { 1 }, new MockHttpSession());
	}
	
	/**
	 * @see PersonAttributeTypeListController#moveUp(null,HttpSession)
	 */
	@Test
	@Verifies(value = "should not fail if not given any ids", method = "moveUp(null,HttpSession)")
	public void moveUp_shouldNotFailIfNotGivenAnyIds() throws Exception {
		new PersonAttributeTypeListController().moveUp(new Integer[] {}, new MockHttpSession());
		new PersonAttributeTypeListController().moveUp(null, new MockHttpSession());
	}
	
	/**
	 * @see PersonAttributeTypeListController#updateGlobalProperties(String,String,String,String,String,HttpSession)
	 */
	@Test
	@Verifies(value = "should save given personListingAttributeTypes", method = "updateGlobalProperties(String,String,String,String,String,HttpSession)")
	public void updateGlobalProperties_shouldSaveGivenPersonListingAttributeTypes() throws Exception {
		new PersonAttributeTypeListController().updateGlobalProperties("asdf", "", "", "", "", new MockHttpSession());
		String attr = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_LISTING_ATTRIBUTES);
		Assert.assertEquals("asdf", attr);
	}
}
