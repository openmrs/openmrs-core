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
		try {
			FileInputStream fis = new FileInputStream(appDataDir + System.getProperty("file.separator")
			        + DatabaseUtil.ORDER_ENTRY_UPGRADE_SETTINGS_FILENAME);
			props.load(fis);
			for (Map.Entry prop : props.entrySet()) {
				if (prop.getKey().equals(units)) {
					conceptId = prop.getValue().toString();
					return Integer.valueOf(conceptId);
				}
			}
			fis.close();
		}
		catch (NumberFormatException e) {
			throw new APIException("upgrade.settings.file.invalid.mapping", new Object[] { units, conceptId }, e);
		}
		catch (IOException e) {
			if (e instanceof FileNotFoundException) {
				throw new APIException("upgrade.settings.unable.find.file", new Object[] { appDataDir }, e);
			} else {
				throw new APIException(e);
			}
		}
		
		throw new APIException("upgrade.settings.file.not.have.mapping", new Object[] { units });
	}
	
	public static String getConceptUuid(Connection connection, int conceptId) throws SQLException {
		PreparedStatement select = connection.prepareStatement("select uuid from concept where concept_id = ?");
		try {
			select.setInt(1, conceptId);
			
			ResultSet resultSet = select.executeQuery();
			if (resultSet.next()) {
				return resultSet.getString(1);
			} else {
				throw new IllegalArgumentException("Concept not found " + conceptId);
			}
		}
		finally {
			select.close();
		}
	}
	
	public static String getGlobalProperty(Connection connection, String gp) throws SQLException {
		PreparedStatement select = connection
		        .prepareStatement("select property_value from global_property where property = ?");
		try {
			select.setString(1, gp);
			
			ResultSet resultSet = select.executeQuery();
			if (resultSet.next()) {
				return resultSet.getString(1);
			} else {
				throw new IllegalArgumentException("Global property not found " + gp);
			}
		}
		finally {
			select.close();
		}
	}
	
	public static List<Integer> getMemberSetIds(Connection connection, String conceptUuid) throws SQLException {
		Integer conceptSetId = null;
		
		PreparedStatement select = connection.prepareStatement("select concept_id from concept where uuid = ?");
		try {
			select.setString(1, conceptUuid);
			
			ResultSet resultSet = select.executeQuery();
			if (resultSet.next()) {
				conceptSetId = resultSet.getInt(1);
			} else {
				throw new IllegalArgumentException("Concept not found " + conceptUuid);
			}
		}
		finally {
			select.close();
		}
		
		List<Integer> conceptIds = new ArrayList<Integer>();
		PreparedStatement selectConceptIds = connection
		        .prepareStatement("select concept_id from concept_set where concept_set = ?");
		try {
			selectConceptIds.setInt(1, conceptSetId);
			
			ResultSet resultSet = selectConceptIds.executeQuery();
			while (resultSet.next()) {
				conceptIds.add(resultSet.getInt(1));
			}
		}
		finally {
			selectConceptIds.close();
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
