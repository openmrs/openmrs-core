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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
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
import org.openmrs.util.HttpClient;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
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

	private CacheManager cacheManager;
	
	/**
	 * Run this before each unit test in this class. It simply assigns the services used in this
	 * class to private variables The "@Before" method in {@link BaseContextSensitiveTest} is run
	 * right before this method and sets up the initial data set and authenticates to the Context
	 * 
	 */
	@Before
	public void runBeforeEachTest() {
		if (adminService == null) {
			adminService = Context.getAdministrationService();
			implementationHttpClient = mock(HttpClient.class);
			adminService.setImplementationIdHttpClient(implementationHttpClient);
			cacheManager = Context.getRegisteredComponent("apiCacheManager", CacheManager.class);
		}
		
	}
	
	/**
	 * Tests the AdministrationService.executeSql method with a sql statement containing a valid
	 * group by clause
	 * 
	 * @see AdministrationService#executeSQL(String,null)
	 */
	@Test
	public void executeSQL_shouldExecuteSqlContainingGroupBy() {
		
		String sql = "select encounter1_.location_id, encounter1_.creator, encounter1_.encounter_type, encounter1_.form_id, location2_.location_id, count(obs0_.obs_id) from obs obs0_ right outer join encounter encounter1_ on obs0_.encounter_id=encounter1_.encounter_id inner join location location2_ on encounter1_.location_id=location2_.location_id inner join users user3_ on encounter1_.creator=user3_.user_id inner join person user3_1_ on user3_.user_id=user3_1_.person_id inner join encounter_type encountert4_ on encounter1_.encounter_type=encountert4_.encounter_type_id inner join form form5_ on encounter1_.form_id=form5_.form_id where encounter1_.date_created>='2007-05-05' and encounter1_.date_created<= '2008-05-05' group by encounter1_.location_id, encounter1_.creator , encounter1_.encounter_type , encounter1_.form_id";
		adminService.executeSQL(sql, true);
		
		String sql2 = "select encounter_id, count(*) from encounter encounter_id group by encounter_id";
		adminService.executeSQL(sql2, true);
	}
	
	/**
	 * @see AdministrationService#setImplementationId(ImplementationId)
	 */
	@Test
	public void setImplementationId_shouldNotFailIfGivenImplementationIdIsNull() {
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
	public void setImplementationId_shouldThrowAPIExceptionIfGivenEmptyImplementationIdObject() {
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
	 * @see AdministrationService#setImplementationId(ImplementationId)
	 */
	@Test
	public void setImplementationId_shouldThrowAPIExceptionIfGivenACaretInTheImplementationIdCode() {
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
	 * @see AdministrationService#setImplementationId(ImplementationId)
	 */
	@Test
	public void setImplementationId_shouldThrowAPIExceptionIfGivenAPipeInTheImplementationIdCode() {
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
	public void setImplementationId_shouldCreateImplementationIdInDatabase() {
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
	public void setImplementationId_shouldOverwriteImplementationIdInDatabaseIfExists() {
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
	public void setImplementationId_shouldSetUuidOnImplementationIdGlobalProperty() {
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
	 * @see AdministrationService#getGlobalProperty(String)
	 */
	@Test
	public void getGlobalProperty_shouldNotFailWithNullPropertyName() {
		adminService.getGlobalProperty(null);
	}
	
	/**
	 * @see AdministrationService#getGlobalProperty(String)
	 */
	@Test
	public void getGlobalProperty_shouldGetPropertyValueGivenValidPropertyName() {
		// put the global property into the database
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		String propertyValue = adminService.getGlobalProperty("a_valid_gp_key");
		
		Assert.assertEquals("correct-value", propertyValue);
	}
	
	/**
	 * @see AdministrationService#getGlobalProperty(String,String)
	 */
	@Test
	public void getGlobalProperty_shouldNotFailWithNullDefaultValue() {
		adminService.getGlobalProperty("asdfsadfsafd", null);
	}
	
	/**
	 * @see AdministrationService#updateGlobalProperty(String,String)
	 */
	@Test
	public void updateGlobalProperty_shouldUpdateGlobalPropertyInDatabase() {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		String propertyValue = adminService.getGlobalProperty("a_valid_gp_key");
		Assert.assertEquals("correct-value", propertyValue);
		
		adminService.updateGlobalProperty("a_valid_gp_key", "new-value");
		
		String newValue = adminService.getGlobalProperty("a_valid_gp_key");
		Assert.assertEquals("new-value", newValue);
	}
	
	/**
	 * @see AdministrationService#updateGlobalProperty(String,String)
	 */
	@Test(expected = IllegalStateException.class)
	public void updateGlobalProperty_shouldFailIfGlobalPropertyBeingUpdatedDoesNotAlreadyExist() {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		adminService.updateGlobalProperty("a_invalid_gp_key", "asdfsadfsafd");
	}
	
	/**
	 * @see AdministrationService#updateGlobalProperty(String,String)
	 */
	@Test
	public void updateGlobalProperty_shouldUpdateAGlobalPropertyWhoseTypedvalueIsHandledByACustomDatatype() {
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
	 * @see AdministrationService#setGlobalProperty(String,String)
	 */
	@Test
	public void setGlobalProperty_shouldCreateGlobalPropertyInDatabase() {
		String newKey = "new_gp_key";
		
		String initialValue = adminService.getGlobalProperty(newKey);
		Assert.assertNull(initialValue); // ensure gp doesn't exist before test
		adminService.setGlobalProperty(newKey, "new_key");
		
		String newValue = adminService.getGlobalProperty(newKey);
		Assert.assertNotNull(newValue);
	}
	
	/**
	 * @see AdministrationService#setGlobalProperty(String,String)
	 */
	@Test
	public void setGlobalProperty_shouldOverwriteGlobalPropertyIfExists() {
		
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		String propertyValue = adminService.getGlobalProperty("a_valid_gp_key");
		Assert.assertEquals("correct-value", propertyValue);
		
		adminService.setGlobalProperty("a_valid_gp_key", "new-value");
		
		String newValue = adminService.getGlobalProperty("a_valid_gp_key");
		Assert.assertEquals("new-value", newValue);
		
	}
	
	/**
	 * @see AdministrationService#setGlobalProperty(String,String)
	 */
	@Test
	public void setGlobalProperty_shouldSaveAGlobalPropertyWhoseTypedValueIsHandledByACustomDatatype() {
		
		String newKey = "Flag";
		String initialValue = adminService.getGlobalProperty(newKey);
		Assert.assertNull(initialValue);
		
		adminService.setGlobalProperty(newKey, Boolean.FALSE.toString());
		Assert.assertEquals(adminService.getGlobalProperty("Flag"), "false");
		
	}
	
	/**
	 * @see AdministrationService#getGlobalProperty(String,String)
	 */
	@Test
	public void getGlobalProperty_shouldReturnDefaultValueIfPropertyNameDoesNotExist() {
		String invalidKey = "asdfasdf";
		String propertyValue = adminService.getGlobalProperty(invalidKey);
		Assert.assertNull(propertyValue); // make sure there isn't a gp
		
		String value = adminService.getGlobalProperty(invalidKey, "default");
		Assert.assertEquals("default", value);
	}
	
	/**
	 * @see AdministrationService#getGlobalPropertiesByPrefix(String)
	 */
	@Test
	public void getGlobalPropertiesByPrefix_shouldReturnAllRelevantGlobalPropertiesInTheDatabase() {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		List<GlobalProperty> properties = adminService.getGlobalPropertiesByPrefix("fake.module.");
		
		for (GlobalProperty property : properties) {
			Assert.assertTrue(property.getProperty().startsWith("fake.module."));
			Assert.assertTrue(property.getPropertyValue().startsWith("correct-value"));
		}
	}
	
	/**
	 * @see AdministrationService#getAllowedLocales()
	 */
	@Test
	public void getAllowedLocales_shouldNotFailIfNotGlobalPropertyForLocalesAllowedDefinedYet() {
		Context.getAdministrationService().purgeGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST));
		Context.getAdministrationService().getAllowedLocales();
	}
	
	/**
	 * @see AdministrationService#getGlobalPropertyByUuid(String)
	 */
	@Test
	public void getGlobalPropertyByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "4f55827e-26fe-102b-80cb-0017a47871b3";
		GlobalProperty prop = Context.getAdministrationService().getGlobalPropertyByUuid(uuid);
		Assert.assertEquals("locale.allowed.list", prop.getProperty());
	}
	
	/**
	 * @see AdministrationService#getGlobalPropertyByUuid(String)
	 */
	@Test
	public void getGlobalPropertyByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		Assert.assertNull(Context.getAdministrationService().getGlobalPropertyByUuid("some invalid uuid"));
	}
	
	/**
	 * @see AdministrationService#saveGlobalProperties(List)
	 */
	@Test
	public void saveGlobalProperties_shouldNotFailWithEmptyList() {
		Context.getAdministrationService().saveGlobalProperties(new ArrayList<GlobalProperty>());
	}
	
	/**
	 * @see AdministrationService#saveGlobalProperties(List)
	 */
	@Test
	public void saveGlobalProperties_shouldSaveAllGlobalPropertiesToTheDatabase() {
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
	public void saveGlobalProperties_shouldAssignUuidToAllNewProperties() {
		// get the current global properties
		List<GlobalProperty> globalProperties = Context.getAdministrationService().getAllGlobalProperties();
		
		// and now add a new one to it and save it
		globalProperties.add(new GlobalProperty("new prop", "new prop value", "desc"));
		Context.getAdministrationService().saveGlobalProperties(globalProperties);
		
		Assert.assertNotNull(Context.getAdministrationService().getGlobalPropertyObject("new prop").getUuid());
	}
	
	/**
	 * @see AdministrationService#getAllGlobalProperties()
	 */
	@Test
	public void getAllGlobalProperties_shouldReturnAllGlobalPropertiesInTheDatabase() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		Assert.assertEquals(21, Context.getAdministrationService().getAllGlobalProperties().size());
	}
	
	/**
	 * @see AdministrationService#getAllowedLocales()
	 */
	@Test
	public void getAllowedLocales_shouldReturnAtLeastOneLocaleIfNoLocalesDefinedInDatabaseYet() {
		Assert.assertTrue(Context.getAdministrationService().getAllowedLocales().size() > 0);
	}
	
	/**
	 * @see AdministrationService#getGlobalPropertyObject(String)
	 */
	@Test
	public void getGlobalPropertyObject_shouldReturnNullWhenNoGlobalPropertyMatchGivenPropertyName() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		Assert.assertNull(Context.getAdministrationService().getGlobalPropertyObject("magicResistSkill"));
	}
	
	/**
	 * @see AdministrationService#getImplementationId()
	 */
	@Test
	public void getImplementationId_shouldReturnNullIfNoImplementationIdIsDefinedYet() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		Assert.assertNull(Context.getAdministrationService().getImplementationId());
	}
	
	/**
	 * @see AdministrationService#getPresentationLocales()
	 */
	@Test
	@Ignore
	//TODO: This test fails for some reason
	public void getPresentationLocales_shouldReturnAtLeastOneLocaleIfNoLocalesDefinedInDatabaseYet() {
		Assert.assertTrue(Context.getAdministrationService().getPresentationLocales().size() > 0);
	}
	
	/**
	 * @see AdministrationService#getPresentationLocales()
	 */
	@Test
	public void getPresentationLocales_shouldNotReturnMoreLocalesThanMessageSourceServiceLocales() {
		Assert.assertFalse(Context.getAdministrationService().getPresentationLocales().size() > Context
		        .getMessageSourceService().getLocales().size());
	}
	
	/**
	 * @see AdministrationService#getSystemVariables()
	 */
	@Test
	public void getSystemVariables_shouldReturnAllRegisteredSystemVariables() {
		// The method implementation adds 11 system variables
		Assert.assertEquals(11, Context.getAdministrationService().getSystemVariables().size());
	}
	
	/**
	 * @see AdministrationService#purgeGlobalProperty(GlobalProperty)
	 */
	@Test
	public void purgeGlobalProperty_shouldDeleteGlobalPropertyFromDatabase() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		AdministrationService as = Context.getAdministrationService();
		
		Assert.assertEquals(21, as.getAllGlobalProperties().size());
		as.purgeGlobalProperty(as.getGlobalPropertyObject("a_valid_gp_key"));
		Assert.assertEquals(20, as.getAllGlobalProperties().size());
	}
	
	/**
	 * @see AdministrationService#saveGlobalProperty(GlobalProperty)
	 */
	@Test
	public void saveGlobalProperty_shouldCreateGlobalPropertyInDatabase() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		AdministrationService as = Context.getAdministrationService();
		
		as.saveGlobalProperty(new GlobalProperty("detectHiddenSkill", "100"));
		Assert.assertNotNull(as.getGlobalProperty("detectHiddenSkill"));
	}
	
	/**
	 * @see AdministrationService#saveGlobalProperty(GlobalProperty)
	 */
	@Test
	public void saveGlobalProperty_shouldOverwriteGlobalPropertyIfExists() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		AdministrationService as = Context.getAdministrationService();
		
		GlobalProperty gp = as.getGlobalPropertyObject("a_valid_gp_key");
		Assert.assertEquals("correct-value", gp.getPropertyValue());
		gp.setPropertyValue("new-even-more-correct-value");
		as.saveGlobalProperty(gp);
		Assert.assertEquals("new-even-more-correct-value", as.getGlobalProperty("a_valid_gp_key"));
	}
	
	/**
	 * @see AdministrationService#getAllowedLocales()
	 */
	@Test
	public void getAllowedLocales_shouldNotReturnDuplicatesEvenIfTheGlobalPropertyHasThem() {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB,fr,es,en_GB"));
		Assert.assertEquals(3, Context.getAdministrationService().getAllowedLocales().size());
	}
	
	/**
	 * @see AdministrationService#getGlobalPropertyValue()
	 */
	@Test
	public void getGlobalPropertyValue_shouldReturnValueInTheSpecifiedIntegerType() {
		// put the global property into the database
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		Object value = adminService.getGlobalPropertyValue("valid.integer", Integer.valueOf(4));
		
		Assert.assertTrue(value instanceof Integer);
		Assert.assertEquals(Integer.valueOf(1234), value);
	}
	
	/**
	 * @see AdministrationService#getGlobalPropertyValue()
	 */
	@Test
	public void getGlobalPropertyValue_shouldReturnDefaultValueForMissingProperty() {
		// put the global property into the database
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		Object value = adminService.getGlobalPropertyValue("does.not.exist", Integer.valueOf(1234));
		
		Assert.assertEquals(Integer.valueOf(1234), value);
	}
	
	/**
	 * @see AdministrationService#getGlobalPropertyValue()
	 */
	@Test
	public void getGlobalPropertyValue_shouldReturnValueInTheSpecifiedDoubleType() {
		// put the global property into the database
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		Object retValue = adminService.getGlobalPropertyValue("valid.double", new Double(4.34));
		
		Assert.assertTrue(retValue instanceof Double);
		Assert.assertEquals(new Double(1234.54), retValue);
	}
	
	/**
	 * @see AdministrationService#getGlobalProperty(String)
	 */
	@Test
	public void getGlobalProperty_shouldGetPropertyInCaseInsensitiveWay() {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		// sanity check
		String orig = adminService.getGlobalProperty("another-global-property");
		Assert.assertEquals("anothervalue", orig);
		
		// try to get a global property with invalid case
		String noprop = adminService.getGlobalProperty("ANOTher-global-property");
		Assert.assertEquals(orig, noprop);
	}
	
	/**
	 * @see AdministrationService#saveGlobalProperty(GlobalProperty)
	 */
	@Test
	public void saveGlobalProperty_shouldNotAllowDifferentPropertiesToHaveTheSameStringWithDifferentCase() {
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
	 * @see AdministrationService#saveGlobalProperties(List<QGlobalProperty;>)
	 */
	@Test
	public void saveGlobalProperties_shouldSavePropertiesWithCaseDifferenceOnly() {
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
	 */
	@Test
	public void purgeGlobalProperties_shouldDeleteGlobalPropertiesFromDatabase() {
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
	 */
	@Test
	public void saveGlobalProperty_shouldSaveAGlobalPropertyWhoseTypedValueIsHandledByACustomDatatype() {
		GlobalProperty gp = new GlobalProperty();
		gp.setProperty("What time is it?");
		gp.setDatatypeClassname(DateDatatype.class.getName());
		gp.setValue(new Date());
		adminService.saveGlobalProperty(gp);
		Assert.assertNotNull(gp.getValueReference());
	}
	
	/**
	 * @see AdministrationService#getSearchLocales(User)
	 */
	@Test
	public void getSearchLocales_shouldExcludeNotAllowedLocales() {
		//given
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_US, en_GB, pl, es"));
		
		User user = Context.getAuthenticatedUser();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_PROFICIENT_LOCALES, "es_CL, en_US, pl");
		Context.getUserService().saveUser(user);
		
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
	 */
	@Test
	public void getSearchLocales_shouldIncludeCurrentlySelectedFullLocaleAndLangugage() {
		//given
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB"));
		User user = Context.getAuthenticatedUser();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_PROFICIENT_LOCALES, "");
		Context.getUserService().saveUser(user);
		Context.setLocale(new Locale("en", "GB"));
		
		//when
		List<Locale> searchLocales = Context.getAdministrationService().getSearchLocales();
		
		//then
		Assert.assertEquals(Context.getLocale(), searchLocales.get(0));
		Assert.assertEquals(new Locale(Context.getLocale().getLanguage()), searchLocales.get(1));
	}
	
	/**
	 * @see AdministrationService#getSearchLocales(User)
	 */
	@Test
	public void getSearchLocales_shouldIncludeUsersProficientLocales() {
		//given
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, en_US, pl"));
		
		User user = Context.getAuthenticatedUser();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_PROFICIENT_LOCALES, "en_GB, en_US");
		Context.getUserService().saveUser(user);
		
		//when
		List<Locale> searchLocales = Context.getAdministrationService().getSearchLocales();
		
		//then
		Assert.assertTrue("en_GB", searchLocales.contains(new Locale("en", "GB")));
		Assert.assertTrue("en_US", searchLocales.contains(new Locale("en", "US")));
		Assert.assertFalse("pl", searchLocales.contains(new Locale("pl")));
	}
	
	/**
	 * @see AdministrationService#validate(Object,Errors)
	 */
	
	@Test(expected = APIException.class)
	public void validate_shouldThrowThrowAPIExceptionIfTheInputIsNull() {
		BindException errors = new BindException(new Object(), "");
		Context.getAdministrationService().validate(null, errors);
	}
	
	/**
	 * @see AdministrationService#getPresentationLocales()
	 */
	@Test
	public void getPresentationLocales_shouldReturnOnlyCountryLocaleIfBothCountryLocaleAndLanguageLocaleAreSpecifiedInAllowedList()
	        {
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
	 * @see AdministrationService#getPresentationLocales()
	 */
	@Test
	public void getPresentationLocales_shouldReturnAllCountryLocalesIfLanguageLocaleAndNoCountryLocalesAreSpecifiedInAllowedList()
	        {
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
	 * @see AdministrationService#getPresentationLocales()
	 */
	@Test
	public void getPresentationLocales_shouldReturnLanguageLocaleIfCountryLocaleIsSpecifiedInAllowedListButCountryLocaleMessageFileIsMissing()
	        {
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
	 * @see AdministrationService#getPresentationLocales()
	 */
	@Test
	public void getPresentationLocales_shouldReturnLanguageLocaleIfItIsSpecifiedInAllowedListAndThereAreNoCountryLocaleMessageFilesAvailable()
	        {
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
	
	/**
	 * @see AdministrationService#getPresentationLocales()
	 */
	@Test
	public void getPresentationLocales_shouldPreserveInsertionOrderInSetReturnedByMethod()
			{
		String globalPropertyLocaleListAllowedData = "en_GB, es, ja_JP, it_IT, pl_PL";
		//The order of languages and locales is described above and should be followed bt `presentationLocales` Set
		Context.getAdministrationService().saveGlobalProperty(
				new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, globalPropertyLocaleListAllowedData));
		
		List<Locale> locales = new ArrayList<Locale>();
		//Add data in random order and verify that order is maintained in the end by checking against order in global property
		locales.add(new Locale("pl", "PL"));
		locales.add(new Locale("es"));
		locales.add(new Locale("en"));
		locales.add(new Locale("it", "IT"));
		
		MutableResourceBundleMessageSource mutableResourceBundleMessageSource = Mockito
				.mock(MutableResourceBundleMessageSource.class);
		Mockito.when(mutableResourceBundleMessageSource.getLocales()).thenReturn(locales);
		
		MutableMessageSource mutableMessageSource = Context.getMessageSourceService().getActiveMessageSource();
		Context.getMessageSourceService().setActiveMessageSource(mutableResourceBundleMessageSource);
		
		List<Locale> presentationLocales = new ArrayList<Locale>(Context.getAdministrationService().getPresentationLocales());
		
		Context.getMessageSourceService().setActiveMessageSource(mutableMessageSource);
		
		//Assert Locales in expected order as set by global property
		Assert.assertEquals(new Locale("en"), presentationLocales.get(0));
		Assert.assertEquals(new Locale("es"), presentationLocales.get(1));
		Assert.assertEquals(new Locale("it", "IT"), presentationLocales.get(2));
		Assert.assertEquals(new Locale("pl", "PL"), presentationLocales.get(3));
	}

	/**
	 * @see AdministrationService#getSearchLocales()
	 */
	@Test
	public void getSearchLocales_shouldCacheResultsForAnUser() {
		//given
		Context.getAdministrationService().saveGlobalProperty(
				new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, en_US, pl"));

		User user = Context.getAuthenticatedUser();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_PROFICIENT_LOCALES, "en_GB, en_US");
		Context.getUserService().saveUser(user);

		//when
		Context.getAdministrationService().getSearchLocales();

		List<Locale> cachedSearchLocales = getCachedSearchLocalesForCurrentUser();

		//then
		assertThat(cachedSearchLocales, hasItem(Locale.ENGLISH));
		assertThat(cachedSearchLocales, hasItem(new Locale("en", "US")));
		assertThat(cachedSearchLocales, not(hasItem(new Locale("pl"))));
	}

	/**
	 * @see AdministrationService#saveGlobalProperty(GlobalProperty)
	 */
	@Test
	public void saveGlobalProperty_shouldEvictCachedResults() {
		//given
		Context.getAdministrationService().saveGlobalProperty(
				new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, en_US, pl"));

		User user = Context.getAuthenticatedUser();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_PROFICIENT_LOCALES, "en_GB, en_US");
		Context.getUserService().saveUser(user);

		//sanity check that cache has been populated
		Context.getAdministrationService().getSearchLocales();
		List<Locale> cachedSearchLocales = getCachedSearchLocalesForCurrentUser();
		assertThat(cachedSearchLocales, hasItem(new Locale("en", "US")));

		//evict cache
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty("test", "TEST"));

		assertThat(getCacheForCurrentUser(), nullValue());
	}

	private Cache.ValueWrapper getCacheForCurrentUser(){
		Object[] params = { Context.getLocale(), Context.getAuthenticatedUser() };
		Object key = (new SimpleKeyGenerator()).generate(null, null, params);
		return cacheManager.getCache("userSearchLocales").get(key);
	}

	private List<Locale> getCachedSearchLocalesForCurrentUser() {
		return (List<Locale>) getCacheForCurrentUser().get();
	}
}
