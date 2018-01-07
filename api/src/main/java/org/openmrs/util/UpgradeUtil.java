/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openmrs.api.APIException;

public class UpgradeUtil {

	private UpgradeUtil() {
	}
	
	/**
	 * Returns conceptId for the given units from DatabaseUtil#ORDER_ENTRY_UPGRADE_SETTINGS_FILENAME
	 * located in application data directory.
	 * 
	 * @param units
	 * @return conceptId
	 * @should return concept_id for units
	 * @should fail if units is not specified
	 */
	public static Integer getConceptIdForUnits(String units) {
		String appDataDir = OpenmrsUtil.getApplicationDataDirectory();
		Properties props = new Properties();
		String conceptId = null;
		String filePath = appDataDir +
				System.getProperty("file.separator") +
				DatabaseUtil.ORDER_ENTRY_UPGRADE_SETTINGS_FILENAME;

		try (FileInputStream fis = new FileInputStream(filePath)) {

			props.load(fis);
			for (Map.Entry prop : props.entrySet()) {
				if (prop.getKey().equals(units)) {
					conceptId = prop.getValue().toString();
					return Integer.valueOf(conceptId);
				}
			}
		}
		catch (NumberFormatException e) {
			throw new APIException("Your order entry upgrade settings file" + "contains invalid mapping from " + units
			        + " to concept ID " + conceptId
			        + ". ID must be an integer or null. Please refer to upgrade instructions for more details. https://wiki.openmrs.org/x/OALpAw Cause:" + e.getMessage());
		}
		catch (IOException e) {
			if (e instanceof FileNotFoundException) {
				throw new APIException("Unable to find file named order_entry_upgrade_settings.txt containing order entry upgrade settings in your "
				        + "application data directory: " + appDataDir
				        + "\nPlease refer to upgrade instructions for more details. https://wiki.openmrs.org/x/OALpAw Cause:" + e.getMessage());
			} else {
				throw new APIException(e);
			}
		}
		
		throw new APIException("Your order entry upgrade settings file" + " does not have mapping for " + units
		        + ". Please refer to upgrade instructions for more details. https://wiki.openmrs.org/x/OALpAw");
	}
	
	public static String getConceptUuid(Connection connection, int conceptId) throws SQLException {

		try (PreparedStatement select = connection.prepareStatement("select uuid from concept where concept_id = ?")) {
			select.setInt(1, conceptId);
			
			ResultSet resultSet = select.executeQuery();
			if (resultSet.next()) {
				return resultSet.getString(1);
			} else {
				throw new IllegalArgumentException("Concept not found " + conceptId);
			}
		}
	}
	
	public static String getGlobalProperty(Connection connection, String gp) throws SQLException {

		try (PreparedStatement select = connection
				.prepareStatement("select property_value from global_property where property = ?")) {
			select.setString(1, gp);
			
			ResultSet resultSet = select.executeQuery();
			if (resultSet.next()) {
				return resultSet.getString(1);
			} else {
				throw new IllegalArgumentException("Global property not found " + gp);
			}
		}
	}
	
	public static List<Integer> getMemberSetIds(Connection connection, String conceptUuid) throws SQLException {
		Integer conceptSetId;

		try (PreparedStatement select = connection.prepareStatement("select concept_id from concept where uuid = ?")) {
			select.setString(1, conceptUuid);
			
			ResultSet resultSet = select.executeQuery();
			if (resultSet.next()) {
				conceptSetId = resultSet.getInt(1);
			} else {
				throw new IllegalArgumentException("Concept not found " + conceptUuid);
			}
		}

		List<Integer> conceptIds = new ArrayList<>();

		try (PreparedStatement selectConceptIds = connection
				.prepareStatement("select concept_id from concept_set where concept_set = ?")) {
			selectConceptIds.setInt(1, conceptSetId);
			
			ResultSet resultSet = selectConceptIds.executeQuery();
			while (resultSet.next()) {
				conceptIds.add(resultSet.getInt(1));
			}
		}

		return conceptIds;
	}
	
	public static Integer getOrderFrequencyIdForConceptId(Connection connection, Integer conceptIdForFrequency)
	        throws SQLException {
		PreparedStatement orderFrequencyIdQuery = connection
		        .prepareStatement("select order_frequency_id from order_frequency where concept_id = ?");
		orderFrequencyIdQuery.setInt(1, conceptIdForFrequency);
		ResultSet orderFrequencyIdResultSet = orderFrequencyIdQuery.executeQuery();
		if (!orderFrequencyIdResultSet.next()) {
			return null;
		}
		return orderFrequencyIdResultSet.getInt("order_frequency_id");
	}
}
