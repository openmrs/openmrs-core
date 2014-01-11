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

import java.util.HashSet;
import java.util.Set;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomPreconditionErrorException;
import liquibase.exception.CustomPreconditionFailedException;
import liquibase.precondition.CustomPrecondition;

import org.apache.commons.lang.StringUtils;
import org.openmrs.util.DatabaseUtil;

/**
 * This changesets runs through all free text drug order dose units and frequencies were mapped to
 * concepts ids prior to upgrading to version 1.10
 */
public class CheckDrugOrderUnitAndFrequencyTextNotMappedToConcepts implements CustomPrecondition {
	
	@Override
	public void check(Database database) throws CustomPreconditionFailedException, CustomPreconditionErrorException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		try {
			Set<Object> doseUnits = DatabaseUtil.getUniqueNonNullColumnValues("units", "drug_order", connection);
			Set<String> unmappedDoseUnits = getUnMappedText(doseUnits, connection);
			if (unmappedDoseUnits.size() > 0) {
				throw new CustomPreconditionFailedException(
				        "Upgrade failed because of the following unmapped drug order dose units that were found: ["
				                + StringUtils.join(unmappedDoseUnits, ", ")
				                + "]. Please make sure you have mapped all free text dose units and "
				                + "frequencies via the global property named orderEntry.unitsToConceptsMappings"
				                + " or use 1.10 upgrade helper module to map them");
			}
			
			Set<Object> frequencies = DatabaseUtil.getUniqueNonNullColumnValues("frequency", "drug_order", connection);
			Set<String> unmappedFrequencies = getUnMappedText(frequencies, connection);
			if (unmappedFrequencies.size() > 0) {
				throw new CustomPreconditionFailedException(
				        "Upgrade failed because of the following unmapped drug order frequencies units that were found: ["
				                + StringUtils.join(unmappedDoseUnits, ", ")
				                + "]. Please make sure you have mapped all free text dose units and "
				                + "frequencies via the global property named orderEntry.unitsToConceptsMappings"
				                + " or use 1.10 upgrade helper module to map them");
			}
		}
		catch (Exception e) {
			throw new CustomPreconditionErrorException("An error occurred while checking for unmapped free text drug "
			        + "order dose units and frequencies", e);
		}
	}
	
	private Set<String> getUnMappedText(Set<Object> textList, JdbcConnection connection) {
		Set<String> unmappedText = new HashSet<String>(textList.size());
		for (Object text : textList) {
			try {
				if (DatabaseUtil.getConceptIdForUnits(connection.getUnderlyingConnection(), text.toString()) != null) {
					continue;
				}
			}
			catch (Exception e) {
				//ignore, mostly like an invalid integer value
			}
			
			unmappedText.add(text.toString());
		}
		return unmappedText;
	}
	
}
