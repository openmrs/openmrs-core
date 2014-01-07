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

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Tests database upgrade from OpenMRS 1.9.7 to OpenMRS 1.10.
 */
public class Database1_9To1_10UpgradeTest {
	
	public final static String databasePath = "/org/openmrs/util/databasechange/openmrs-1.9.7.h2.db";
	
	private DatabaseUpgradeTestUtil upgradeTestUtil;
	
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
	}
	
	@Test(expected = Exception.class)
	public void shouldFailMigratingDrugOrdersIfUnitsToConceptsMappingsIsNotSet() throws IOException, SQLException {
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/standardTest-1.9.7-dataSet.xml");
		
		upgradeTestUtil.upgrade();
	}
	
	@Test(expected = Exception.class)
	public void shouldFailMigratingDrugOrdersIUnitsToConceptsMappingsDoesNotPointToValidCodedDoseUnits() throws IOException,
	        SQLException {
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/standardTest-1.9.7-dataSet.xml");
		
		upgradeTestUtil.insertGlobalProperty("orderEntry.unitsToConceptsMappings", "mg:-1,tab(s):-2");
		
		upgradeTestUtil.upgrade();
	}
	
	@Test
	public void shouldMigrateDrugOrders() throws IOException, SQLException {
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/standardTest-1.9.7-dataSet.xml");
		
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/database1_9To1_10UpgradeTest-dataSet.xml");
		
		upgradeTestUtil.insertGlobalProperty("orderEntry.unitsToConceptsMappings",
		    "mg:111,tab(s):112,1/day x 7 days/week:113,2/day x 7 days/week:114");
		
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
	
	private Map<String, String> row(String... values) {
		Map<String, String> row = new HashMap<String, String>();
		for (int i = 0; i < values.length; i += 2) {
			row.put(values[i], values[i + 1]);
		}
		return row;
	}
}
