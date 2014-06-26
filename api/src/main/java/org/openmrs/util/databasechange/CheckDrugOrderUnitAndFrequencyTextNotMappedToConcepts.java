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
import org.openmrs.util.UpgradeUtil;

/**
 * This changesets finds all free text drug order dose units and frequencies and checks that they
 * are all mapped to concepts ids via the OpenmrsConstants#GP_ORDER_ENTRY_UNITS_TO_CONCEPTS_MAPPINGS
 * global property prior to upgrading to version 1.10. It MUST be the first 1.10.x changeset to be
 * executed
 */
public class CheckDrugOrderUnitAndFrequencyTextNotMappedToConcepts implements CustomPrecondition {
	
	@Override
	public void check(Database database) throws CustomPreconditionFailedException, CustomPreconditionErrorException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		try {
			Set<String> doseUnits = DatabaseUtil.getUniqueNonNullColumnValues("units", "drug_order", String.class,
			    connection.getUnderlyingConnection());
			Set<String> unmappedDoseUnits = getUnMappedText(doseUnits, connection);
			if (unmappedDoseUnits.size() > 0) {
				throw new CustomPreconditionFailedException(
				        "Upgrade failed because of the following unmapped drug order dose units that were found: ["
				                + StringUtils.join(unmappedDoseUnits, ", ")
				                + "]. Please make sure you have mapped all free text dose units and "
				                + "frequencies via the global property named orderEntry.unitsToConceptsMappings"
				                + " or use 1.10 upgrade helper module to map them");
			}
			
			Set<String> frequencies = DatabaseUtil.getUniqueNonNullColumnValues("frequency", "drug_order", String.class,
			    connection.getUnderlyingConnection());
			Set<String> unmappedFrequencies = getUnMappedText(frequencies, connection);
			if (unmappedFrequencies.size() > 0) {
				throw new CustomPreconditionFailedException(
				        "Upgrade failed because of the following unmapped drug order frequencies that were found: ["
				                + StringUtils.join(unmappedFrequencies, ", ")
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
	
	private Set<String> getUnMappedText(Set<String> textList, JdbcConnection connection) {
		Set<String> unmappedText = new HashSet<String>(textList.size());
		for (String text : textList) {
			if (StringUtils.isBlank(text) || UpgradeUtil.getConceptIdForUnits(text) != null) {
				continue;
			}
			
			unmappedText.add(text);
		}
		return unmappedText;
	}
	
}
