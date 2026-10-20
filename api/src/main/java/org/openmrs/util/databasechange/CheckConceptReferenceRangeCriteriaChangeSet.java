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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.openmrs.util.ConceptReferenceRangeUtility;
import org.openmrs.util.DatabaseUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ParseException;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Reports stored concept reference range criteria that can no longer be evaluated because they call
 * constructs the criteria sandbox refuses, or because they do not parse. Nothing is modified; each
 * offending range is registered as an update warning so it can be corrected after the upgrade.
 *
 * @since 3.0.0
 */
public class CheckConceptReferenceRangeCriteriaChangeSet implements CustomTaskChange {

	private static final Logger log = LoggerFactory.getLogger(CheckConceptReferenceRangeCriteriaChangeSet.class);

	private static final Pattern READ_ONLY_CALL = Pattern.compile(
	    "^(equals|equalsIgnoreCase|matches|contains|startsWith|endsWith|after|before|compareTo|size|length|toString"
	            + "|toUpperCase|toLowerCase|trim)\\(.*");

	@Override
	public void execute(Database database) throws CustomChangeException {
		List<String> warnings;
		try {
			warnings = findWarnings((JdbcConnection) database.getConnection());
		} catch (SQLException | DatabaseException e) {
			throw new CustomChangeException("Failed to check concept reference range criteria", e);
		}

		for (String warning : warnings) {
			log.warn(warning);
		}
		if (!warnings.isEmpty()) {
			DatabaseUpdater.reportUpdateWarnings(warnings);
		}
	}

	List<String> findWarnings(JdbcConnection connection) throws SQLException, DatabaseException {
		List<String> warnings = new ArrayList<>();
		try (Statement statement = connection.createStatement();
		        ResultSet rs = statement.executeQuery(
		            "SELECT concept_reference_range_id, concept_id, uuid, criteria FROM concept_reference_range")) {
			while (rs.next()) {
				String problem = describeProblem(rs.getString("criteria"));
				if (problem != null) {
					warnings.add("Concept reference range " + rs.getInt("concept_reference_range_id") + " (uuid "
					        + rs.getString("uuid") + ") for concept " + rs.getInt("concept_id") + " " + problem);
				}
			}
		}
		return warnings;
	}

	static String describeProblem(String criteria) {
		if (StringUtils.isBlank(criteria)) {
			return null;
		}
		List<String> disallowed;
		try {
			disallowed = ConceptReferenceRangeUtility.findDisallowedConstructs(StringEscapeUtils.unescapeHtml4(criteria));
		} catch (ParseException e) {
			return "has criteria that does not parse (" + e.getSimpleMessage() + ") and must be corrected before "
			        + "observations for this concept can be saved: " + criteria;
		}
		if (disallowed.isEmpty()) {
			return null;
		}
		String constructs = String.join(", ", disallowed);
		if (disallowed.stream().allMatch(construct -> READ_ONLY_CALL.matcher(construct).matches())) {
			return "has criteria that can no longer be evaluated because it calls " + constructs
			        + ". Rewrite it using only get*, is* and has* methods and $fn helper methods "
			        + "so observations for this concept can be saved: " + criteria;
		}
		return "has criteria that may modify data (" + constructs + "). Until this release, the "
		        + "expression ran on every observation saved for this concept with the saving user's permissions; "
		        + "review who stored it and what it changed, then correct it: " + criteria;
	}

	@Override
	public String getConfirmationMessage() {
		return "Finished checking concept reference range criteria";
	}

	@Override
	public void setUp() throws SetupException {
		// nothing to set up
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// no changelog resources are read by this change
	}

	@Override
	public ValidationErrors validate(Database database) {
		return new ValidationErrors();
	}
}
