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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openmrs.GlobalProperty;
import org.openmrs.ImplementationId;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.BooleanDatatype;
import org.openmrs.customdatatype.datatype.DateDatatype;
import org.openmrs.messagesource.MutableMessageSource;
import org.openmrs.messagesource.impl.MutableResourceBundleMessageSource;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.HttpClient;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.validation.BindException;

/**
 * Tests the {@link AdministrationService} using a database.
 * Unit tests are {@link AdministrationServiceUnitTest}.
 */
public class AdministrationServiceTest extends BaseContextSensitiveTest {
	
	private AdministrationService adminService;
	
	protected static final String ADMIN_INITIAL_DATA_XML = "org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml";
	
	private HttpClient implementationHttpClient;

	private CacheManager cacheManager;
	
	@BeforeEach
	public void runBeforeEachTest() {
		adminService = Context.getAdministrationService();
		implementationHttpClient = mock(HttpClient.class);
		adminService.setImplementationIdHttpClient(implementationHttpClient);
		cacheManager = Context.getRegisteredComponent("apiCacheManager", CacheManager.class);
	}
	
	@Test
	public void executeSQL_shouldExecuteSqlContainingGroupBy() {
		
		String sql = "select encounter1_.location_id, encounter1_.creator, encounter1_.encounter_type, encounter1_.form_id, location2_.location_id, count(obs0_.obs_id) from obs obs0_ right outer join encounter encounter1_ on obs0_.encounter_id=encounter1_.encounter_id inner join location location2_ on encounter1_.location_id=location2_.location_id inner join users user3_ on encounter1_.creator=user3_.user_id inner join person user3_1_ on user3_.user_id=user3_1_.person_id inner join encounter_type encountert4_ on encounter1_.encounter_type=encountert4_.encounter_type_id inner join form form5_ on encounter1_.form_id=form5_.form_id where encounter1_.date_created>='2007-05-05' and encounter1_.date_created<= '2008-05-05' group by encounter1_.location_id, encounter1_.creator , encounter1_.encounter_type , encounter1_.form_id";
		adminService.executeSQL(sql, true);
		
		String sql2 = "select encounter_id, count(*) from encounter encounter_id group by encounter_id";
		adminService.executeSQL(sql2, true);
	}
	
	@Test
	public void setImplementationId_shouldNotFailIfGivenImplementationIdIsNull() {
		// save a null impl id. no exception thrown
		adminService.setImplementationId(null);
		ImplementationId afterNull = adminService.getImplementationId();
		assertNull(afterNull, "There shouldn't be an impl id defined after setting a null impl id");
	}
	
	/**
	 * This uses a try/catch so that we can make sure no blank id is saved to the database.
	 * 
	 * @see AdministrationService#setImplementationId(ImplementationId)
	 */
	@Test()
	public void setImplementationId_shouldThrowAPIExceptionIfGivenEmptyImplementationIdObject() {
		// save a blank impl id. exception thrown
		assertThrows(APIException.class, () -> adminService.setImplementationId(new ImplementationId()));
		ImplementationId afterBlank = adminService.getImplementationId();
		assertNull(afterBlank, "There shouldn't be an impl id defined after setting a blank impl id");
	}
	
	/**
	 * This uses a try/catch so that we can make sure no blank id is saved to the database.
	 */
	@Test
	public void setImplementationId_shouldThrowAPIExceptionIfGivenACaretInTheImplementationIdCode() {
		// save an impl id with an invalid hl7 code
		ImplementationId invalidId = new ImplementationId();
		invalidId.setImplementationId("caret^caret");
		invalidId.setName("an invalid impl id for a unit test");
		invalidId.setPassphrase("some valid passphrase");
		invalidId.setDescription("Some valid description");
		assertThrows(APIException.class, () -> adminService.setImplementationId(invalidId));
		ImplementationId afterInvalid = adminService.getImplementationId();
		assertNull(afterInvalid, "There shouldn't be an impl id defined after setting an invalid impl id");
	}
	
