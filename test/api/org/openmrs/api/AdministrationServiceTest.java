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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.ImplementationId;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsConstants;

/**
 * TODO clean up and finish this test class. Should test all methods in the
 * {@link AdministrationService}
 */
public class AdministrationServiceTest extends BaseContextSensitiveTest {
	
	private AdministrationService adminService = null;
	
	/**
	 * Run this before each unit test in this class. It simply assigns the services used in this
	 * class to private variables The "@Before" method in {@link BaseContextSensitiveTest} is run
	 * right before this method and sets up the initial data set and authenticates to the Context
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		if (adminService == null)
			adminService = Context.getAdministrationService();
	}
	
	/**
	 * Tests the AdministrationService.executeSql method with a sql statement containing a valid
	 * group by clause
	 * 
	 * @see {@link AdministrationService#executeSQL(String,null)}
	 */
	@Test
	@Verifies(value = "should execute sql containing group by", method = "executeSQL(String,null)")
	public void executeSQL_shouldExecuteSqlContainingGroupBy() throws Exception {
		
		String sql = "select encounter1_.location_id, encounter1_.creator, encounter1_.encounter_type, encounter1_.form_id, location2_.location_id, count(obs0_.obs_id) from obs obs0_ right outer join encounter encounter1_ on obs0_.encounter_id=encounter1_.encounter_id inner join location location2_ on encounter1_.location_id=location2_.location_id inner join users user3_ on encounter1_.creator=user3_.user_id inner join person user3_1_ on user3_.user_id=user3_1_.person_id inner join encounter_type encountert4_ on encounter1_.encounter_type=encountert4_.encounter_type_id inner join form form5_ on encounter1_.form_id=form5_.form_id where encounter1_.date_created>='2007-05-05' and encounter1_.date_created<= '2008-05-05' group by encounter1_.location_id, encounter1_.creator , encounter1_.encounter_type , encounter1_.form_id";
		adminService.executeSQL(sql, true);
		
		String sql2 = "select encounter_id, count(*) from encounter encounter_id group by encounter_id";
		adminService.executeSQL(sql2, true);
	}
	
	/**
	 * This test runs over the set and get methods in AdminService to make sure that the impl id can
	 * be saved and verified correctly
	 * 
	 * @throws Exception
	 */
	public void testSetVerifyGetImplementationId() throws Exception {
		
		// make sure we have no impl id already
		ImplementationId currentImplId = adminService.getImplementationId();
		assertNull("There shouldn't be an impl id defined alread", currentImplId);
		
		// save a null impl id. no exception thrown
		adminService.setImplementationId(null);
		ImplementationId afterNull = adminService.getImplementationId();
		assertNull("There shouldn't be an impl id defined after setting a null impl id", afterNull);
		
		// save a blank impl id. exception thrown
		ImplementationId blankId = new ImplementationId();
		try {
			adminService.setImplementationId(blankId);
			fail("An exception should be thrown on a blank impl id save");
		}
		catch (APIException e) {
			// expected exception
		}
		ImplementationId afterBlank = adminService.getImplementationId();
		assertNull("There shouldn't be an impl id defined after setting a blank impl id", afterBlank);
		
		// save an impl id with an invalid hl7 code
		ImplementationId invalidId = new ImplementationId();
		invalidId.setImplementationId("caret^caret");
		invalidId.setPassphrase("some valid passphrase");
		invalidId.setDescription("Some valid description");
		try {
			adminService.setImplementationId(invalidId);
			fail("An exception should be thrown on an invalid impl id save");
		}
		catch (APIException e) {
			// expected exception
		}
		ImplementationId afterInvalid = adminService.getImplementationId();
		assertNull("There shouldn't be an impl id defined after setting an invalid impl id", afterInvalid);
		
		// save an impl id with an invalid hl7 code
		ImplementationId invalidId2 = new ImplementationId();
		invalidId.setImplementationId("pipe|pipe");
		invalidId.setPassphrase("some valid passphrase");
		invalidId.setDescription("Some valid description");
		try {
			adminService.setImplementationId(invalidId2);
			fail("An exception should be thrown on an invalid impl id save");
		}
		catch (APIException e) {
			// expected exception
		}
		ImplementationId afterInvalid2 = adminService.getImplementationId();
		assertNull("There shouldn't be an impl id defined after setting an invalid impl id", afterInvalid2);
		
		// save a valid impl id
		ImplementationId validId = new ImplementationId();
		validId.setImplementationId("JUNIT-TEST");
		validId.setPassphrase("This is the junit test passphrase");
		validId.setDescription("This is the junit impl id used for testing of the openmrs API only.");
		adminService.setImplementationId(validId);
		ImplementationId afterValid = adminService.getImplementationId();
		assertEquals(validId, afterValid);
		
		// save a second valid id
		ImplementationId validId2 = new ImplementationId();
		validId2.setImplementationId("JUNIT-TEST 2");
		validId2.setPassphrase("This is the junit test passphrase 2");
		validId2.setDescription("This is the junit impl id (2) used for testing of the openmrs API only.");
		adminService.setImplementationId(validId2);
		ImplementationId afterValid2 = adminService.getImplementationId();
		assertEquals(validId2, afterValid2);
		
	}
	
