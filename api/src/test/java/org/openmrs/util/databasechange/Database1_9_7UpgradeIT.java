/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util.databasechange;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.DatabaseUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * Tests database upgrade from OpenMRS 1.9.7.
 */
public class Database1_9_7UpgradeIT extends BaseContextSensitiveTest {
	
	public static final String TEST_DATA_DIR = "/org/openmrs/util/databasechange/";
	
	public static final String UPGRADE_TEST_1_9_7_TO_1_10_DATASET = TEST_DATA_DIR
	        + "database1_9To1_10UpgradeTest-dataSet.xml";
	
	public static final String STANDARD_TEST_1_9_7_DATASET = TEST_DATA_DIR + "standardTest-1.9.7-dataSet.xml";
	
	public final static String DATABASE_PATH = TEST_DATA_DIR + "openmrs-1.9.7.h2.db";
	
	private DatabaseUpgradeTestUtil upgradeTestUtil;
	
	private static File testAppDataDir;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	private Map<String, String> row(String... values) {
		Map<String, String> row = new HashMap<>();
		for (int i = 0; i < values.length; i += 2) {
			row.put(values[i], values[i + 1]);
		}
		return row;
	}
	
	private class OrderAndPerson {
		
		private Integer orderId;
		
		private Integer personId;
		
		OrderAndPerson(Integer orderId, Integer personId) {
			this.orderId = orderId;
			this.personId = personId;
		}
		
		Integer getOrderId() {
			return orderId;
		}
		
		void setOrderId(Integer orderId) {
			this.orderId = orderId;
		}
		
		Integer getPersonId() {
			return personId;
		}
		
		void setPersonId(Integer personId) {
			this.personId = personId;
		}
	}
	
	/**
	 * This method creates mock order entry upgrade file
	 * 
	 * @see org.openmrs.util.UpgradeUtil#getConceptIdForUnits(String)
	 */
	public static void createOrderEntryUpgradeFileWithTestData(String propString) throws IOException {
		Properties props = new Properties();
		props.load(new StringReader(propString));
		String appDataDir = OpenmrsUtil.getApplicationDataDirectory();
		File propFile = new File(appDataDir, DatabaseUtil.ORDER_ENTRY_UPGRADE_SETTINGS_FILENAME);
		props.store(new FileWriter(propFile), null);
		propFile.deleteOnExit();
	}
	
	@BeforeClass
	public static void beforeClass() throws IOException {
		testAppDataDir = File.createTempFile("appdir-for-unit-tests", "");
		testAppDataDir.delete();// so we can make turn it into a directory
		testAppDataDir.mkdir();
		
		System.setProperty(OpenmrsConstants.APPLICATION_DATA_DIRECTORY_RUNTIME_PROPERTY, testAppDataDir.getAbsolutePath());
		OpenmrsUtil.setApplicationDataDirectory(testAppDataDir.getAbsolutePath());
	}
	
	@AfterClass
	public static void afterClass() throws Exception {
		FileUtils.deleteDirectory(testAppDataDir);
		//Just to be safe, not to affect other units in the test suite
		System.clearProperty(OpenmrsConstants.APPLICATION_DATA_DIRECTORY_RUNTIME_PROPERTY);
	}
	
	@Before
	public void before() throws IOException, SQLException {
		upgradeTestUtil = new DatabaseUpgradeTestUtil(DATABASE_PATH);
	}
	
	@After
	public void after() throws SQLException {
		upgradeTestUtil.close();
	}
	
	@Test
	public void shouldUpgradeFromClean1_9To1_10() throws IOException, SQLException {
		upgradeTestUtil.upgrade();
		
		List<Map<String, String>> orderFrequencySelect = upgradeTestUtil.select("order_frequency", null,
		    "order_frequency_id");
		Assert.assertThat(orderFrequencySelect.size(), Matchers.is(0));
		
		List<Map<String, String>> drugOrderSelect = upgradeTestUtil.select("drug_order", null, "order_id");
		Assert.assertThat(drugOrderSelect.size(), Matchers.is(0));
		
		//Test if the generated schema corresponds to Hibernate mappings
		upgradeTestUtil.buildSessionFactory();
	}
	
