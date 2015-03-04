/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.GlobalProperty;
import org.openmrs.ImplementationId;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.BooleanDatatype;
import org.openmrs.customdatatype.datatype.DateDatatype;
import org.openmrs.messagesource.MutableMessageSource;
import org.openmrs.messagesource.impl.MutableResourceBundleMessageSource;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.HttpClient;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * TODO clean up and finish this test class. Should test all methods in the
 * {@link AdministrationService}
 */
public class AdministrationServiceTest extends BaseContextSensitiveTest {
	
	private AdministrationService adminService = null;
	
	protected static final String ADMIN_INITIAL_DATA_XML = "org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml";
	
	private HttpClient implementationHttpClient;
	
	/**
	 * Run this before each unit test in this class. It simply assigns the services used in this
	 * class to private variables The "@Before" method in {@link BaseContextSensitiveTest} is run
	 * right before this method and sets up the initial data set and authenticates to the Context
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		if (adminService == null) {
			adminService = Context.getAdministrationService();
			implementationHttpClient = mock(HttpClient.class);
			adminService.setImplementationIdHttpClient(implementationHttpClient);
		}
		
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
	 * @see AdministrationService#setImplementationId(ImplementationId)
	 */
	@Test
	@Verifies(value = "should not fail if given implementationId is null", method = "setImplementationId(ImplementationId)")
	public void setImplementationId_shouldNotFailIfGivenImplementationIdIsNull() throws Exception {
		// save a null impl id. no exception thrown
		adminService.setImplementationId(null);
		ImplementationId afterNull = adminService.getImplementationId();
		assertNull("There shouldn't be an impl id defined after setting a null impl id", afterNull);
	}
	
	/**
	 * This uses a try/catch so that we can make sure no blank id is saved to the database.
	 * 
	 * @see AdministrationService#setImplementationId(ImplementationId)
	 */
	@Test()
	@Verifies(value = "should throw APIException if given empty implementationId object", method = "setImplementationId(ImplementationId)")
	public void setImplementationId_shouldThrowAPIExceptionIfGivenEmptyImplementationIdObject() throws Exception {
		// save a blank impl id. exception thrown
		try {
			adminService.setImplementationId(new ImplementationId());
			fail("An exception should be thrown on a blank impl id save");
		}
		catch (APIException e) {
			// expected exception
		}
		ImplementationId afterBlank = adminService.getImplementationId();
		assertNull("There shouldn't be an impl id defined after setting a blank impl id", afterBlank);
	}
	
	/**
	 * This uses a try/catch so that we can make sure no blank id is saved to the database.
	 * 
	 * @see {@link AdministrationService#setImplementationId(ImplementationId)}
	 */
	@Test
	@Verifies(value = "should throw APIException if given a caret in the implementationId code", method = "setImplementationId(ImplementationId)")
	public void setImplementationId_shouldThrowAPIExceptionIfGivenACaretInTheImplementationIdCode() throws Exception {
		// save an impl id with an invalid hl7 code
		ImplementationId invalidId = new ImplementationId();
		invalidId.setImplementationId("caret^caret");
		invalidId.setName("an invalid impl id for a unit test");
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
	}
	
	/**
	 * @see {@link AdministrationService#setImplementationId(ImplementationId)}
	 */
	@Test
	@Verifies(value = "should throw APIException if given a pipe in the implementationId code", method = "setImplementationId(ImplementationId)")
	public void setImplementationId_shouldThrowAPIExceptionIfGivenAPipeInTheImplementationIdCode() throws Exception {
		// save an impl id with an invalid hl7 code
		ImplementationId invalidId2 = new ImplementationId();
		invalidId2.setImplementationId("pipe|pipe");
		invalidId2.setName("an invalid impl id for a unit test");
		invalidId2.setPassphrase("some valid passphrase");
		invalidId2.setDescription("Some valid description");
		try {
			adminService.setImplementationId(invalidId2);
			fail("An exception should be thrown on an invalid impl id save");
		}
		catch (APIException e) {
			// expected exception
		}
		ImplementationId afterInvalid2 = adminService.getImplementationId();
		assertNull("There shouldn't be an impl id defined after setting an invalid impl id", afterInvalid2);
	}
	