	@Test
	public void setImplementationId_shouldThrowAPIExceptionIfGivenAPipeInTheImplementationIdCode() {
		// save an impl id with an invalid hl7 code
		ImplementationId invalidId2 = new ImplementationId();
		invalidId2.setImplementationId("pipe|pipe");
		invalidId2.setName("an invalid impl id for a unit test");
		invalidId2.setPassphrase("some valid passphrase");
		invalidId2.setDescription("Some valid description");
		assertThrows(APIException.class, () -> adminService.setImplementationId(invalidId2));
		ImplementationId afterInvalid2 = adminService.getImplementationId();
		assertNull(afterInvalid2, "There shouldn't be an impl id defined after setting an invalid impl id");
	}
	
	@Test
	@Disabled
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
	
	@Test
	@Disabled
	public void setImplementationId_shouldOverwriteImplementationIdInDatabaseIfExists() {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-general.xml");
		
		// sanity check to make sure we have an implementation id
		assertNotNull(adminService.getImplementationId());
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
	
	@Test
	@Disabled
	public void setImplementationId_shouldSetUuidOnImplementationIdGlobalProperty() {
		ImplementationId validId = new ImplementationId();
		validId.setImplementationId("JUNIT-TEST");
		validId.setName("JUNIT-TEST implementation id");
		validId.setPassphrase("This is the junit test passphrase");
		validId.setDescription("This is the junit impl id used for testing of the openmrs API only.");
		adminService.setImplementationId(validId);
		
		GlobalProperty gp = adminService.getGlobalPropertyObject(OpenmrsConstants.GLOBAL_PROPERTY_IMPLEMENTATION_ID);
		assertNotNull(gp.getUuid());
	}
	
	@Test
	public void getGlobalProperty_shouldNotFailWithNullPropertyName() {
		adminService.getGlobalProperty(null);
	}
	
	@Test
	public void getGlobalProperty_shouldGetPropertyValueGivenValidPropertyName() {
		// put the global property into the database
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		String propertyValue = adminService.getGlobalProperty("a_valid_gp_key");
		
		assertEquals("correct-value", propertyValue);
	}
	
	@Test
	public void getGlobalProperty_shouldNotFailWithNullDefaultValue() {
		adminService.getGlobalProperty("asdfsadfsafd", null);
	}
	
	@Test
	public void updateGlobalProperty_shouldUpdateGlobalPropertyInDatabase() {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		String propertyValue = adminService.getGlobalProperty("a_valid_gp_key");
		assertEquals("correct-value", propertyValue);
		
		adminService.updateGlobalProperty("a_valid_gp_key", "new-value");
		
		String newValue = adminService.getGlobalProperty("a_valid_gp_key");
		assertEquals("new-value", newValue);
	}
	
	@Test
	public void updateGlobalProperty_shouldFailIfGlobalPropertyBeingUpdatedDoesNotAlreadyExist() {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		assertThrows(IllegalStateException.class, () -> adminService.updateGlobalProperty("a_invalid_gp_key", "asdfsadfsafd"));
	}
	
	@Test
	public void updateGlobalProperty_shouldUpdateAGlobalPropertyWhoseTypedvalueIsHandledByACustomDatatype() {
		GlobalProperty gp = new GlobalProperty();
		gp.setProperty("Flag");
		gp.setDatatypeClassname(BooleanDatatype.class.getName());
		gp.setValue(Boolean.FALSE);
		adminService.saveGlobalProperty(gp);
		assertEquals(adminService.getGlobalProperty("Flag"), "false");
		
		adminService.updateGlobalProperty("Flag", Boolean.TRUE.toString());
		assertEquals(adminService.getGlobalProperty("Flag"), "true");
	}
	
	@Test
	public void setGlobalProperty_shouldCreateGlobalPropertyInDatabase() {
		String newKey = "new_gp_key";
		
		String initialValue = adminService.getGlobalProperty(newKey);
		assertNull(initialValue); // ensure gp doesn't exist before test
		adminService.setGlobalProperty(newKey, "new_key");
		
		String newValue = adminService.getGlobalProperty(newKey);
		assertNotNull(newValue);
	}
	
	@Test
	public void setGlobalProperty_shouldOverwriteGlobalPropertyIfExists() {
		
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		String propertyValue = adminService.getGlobalProperty("a_valid_gp_key");
		assertEquals("correct-value", propertyValue);
		
		adminService.setGlobalProperty("a_valid_gp_key", "new-value");
		
		String newValue = adminService.getGlobalProperty("a_valid_gp_key");
		assertEquals("new-value", newValue);
		
	}
	
	@Test
	public void setGlobalProperty_shouldSaveAGlobalPropertyWhoseTypedValueIsHandledByACustomDatatype() {
		
		String newKey = "Flag";
		String initialValue = adminService.getGlobalProperty(newKey);
		assertNull(initialValue);
		
		adminService.setGlobalProperty(newKey, Boolean.FALSE.toString());
		assertEquals(adminService.getGlobalProperty("Flag"), "false");
		
	}
	
	@Test
	public void getGlobalProperty_shouldReturnDefaultValueIfPropertyNameDoesNotExist() {
		String invalidKey = "asdfasdf";
		String propertyValue = adminService.getGlobalProperty(invalidKey);
		assertNull(propertyValue); // make sure there isn't a gp
		
		String value = adminService.getGlobalProperty(invalidKey, "default");
		assertEquals("default", value);
	}
	
	@Test
	public void getGlobalPropertiesByPrefix_shouldReturnAllRelevantGlobalPropertiesInTheDatabase() {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		List<GlobalProperty> properties = adminService.getGlobalPropertiesByPrefix("fake.module.");
		
		for (GlobalProperty property : properties) {
			assertTrue(property.getProperty().startsWith("fake.module."));
			assertTrue(property.getPropertyValue().startsWith("correct-value"));
		}
	}
	
	@Test
	public void getAllowedLocales_shouldNotFailIfNotGlobalPropertyForLocalesAllowedDefinedYet() {
		adminService.purgeGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST));
		adminService.getAllowedLocales();
	}
	
	@Test
	public void getGlobalPropertyByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "4f55827e-26fe-102b-80cb-0017a47871b3";
		GlobalProperty prop = adminService.getGlobalPropertyByUuid(uuid);
		assertEquals("locale.allowed.list", prop.getProperty());
	}
	
	@Test
	public void getGlobalPropertyByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(adminService.getGlobalPropertyByUuid("some invalid uuid"));
	}
	
	@Test
	public void saveGlobalProperties_shouldNotFailWithEmptyList() {
		adminService.saveGlobalProperties(new ArrayList<>());
	}
	
	@Test
	public void saveGlobalProperties_shouldSaveAllGlobalPropertiesToTheDatabase() {
		// get the current global properties
		List<GlobalProperty> globalProperties = adminService.getAllGlobalProperties();
		
		// and now add some new ones to it
		globalProperties.add(new GlobalProperty("new prop1", "new prop value1", "desc"));
		globalProperties.add(new GlobalProperty("new prop2", "new prop value2", "desc"));
		
		adminService.saveGlobalProperties(globalProperties);
		
		assertEquals("new prop value1", adminService.getGlobalProperty("new prop1"));
		assertEquals("new prop value2", adminService.getGlobalProperty("new prop2"));
	}
	
	@Test
	public void saveGlobalProperties_shouldAssignUuidToAllNewProperties() {
		// get the current global properties
		List<GlobalProperty> globalProperties = adminService.getAllGlobalProperties();
		
		// and now add a new one to it and save it
		globalProperties.add(new GlobalProperty("new prop", "new prop value", "desc"));
		adminService.saveGlobalProperties(globalProperties);
		
		assertNotNull(adminService.getGlobalPropertyObject("new prop").getUuid());
	}
	
	@Test
	public void getAllGlobalProperties_shouldReturnAllGlobalPropertiesInTheDatabase() {
		int allGlobalPropertiesSize = adminService.getAllGlobalProperties().size();
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		assertEquals(allGlobalPropertiesSize + 9, adminService.getAllGlobalProperties().size());
	}
	
	@Test
	public void getAllowedLocales_shouldReturnAtLeastOneLocaleIfNoLocalesDefinedInDatabaseYet() {
		assertTrue(adminService.getAllowedLocales().size() > 0);
	}
	
	@Test
	public void getGlobalPropertyObject_shouldReturnNullWhenNoGlobalPropertyMatchGivenPropertyName() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		assertNull(adminService.getGlobalPropertyObject("magicResistSkill"));
	}
	
	@Test
	public void getImplementationId_shouldReturnNullIfNoImplementationIdIsDefinedYet() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		assertNull(adminService.getImplementationId());
	}
	
	@Test
	@Disabled
	//TODO: This test fails for some reason
	public void getPresentationLocales_shouldReturnAtLeastOneLocaleIfNoLocalesDefinedInDatabaseYet() {
		assertTrue(adminService.getPresentationLocales().size() > 0);
	}
	
	@Test
	public void getPresentationLocales_shouldNotReturnMoreLocalesThanMessageSourceServiceLocales() {
		assertFalse(adminService.getPresentationLocales().size() > Context
		        .getMessageSourceService().getLocales().size());
	}
	
	@Test
	public void getSystemVariables_shouldReturnAllRegisteredSystemVariables() {
		// The method implementation adds 11 system variables
		assertEquals(11, adminService.getSystemVariables().size());
	}
	
	@Test
	public void purgeGlobalProperty_shouldDeleteGlobalPropertyFromDatabase() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		int allGlobalPropertiesSize = adminService.getAllGlobalProperties().size();
		adminService.purgeGlobalProperty(adminService.getGlobalPropertyObject("a_valid_gp_key"));
		assertEquals(allGlobalPropertiesSize -1, adminService.getAllGlobalProperties().size());
	}
	
	@Test
	public void saveGlobalProperty_shouldCreateGlobalPropertyInDatabase() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		
		adminService.saveGlobalProperty(new GlobalProperty("detectHiddenSkill", "100"));
		assertNotNull(adminService.getGlobalProperty("detectHiddenSkill"));
	}
	
	@Test
	public void saveGlobalProperty_shouldOverwriteGlobalPropertyIfExists() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		
		GlobalProperty gp = adminService.getGlobalPropertyObject("a_valid_gp_key");
		assertEquals("correct-value", gp.getPropertyValue());
		gp.setPropertyValue("new-even-more-correct-value");
		adminService.saveGlobalProperty(gp);
		assertEquals("new-even-more-correct-value", adminService.getGlobalProperty("a_valid_gp_key"));
	}
	
	@Test
	public void saveGlobalProperty_shouldFailIfGivenAllowedLocaleListDoesNotContainDefaultLocale() {

		String localeList = "fr,es";

		assertThat("localeList contains default locale but should not for this test case", localeList,
			not(containsString(LocaleUtility.getDefaultLocale().toString())));

		
		APIException exception = assertThrows(APIException.class, () -> adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, localeList)));
		assertThat(exception.getMessage(), containsString("can not be removed from allowed locales list because it is the default locale"));
	}

	@Test
	public void saveGlobalProperty_shouldFailIfDefaultLocaleNotInAllowedLocaleList() {

		Locale defaultLocale = new Locale("fr");


		APIException exception = assertThrows(APIException.class, () -> adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, defaultLocale.toString())));
		assertThat(exception.getMessage(), containsString("is not in allowed locales list"));
	}
	
	@Test
	public void getAllowedLocales_shouldNotReturnDuplicatesEvenIfTheGlobalPropertyHasThem() {
		adminService.saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB,fr,es,en_GB"));
		assertEquals(3, adminService.getAllowedLocales().size());
	}
	
	@Test
	public void getGlobalPropertyValue_shouldReturnValueInTheSpecifiedIntegerType() {
		// put the global property into the database
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		Object value = adminService.getGlobalPropertyValue("valid.integer", 4);
		
		assertTrue(value instanceof Integer);
		assertEquals(1234, value);
	}
	
	@Test
	public void getGlobalPropertyValue_shouldReturnDefaultValueForMissingProperty() {
		// put the global property into the database
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		Object value = adminService.getGlobalPropertyValue("does.not.exist", 1234);
		
		assertEquals(1234, value);
	}
	
	@Test
	public void getGlobalPropertyValue_shouldReturnValueInTheSpecifiedDoubleType() {
		// put the global property into the database
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		Object retValue = adminService.getGlobalPropertyValue("valid.double", 4.34);
		
		assertTrue(retValue instanceof Double);
		assertEquals(1234.54, retValue);
	}
	
	@Test
	public void getGlobalProperty_shouldGetPropertyInCaseInsensitiveWay() {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		// sanity check
		String orig = adminService.getGlobalProperty("another-global-property");
		assertEquals("anothervalue", orig);
		
		// try to get a global property with invalid case
		String noprop = adminService.getGlobalProperty("ANOTher-global-property");
		assertEquals(orig, noprop);
	}
	
	@Test
	public void saveGlobalProperty_shouldNotAllowDifferentPropertiesToHaveTheSameStringWithDifferentCase() {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		
		// sanity check
		String orig = adminService.getGlobalProperty("another-global-property");
		assertEquals("anothervalue", orig);
		
		// should match current gp and update
		GlobalProperty gp = new GlobalProperty("ANOTher-global-property", "somethingelse");
		adminService.saveGlobalProperty(gp);
		String prop = adminService.getGlobalProperty("ANOTher-global-property", "boo");
		assertEquals("somethingelse", prop);
		
		orig = adminService.getGlobalProperty("another-global-property");
		assertEquals("somethingelse", orig);
	}
	
	@Test
	public void saveGlobalProperties_shouldSavePropertiesWithCaseDifferenceOnly() {
		int originalSize = adminService.getAllGlobalProperties().size();
		
		List<GlobalProperty> props = new ArrayList<>();
		props.add(new GlobalProperty("a.property.key", "something"));
		props.add(new GlobalProperty("a.property.KEY", "somethingelse"));
		adminService.saveGlobalProperties(props);
		
		// make sure that we now have two properties
		props = adminService.getAllGlobalProperties();
		assertEquals(originalSize + 1, props.size());
		
		assertTrue(props.contains(adminService.getGlobalPropertyObject("a.property.KEY")));
	}
	
	@Test
	public void purgeGlobalProperties_shouldDeleteGlobalPropertiesFromDatabase() {
		int originalSize = adminService.getAllGlobalProperties().size();
		
		List<GlobalProperty> props = new ArrayList<>();
		props.add(new GlobalProperty("a.property.key", "something"));
		props.add(new GlobalProperty("a.property.KEY", "somethingelse"));
		adminService.saveGlobalProperties(props);
		int afterSaveSize = adminService.getAllGlobalProperties().size();
		
		assertEquals(originalSize + 1, afterSaveSize);
		
		adminService.purgeGlobalProperties(props);
		int afterPurgeSize = adminService.getAllGlobalProperties().size();
		
		assertEquals(originalSize, afterPurgeSize);
	}
	
	@Test
	public void saveGlobalProperty_shouldSaveAGlobalPropertyWhoseTypedValueIsHandledByACustomDatatype() {
		GlobalProperty gp = new GlobalProperty();
		gp.setProperty("What time is it?");
		gp.setDatatypeClassname(DateDatatype.class.getName());
		gp.setValue(new Date());
		adminService.saveGlobalProperty(gp);
		assertNotNull(gp.getValueReference());
	}
	
	@Test
	public void getSearchLocales_shouldExcludeNotAllowedLocales() {
		//given
		adminService.saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_US, en_GB, pl, es"));
		
		User user = Context.getAuthenticatedUser();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_PROFICIENT_LOCALES, "es_CL, en_US, pl");
		Context.getUserService().saveUser(user);
		
		//when
		List<Locale> searchLocales = adminService.getSearchLocales();
		
		//then
		assertTrue(searchLocales.contains(new Locale("en", "US")), "en_US");
		assertTrue(searchLocales.contains(new Locale("pl")), "pl");
		assertTrue(searchLocales.contains(new Locale("es")), "es");
		assertFalse(searchLocales.contains(new Locale("es", "CL")), "es_CL");
	}
	
	@Test
	public void getSearchLocales_shouldIncludeCurrentlySelectedFullLocaleAndLangugage() {
		//given
		adminService.saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB"));
		User user = Context.getAuthenticatedUser();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_PROFICIENT_LOCALES, "");
		Context.getUserService().saveUser(user);
		Context.setLocale(new Locale("en", "GB"));
		
		//when
		List<Locale> searchLocales = adminService.getSearchLocales();
		
		//then
		assertEquals(Context.getLocale(), searchLocales.get(0));
		assertEquals(new Locale(Context.getLocale().getLanguage()), searchLocales.get(1));
	}
	
	@Test
	public void getSearchLocales_shouldIncludeUsersProficientLocales() {
		//given
		adminService.saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, en_US, pl"));
		
		User user = Context.getAuthenticatedUser();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_PROFICIENT_LOCALES, "en_GB, en_US");
		Context.getUserService().saveUser(user);
		
		//when
		List<Locale> searchLocales = adminService.getSearchLocales();
		
		//then
		assertTrue(searchLocales.contains(new Locale("en", "GB")), "en_GB");
		assertTrue(searchLocales.contains(new Locale("en", "US")), "en_US");
		assertFalse(searchLocales.contains(new Locale("pl")), "pl");
	}
	
	@Test
	public void validate_shouldThrowThrowAPIExceptionIfTheInputIsNull() {
		BindException errors = new BindException(new Object(), "");
		assertThrows(APIException.class, () -> adminService.validate(null, errors));
	}
	
	@Test
	public void getPresentationLocales_shouldReturnOnlyCountryLocaleIfBothCountryLocaleAndLanguageLocaleAreSpecifiedInAllowedList()
	        {
		adminService.saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, es, es_CL"));
		
		List<Locale> locales = new ArrayList<>();
		locales.add(new Locale("pl", "PL"));
		locales.add(new Locale("en"));
		locales.add(new Locale("es"));
		locales.add(new Locale("es", "CL"));
		
		MutableResourceBundleMessageSource mutableResourceBundleMessageSource = Mockito
		        .mock(MutableResourceBundleMessageSource.class);
		Mockito.when(mutableResourceBundleMessageSource.getLocales()).thenReturn(locales);
		
		MutableMessageSource mutableMessageSource = Context.getMessageSourceService().getActiveMessageSource();
		Context.getMessageSourceService().setActiveMessageSource(mutableResourceBundleMessageSource);
		
		Set<Locale> presentationLocales = adminService.getPresentationLocales();
		
		Context.getMessageSourceService().setActiveMessageSource(mutableMessageSource);
		
		assertEquals(2, presentationLocales.size());
		assertTrue(presentationLocales.contains(new Locale("en")), "en");
		assertTrue(presentationLocales.contains(new Locale("es", "CL")), "es_CL");
	}
	
	@Test
	public void getPresentationLocales_shouldReturnAllCountryLocalesIfLanguageLocaleAndNoCountryLocalesAreSpecifiedInAllowedList()
	        {
		adminService.saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, es"));
		
		List<Locale> locales = new ArrayList<>();
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
		
		Set<Locale> presentationLocales = adminService.getPresentationLocales();
		
		Context.getMessageSourceService().setActiveMessageSource(mutableMessageSource);
		
		assertEquals(3, presentationLocales.size());
		assertTrue(presentationLocales.contains(new Locale("es", "CL")), "es_CL");
		assertTrue(presentationLocales.contains(new Locale("es", "SN")), "es_SN");
		assertTrue(presentationLocales.contains(new Locale("en")), "en");
	}
	
	@Test
	public void getPresentationLocales_shouldReturnLanguageLocaleIfCountryLocaleIsSpecifiedInAllowedListButCountryLocaleMessageFileIsMissing()
	        {
		adminService.saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, es_CL"));
		
		List<Locale> locales = new ArrayList<>();
		locales.add(new Locale("pl", "PL"));
		locales.add(new Locale("en"));
		locales.add(new Locale("es"));
		
		MutableResourceBundleMessageSource mutableResourceBundleMessageSource = Mockito
		        .mock(MutableResourceBundleMessageSource.class);
		Mockito.when(mutableResourceBundleMessageSource.getLocales()).thenReturn(locales);
		
		MutableMessageSource mutableMessageSource = Context.getMessageSourceService().getActiveMessageSource();
		Context.getMessageSourceService().setActiveMessageSource(mutableResourceBundleMessageSource);
		
		Set<Locale> presentationLocales = adminService.getPresentationLocales();
		
		Context.getMessageSourceService().setActiveMessageSource(mutableMessageSource);
		
		assertEquals(2, presentationLocales.size());
		assertTrue(presentationLocales.contains(new Locale("en")), "en");
		assertTrue(presentationLocales.contains(new Locale("es")), "es");
	}
	
	@Test
	public void getPresentationLocales_shouldReturnLanguageLocaleIfItIsSpecifiedInAllowedListAndThereAreNoCountryLocaleMessageFilesAvailable()
	        {
		adminService.saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, es"));
		
		List<Locale> locales = new ArrayList<>();
		locales.add(new Locale("pl", "PL"));
		locales.add(new Locale("en"));
		locales.add(new Locale("es"));
		
		MutableResourceBundleMessageSource mutableResourceBundleMessageSource = Mockito
		        .mock(MutableResourceBundleMessageSource.class);
		Mockito.when(mutableResourceBundleMessageSource.getLocales()).thenReturn(locales);
		
		MutableMessageSource mutableMessageSource = Context.getMessageSourceService().getActiveMessageSource();
		Context.getMessageSourceService().setActiveMessageSource(mutableResourceBundleMessageSource);
		
		Set<Locale> presentationLocales = adminService.getPresentationLocales();
		
		Context.getMessageSourceService().setActiveMessageSource(mutableMessageSource);
		
		assertEquals(2, presentationLocales.size());
		assertTrue(presentationLocales.contains(new Locale("en")), "en");
		assertTrue(presentationLocales.contains(new Locale("es")), "es");
	}
	
	@Test
	public void getPresentationLocales_shouldPreserveInsertionOrderInSetReturnedByMethod()
			{
		String globalPropertyLocaleListAllowedData = "en_GB, es, ja_JP, it_IT, pl_PL";
		//The order of languages and locales is described above and should be followed bt `presentationLocales` Set
		adminService.saveGlobalProperty(
				new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, globalPropertyLocaleListAllowedData));
		
		List<Locale> locales = new ArrayList<>();
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
		
		List<Locale> presentationLocales = new ArrayList<>(adminService.getPresentationLocales());
		
		Context.getMessageSourceService().setActiveMessageSource(mutableMessageSource);
		
		//Assert Locales in expected order as set by global property
		assertEquals(new Locale("en"), presentationLocales.get(0));
		assertEquals(new Locale("es"), presentationLocales.get(1));
		assertEquals(new Locale("it", "IT"), presentationLocales.get(2));
		assertEquals(new Locale("pl", "PL"), presentationLocales.get(3));
	}

	@Test
	public void getSearchLocales_shouldCacheResultsForAnUser() {
		//given
		adminService.saveGlobalProperty(
				new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, en_US, pl"));

		User user = Context.getAuthenticatedUser();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_PROFICIENT_LOCALES, "en_GB, en_US");
		Context.getUserService().saveUser(user);

		//when
		adminService.getSearchLocales();

		List<Locale> cachedSearchLocales = getCachedSearchLocalesForCurrentUser();

		//then
		assertThat(cachedSearchLocales, hasItem(Locale.ENGLISH));
		assertThat(cachedSearchLocales, hasItem(new Locale("en", "US")));
		assertThat(cachedSearchLocales, not(hasItem(new Locale("pl"))));
	}

	@Test
	public void saveGlobalProperty_shouldEvictCachedResults() {
		//given
		adminService.saveGlobalProperty(
				new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB, en_US, pl"));

		User user = Context.getAuthenticatedUser();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_PROFICIENT_LOCALES, "en_GB, en_US");
		Context.getUserService().saveUser(user);

		//sanity check that cache has been populated
		adminService.getSearchLocales();
		List<Locale> cachedSearchLocales = getCachedSearchLocalesForCurrentUser();
		assertThat(cachedSearchLocales, hasItem(new Locale("en", "US")));

		//evict cache
		adminService.saveGlobalProperty(new GlobalProperty("test", "TEST"));

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