	@Test
	public void shouldFailMigratingDrugOrdersIfUnitsToConceptsMappingsIsNotSet() throws IOException, SQLException {
		upgradeTestUtil.executeDataset(STANDARD_TEST_1_9_7_DATASET);
		createOrderEntryUpgradeFileWithTestData("");
		expectedException.expect(IOException.class);
		String errorMsgSubString1 = "liquibase.exception.MigrationFailedException: Migration failed for change set liquibase-update-to-latest.xml::201401101647-TRUNK-4187::wyclif";
		expectedException.expectMessage(errorMsgSubString1);
		String errorMsgSubString2 = Context.getMessageSourceService().getMessage("upgrade.settings.file.not.have.mapping",
		    new Object[] { "mg" }, null);
		expectedException.expectMessage(errorMsgSubString2);
		upgradeTestUtil.upgrade();
	}
	
	@Test
	public void shouldFailMigratingDrugOrdersIfUnitsToConceptsMappingsDoesNotPointToValidCodedDoseUnits()
	    throws IOException, SQLException {
		upgradeTestUtil.executeDataset(STANDARD_TEST_1_9_7_DATASET);
		upgradeTestUtil.executeDataset(UPGRADE_TEST_1_9_7_TO_1_10_DATASET);
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=invalid");
		
		expectedException.expect(IOException.class);
		String errorMsgSubString1 = "liquibase.exception.MigrationFailedException: Migration failed for change set liquibase-update-to-latest.xml::201401101647-TRUNK-4187::wyclif";
		expectedException.expectMessage(errorMsgSubString1);
		expectedException.expectMessage("For input string: \"invalid\"");
		upgradeTestUtil.upgrade();
	}
	
	@Test
	public void shouldMigrateDrugOrders() throws IOException, SQLException {
		upgradeTestUtil.executeDataset(STANDARD_TEST_1_9_7_DATASET);
		
		upgradeTestUtil.executeDataset(UPGRADE_TEST_1_9_7_TO_1_10_DATASET);
		
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		upgradeTestUtil.upgrade();
		
		List<Map<String, String>> orderFrequencySelect = upgradeTestUtil.select("order_frequency", null,
		    "order_frequency_id", "concept_id");
		Assert.assertThat(orderFrequencySelect.size(), Matchers.is(2));
		
		Map<String, String> conceptsToFrequencies = new HashMap<>();
		conceptsToFrequencies.put(orderFrequencySelect.get(0).get("concept_id"),
		    orderFrequencySelect.get(0).get("order_frequency_id"));
		conceptsToFrequencies.put(orderFrequencySelect.get(1).get("concept_id"),
		    orderFrequencySelect.get(1).get("order_frequency_id"));
		
		Assert.assertThat(conceptsToFrequencies.keySet(), Matchers.containsInAnyOrder("113", "114"));
		
		List<Map<String, String>> drugOrderSelect = upgradeTestUtil.select("drug_order", null, "order_id", "frequency");
		
		Assert.assertThat(
		    drugOrderSelect,
		    Matchers.containsInAnyOrder(row("order_id", "1", "frequency", conceptsToFrequencies.get("113")),
		        row("order_id", "2", "frequency", conceptsToFrequencies.get("113")),
		        row("order_id", "3", "frequency", conceptsToFrequencies.get("114")),
		        row("order_id", "4", "frequency", conceptsToFrequencies.get("113")),
		        row("order_id", "5", "frequency", conceptsToFrequencies.get("114"))));
	}
	