	/**
	 * @see {@link AdministrationService#getGlobalProperty(String)}
	 */
	@Test
	@Verifies(value = "should not fail with null propertyName", method = "getGlobalProperty(String)")
	public void getGlobalProperty_shouldNotFailWithNullPropertyName() throws Exception {
		adminService.getGlobalProperty(null);
	}
	
	/**
	 * @see {@link AdministrationService#getGlobalProperty(String)}
	 */
	@Test
	@Verifies(value = "should get property value given valid property name", method = "getGlobalProperty(String)")
	public void getGlobalProperty_shouldGetPropertyValueGivenValidPropertyName() throws Exception {
		// put the global property into the database
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		String propertyValue = adminService.getGlobalProperty("a_valid_gp_key");
		
		Assert.assertEquals("correct-value", propertyValue);
	}
	
	/**
	 * @see {@link AdministrationService#getGlobalProperty(String,String)}
	 */
	@Test
	@Verifies(value = "should not fail with null default value", method = "getGlobalProperty(String,String)")
	public void getGlobalProperty_shouldNotFailWithNullDefaultValue() throws Exception {
		adminService.getGlobalProperty("asdfsadfsafd", null);
	}
	
	/**
	 * @see {@link AdministrationService#getGlobalProperty(String,String)}
	 */
	@Test
	@Verifies(value = "should return default value if property name does not exist", method = "getGlobalProperty(String,String)")
	public void getGlobalProperty_shouldReturnDefaultValueIfPropertyNameDoesNotExist() throws Exception {
		String invalidKey = "asdfasdf";
		String propertyValue = adminService.getGlobalProperty(invalidKey);
		Assert.assertNull(propertyValue); // make sure there isn't a gp
		
		String value = adminService.getGlobalProperty(invalidKey, "default");
		Assert.assertEquals("default", value);
	}
	
	/**
	 * @see {@link AdministrationService#getGlobalPropertiesByPrefix(String)}
	 */
	@Test
	@Verifies(value = "should return all relevant global properties in the database", method = "getGlobalPropertiesByPrefix(String)")
	public void getGlobalPropertiesByPrefix_shouldReturnAllRelevantGlobalPropertiesInTheDatabase() throws Exception {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		List<GlobalProperty> properties = adminService.getGlobalPropertiesByPrefix("fake.module.");
		
		for (GlobalProperty property : properties) {
			Assert.assertTrue(property.getProperty().startsWith("fake.module."));
			Assert.assertTrue(property.getPropertyValue().startsWith("correct-value"));
		}
	}
	
	/**
	 * @see {@link AdministrationService#getAllowedLocales()}
	 */
	@Test
	@Verifies(value = "should not fail if not global property for locales allowed defined yet", method = "getAllowedLocales()")
	public void getAllowedLocales_shouldNotFailIfNotGlobalPropertyForLocalesAllowedDefinedYet() throws Exception {
		Context.getAdministrationService().purgeGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST));
		Context.getAdministrationService().getAllowedLocales();
	}
	
}
