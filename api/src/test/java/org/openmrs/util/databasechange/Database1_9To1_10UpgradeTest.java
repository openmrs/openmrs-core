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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
		
		insertGlobalProperty("orderEntry.unitsToConceptsMappings", "mg:-1,tab(s):-2");
		
		upgradeTestUtil.upgrade();
	}
	
	@Test
	public void shouldMigrateDrugOrders() throws IOException, SQLException {
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/standardTest-1.9.7-dataSet.xml");
		
		upgradeTestUtil.executeDataset("/org/openmrs/util/databasechange/database1_9To1_10UpgradeTest-dataSet.xml");
		
		insertGlobalProperty("orderEntry.unitsToConceptsMappings", "mg:111,tab(s):112,1/day x 7 days/week:113");
		
		upgradeTestUtil.upgrade();
		
		//TODO: add assertions
	}
	
	public void insertGlobalProperty(String globalProperty, String value) throws SQLException {
		Connection connection = upgradeTestUtil.getConnection();
		PreparedStatement insert = connection
		        .prepareStatement("insert into global_property (property, property_value, uuid) values (?, ?, ?)");
		insert.setString(1, globalProperty);
		insert.setString(2, value);
		insert.setString(3, UUID.randomUUID().toString());
		
		insert.executeUpdate();
		
		connection.commit();
	}
}