	@Test
	public void shouldFailIfAnyDrugOrderUnitsNotMappedToConceptsAreFound() throws Exception {
		//sanity check that we have some unmapped drug order dose units
		upgradeTestUtil.executeDataset(STANDARD_TEST_1_9_7_DATASET);
		Set<String> uniqueUnits = DatabaseUtil.getUniqueNonNullColumnValues("units", "drug_order", String.class,
		    upgradeTestUtil.getConnection());
		Assert.assertTrue(uniqueUnits.size() > 0);
		
		//map the frequencies only
		createOrderEntryUpgradeFileWithTestData("1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		expectedException.expect(IOException.class);
		String errorMsgSubString1 = "liquibase.exception.MigrationFailedException: Migration failed for change set liquibase-update-to-latest.xml::201401101647-TRUNK-4187::wyclif";
		expectedException.expectMessage(errorMsgSubString1);
		
		upgradeTestUtil.upgrade();
	}
	
	@Test
	public void shouldFailIfAnyDrugOrderFrequenciesNotMappedToConceptsAreFound() throws Exception {
		//sanity check that we have some unmapped drug order frequencies
		upgradeTestUtil.executeDataset(STANDARD_TEST_1_9_7_DATASET);
		Set<String> uniqueFrequencies = DatabaseUtil.getUniqueNonNullColumnValues("frequency", "drug_order", String.class,
		    upgradeTestUtil.getConnection());
		Assert.assertTrue(uniqueFrequencies.size() > 0);
		
		//map the dose units only
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112");
		
		expectedException.expect(IOException.class);
		String errorMsgSubString1 = "liquibase.exception.MigrationFailedException: Migration failed for change set liquibase-update-to-latest.xml::201401101647-TRUNK-4187::wyclif";
		expectedException.expectMessage(errorMsgSubString1);
		
		upgradeTestUtil.upgrade();
	}
	
	@Test
	public void shouldPassIfAllExistingDrugOrderUnitsAndFrequenciesAreMappedToConcepts() throws Exception {
		//sanity check that we have some drug order dose units and frequencies in the test dataset
		upgradeTestUtil.executeDataset(STANDARD_TEST_1_9_7_DATASET);
		Set<String> uniqueUnits = DatabaseUtil.getUniqueNonNullColumnValues("units", "drug_order", String.class,
		    upgradeTestUtil.getConnection());
		Assert.assertTrue(uniqueUnits.size() > 0);
		
		Set<String> uniqueFrequencies = DatabaseUtil.getUniqueNonNullColumnValues("frequency", "drug_order", String.class,
		    upgradeTestUtil.getConnection());
		Assert.assertTrue(uniqueFrequencies.size() > 0);
		
		upgradeTestUtil.executeDataset(UPGRADE_TEST_1_9_7_TO_1_10_DATASET);
		
		//set the mappings for all existing frequencies and dose units
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		upgradeTestUtil.upgrade();
		
		//Test if the generated schema corresponds to Hibernate mappings
		upgradeTestUtil.buildSessionFactory();
	}
	
	@Test
	public void shouldConvertOrderersToBeingProvidersInsteadOfUsers() throws Exception {
		upgradeTestUtil.executeDataset(STANDARD_TEST_1_9_7_DATASET);
		upgradeTestUtil.executeDataset(UPGRADE_TEST_1_9_7_TO_1_10_DATASET);
		
		//Check that we have some orders with no orderers
		List<List<Object>> rows = DatabaseUtil.executeSQL(upgradeTestUtil.getConnection(),
		    "select order_id from orders where orderer is null", true);
		Assert.assertEquals(2, rows.size());
		List<Integer> orderIdsWithNoOrderer = Arrays.asList((Integer) rows.get(0).get(0), (Integer) rows.get(1).get(0));
		
		//Sanity check that we have orders with orderer column set
		rows = DatabaseUtil.executeSQL(upgradeTestUtil.getConnection(),
		    "select order_id, orderer from orders where orderer is not null", true);
		List<OrderAndPerson> ordersAndOrderersWithAProviderAccount = new ArrayList<>();
		for (List<Object> row : rows) {
			ordersAndOrderersWithAProviderAccount.add(new OrderAndPerson((Integer) row.get(0), (Integer) row.get(1)));
		}
		Assert.assertEquals(3, ordersAndOrderersWithAProviderAccount.size());
		
		Set<Integer> originalProviderIds = DatabaseUtil.getUniqueNonNullColumnValues("provider_id", "provider",
		    Integer.class, upgradeTestUtil.getConnection());
		
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		upgradeTestUtil.upgrade();
		
		//That correct providers were set for each order, i.e the person record for the provider
		//should match the that of the user account before upgrade
		for (OrderAndPerson op : ordersAndOrderersWithAProviderAccount) {
			rows = DatabaseUtil.executeSQL(upgradeTestUtil.getConnection(),
			    "select p.provider_id, p.person_id from provider p join orders o on p.provider_id = o.orderer where order_id = "
			            + op.getOrderId(), true);
			Assert.assertEquals(op.getPersonId(), rows.get(0).get(1));
			//The provider account should have been among the existing ones prior to upgrade
			Assert.assertTrue(originalProviderIds.contains(rows.get(0).get(0)));
		}
		
		//The orderer column for orders with null orderers previously should be set to Unknown Provider
		rows = DatabaseUtil.executeSQL(upgradeTestUtil.getConnection(),
		    "select order_id from orders where orderer = (Select provider_id from provider where uuid ="
		            + "(select property_value from global_property where property = '"
		            + OpenmrsConstants.GP_UNKNOWN_PROVIDER_UUID + "'))", true);
		
		Assert.assertEquals(orderIdsWithNoOrderer.size(), rows.size());
		Assert.assertTrue(orderIdsWithNoOrderer.contains(rows.get(0).get(0)));
		Assert.assertTrue(orderIdsWithNoOrderer.contains(rows.get(1).get(0)));
	}
	
	@Test
	public void shouldConcatenateDoseStrengthAndUnits() throws IOException, SQLException {
		upgradeTestUtil.executeDataset(STANDARD_TEST_1_9_7_DATASET);
		upgradeTestUtil.executeDataset(UPGRADE_TEST_1_9_7_TO_1_10_DATASET);
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		upgradeTestUtil.upgrade();
		
		List<Map<String, String>> drugs = upgradeTestUtil.select("drug", null, "strength");
		
		Assert.assertThat(drugs.size(), Matchers.is(3));
		Assert.assertTrue(drugs.get(0).containsValue("1.0tab(s)"));
		Assert.assertTrue(drugs.get(1).containsValue("325.0mg"));
		Assert.assertNull(drugs.get(2).get("strength"));
	}
	
	@Test
	public void shouldFailIfThereAreDrugsWithDoseStrengthAndNoNullUnits() throws IOException, SQLException {
		upgradeTestUtil.executeDataset(STANDARD_TEST_1_9_7_DATASET);
		upgradeTestUtil.executeDataset(UPGRADE_TEST_1_9_7_TO_1_10_DATASET);
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/UpgradeTest-orderWithStrengthButNullUnits.xml");
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		expectedException.expect(IOException.class);
		String errorMsgSubString = "liquibase.exception.MigrationFailedException: Migration failed for change set liquibase-update-to-latest.xml::201403262140-TRUNK-4265::wyclif";
		expectedException.expectMessage(errorMsgSubString);
		upgradeTestUtil.upgrade();
	}
	
	@Test
	public void shouldFailIfThereAreDrugsWithDoseStrengthAndNoBlankUnits() throws IOException, SQLException {
		upgradeTestUtil.executeDataset(STANDARD_TEST_1_9_7_DATASET);
		upgradeTestUtil.executeDataset(UPGRADE_TEST_1_9_7_TO_1_10_DATASET);
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/UpgradeTest-orderWithStrengthButBlankUnits.xml");
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		expectedException.expect(IOException.class);
		String errorMsgSubString = "liquibase.exception.MigrationFailedException: Migration failed for change set liquibase-update-to-latest.xml::201403262140-TRUNK-4265::wyclif";
		expectedException.expectMessage(errorMsgSubString);
		upgradeTestUtil.upgrade();
	}
	
	@Test
	public void shouldFailIfThereAreAnyOrderTypesInTheDatabaseOtherThanDrugOrderTypeAndNoNewColumns() throws IOException,
	    SQLException {
		upgradeTestUtil.executeDataset(STANDARD_TEST_1_9_7_DATASET);
		upgradeTestUtil.executeDataset(UPGRADE_TEST_1_9_7_TO_1_10_DATASET);
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/UpgradeTest-otherOrderTypes.xml");
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		expectedException.expect(IOException.class);
		String errorMsgSubString = "liquibase.exception.MigrationFailedException: Migration failed for change set liquibase-update-to-latest.xml::201404091110::wyclif";
		expectedException.expectMessage(errorMsgSubString);
		upgradeTestUtil.upgrade();
	}
	
	@Test
	public void shouldPassIfThereAreAnyOrderTypesInTheDatabaseOtherThanDrugOrderTypeAndTheNewColumnsExist()
	    throws IOException, SQLException {
		upgradeTestUtil.executeDataset(STANDARD_TEST_1_9_7_DATASET);
		upgradeTestUtil.executeDataset(UPGRADE_TEST_1_9_7_TO_1_10_DATASET);
		upgradeTestUtil.executeDataset("UpgradeTest-otherOrderTypes.xml");
		upgradeTestUtil.getConnection().createStatement()
		        .executeUpdate("alter table `order_type` add java_class_name varchar(255) default 'org.openmrs.Order'");
		upgradeTestUtil.getConnection().createStatement().executeUpdate("alter table `order_type` add parent int(11)");
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		upgradeTestUtil.upgrade();
	}
	
	@Test
	public void shouldCreateDiscontinuationOrderForStoppedOrders() throws IOException, SQLException {
		upgradeTestUtil.executeDataset(STANDARD_TEST_1_9_7_DATASET);
		upgradeTestUtil.executeDataset(UPGRADE_TEST_1_9_7_TO_1_10_DATASET);
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		List<List<Object>> discontinuedOrders = DatabaseUtil.executeSQL(upgradeTestUtil.getConnection(),
		    "SELECT count(*) order_id FROM orders WHERE discontinued = true", true);
		long discontinuedOrdersCount = (Long) discontinuedOrders.get(0).get(0);
		assertEquals(3, discontinuedOrdersCount);
		
		upgradeTestUtil.upgrade();
		List<List<Object>> discontinuationOrders = DatabaseUtil.executeSQL(upgradeTestUtil.getConnection(),
		    "SELECT count(*) FROM orders WHERE order_action = 'DISCONTINUE'", true);
		assertEquals(discontinuedOrdersCount, discontinuationOrders.get(0).get(0));
		
		//There should be no DC order with a null stop date
		List<List<Object>> discontinuationOrdersWithNotStartDate = DatabaseUtil.executeSQL(upgradeTestUtil.getConnection(),
		    "SELECT count(*) FROM orders WHERE order_action = 'DISCONTINUE' AND auto_expire_date IS NULL", true);
		assertEquals(0L, discontinuationOrdersWithNotStartDate.get(0).get(0));
		
		List<List<Object>> newer = DatabaseUtil.executeSQL(upgradeTestUtil.getConnection(),
		    "SELECT count(*) FROM orders WHERE order_action = 'DISCONTINUE' AND "
		            + "(date_activated IS NULL OR orderer IS NULL OR encounter_id IS NULL OR previous_order_id IS NULL)",
		    true);
		assertEquals(0L, newer.get(0).get(0));
	}
	
	@Test
	public void shouldFailIfThereAreOrderersWithNoAssociatedProviderAccounts() throws IOException, SQLException {
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/standardTest-1.9.7-dataSet.xml");
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/database1_9To1_10UpgradeTest-dataSet.xml");
		upgradeTestUtil
		        .executeDataset("/org/openmrs/util/databasechange/UpgradeTest-orderWithOrdererThatIsNotAProvider.xml");
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		expectedException.expect(IOException.class);
		String errorMsgSubString = "liquibase.exception.MigrationFailedException: Migration failed for change set liquibase-update-to-latest.xml::201406262016::wyclif";
		expectedException.expectMessage(errorMsgSubString);
		upgradeTestUtil.upgrade();
	}
	
	@Test
	public void shouldSetValuesToNullIfUnitsOrFrequencyBlank() throws Exception {
		upgradeTestUtil.executeDataset(STANDARD_TEST_1_9_7_DATASET);
		upgradeTestUtil.executeDataset(UPGRADE_TEST_1_9_7_TO_1_10_DATASET);
		upgradeTestUtil.executeDataset(TEST_DATA_DIR + "UpgradeTest-orderWithBlankUnitsOrFrequency.xml");
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		upgradeTestUtil.upgrade();
		
		List<Map<String, String>> drug_orders = upgradeTestUtil.select("drug_order", "order_id = 6 or order_id = 7",
		    "order_id", "dose_units", "frequency");
		
		assertThat(
		    drug_orders,
		    containsInAnyOrder(row("order_id", "6", "dose_units", null, "frequency", null),
		        row("order_id", "7", "dose_units", null, "frequency", null)));
	}
	
	@Test
	public void shouldAddTheNecessaryPrivilegesAndAssignThemToSpecificRoles() throws Exception {
		final String GET_ENCOUNTERS = "Get Encounters";
		final String ADD_VISITS = "Add Visits";
		final String ADD_ENCOUNTERS = "Add Encounters";
		final String EDIT_ENCOUNTERS = "Edit Encounters";
		final String GET_VISITS = "Get Visits";
		final String GET_PROVIDERS = "Get Providers";
		final String PROVIDER_ROLE = "Provider";
		final String AUTHENTICATED_ROLE = "Authenticated";
		Connection connection = upgradeTestUtil.getConnection();
		//Insert Get encounters privilege for testing purposes
		final String insertPrivilegeQuery = "insert into privilege (privilege,uuid) values ('" + GET_ENCOUNTERS
		        + "','a6a521de-3992-11e6-899a-a4d646d86a8a')";
		DatabaseUtil.executeSQL(connection, insertPrivilegeQuery, false);
		//Assign some privileges to some roles for testing purposes
		DatabaseUtil.executeSQL(connection, "insert into role_privilege (role,privilege) values ('" + PROVIDER_ROLE + "', '"
		        + GET_ENCOUNTERS + "'), ('" + PROVIDER_ROLE + "', '" + EDIT_ENCOUNTERS + "'), ('" + AUTHENTICATED_ROLE
		        + "', '" + ADD_ENCOUNTERS + "')", false);
		connection.commit();
		
		String query = "select privilege from privilege where privilege = '" + GET_VISITS + "' or " + "privilege = '"
		        + GET_PROVIDERS + "'";
		assertEquals(0, DatabaseUtil.executeSQL(connection, query, true).size());
		assertTrue(roleHasPrivilege(PROVIDER_ROLE, GET_ENCOUNTERS));
		assertTrue(roleHasPrivilege(PROVIDER_ROLE, EDIT_ENCOUNTERS));
		assertFalse(roleHasPrivilege(PROVIDER_ROLE, GET_VISITS));
		assertFalse(roleHasPrivilege(PROVIDER_ROLE, GET_PROVIDERS));
		assertFalse(roleHasPrivilege(PROVIDER_ROLE, ADD_VISITS));
		assertTrue(roleHasPrivilege(AUTHENTICATED_ROLE, ADD_ENCOUNTERS));
		
		upgradeTestUtil.upgrade();
		connection = upgradeTestUtil.getConnection();
		assertEquals(2, DatabaseUtil.executeSQL(connection, query, true).size());
		assertTrue(roleHasPrivilege(PROVIDER_ROLE, GET_VISITS));
		assertTrue(roleHasPrivilege(PROVIDER_ROLE, GET_PROVIDERS));
		assertTrue(roleHasPrivilege(PROVIDER_ROLE, ADD_VISITS));
		assertTrue(roleHasPrivilege(AUTHENTICATED_ROLE, ADD_VISITS));
	}
	
	private boolean roleHasPrivilege(String role, String privilege) {
		final String query = "select * from role_privilege where role='" + role + "' and privilege ='" + privilege + "'";
		return DatabaseUtil.executeSQL(upgradeTestUtil.getConnection(), query, true).size() == 1;
	}

	@Test
	public void shouldUpgradeLiquibase() throws IOException, SQLException {
		upgradeTestUtil.upgrade("liquibase-update-to-latest.xml");

		// no explicit assertions here, this test serves to see whether the master is executed without raising an exception.

	}
}
