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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.util.DatabaseUtil;
import org.openmrs.util.UpgradeUtil;

import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomPreconditionErrorException;
import liquibase.exception.CustomPreconditionFailedException;
import liquibase.precondition.CustomPrecondition;

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
			if (!unmappedDoseUnits.isEmpty()) {
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
			if (!unmappedFrequencies.isEmpty()) {
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
		Set<String> unmappedText = new HashSet<>(textList.size());
		for (String text : textList) {
			if (StringUtils.isBlank(text) || UpgradeUtil.getConceptIdForUnits(text) != null) {
				continue;
			}
			
			unmappedText.add(text);
		}
		return unmappedText;
	}
	
}