	/**
	 * @see AdministrationService#setImplementationId(ImplementationId)
	 */
	@Test
	@Ignore
	@Verifies(value = "should create implementation id in database", method = "setImplementationId(ImplementationId)")
	public void setImplementationId_shouldCreateImplementationIdInDatabase() throws Exception {
		// save a valid impl id
		ImplementationId validId = new ImplementationId();
		validId.setImplementationId("JUNIT-TEST");
		validId.setName("JUNIT-TEST implementation id");
		validId.setPassphrase("This is the junit test passphrase");
		validId.setDescription("This is the junit impl id used for testing of the openmrs API only.");
		adminService.setImplementationId(validId);
		
		assertEquals(validId, adminService.getImplementationId());
	}
	
	/**
	 * @see AdministrationService#setImplementationId(ImplementationId)
	 */
	@Test
	@Ignore
	@Verifies(value = "should overwrite implementation id in database if exists", method = "setImplementationId(ImplementationId)")
	public void setImplementationId_shouldOverwriteImplementationIdInDatabaseIfExists() throws Exception {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-general.xml");
		
		// sanity check to make sure we have an implementation id
		Assert.assertNotNull(adminService.getImplementationId());
		Context.clearSession(); // so a NonUniqueObjectException doesn't occur on the global property later
		
		// save a second valid id
		ImplementationId validId2 = new ImplementationId();
		validId2.setImplementationId("JUNIT-TEST 2");
		validId2.setName("JUNIT-TEST (#2) implementation id");
		validId2.setPassphrase("This is the junit test passphrase 2");
		validId2.setDescription("This is the junit impl id (2) used for testing of the openmrs API only.");
		adminService.setImplementationId(validId2);
		assertEquals(validId2, adminService.getImplementationId());
	}
	
