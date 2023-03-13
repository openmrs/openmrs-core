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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import org.openmrs.AllergySeverity;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Moves un voided allergies from the old active_list and active_list_allergy tables to the new
 * allergy and allergy_recation tables
 */
public class MigrateAllergiesChangeSet implements CustomTaskChange {
	
	private Integer mildConcept;
	private Integer moderateConcept;
	private Integer severeConcept;
	
	@Override
	public String getConfirmationMessage() {
		return "Successfully moved un voided allergies from old to new tables";
	}
	
	@Override
	public void setUp() throws SetupException {
		
	}
	
	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		
	}
	
	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}
	
	@Override
	public void execute(Database database) throws CustomChangeException {
		try {
			loadSeverityConcepts(database);
			
			JdbcConnection connection = (JdbcConnection) database.getConnection();
			
			String sql = "select active_list_type_id from active_list_type where name = 'Allergy'";
			Statement selectStatement = connection.createStatement();
			ResultSet rs = selectStatement.executeQuery(sql);
			if (!rs.next()) {
				throw new CustomChangeException("Failed to find row with name 'Allergy' in the active_list_type");
			}
			int allergyTypeId = rs.getInt(1);
			
			sql = "insert into allergy (patient_id, coded_allergen, severity_concept_id, creator, date_created, uuid, comment, allergen_type) " +
					"values(?,?,?,?,?,?,?,?)";
			PreparedStatement allergyInsertStatement = connection.prepareStatement(sql);
			
			sql = "insert into allergy_reaction (allergy_id, reaction_concept_id, uuid) " +
					"values (?,?,?)";
			PreparedStatement reactionInsertStatement = connection.prepareStatement(sql);
			
			sql = "select allergy_id from allergy where uuid = ?";
			PreparedStatement allergySelectStatement = connection.prepareStatement(sql);
			
			sql = "select person_id, concept_id, comments, creator, date_created, uuid, reaction_concept_id, severity, allergy_type "
			        + "from active_list al inner join active_list_allergy ala on al.active_list_id=ala.active_list_id "
			        + "where voided = 0 and active_list_type_id = " + allergyTypeId;
			
			selectStatement = connection.createStatement();
			rs = selectStatement.executeQuery(sql);
			while (rs.next()) {
				String uuid = rs.getString("uuid");	
				
				//insert allergy
				allergyInsertStatement.setInt(1, rs.getInt("person_id"));
				allergyInsertStatement.setInt(2, rs.getInt("concept_id"));
				
				Integer severityConcept = null;
				String severity = rs.getString("severity");
				if (AllergySeverity.MILD.name().equals(severity)) {
					severityConcept = mildConcept;
				}
				else if (AllergySeverity.MODERATE.name().equals(severity)) {
					severityConcept = moderateConcept;
				}
				else if (AllergySeverity.SEVERE.name().equals(severity)) {
					severityConcept = severeConcept;
				}
				//TODO what do we do with the other severities?
				
				if (severityConcept != null) {
					allergyInsertStatement.setInt(3, severityConcept);
				}
				
				allergyInsertStatement.setInt(4, rs.getInt("creator"));
				allergyInsertStatement.setDate(5, rs.getDate("date_created"));
				allergyInsertStatement.setString(6, uuid);
				allergyInsertStatement.setString(7, rs.getString("comments"));
				
				String allergyType = rs.getString("allergy_type");
				if (allergyType == null) {
					allergyType = "DRUG";
				}
				else if ("ENVIRONMENTAL".equals(allergyType)) {
					allergyType = "ENVIRONMENT";
				}
				
				allergyInsertStatement.setString(8, allergyType);
				
				allergyInsertStatement.execute();
				
				//get inserted allergy_id
				allergySelectStatement.setString(1, uuid);
				ResultSet rs2 = allergySelectStatement.executeQuery();
				rs2.next();
				
				//insert reaction
				reactionInsertStatement.setInt(1, rs2.getInt(1));
				reactionInsertStatement.setInt(2, rs.getInt("reaction_concept_id"));
				reactionInsertStatement.setString(3, UUID.randomUUID().toString());
				
				//some active lists do not have reactions recorded
				if (!rs.wasNull()) {
					reactionInsertStatement.execute();
				}
			}
		}
		catch (Exception ex) {
			throw new CustomChangeException(ex);
		}
	}
	
	private void loadSeverityConcepts(Database database) throws Exception {
		mildConcept = getConceptByGlobalProperty(database, "allergy.concept.severity.mild");
		moderateConcept = getConceptByGlobalProperty(database, "allergy.concept.severity.moderate");
		severeConcept = getConceptByGlobalProperty(database, "allergy.concept.severity.severe");
	}
	
	private Integer getConceptByGlobalProperty(Database database, String globalPropertyName) throws Exception {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		PreparedStatement stmt = connection.prepareStatement("SELECT property_value FROM global_property WHERE property = ?");
		stmt.setString(1, globalPropertyName);
		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			String uuid = rs.getString("property_value");
			
			rs = stmt.executeQuery("SELECT concept_id FROM concept WHERE uuid = '" + uuid + "'");
			if (rs.next()) {
				return rs.getInt("concept_id");
			}
		}
		
		throw new IllegalStateException("Configuration required: " + globalPropertyName);
	}
}
