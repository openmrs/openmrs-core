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
package org.openmrs.util.databasechange;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.util.DatabaseUtil;
import org.openmrs.util.OpenmrsUtil;

/**
 * Tests database upgrade from OpenMRS 1.9.7.
 */
public class Database1_9_7UpgradeTest {
	
	public final static String databasePath = "/org/openmrs/util/databasechange/openmrs-1.9.7.h2.db";
	
	private DatabaseUpgradeTestUtil upgradeTestUtil;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

    private Map<String, String> row(String... values) {
        Map<String, String> row = new HashMap<String, String>();
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
	
	@Before
	public void before() throws IOException, SQLException {
		upgradeTestUtil = new DatabaseUpgradeTestUtil(databasePath);
	}
	
	@After
	public void after() throws SQLException {
		upgradeTestUtil.close();
	}
	
	@Test
	public void shouldUpgradeFromClean1_9To1_10() throws IOException, SQLException {
		upgradeTestUtil.upgrade();
		
		List<Map<String, String>> orderFrequencySelect = upgradeTestUtil.select("order_frequency", "order_frequency_id");
		Assert.assertThat(orderFrequencySelect.size(), Matchers.is(0));
		
		List<Map<String, String>> drugOrderSelect = upgradeTestUtil.select("drug_order", "order_id");
		Assert.assertThat(drugOrderSelect.size(), Matchers.is(0));
		
		//Test if the generated schema corresponds to Hibernate mappings
		upgradeTestUtil.buildSessionFactory();
	}
	
	@Test
	public void shouldFailMigratingDrugOrdersIfUnitsToConceptsMappingsIsNotSet() throws IOException, SQLException {
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/standardTest-1.9.7-dataSet.xml");
		createOrderEntryUpgradeFileWithTestData("");
		expectedException.expect(IOException.class);
		String errorMsgSubString1 = "liquibase.exception.MigrationFailedException: Migration failed for change set liquibase-update-to-latest.xml::201401101645-TRUNK-4187::wyclif";
		expectedException.expectMessage(errorMsgSubString1);
		String errorMsgSubString2 = "Your order entry upgrade settings file does not have mapping for mg";
		expectedException.expectMessage(errorMsgSubString2);
		upgradeTestUtil.upgrade();
	}
	
	@Test
	public void shouldFailMigratingDrugOrdersIfUnitsToConceptsMappingsDoesNotPointToValidCodedDoseUnits()
	        throws IOException, SQLException {
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/standardTest-1.9.7-dataSet.xml");
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/database1_9To1_10UpgradeTest-dataSet.xml");
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=invalid");
		
		expectedException.expect(IOException.class);
		String errorMsgSubString1 = "liquibase.exception.MigrationFailedException: Migration failed for change set liquibase-update-to-latest.xml::201401101645-TRUNK-4187::wyclif";
		expectedException.expectMessage(errorMsgSubString1);
		expectedException.expectMessage("For input string: \"invalid\"");
		upgradeTestUtil.upgrade();
	}
	
	@Test
	public void shouldMigrateDrugOrders() throws IOException, SQLException {
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/standardTest-1.9.7-dataSet.xml");
		
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/database1_9To1_10UpgradeTest-dataSet.xml");
		
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		upgradeTestUtil.upgrade();
		
		List<Map<String, String>> orderFrequencySelect = upgradeTestUtil.select("order_frequency", "order_frequency_id",
		    "concept_id");
		Assert.assertThat(orderFrequencySelect.size(), Matchers.is(2));
		
		Map<String, String> conceptsToFrequencies = new HashMap<String, String>();
		conceptsToFrequencies.put(orderFrequencySelect.get(0).get("concept_id"), orderFrequencySelect.get(0).get(
		    "order_frequency_id"));
		conceptsToFrequencies.put(orderFrequencySelect.get(1).get("concept_id"), orderFrequencySelect.get(1).get(
		    "order_frequency_id"));
		
		Assert.assertThat(conceptsToFrequencies.keySet(), Matchers.containsInAnyOrder("113", "114"));
		
		List<Map<String, String>> drugOrderSelect = upgradeTestUtil.select("drug_order", "order_id", "frequency");
		
		Assert.assertThat(drugOrderSelect, Matchers.containsInAnyOrder(row("order_id", "1", "frequency",
		    conceptsToFrequencies.get("113")), row("order_id", "2", "frequency", conceptsToFrequencies.get("113")), row(
		    "order_id", "3", "frequency", conceptsToFrequencies.get("114")), row("order_id", "4", "frequency",
		    conceptsToFrequencies.get("113")), row("order_id", "5", "frequency", conceptsToFrequencies.get("114"))));
	}
	
	@Test(expected = Exception.class)
	public void shouldFailIfAnyDrugOrderUnitsNotMappedToConceptsAreFound() throws Exception {
		//sanity check that we have some unmapped drug order dose units
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/standardTest-1.9.7-dataSet.xml");
		Set<String> uniqueUnits = DatabaseUtil.getUniqueNonNullColumnValues("units", "drug_order", String.class,
		    upgradeTestUtil.getConnection());
		Assert.assertTrue(uniqueUnits.size() > 0);
		
		//map the frequencies only
		createOrderEntryUpgradeFileWithTestData("1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		upgradeTestUtil.upgrade();
	}
	
	@Test(expected = Exception.class)
	public void shouldFailIfAnyDrugOrderFrequenciesNotMappedToConceptsAreFound() throws Exception {
		//sanity check that we have some unmapped drug order frequencies
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/standardTest-1.9.7-dataSet.xml");
		Set<String> uniqueFrequencies = DatabaseUtil.getUniqueNonNullColumnValues("frequency", "drug_order", String.class,
		    upgradeTestUtil.getConnection());
		Assert.assertTrue(uniqueFrequencies.size() > 0);
		
		//map the dose units only
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112");
		
		upgradeTestUtil.upgrade();
	}
	
	@Test
	public void shouldPassIfAllExistingDrugOrderUnitsAndFrequenciesAreMappedToConcepts() throws Exception {
		//sanity check that we have some drug order dose units and frequencies in the test dataset
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/standardTest-1.9.7-dataSet.xml");
		Set<String> uniqueUnits = DatabaseUtil.getUniqueNonNullColumnValues("units", "drug_order", String.class,
		    upgradeTestUtil.getConnection());
		Assert.assertTrue(uniqueUnits.size() > 0);
		
		Set<String> uniqueFrequencies = DatabaseUtil.getUniqueNonNullColumnValues("frequency", "drug_order", String.class,
		    upgradeTestUtil.getConnection());
		Assert.assertTrue(uniqueFrequencies.size() > 0);
		
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/database1_9To1_10UpgradeTest-dataSet.xml");
		
		//set the mappings for all existing frequencies and dose units
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		upgradeTestUtil.upgrade();
		
		//Test if the generated schema corresponds to Hibernate mappings
		upgradeTestUtil.buildSessionFactory();
	}
	
	@Test
	public void shouldConvertOrderersToBeingProvidersInsteadOfUsers() throws Exception {
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/standardTest-1.9.7-dataSet.xml");
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/database1_9To1_10UpgradeTest-dataSet.xml");
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/UpgradeTest-convertOrdererToProvider.xml");
		
		//Sanity check that we have 3 orders where orderer has no provider account
		Set<Integer> personIdsWithNoProviderAccount = new HashSet<Integer>();
		List<OrderAndPerson> ordersAndOrderersWithNoProviderAccount = new ArrayList<OrderAndPerson>();
		List<List<Object>> rows = DatabaseUtil.executeSQL(upgradeTestUtil.getConnection(),
		    "select o.order_id, u.person_id from orders o join users u on o.orderer = u.user_id "
		            + "where u.person_id not in (select distinct person_id from provider)", true);
		for (List<Object> row : rows) {
			ordersAndOrderersWithNoProviderAccount.add(new OrderAndPerson((Integer) row.get(0), (Integer) row.get(1)));
			personIdsWithNoProviderAccount.add((Integer) row.get(1));
		}
		Assert.assertEquals(3, ordersAndOrderersWithNoProviderAccount.size());
		Assert.assertEquals(2, personIdsWithNoProviderAccount.size());
		Assert.assertThat(personIdsWithNoProviderAccount, Matchers.hasItems(101, 102));
		
		//Sanity check that we have 1 order where orderer has a provider account
		rows = DatabaseUtil.executeSQL(upgradeTestUtil.getConnection(),
		    "select o.order_id, o.orderer, u.person_id from orders o join users u on o.orderer = u.user_id "
		            + "where u.person_id in (select distinct person_id from provider)", true);
		List<OrderAndPerson> ordersAndOrderersWithAProviderAccount = new ArrayList<OrderAndPerson>();
		for (List<Object> row : rows) {
			ordersAndOrderersWithAProviderAccount.add(new OrderAndPerson((Integer) row.get(0), (Integer) row.get(1)));
		}
		Assert.assertEquals(9, ordersAndOrderersWithAProviderAccount.size());
		
		Set<Integer> originalProviderIds = DatabaseUtil.getUniqueNonNullColumnValues("provider_id", "provider",
		    Integer.class, upgradeTestUtil.getConnection());
		
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		upgradeTestUtil.upgrade();
		
		Set<Integer> newProviderIds = DatabaseUtil.getUniqueNonNullColumnValues("provider_id", "provider", Integer.class,
		    upgradeTestUtil.getConnection());
		//A provider account should have been created for each user with none
		Assert.assertEquals(originalProviderIds.size() + personIdsWithNoProviderAccount.size(), newProviderIds.size());
		
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
		
		//That correct providers were set for each order, i.e the person record for the created provider
		//should match the that of the user account before upgrade
		for (OrderAndPerson op : ordersAndOrderersWithNoProviderAccount) {
			rows = DatabaseUtil.executeSQL(upgradeTestUtil.getConnection(),
			    "select p.provider_id, p.person_id from provider p join orders o on p.provider_id = o.orderer where o.order_id = "
			            + op.getOrderId(), true);
			Assert.assertEquals(1, rows.size());
			Assert.assertEquals(op.getPersonId(), rows.get(0).get(1));
			//The provider account shouldn't have been among the existing ones prior to upgrade
			Assert.assertFalse(originalProviderIds.contains(rows.get(0).get(0)));
		}
	}
	
	@Test
	public void shouldConcatenateDoseStrengthAndUnits() throws IOException, SQLException {
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/standardTest-1.9.7-dataSet.xml");
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/database1_9To1_10UpgradeTest-dataSet.xml");
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		upgradeTestUtil.upgrade();
		
		List<Map<String, String>> drugs = upgradeTestUtil.select("drug", "strength");
		
		Assert.assertThat(drugs.size(), Matchers.is(3));
		Assert.assertTrue(drugs.get(0).containsValue("1.0tab(s)"));
		Assert.assertTrue(drugs.get(1).containsValue("325.0mg"));
		Assert.assertNull(drugs.get(2).get("strength"));
	}
	
	@Test
	public void shouldFailIfThereAreDrugsWithDoseStrengthAndNoNullUnits() throws IOException, SQLException {
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/standardTest-1.9.7-dataSet.xml");
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/database1_9To1_10UpgradeTest-dataSet.xml");
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/UpgradeTest-orderWithStrengthButNullUnits.xml");
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		expectedException.expect(IOException.class);
		String errorMsgSubString = "liquibase.exception.MigrationFailedException: Migration failed for change set liquibase-update-to-latest.xml::201403262139-TRUNK-4265::wyclif";
		expectedException.expectMessage(errorMsgSubString);
		upgradeTestUtil.upgrade();
	}
	
	@Test
	public void shouldFailIfThereAreDrugsWithDoseStrengthAndNoBlankUnits() throws IOException, SQLException {
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/standardTest-1.9.7-dataSet.xml");
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/database1_9To1_10UpgradeTest-dataSet.xml");
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/UpgradeTest-orderWithStrengthButBlankUnits.xml");
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		expectedException.expect(IOException.class);
		String errorMsgSubString = "liquibase.exception.MigrationFailedException: Migration failed for change set liquibase-update-to-latest.xml::201403262139-TRUNK-4265::wyclif";
		expectedException.expectMessage(errorMsgSubString);
		upgradeTestUtil.upgrade();
	}
	
	@Test
	public void shouldFailIfThereAreAnyOrderTypesInTheDatabaseOtherThanDrugOrderTypeAndNoNewColumns() throws IOException,
	        SQLException {
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/standardTest-1.9.7-dataSet.xml");
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/database1_9To1_10UpgradeTest-dataSet.xml");
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
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/standardTest-1.9.7-dataSet.xml");
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/database1_9To1_10UpgradeTest-dataSet.xml");
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/UpgradeTest-otherOrderTypes.xml");
		upgradeTestUtil.getConnection().createStatement().executeUpdate(
		    "alter table `order_type` add java_class_name varchar(255) default 'org.openmrs.Order'");
		upgradeTestUtil.getConnection().createStatement().executeUpdate("alter table `order_type` add parent int(11)");
		createOrderEntryUpgradeFileWithTestData("mg=111\ntab(s)=112\n1/day\\ x\\ 7\\ days/week=113\n2/day\\ x\\ 7\\ days/week=114");
		
		upgradeTestUtil.upgrade();
	}
}
