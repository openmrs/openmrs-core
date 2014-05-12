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
			props.load(new FileInputStream(appDataDir + System.getProperty("file.separator")
			        + DatabaseUtil.ORDER_ENTRY_UPGRADE_SETTINGS_FILENAME));
			for (Map.Entry prop : props.entrySet()) {
				if (prop.getKey().equals(units)) {
					conceptId = prop.getValue().toString();
					
					if (conceptId != null) {
						return Integer.valueOf(conceptId);
					} else {
						return null;
					}
				}
			}
		}
		catch (NumberFormatException e) {
			throw new APIException("Your order entry upgrade settings file" + "contains invalid mapping from " + units
			        + " to concept ID " + conceptId
			        + ". ID must be an integer or null. Please refer to upgrade instructions for more details.", e);
		}
		catch (IOException e) {
			if (e instanceof FileNotFoundException) {
				throw new APIException("Unable to find file containing order entry upgrade settings in your "
				        + "application data directory: " + appDataDir
				        + "\nPlease refer to upgrade instructions for more details.", e);
			} else {
				throw new APIException(e);
			}
		}
		
		throw new APIException("Your order entry upgrade settings file" + " does not have mapping for " + units
		        + ". Please refer to upgrade instructions for more details.");
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