	/**
	 * @see AdministrationService#setImplementationId(ImplementationId)
	 */
	@Test
	@Ignore
	@Verifies(value = "should set uuid on implementation id global property", method = "setImplementationId(ImplementationId)")
	public void setImplementationId_shouldSetUuidOnImplementationIdGlobalProperty() throws Exception {
		ImplementationId validId = new ImplementationId();
		validId.setImplementationId("JUNIT-TEST");
		validId.setName("JUNIT-TEST implementation id");
		validId.setPassphrase("This is the junit test passphrase");
		validId.setDescription("This is the junit impl id used for testing of the openmrs API only.");
		adminService.setImplementationId(validId);
		
		GlobalProperty gp = adminService.getGlobalPropertyObject(OpenmrsConstants.GLOBAL_PROPERTY_IMPLEMENTATION_ID);
		Assert.assertNotNull(gp.getUuid());
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
	 * @see {@link AdministrationService#updateGlobalProperty(String,String)}
	 */
	@Test
	@Verifies(value = "should update global property in database", method = "updateGlobalProperty(String,String)")
	public void updateGlobalProperty_shouldUpdateGlobalPropertyInDatabase() throws Exception {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		String propertyValue = adminService.getGlobalProperty("a_valid_gp_key");
		Assert.assertEquals("correct-value", propertyValue);
		
		adminService.updateGlobalProperty("a_valid_gp_key", "new-value");
		
		String newValue = adminService.getGlobalProperty("a_valid_gp_key");
		Assert.assertEquals("new-value", newValue);
	}
	
	/**
	 * @see {@link AdministrationService#updateGlobalProperty(String,String)}
	 */
	@Test(expected = IllegalStateException.class)
	@Verifies(value = "should fail if global property being updated does not already exist", method = "updateGlobalProperty(String,String)")
	public void updateGlobalProperty_shouldFailIfGlobalPropertyBeingUpdatedDoesNotAlreadyExist() throws Exception {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		adminService.updateGlobalProperty("a_invalid_gp_key", "asdfsadfsafd");
	}
	
	/**
	 * @see {@link AdministrationService#updateGlobalProperty(String,String)}
	 */
	@Test
	@Verifies(value = "should update a global property whose typed value is handled by a custom datatype", method = "updateGlobalProperty(String,String)")
	public void updateGlobalProperty_shouldUpdateAGlobalPropertyWhoseTypedvalueIsHandledByACustomDatatype() throws Exception {
		GlobalProperty gp = new GlobalProperty();
		gp.setProperty("Flag");
		gp.setDatatypeClassname(BooleanDatatype.class.getName());
		gp.setValue(Boolean.FALSE);
		adminService.saveGlobalProperty(gp);
		Assert.assertEquals(adminService.getGlobalProperty("Flag"), "false");
		
		adminService.updateGlobalProperty("Flag", Boolean.TRUE.toString());
		Assert.assertEquals(adminService.getGlobalProperty("Flag"), "true");
	}
	
	/**
	 * @see {@link AdministrationService#setGlobalProperty(String,String)}
	 */
	@Test
	@Verifies(value = "should create global property in database", method = "setGlobalProperty(String,String)")
	public void setGlobalProperty_shouldCreateGlobalPropertyInDatabase() throws Exception {
		String newKey = "new_gp_key";
		
		String initialValue = adminService.getGlobalProperty(newKey);
		Assert.assertNull(initialValue); // ensure gp doesnt exist before test
		adminService.setGlobalProperty(newKey, "new_key");
		
		String newValue = adminService.getGlobalProperty(newKey);
		Assert.assertNotNull(newValue);
	}
	
	/**
	 * @see {@link AdministrationService#setGlobalProperty(String,String)}
	 */
	@Test
	@Verifies(value = "should overwrite global property if exists", method = "setGlobalProperty(String,String)")
	public void setGlobalProperty_shouldOverwriteGlobalPropertyIfExists() throws Exception {
		
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		String propertyValue = adminService.getGlobalProperty("a_valid_gp_key");
		Assert.assertEquals("correct-value", propertyValue);
		
		adminService.setGlobalProperty("a_valid_gp_key", "new-value");
		
		String newValue = adminService.getGlobalProperty("a_valid_gp_key");
		Assert.assertEquals("new-value", newValue);
		
	}
	
	/**
	 * @see {@link AdministrationService#setGlobalProperty(String,String)}
	 */
	@Test
	@Verifies(value = "should save a global property whose typed value is handled by a custom datatype", method = "setGlobalProperty(String,String)")
	public void setGlobalProperty_shouldSaveAGlobalPropertyWhoseTypedValueIsHandledByACustomDatatype() throws Exception {
		
		String newKey = "Flag";
		String initialValue = adminService.getGlobalProperty(newKey);
		Assert.assertNull(initialValue);
		
		adminService.setGlobalProperty(newKey, Boolean.FALSE.toString());
		Assert.assertEquals(adminService.getGlobalProperty("Flag"), "false");
		
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
	
	/**
	 * @see {@link AdministrationService#getGlobalPropertyByUuid(String)}
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getGlobalPropertyByUuid(String)")
	public void getGlobalPropertyByUuid_shouldFindObjectGivenValidUuid() throws Exception {
		String uuid = "4f55827e-26fe-102b-80cb-0017a47871b3";
		GlobalProperty prop = Context.getAdministrationService().getGlobalPropertyByUuid(uuid);
		Assert.assertEquals("locale.allowed.list", prop.getProperty());
	}
	
	/**
	 * @see {@link AdministrationService#getGlobalPropertyByUuid(String)}
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getGlobalPropertyByUuid(String)")
	public void getGlobalPropertyByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() throws Exception {
		Assert.assertNull(Context.getAdministrationService().getGlobalPropertyByUuid("some invalid uuid"));
	}
	
	/**
	 * @see AdministrationService#saveGlobalProperties(List)
	 */
	@Test
	@Verifies(value = "should not fail with empty list", method = "saveGlobalProperties(List<QGlobalProperty;>)")
	public void saveGlobalProperties_shouldNotFailWithEmptyList() throws Exception {
		Context.getAdministrationService().saveGlobalProperties(new ArrayList<GlobalProperty>());
	}
	
	/**
	 * @see AdministrationService#saveGlobalProperties(List)
	 */
	@Test
	@Verifies(value = "should save all global properties to the database", method = "saveGlobalProperties(List<QGlobalProperty;>)")
	public void saveGlobalProperties_shouldSaveAllGlobalPropertiesToTheDatabase() throws Exception {
		// get the current global properties
		List<GlobalProperty> globalProperties = Context.getAdministrationService().getAllGlobalProperties();
		
		// and now add some new ones to it
		globalProperties.add(new GlobalProperty("new prop1", "new prop value1", "desc"));
		globalProperties.add(new GlobalProperty("new prop2", "new prop value2", "desc"));
		
		Context.getAdministrationService().saveGlobalProperties(globalProperties);
		
		Assert.assertEquals("new prop value1", Context.getAdministrationService().getGlobalProperty("new prop1"));
		Assert.assertEquals("new prop value2", Context.getAdministrationService().getGlobalProperty("new prop2"));
	}
	
	/**
	 * @see AdministrationService#saveGlobalProperties(List)
	 */
	@Test
	@Verifies(value = "should assign uuid to all new properties", method = "saveGlobalProperties(List<QGlobalProperty;>)")
	public void saveGlobalProperties_shouldAssignUuidToAllNewProperties() throws Exception {
		// get the current global properties
		List<GlobalProperty> globalProperties = Context.getAdministrationService().getAllGlobalProperties();
		
		// and now add a new one to it and save it
		globalProperties.add(new GlobalProperty("new prop", "new prop value", "desc"));
		Context.getAdministrationService().saveGlobalProperties(globalProperties);
		
		Assert.assertNotNull(Context.getAdministrationService().getGlobalPropertyObject("new prop").getUuid());
	}
	
	/**
	 * @see {@link AdministrationService#getAllGlobalProperties()}
	 */
	@Test
	@Verifies(value = "should return all global properties in the database", method = "getAllGlobalProperties()")
	public void getAllGlobalProperties_shouldReturnAllGlobalPropertiesInTheDatabase() throws Exception {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		Assert.assertEquals(19, Context.getAdministrationService().getAllGlobalProperties().size());
	}
	
	/**
	 * @see {@link AdministrationService#getAllowedLocales()}
	 */
	@Test
	@Verifies(value = "should return at least one locale if no locales defined in database yet", method = "getAllowedLocales()")
	public void getAllowedLocales_shouldReturnAtLeastOneLocaleIfNoLocalesDefinedInDatabaseYet() throws Exception {
		Assert.assertTrue(Context.getAdministrationService().getAllowedLocales().size() > 0);
	}
	
	/**
	 * @see {@link AdministrationService#getGlobalPropertyObject(String)}
	 */
	@Test
	@Verifies(value = "should return null when no global property match given property name", method = "getGlobalPropertyObject(String)")
	public void getGlobalPropertyObject_shouldReturnNullWhenNoGlobalPropertyMatchGivenPropertyName() throws Exception {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		Assert.assertNull(Context.getAdministrationService().getGlobalPropertyObject("magicResistSkill"));
	}
	
	/**
	 * @see {@link AdministrationService#getImplementationId()}
	 */
	@Test
	@Verifies(value = "should return null if no implementation id is defined yet", method = "getImplementationId()")
	public void getImplementationId_shouldReturnNullIfNoImplementationIdIsDefinedYet() throws Exception {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		Assert.assertNull(Context.getAdministrationService().getImplementationId());
	}
	
	/**
	 * @see {@link AdministrationService#getPresentationLocales()}
	 */
	@Test
	@Ignore
	//TODO: This test fails for some reason
	@Verifies(value = "should return at least one locale if no locales defined in database yet", method = "getPresentationLocales()")
	public void getPresentationLocales_shouldReturnAtLeastOneLocaleIfNoLocalesDefinedInDatabaseYet() throws Exception {
		Assert.assertTrue(Context.getAdministrationService().getPresentationLocales().size() > 0);
	}
	
	/**
	 * @see {@link AdministrationService#getPresentationLocales()}
	 */
	@Test
	@Verifies(value = "should not return more locales than message source service locales", method = "getPresentationLocales()")
	public void getPresentationLocales_shouldNotReturnMoreLocalesThanMessageSourceServiceLocales() throws Exception {
		Assert.assertFalse(Context.getAdministrationService().getPresentationLocales().size() > Context
		        .getMessageSourceService().getLocales().size());
	}
	
	/**
	 * @see {@link AdministrationService#getSystemVariables()}
	 */
	@Test
	@Verifies(value = "should return all registered system variables", method = "getSystemVariables()")
	public void getSystemVariables_shouldReturnAllRegisteredSystemVariables() throws Exception {
		// The method implementation adds 11 system variables
		Assert.assertEquals(11, Context.getAdministrationService().getSystemVariables().size());
	}
	
	/**
	 * @see {@link AdministrationService#purgeGlobalProperty(GlobalProperty)}
	 */
	@Test
	@Verifies(value = "should delete global property from database", method = "purgeGlobalProperty(GlobalProperty)")
	public void purgeGlobalProperty_shouldDeleteGlobalPropertyFromDatabase() throws Exception {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		AdministrationService as = Context.getAdministrationService();
		
		Assert.assertEquals(19, as.getAllGlobalProperties().size());
		as.purgeGlobalProperty(as.getGlobalPropertyObject("a_valid_gp_key"));
		Assert.assertEquals(18, as.getAllGlobalProperties().size());
	}
	
	/**
	 * @see {@link AdministrationService#saveGlobalProperty(GlobalProperty)}
	 */
	@Test
	@Verifies(value = "should create global property in database", method = "saveGlobalProperty(GlobalProperty)")
	public void saveGlobalProperty_shouldCreateGlobalPropertyInDatabase() throws Exception {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		AdministrationService as = Context.getAdministrationService();
		
		as.saveGlobalProperty(new GlobalProperty("detectHiddenSkill", "100"));
		Assert.assertNotNull(as.getGlobalProperty("detectHiddenSkill"));
	}
	
	/**
	 * @see {@link AdministrationService#saveGlobalProperty(GlobalProperty)}
	 */
	@Test
	@Verifies(value = "should overwrite global property if exists", method = "saveGlobalProperty(GlobalProperty)")
	public void saveGlobalProperty_shouldOverwriteGlobalPropertyIfExists() throws Exception {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		AdministrationService as = Context.getAdministrationService();
		
		GlobalProperty gp = as.getGlobalPropertyObject("a_valid_gp_key");
		Assert.assertEquals("correct-value", gp.getPropertyValue());
		gp.setPropertyValue("new-even-more-correct-value");
		as.saveGlobalProperty(gp);
		Assert.assertEquals("new-even-more-correct-value", as.getGlobalProperty("a_valid_gp_key"));
	}
	
	/**
	 * @see {@link AdministrationService#getAllowedLocales()}
	 */
	@Test
	@Verifies(value = "should not return duplicates even if the global property has them", method = "getAllowedLocales()")
	public void getAllowedLocales_shouldNotReturnDuplicatesEvenIfTheGlobalPropertyHasThem() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB,fr,es,en_GB"));
		Assert.assertEquals(3, Context.getAdministrationService().getAllowedLocales().size());
	}
	
	/**
	 * @see {@link AdministrationService#getGlobalPropertyValue()}
	 */
	@Test
	@Verifies(value = "should get property value in the proper type specified", method = "getGlobalPropertyValue()")
	public void getGlobalPropertyValue_shouldReturnValueInTheSpecifiedIntegerType() throws Exception {
		// put the global property into the database
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		Object value = adminService.getGlobalPropertyValue("valid.integer", Integer.valueOf(4));
		
		Assert.assertTrue(value instanceof Integer);
		Assert.assertEquals(Integer.valueOf(1234), value);
	}
	
	/**
	 * @see {@link AdministrationService#getGlobalPropertyValue()}
	 */
	@Test
	@Verifies(value = "should return default value if property name does not exist", method = "getGlobalPropertyValue()")
	public void getGlobalPropertyValue_shouldReturnDefaultValueForMissingProperty() throws Exception {
		// put the global property into the database
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		Object value = adminService.getGlobalPropertyValue("does.not.exist", Integer.valueOf(1234));
		
		Assert.assertEquals(Integer.valueOf(1234), value);
	}
	
	/**
	 * @see {@link AdministrationService#getGlobalPropertyValue()}
	 */
	@Test
	@Verifies(value = "should get property value in the proper type specified", method = "getGlobalPropertyValue()")
	public void getGlobalPropertyValue_shouldReturnValueInTheSpecifiedDoubleType() throws Exception {
		// put the global property into the database
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		Object retValue = adminService.getGlobalPropertyValue("valid.double", new Double(4.34));
		
		Assert.assertTrue(retValue instanceof Double);
		Assert.assertEquals(new Double(1234.54), retValue);
	}
	
	/**
	 * @see {@link AdministrationService#getGlobalProperty(String)}
	 */
	@Test
	@Verifies(value = "should get property in case insensitive way", method = "getGlobalProperty(String)")
	public void getGlobalProperty_shouldGetPropertyInCaseInsensitiveWay() throws Exception {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		// sanity check
		String orig = adminService.getGlobalProperty("another-global-property");
		Assert.assertEquals("anothervalue", orig);
		
		// try to get a global property with invalid case
		String noprop = adminService.getGlobalProperty("ANOTher-global-property");
		Assert.assertEquals(orig, noprop);
	}
	
	/**
	 * @see {@link AdministrationService#saveGlobalProperty(GlobalProperty)}
	 */
	@Test
	@Verifies(value = "should not allow different properties to have the same string with different case", method = "saveGlobalProperty(GlobalProperty)")
	public void saveGlobalProperty_shouldNotAllowDifferentPropertiesToHaveTheSameStringWithDifferentCase() throws Exception {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		// sanity check
		String orig = adminService.getGlobalProperty("another-global-property");
		Assert.assertEquals("anothervalue", orig);
		
		// should match current gp and update
		GlobalProperty gp = new GlobalProperty("ANOTher-global-property", "somethingelse");
		adminService.saveGlobalProperty(gp);
		String prop = adminService.getGlobalProperty("ANOTher-global-property", "boo");
		Assert.assertEquals("somethingelse", prop);
		
		orig = adminService.getGlobalProperty("another-global-property");
		Assert.assertEquals("somethingelse", orig);
	}
	
	/**
	 * @see {@link AdministrationService#saveGlobalProperties(List<QGlobalProperty;>)}
	 */
	@Test
	@Verifies(value = "should save properties with case difference only", method = "saveGlobalProperties(List<QGlobalProperty;>)")
	public void saveGlobalProperties_shouldSavePropertiesWithCaseDifferenceOnly() throws Exception {
		int originalSize = adminService.getAllGlobalProperties().size();
		
		List<GlobalProperty> props = new ArrayList<GlobalProperty>();
		props.add(new GlobalProperty("a.property.key", "something"));
		props.add(new GlobalProperty("a.property.KEY", "somethingelse"));
		adminService.saveGlobalProperties(props);
		
		// make sure that we now have two properties
		props = adminService.getAllGlobalProperties();
		Assert.assertEquals(originalSize + 1, props.size());
		
		Assert.assertTrue(props.contains(adminService.getGlobalPropertyObject("a.property.KEY")));
	}
	
	/**
	 * @see AdministrationService#purgeGlobalProperties(List)
	 * @verifies delete global properties from database
	 */
	@Test
	public void purgeGlobalProperties_shouldDeleteGlobalPropertiesFromDatabase() throws Exception {
		int originalSize = adminService.getAllGlobalProperties().size();
		
		List<GlobalProperty> props = new ArrayList<GlobalProperty>();
		props.add(new GlobalProperty("a.property.key", "something"));
		props.add(new GlobalProperty("a.property.KEY", "somethingelse"));
		adminService.saveGlobalProperties(props);
		int afterSaveSize = adminService.getAllGlobalProperties().size();
		
		Assert.assertEquals(originalSize + 1, afterSaveSize);
		
		adminService.purgeGlobalProperties(props);
		int afterPurgeSize = adminService.getAllGlobalProperties().size();
		
		Assert.assertEquals(originalSize, afterPurgeSize);
	}
	
	/**
	 * @see AdministrationService#saveGlobalProperty(GlobalProperty)
	 * @verifies save a global property whose typed value is handled by a custom datatype
	 */
	@Test
	public void saveGlobalProperty_shouldSaveAGlobalPropertyWhoseTypedValueIsHandledByACustomDatatype() throws Exception {
		GlobalProperty gp = new GlobalProperty();
		gp.setProperty("What time is it?");
		gp.setDatatypeClassname(DateDatatype.class.getName());
		gp.setValue(new Date());
		adminService.saveGlobalProperty(gp);
		Assert.assertNotNull(gp.getValueReference());
	}
	
	/**
	 * @see AdministrationService#getSearchLocales(User)
	 * @verifies exclude not allowed locales
	 */
	@Test
	public void getSearchLocales_shouldExcludeNotAllowedLocales() throws Exception {
		//given
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_US, en_GB, pl, es"));
		
		User user = Context.getAuthenticatedUser();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_PROFICIENT_LOCALES, "es_CL, en_US, pl");
		Context.getUserService().saveUser(user, null);
		
		//when
		List<Locale> searchLocales = Context.getAdministrationService().getSearchLocales();
		
		//then
		Assert.assertTrue("en_US", searchLocales.contains(new Locale("en", "US")));
		Assert.assertTrue("pl", searchLocales.contains(new Locale("pl")));
		Assert.assertTrue("es", searchLocales.contains(new Locale("es")));
		Assert.assertFalse("es_CL", searchLocales.contains(new Locale("es", "CL")));
	}
	
	/**
	 * @see AdministrationService#getSearchLocales(User)
	 * @verifies include currently selected full locale and langugage
	 */
	@Test
	public void getSearchLocales_shouldIncludeCurrentlySelectedFullLocaleAndLangugage() throws Exception {
		//given
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB"));
		User user = Context.getAuthenticatedUser();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_PROFICIENT_LOCALES, "");
		Context.getUserService().saveUser(user, null);
		Context.setLocale(new Locale("en", "GB"));
		
		//when
		List<Locale> searchLocales = Context.getAdministrationService().getSearchLocales();
		
		//then
		Assert.assertEquals(Context.getLocale(), searchLocales.get(0));
		Assert.assertEquals(new Locale(Context.getLocale().getLanguage()), searchLocales.get(1));
	}
	
