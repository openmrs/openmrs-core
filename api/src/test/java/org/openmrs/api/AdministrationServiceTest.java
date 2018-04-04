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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.openmrs.GlobalProperty;
import org.openmrs.ImplementationId;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.AdministrationServiceImpl;
import org.openmrs.api.impl.PersonNameGlobalPropertyListener;
import org.openmrs.customdatatype.datatype.BooleanDatatype;
import org.openmrs.customdatatype.datatype.DateDatatype;
import org.openmrs.messagesource.MutableMessageSource;
import org.openmrs.messagesource.impl.MutableResourceBundleMessageSource;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleUtil;
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

	protected static final String ADMIN_INITIAL_DATA_XML = "org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml";

	private AdministrationService adminService = null;

	private HttpClient implementationHttpClient;

	private CacheManager cacheManager;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	/**
	 * Run this before each unit test in this class. It simply assigns the services used in this
	 * class to private variables The "@Before" method in {@link BaseContextSensitiveTest} is run
	 * right before this method and sets up the initial data set and authenticates to the Context
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

	@Test
	public void getSystemInformation_shouldReturnSystemInformation() {

		LinkedHashMap<String, String> openmrsInformation = new LinkedHashMap<>();
		openmrsInformation.put("SystemInfo.OpenMRSInstallation.systemDate",
			new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
		openmrsInformation.put("SystemInfo.OpenMRSInstallation.systemTime",
			new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		openmrsInformation.put("SystemInfo.OpenMRSInstallation.openmrsVersion", OpenmrsConstants.OPENMRS_VERSION);
		try {
			openmrsInformation.put("SystemInfo.hostname", InetAddress.getLocalHost().getCanonicalHostName());
		}
		catch (UnknownHostException e) {
			openmrsInformation.put("SystemInfo.hostname", "Unknown host: " + e.getMessage());
		}

		LinkedHashMap<String, String> javaRuntimeEnvironmentInformation = new LinkedHashMap<String, String>() {

			Properties properties = System.getProperties();

			{
				put("SystemInfo.JavaRuntimeEnv.operatingSystem", properties.getProperty("os.name"));
				put("SystemInfo.JavaRuntimeEnv.operatingSystemArch", properties.getProperty("os.arch"));
				put("SystemInfo.JavaRuntimeEnv.operatingSystemVersion", properties.getProperty("os.version"));
				put("SystemInfo.JavaRuntimeEnv.javaVersion", properties.getProperty("java.version"));
				put("SystemInfo.JavaRuntimeEnv.javaVendor", properties.getProperty("java.vendor"));
				put("SystemInfo.JavaRuntimeEnv.jvmVersion", properties.getProperty("java.vm.version"));
				put("SystemInfo.JavaRuntimeEnv.jvmVendor", properties.getProperty("java.vm.vendor"));
				put("SystemInfo.JavaRuntimeEnv.javaRuntimeName", properties.getProperty("java.runtime.name"));
				put("SystemInfo.JavaRuntimeEnv.javaRuntimeVersion", properties.getProperty("java.runtime.version"));
				put("SystemInfo.JavaRuntimeEnv.userName", properties.getProperty("user.name"));
				put("SystemInfo.JavaRuntimeEnv.systemLanguage", properties.getProperty("user.language"));
				put("SystemInfo.JavaRuntimeEnv.systemTimezone", properties.getProperty("user.timezone"));
				put("SystemInfo.JavaRuntimeEnv.fileSystemEncoding", properties.getProperty("sun.jnu.encoding"));
				put("SystemInfo.JavaRuntimeEnv.userDirectory", properties.getProperty("user.dir"));
				put("SystemInfo.JavaRuntimeEnv.tempDirectory", properties.getProperty("java.io.tmpdir"));
			}
		};

		LinkedHashMap<String, String> dataBaseInformation = new LinkedHashMap<String, String>() {

			Properties properties = Context.getRuntimeProperties();

			{
				put("SystemInfo.Database.name", OpenmrsConstants.DATABASE_NAME);
				put("SystemInfo.Database.connectionURL", properties.getProperty("connection.url"));
				put("SystemInfo.Database.userName", properties.getProperty("connection.username"));
				put("SystemInfo.Database.driver", properties.getProperty("hibernate.connection.driver_class"));
				put("SystemInfo.Database.dialect", properties.getProperty("hibernate.dialect"));

			}
		};

		LinkedHashMap<String, String> moduleInformation = new LinkedHashMap<String, String>() {

			{
				put("SystemInfo.Module.repositoryPath", ModuleUtil.getModuleRepository().getAbsolutePath());
				Collection<Module> loadedModules = ModuleFactory.getLoadedModules();
				for (Module module : loadedModules) {
					String moduleInfo = module.getVersion() + " "
						+ (module.isStarted() ? "" : Context.getMessageSourceService().getMessage("Module.notStarted"));
					put(module.getName(), moduleInfo);
				}
			}
		};

		Map<String, Map<String, String>> systemInfoMap = adminService.getSystemInformation();

		assertEquals(moduleInformation, systemInfoMap.get("SystemInfo.title.moduleInformation"));
		assertEquals(dataBaseInformation, systemInfoMap.get("SystemInfo.title.dataBaseInformation"));
		assertEquals(javaRuntimeEnvironmentInformation,
			systemInfoMap.get("SystemInfo.title.javaRuntimeEnvironmentInformation"));
		assertEquals(openmrsInformation, systemInfoMap.get("SystemInfo.title.openmrsInformation"));
		assertEquals(5, systemInfoMap.size());
	}

	@Test
	public void getSystemInformation_shouldAddAllLoadedModulesInformation() {
		int numberOfModulesOld = ModuleFactory.getLoadedModules().size();
		Module module = new Module("name", "moduleId", "packageName", "author", "description", "version");
		ModuleFactory.loadModule(module, false);
		int numberOfModulesNew = ModuleFactory.getLoadedModules().size();

		Map<String, Map<String, String>> systemInfoMap = adminService.getSystemInformation();

		assertEquals(numberOfModulesNew, numberOfModulesOld + 1);
		assertEquals(numberOfModulesNew + 1, systemInfoMap.get("SystemInfo.title.moduleInformation").size());
	}
	
	@Test
	public void removeGlobalPropertyListener_shouldNotFailOnNullListener() {
		adminService.removeGlobalPropertyListener(null);
	}

	@Test
	public void removeGlobalPropertyListener_shouldNotFailIfListenerNotNull() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		EventListeners eventListener = new EventListeners();
		List<GlobalPropertyListener> globalPropertyListenerList = new ArrayList<>();
		PersonNameGlobalPropertyListener personNameGlobalPropertyListener = new PersonNameGlobalPropertyListener();

		personNameGlobalPropertyListener.supportsPropertyName("a_valid_gp_key");
		globalPropertyListenerList.add(personNameGlobalPropertyListener);
		eventListener.setGlobalPropertyListeners(globalPropertyListenerList);

		assertNotNull(eventListener.getGlobalPropertyListeners());
		adminService.removeGlobalPropertyListener(personNameGlobalPropertyListener);
	}

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

	@Test()
	public void setImplementationId_shouldThrowAPIExceptionIfGivenEmptyImplementationIdObject() {
		// save a blank impl id. exception thrown
		expectedException.expect(APIException.class);

		adminService.setImplementationId(new ImplementationId());
		fail("An exception should be thrown on a blank impl id save");

		ImplementationId afterBlank = adminService.getImplementationId();
		assertNull("There shouldn't be an impl id defined after setting a blank impl id", afterBlank);
	}
	
	@Test
	public void setImplementationId_shouldThrowAPIExceptionIfGivenACaretInTheImplementationIdCode() {
		// save an impl id with an invalid hl7 code
		ImplementationId invalidId = new ImplementationId();
		invalidId.setImplementationId("caret^caret");
		invalidId.setName("an invalid impl id for a unit test");
		invalidId.setPassphrase("some valid passphrase");
		invalidId.setDescription("Some valid description");
		expectedException.expect(APIException.class);
		
		adminService.setImplementationId(invalidId);
		fail("An exception should be thrown on an invalid impl id save");

		ImplementationId afterInvalid = adminService.getImplementationId();
		assertNull("There shouldn't be an impl id defined after setting an invalid impl id", afterInvalid);
	}
	
	@Test
	public void setImplementationId_shouldThrowAPIExceptionIfGivenAPipeInTheImplementationIdCode() {
		// save an impl id with an invalid hl7 code
		ImplementationId invalidId2 = new ImplementationId();
		invalidId2.setImplementationId("pipe|pipe");
		invalidId2.setName("an invalid impl id for a unit test");
		invalidId2.setPassphrase("some valid passphrase");
		invalidId2.setDescription("Some valid description");
		expectedException.expect(APIException.class);
		
		adminService.setImplementationId(invalidId2);
		fail("An exception should be thrown on an invalid impl id save");
			
		ImplementationId afterInvalid2 = adminService.getImplementationId();
		assertNull("There shouldn't be an impl id defined after setting an invalid impl id", afterInvalid2);
	}

	@Test
	public void setImplementationId_shouldCreateImplementationIdInDatabase() {
		// save a valid impl id
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-general.xml");
		ImplementationId validId = new ImplementationId();
		validId.setImplementationId("JUNIT-TEST-3");
		validId.setName("JUNIT-TEST-3 implementation id");
		validId.setPassphrase("This is a junit test passphrase");
		validId.setDescription("This is the junit impl id used for testing of the openmrs API only.");
		expectedException.expect(APIException.class);
		
		adminService.setImplementationId(validId);
		
		assertNotNull(adminService.getImplementationId());
	}
	
	@Test
	public void setImplementationId_shouldOverwriteImplementationIdInDatabaseIfExists() {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-general.xml");
		// save a second valid id
		ImplementationId validId2 = new ImplementationId();
		validId2.setImplementationId("JUNIT-TEST");
		validId2.setName("JUNIT-TEST (#2) implementation id");
		validId2.setPassphrase("This is the junit test passphrase 2");
		validId2.setDescription("This is the junit impl id (2) used for testing of the openmrs API only.");
		expectedException.expect(APIException.class);
		
		adminService.setImplementationId(validId2);
		
		assertEquals(validId2, adminService.getImplementationId());
	}
	
	@Test
	public void setImplementationId_shouldSerializeAndSaveTheImplementationId() {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-general.xml");
		ImplementationId validId = new ImplementationId();
		validId.setImplementationId("JUNIT-TEST");
		validId.setName("JUNIT-TEST implementation id");
		validId.setPassphrase("This is the junit test passphrase");
		validId.setDescription("This is the junit impl id used for testing of the openmrs API only.");
		expectedException.expect(APIException.class);
		
		adminService.setImplementationId(validId);
		
		GlobalProperty gp = adminService.getGlobalPropertyObject(OpenmrsConstants.GLOBAL_PROPERTY_IMPLEMENTATION_ID);
		assertNotNull(gp.getPropertyValue());
	}
	
	@Test
	public void getGlobalProperty_shouldNotFailWithNullPropertyName() {
		assertNull(adminService.getGlobalProperty(null));
	}

	/**
	 * @see AdministrationService#getGlobalProperty(String)
	 */
	@Test
	public void getGlobalProperty_shouldGetPropertyValueGivenValidPropertyName() {
		// put the global property into the database
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");

		String propertyValue = adminService.getGlobalProperty("a_valid_gp_key");

		assertEquals("correct-value", propertyValue);
	}

	@Test
	public void getGlobalProperty_shouldNotFailWithNullDefaultValue() {
		assertNull(adminService.getGlobalProperty("asdfsadfsafd", null));
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

	@Test(expected = IllegalStateException.class)
	public void updateGlobalProperty_shouldFailIfGlobalPropertyBeingUpdatedDoesNotAlreadyExist() {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		adminService.updateGlobalProperty("a_invalid_gp_key", "asdfsadfsafd");
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

	/**
	 * @see AdministrationService#setGlobalProperty(String, String)
	 */
	@Test
	public void setGlobalProperty_shouldOverwriteGlobalPropertyIfExists() {

		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");

		String propertyValue = adminService.getGlobalProperty("a_valid_gp_key");
		assertEquals("correct-value", propertyValue);

		adminService.setGlobalProperty("a_valid_gp_key", "new-value");

		String newValue = adminService.getGlobalProperty("a_valid_gp_key");
		assertEquals("new-value", newValue);

	}

	/**
	 * @see AdministrationService#setGlobalProperty(String, String)
	 */
	@Test
	public void setGlobalProperty_shouldSaveAGlobalPropertyWhoseTypedValueIsHandledByACustomDatatype() {

		String newKey = "Flag";
		String initialValue = adminService.getGlobalProperty(newKey);
		assertNull(initialValue);

		adminService.setGlobalProperty(newKey, Boolean.FALSE.toString());
		assertEquals(adminService.getGlobalProperty("Flag"), "false");

	}

	/**
	 * @see AdministrationService#getGlobalProperty(String, String)
	 */
	@Test
	public void getGlobalProperty_shouldReturnDefaultValueIfPropertyNameDoesNotExist() {
		String invalidKey = "asdfasdf";
		String propertyValue = adminService.getGlobalProperty(invalidKey);
		assertNull(propertyValue); // make sure there isn't a gp

		String value = adminService.getGlobalProperty(invalidKey, "default");
		assertEquals("default", value);
	}

	/**
	 * @see AdministrationService#getGlobalPropertiesByPrefix(String)
	 */
	@Test
	public void getGlobalPropertiesByPrefix_shouldReturnAllRelevantGlobalPropertiesInTheDatabase() {
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");

		List<GlobalProperty> properties = adminService.getGlobalPropertiesByPrefix("fake.module.");

		for (GlobalProperty property : properties) {
			assertTrue(property.getProperty().startsWith("fake.module."));
			assertTrue(property.getPropertyValue().startsWith("correct-value"));
		}
	}

	/**
	 * @see AdministrationService#getAllowedLocales()
	 */
	@Test
	public void getAllowedLocales_shouldNotFailIfGlobalLocaleListIsNull() {
		AdministrationServiceImpl as = new AdministrationServiceImpl();

		as.setGlobalLocaleList(null);

		assertNotNull(adminService.getAllowedLocales());
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
		assertEquals("locale.allowed.list", prop.getProperty());
	}

	/**
	 * @see AdministrationService#getGlobalPropertyByUuid(String)
	 */
	@Test
	public void getGlobalPropertyByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getAdministrationService().getGlobalPropertyByUuid("some invalid uuid"));
	}

	/**
	 * @see AdministrationService#saveGlobalProperties(List)
	 */
	@Test
	public void saveGlobalProperties_shouldNotFailWithEmptyList() {
		Context.getAdministrationService().saveGlobalProperties(new ArrayList<>());
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

		assertEquals("new prop value1", Context.getAdministrationService().getGlobalProperty("new prop1"));
		assertEquals("new prop value2", Context.getAdministrationService().getGlobalProperty("new prop2"));
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

		assertNotNull(Context.getAdministrationService().getGlobalPropertyObject("new prop").getUuid());
	}

	/**
	 * @see AdministrationService#getAllGlobalProperties()
	 */
	@Test
	public void getAllGlobalProperties_shouldReturnAllGlobalPropertiesInTheDatabase() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		assertEquals(21, Context.getAdministrationService().getAllGlobalProperties().size());
	}

	/**
	 * @see AdministrationService#getAllowedLocales()
	 */
	@Test
	public void getAllowedLocales_shouldReturnAtLeastOneLocaleIfNoLocalesDefinedInDatabaseYet() {
		assertTrue(Context.getAdministrationService().getAllowedLocales().size() > 0);
	}

	/**
	 * @see AdministrationService#getGlobalPropertyObject(String)
	 */
	@Test
	public void getGlobalPropertyObject_shouldReturnNullWhenNoGlobalPropertyMatchGivenPropertyName() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		assertNull(Context.getAdministrationService().getGlobalPropertyObject("magicResistSkill"));
	}

	/**
	 * @see AdministrationService#getImplementationId()
	 */
	@Test
	public void getImplementationId_shouldReturnNullIfNoImplementationIdIsDefinedYet() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		assertNull(adminService.getImplementationId());
	}
	
	/**
	 * @see AdministrationService#getPresentationLocales()
	 */
	@Test
	public void getPresentationLocales_shouldNotReturnMoreLocalesThanMessageSourceServiceLocales() {
		assertFalse(adminService.getPresentationLocales().size() > Context
			.getMessageSourceService().getLocales().size());
	}

	/**
	 * @see AdministrationService#getSystemVariables()
	 */
	@Test
	public void getSystemVariables_shouldReturnAllRegisteredSystemVariables() {
		// The method implementation adds 11 system variables
		assertEquals(11, Context.getAdministrationService().getSystemVariables().size());
	}
	
	/**
	 * @see AdministrationService#purgeGlobalProperty(GlobalProperty)
	 */
	@Test
	public void purgeGlobalProperty_shouldDeleteGlobalPropertyFromDatabase() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		AdministrationService as = Context.getAdministrationService();

		assertEquals(21, as.getAllGlobalProperties().size());
		as.purgeGlobalProperty(as.getGlobalPropertyObject("a_valid_gp_key"));
		assertEquals(20, as.getAllGlobalProperties().size());
	}

	/**
	 * @see AdministrationService#saveGlobalProperty(GlobalProperty)
	 */
	@Test(expected = APIException.class)
	public void saveGlobalProperty_shouldThrowAPIExceptionIfLocaleListNotIncludeDefaultLocale() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "123");
		adminService.saveGlobalProperty(gp);
	}

	/**
	 * @see AdministrationService#saveGlobalProperty(GlobalProperty)
	 */
	@Test(expected = APIException.class)
	public void saveGlobalProperty_shouldThrowAPIExceptionIfDefaultNotInAllowedLocaleList() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, "123");
		adminService.saveGlobalProperty(gp);
	}

	/**
	 * @see AdministrationService#saveGlobalProperty(GlobalProperty)
	 */
	@Test
	public void saveGlobalProperty_shouldNotFailIfPropertyIsNullOrBlank() {
		GlobalProperty gp = new GlobalProperty(null, null);
		assertNotNull(adminService.saveGlobalProperty(gp));
		GlobalProperty gp2 = new GlobalProperty("", "");
		assertNotNull(adminService.saveGlobalProperty(gp2));
	}

	/**
	 * @see AdministrationService#saveGlobalProperty(GlobalProperty)
	 */
	@Test
	public void saveGlobalProperty_shouldCreateGlobalPropertyInDatabase() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		AdministrationService as = Context.getAdministrationService();

		as.saveGlobalProperty(new GlobalProperty("detectHiddenSkill", "100"));
		assertNotNull(as.getGlobalProperty("detectHiddenSkill"));
	}

	/**
	 * @see AdministrationService#saveGlobalProperty(GlobalProperty)
	 */
	@Test
	public void saveGlobalProperty_shouldOverwriteGlobalPropertyIfExists() {
		executeDataSet(ADMIN_INITIAL_DATA_XML);
		AdministrationService as = Context.getAdministrationService();

		GlobalProperty gp = as.getGlobalPropertyObject("a_valid_gp_key");
		assertEquals("correct-value", gp.getPropertyValue());
		gp.setPropertyValue("new-even-more-correct-value");
		as.saveGlobalProperty(gp);
		assertEquals("new-even-more-correct-value", as.getGlobalProperty("a_valid_gp_key"));
	}

	/**
	 * @see AdministrationService#getAllowedLocales()
	 */
	@Test
	public void getAllowedLocales_shouldNotReturnDuplicatesEvenIfTheGlobalPropertyHasThem() {
		Context.getAdministrationService().saveGlobalProperty(
			new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en_GB,fr,es,en_GB"));
		assertEquals(3, Context.getAdministrationService().getAllowedLocales().size());
	}

	/**
	 * @see AdministrationService#getGlobalPropertyValue(String, Object) 
	 */
	@Test
	public void getGlobalPropertyValue_shouldReturnValueInTheSpecifiedIntegerType() {
		// put the global property into the database
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");

		Object value = adminService.getGlobalPropertyValue("valid.integer", 4);

		assertTrue(value instanceof Integer);
		assertEquals(1234, value);
	}

	/**
	 * @see AdministrationService#getGlobalPropertyValue(String, Object) 
	 */
	@Test
	public void getGlobalPropertyValue_shouldReturnDefaultValueForMissingProperty() {
		// put the global property into the database
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");

		Object value = adminService.getGlobalPropertyValue("does.not.exist", 1234);

		assertEquals(1234, value);
	}

	/**
	 * @see AdministrationService#getGlobalPropertyValue(String, Object) 
	 */
	@Test
	public void getGlobalPropertyValue_shouldReturnValueInTheSpecifiedDoubleType() {
		// put the global property into the database
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");

		Object retValue = adminService.getGlobalPropertyValue("valid.double", 4.34);

		assertTrue(retValue instanceof Double);
		assertEquals(1234.54, retValue);
	}

	/**
	 * @see AdministrationService#getGlobalPropertyValue(String, Object)
	 */
	@Test(expected = APIException.class)
	public void getGlobalPropertyValue_shouldThrowAPIExceptionIfCatchesNoSuchMethodException() {
		AtomicInteger number= new AtomicInteger();
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		assertNull(adminService.getGlobalPropertyValue("a_valid_gp_key", number));
	}

	/**
	 * @see AdministrationService#getGlobalPropertyValue(String, Object)
	 */
	@Test
	public void getGlobalPropertyValue_shouldNotFailIfUnableToConvertTypes() {
		boolean dv=true;
		executeDataSet("org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml");
		assertNotNull(adminService.getGlobalPropertyValue("a_valid_gp_key", dv));
	}
	
	/**
	 * @see AdministrationService#getGlobalProperty(String)
	 */
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

	/**
	 * @see AdministrationService#saveGlobalProperty(GlobalProperty)
	 */
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

	/**
	 * @see AdministrationService#saveGlobalProperties(List<GlobalProperty;>)
	 */
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

	/**
	 * @see AdministrationService#purgeGlobalProperties(List)
	 */
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
		assertNotNull(gp.getValueReference());
	}

	/**
	 * @see AdministrationService#getSearchLocales(Locale, User) 
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
		assertTrue("en_US", searchLocales.contains(new Locale("en", "US")));
		assertTrue("pl", searchLocales.contains(new Locale("pl")));
		assertTrue("es", searchLocales.contains(new Locale("es")));
		assertFalse("es_CL", searchLocales.contains(new Locale("es", "CL")));
	}

	/**
	 * @see AdministrationService#getSearchLocales(Locale, User) 
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
		assertEquals(Context.getLocale(), searchLocales.get(0));
		assertEquals(new Locale(Context.getLocale().getLanguage()), searchLocales.get(1));
	}

	/**
	 * @see AdministrationService#getSearchLocales(Locale, User) 
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
		assertTrue("en_GB", searchLocales.contains(new Locale("en", "GB")));
		assertTrue("en_US", searchLocales.contains(new Locale("en", "US")));
		assertFalse("pl", searchLocales.contains(new Locale("pl")));
	}

	/**
	 * @see AdministrationService#validate(Object, Errors)
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
	public void getPresentationLocales_shouldReturnOnlyCountryLocaleIfBothCountryLocaleAndLanguageLocaleAreSpecifiedInAllowedList() {
		Context.getAdministrationService().saveGlobalProperty(
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

		Set<Locale> presentationLocales = Context.getAdministrationService().getPresentationLocales();

		Context.getMessageSourceService().setActiveMessageSource(mutableMessageSource);

		assertEquals(2, presentationLocales.size());
		assertTrue("en", presentationLocales.contains(new Locale("en")));
		assertTrue("es_CL", presentationLocales.contains(new Locale("es", "CL")));
	}

	/**
	 * @see AdministrationService#getPresentationLocales()
	 */
	@Test
	public void getPresentationLocales_shouldReturnAllCountryLocalesIfLanguageLocaleAndNoCountryLocalesAreSpecifiedInAllowedList() {
		Context.getAdministrationService().saveGlobalProperty(
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

		Set<Locale> presentationLocales = Context.getAdministrationService().getPresentationLocales();

		Context.getMessageSourceService().setActiveMessageSource(mutableMessageSource);

		assertEquals(3, presentationLocales.size());
		assertTrue("es_CL", presentationLocales.contains(new Locale("es", "CL")));
		assertTrue("es_SN", presentationLocales.contains(new Locale("es", "SN")));
		assertTrue("en", presentationLocales.contains(new Locale("en")));
	}

	/**
	 * @see AdministrationService#getPresentationLocales()
	 */
	@Test
	public void getPresentationLocales_shouldReturnLanguageLocaleIfCountryLocaleIsSpecifiedInAllowedListButCountryLocaleMessageFileIsMissing() {
		Context.getAdministrationService().saveGlobalProperty(
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

		Set<Locale> presentationLocales = Context.getAdministrationService().getPresentationLocales();

		Context.getMessageSourceService().setActiveMessageSource(mutableMessageSource);

		assertEquals(2, presentationLocales.size());
		assertTrue("en", presentationLocales.contains(new Locale("en")));
		assertTrue("es", presentationLocales.contains(new Locale("es")));
	}

	/**
	 * @see AdministrationService#getPresentationLocales()
	 */
	@Test
	public void getPresentationLocales_shouldReturnLanguageLocaleIfItIsSpecifiedInAllowedListAndThereAreNoCountryLocaleMessageFilesAvailable() {
		Context.getAdministrationService().saveGlobalProperty(
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

		Set<Locale> presentationLocales = Context.getAdministrationService().getPresentationLocales();

		Context.getMessageSourceService().setActiveMessageSource(mutableMessageSource);

		assertEquals(2, presentationLocales.size());
		assertTrue("en", presentationLocales.contains(new Locale("en")));
		assertTrue("es", presentationLocales.contains(new Locale("es")));
	}

	/**
	 * @see AdministrationService#getPresentationLocales()
	 */
	@Test
	public void getPresentationLocales_shouldPreserveInsertionOrderInSetReturnedByMethod() {
		String globalPropertyLocaleListAllowedData = "en_GB, es, ja_JP, it_IT, pl_PL";
		//The order of languages and locales is described above and should be followed bt `presentationLocales` Set
		Context.getAdministrationService().saveGlobalProperty(
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

		List<Locale> presentationLocales = new ArrayList<>(Context.getAdministrationService().getPresentationLocales());

		Context.getMessageSourceService().setActiveMessageSource(mutableMessageSource);

		//Assert Locales in expected order as set by global property
		assertEquals(new Locale("en"), presentationLocales.get(0));
		assertEquals(new Locale("es"), presentationLocales.get(1));
		assertEquals(new Locale("it", "IT"), presentationLocales.get(2));
		assertEquals(new Locale("pl", "PL"), presentationLocales.get(3));
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

	private Cache.ValueWrapper getCacheForCurrentUser() {
		Object[] params = { Context.getLocale(), Context.getAuthenticatedUser() };
		Object key = (new SimpleKeyGenerator()).generate(null, null, params);
		return cacheManager.getCache("userSearchLocales").get(key);
	}

	private List<Locale> getCachedSearchLocalesForCurrentUser() {
		return (List<Locale>) getCacheForCurrentUser().get();
	}
}