	/**
	 * @see AdministrationService#getSearchLocales(User)
	 * @verifies include users proficient locales
	 */
	@Test
	public void getSearchLocales_shouldIncludeUsersProficientLocales() throws Exception {
		//given
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, en_US, pl"));
		
		User user = Context.getAuthenticatedUser();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_PROFICIENT_LOCALES, "en_GB, en_US");
		Context.getUserService().saveUser(user, null);
		
		//when
		List<Locale> searchLocales = Context.getAdministrationService().getSearchLocales();
		
		//then
		Assert.assertTrue("en_GB", searchLocales.contains(new Locale("en", "GB")));
		Assert.assertTrue("en_US", searchLocales.contains(new Locale("en", "US")));
		Assert.assertFalse("pl", searchLocales.contains(new Locale("pl")));
	}
	
	/**
	 * @see AdministrationService#validate(Object,Errors)
	 * @verifies throws APIException if the input is null
	 */
	
	@Test(expected = APIException.class)
	public void validate_shouldThrowThrowAPIExceptionIfTheInputIsNull() throws Exception {
		BindException errors = new BindException(new Object(), "");
		Context.getAdministrationService().validate(null, errors);
	}
	
	/**
	 * @see {@link AdministrationService#getPresentationLocales()}
	 */
	@Test
	@Verifies(value = "should return only country locale if both country locale and language locale are specified in allowed list", method = "getPresentationLocales()")
	public void getPresentationLocales_shouldReturnOnlyCountryLocaleIfBothCountryLocaleAndLanguageLocaleAreSpecifiedInAllowedList()
	        throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, es, es_CL"));
		
		List<Locale> locales = new ArrayList<Locale>();
		locales.add(new Locale("pl", "PL"));
		locales.add(new Locale("en"));
		locales.add(new Locale("es"));
		locales.add(new Locale("es", "CL"));
		
		MutableResourceBundleMessageSource mutableResourceBundleMessageSource = Mockito
		        .mock(MutableResourceBundleMessageSource.class);
		Mockito.when(mutableResourceBundleMessageSource.getLocales()).thenReturn(locales);
		
		MutableMessageSource mutableMessageSource = Context.getMessageSourceService().getActiveMessageSource();
		Context.getMessageSourceService().setActiveMessageSource(mutableResourceBundleMessageSource);
		
		Set<Locale> presentationLocales = Context.getAdministrationService().getPresentationLocales();
		
		Context.getMessageSourceService().setActiveMessageSource(mutableMessageSource);
		
		Assert.assertEquals(2, presentationLocales.size());
		Assert.assertTrue("en", presentationLocales.contains(new Locale("en")));
		Assert.assertTrue("es_CL", presentationLocales.contains(new Locale("es", "CL")));
	}
	
	/**
	 * @see {@link AdministrationService#getPresentationLocales()}
	 */
	@Test
	@Verifies(value = "should return all country locales if language locale and no country locales are specified in allowed list", method = "getPresentationLocales()")
	public void getPresentationLocales_shouldReturnAllCountryLocalesIfLanguageLocaleAndNoCountryLocalesAreSpecifiedInAllowedList()
	        throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, es"));
		
		List<Locale> locales = new ArrayList<Locale>();
		locales.add(new Locale("pl", "PL"));
		locales.add(new Locale("en"));
		locales.add(new Locale("es"));
		locales.add(new Locale("es", "CL"));
		locales.add(new Locale("es", "SN"));
		
		MutableResourceBundleMessageSource mutableResourceBundleMessageSource = Mockito
		        .mock(MutableResourceBundleMessageSource.class);
		Mockito.when(mutableResourceBundleMessageSource.getLocales()).thenReturn(locales);
		
		MutableMessageSource mutableMessageSource = Context.getMessageSourceService().getActiveMessageSource();
		Context.getMessageSourceService().setActiveMessageSource(mutableResourceBundleMessageSource);
		
		Set<Locale> presentationLocales = Context.getAdministrationService().getPresentationLocales();
		
		Context.getMessageSourceService().setActiveMessageSource(mutableMessageSource);
		
		Assert.assertEquals(3, presentationLocales.size());
		Assert.assertTrue("es_CL", presentationLocales.contains(new Locale("es", "CL")));
		Assert.assertTrue("es_SN", presentationLocales.contains(new Locale("es", "SN")));
		Assert.assertTrue("en", presentationLocales.contains(new Locale("en")));
	}
	
	/**
	 * @see {@link AdministrationService#getPresentationLocales()}
	 */
	@Test
	@Verifies(value = "should return language locale if country locale is specified in allowed list but country locale message file is missing", method = "getPresentationLocales()")
	public void getPresentationLocales_shouldReturnLanguageLocaleIfCountryLocaleIsSpecifiedInAllowedListButCountryLocaleMessageFileIsMissing()
	        throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, es_CL"));
		
		List<Locale> locales = new ArrayList<Locale>();
		locales.add(new Locale("pl", "PL"));
		locales.add(new Locale("en"));
		locales.add(new Locale("es"));
		
		MutableResourceBundleMessageSource mutableResourceBundleMessageSource = Mockito
		        .mock(MutableResourceBundleMessageSource.class);
		Mockito.when(mutableResourceBundleMessageSource.getLocales()).thenReturn(locales);
		
		MutableMessageSource mutableMessageSource = Context.getMessageSourceService().getActiveMessageSource();
		Context.getMessageSourceService().setActiveMessageSource(mutableResourceBundleMessageSource);
		
		Set<Locale> presentationLocales = Context.getAdministrationService().getPresentationLocales();
		
		Context.getMessageSourceService().setActiveMessageSource(mutableMessageSource);
		
		Assert.assertEquals(2, presentationLocales.size());
		Assert.assertTrue("en", presentationLocales.contains(new Locale("en")));
		Assert.assertTrue("es", presentationLocales.contains(new Locale("es")));
	}
	
	/**
	 * @see {@link AdministrationService#getPresentationLocales()}
	 */
	@Test
	@Verifies(value = "should return language locale if it is specified in allowed list and there are no country locale message files available", method = "getPresentationLocales()")
	public void getPresentationLocales_shouldReturnLanguageLocaleIfItIsSpecifiedInAllowedListAndThereAreNoCountryLocaleMessageFilesAvailable()
	        throws Exception {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, es"));
		
		List<Locale> locales = new ArrayList<Locale>();
		locales.add(new Locale("pl", "PL"));
		locales.add(new Locale("en"));
		locales.add(new Locale("es"));
		
		MutableResourceBundleMessageSource mutableResourceBundleMessageSource = Mockito
		        .mock(MutableResourceBundleMessageSource.class);
		Mockito.when(mutableResourceBundleMessageSource.getLocales()).thenReturn(locales);
		
		MutableMessageSource mutableMessageSource = Context.getMessageSourceService().getActiveMessageSource();
		Context.getMessageSourceService().setActiveMessageSource(mutableResourceBundleMessageSource);
		
		Set<Locale> presentationLocales = Context.getAdministrationService().getPresentationLocales();
		
		Context.getMessageSourceService().setActiveMessageSource(mutableMessageSource);
		
		Assert.assertEquals(2, presentationLocales.size());
		Assert.assertTrue("en", presentationLocales.contains(new Locale("en")));
		Assert.assertTrue("es", presentationLocales.contains(new Locale("es")));
	}
}
