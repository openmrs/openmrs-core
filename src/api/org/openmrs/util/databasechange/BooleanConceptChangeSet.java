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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import org.openmrs.util.OpenmrsConstants;

/**
 * Adds yes/no boolean concepts and changes all boolean obs values to match these concepts
 */
public class BooleanConceptChangeSet implements CustomTaskChange {
	
	private int trueConceptId;
	
	private int falseConceptId;
	
	//string values for boolean concepts
	public static final String[][] BOOLEAN_CONCEPTS_VALUES = { { "True", "False" }, { "Yes", "No" } };
	
	/**
	 * @see liquibase.change.custom.CustomTaskChange#execute(liquibase.database.Database)
	 */
	public void execute(Database database) throws CustomChangeException, UnsupportedChangeException {
		DatabaseConnection connection = database.getConnection();
		
		String trueConceptName = "";
		String falseConceptName = "";
		final boolean trueFalseGlobalPropertiesPresent = getInt(connection, "SELECT COUNT(*) FROM global_property WHERE property IN ('"
	        + OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT + "', '" + OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT + "')") == 2;
		for (String[] trueFalseConceptNames : BOOLEAN_CONCEPTS_VALUES) {
			trueConceptName = trueFalseConceptNames[0];
			falseConceptName = trueFalseConceptNames[1];
			final boolean conceptNamesPresent = getInt(connection, "SELECT COUNT(*) FROM concept_name WHERE name IN ('"
			        + trueConceptName + "', '" + falseConceptName + "')") == 2;
			
			if (conceptNamesPresent) {
				if(!trueFalseGlobalPropertiesPresent)
					createGlobalProperties(connection, trueConceptName, falseConceptName);
				changeObs(connection, trueConceptName, falseConceptName);
				return;
			}
		}
		
		trueConceptName = BOOLEAN_CONCEPTS_VALUES[0][0];
		falseConceptName = BOOLEAN_CONCEPTS_VALUES[0][1];
		if(!trueFalseGlobalPropertiesPresent)
			createConcepts(connection, trueConceptName, falseConceptName);
		createGlobalProperties(connection, trueConceptName, falseConceptName);
		changeObs(connection, trueConceptName, falseConceptName);
	}
	
	/**
	 * creates the boolean concepts
	 * 
	 * @param connection a DatabaseConnection
	 * @param trueConceptName the concept name for boolean true values
	 * @param falseConceptName the concept name for boolean false values
	 * @throws CustomChangeException
	 */
	private void createConcepts(DatabaseConnection connection, String trueConceptName, String falseConceptName)
	                                                                                                           throws CustomChangeException {
		for (String conceptName : new String[] { trueConceptName, falseConceptName }) {
			final boolean conceptNamePresent = getInt(connection, "SELECT COUNT(*) FROM concept_name WHERE name = '"
			        + conceptName + "'") == 1;
			if (!conceptNamePresent) {
				createConcept(connection, conceptName);
			} else
				throw new CustomChangeException("Another concept already exists with the name '" + conceptName + "'");
		}
	}
	
	/**
	 * creates a concept
	 * 
	 * @param connection a DatabaseConnection
	 * @param conceptName the name of the concept to create
	 * @throws CustomChangeException
	 */
	private void createConcept(DatabaseConnection connection, String conceptName) throws CustomChangeException {
		PreparedStatement updateStatement = null;
		
		try {
			final int conceptId = getInt(connection, "SELECT MAX(concept_id) + 1 FROM concept");
			
			updateStatement = connection
			        .prepareStatement("INSERT INTO concept (concept_id, short_name, description, datatype_id, class_id, is_set, creator, date_created, uuid) VALUES (?, '', '', 4, 11, 0, 1, NOW(), ?)");
			updateStatement.setInt(1, conceptId);
			updateStatement.setString(2, UUID.randomUUID().toString());
			updateStatement.executeUpdate();
			
			final int conceptNameId = getInt(connection, "SELECT MAX(concept_name_id) + 1 FROM concept_name");
			
			updateStatement = connection
			        .prepareStatement("INSERT INTO concept_name (concept_name_id, concept_id, locale, name, creator, date_created, uuid) VALUES (?, ?, 'en', ?, 1, NOW(), ?)");
			updateStatement.setInt(1, conceptNameId);
			updateStatement.setInt(2, conceptId);
			updateStatement.setString(3, conceptName);
			updateStatement.setString(4, UUID.randomUUID().toString());
			updateStatement.executeUpdate();
			
			updateStatement = connection
			        .prepareStatement("INSERT INTO concept_name_tag_map (concept_name_id, concept_name_tag_id) VALUES (?, 4)");
			updateStatement.setInt(1, conceptNameId);
			updateStatement.executeUpdate();
			
			updateStatement = connection
			        .prepareStatement("INSERT INTO concept_word (concept_id, word, locale, concept_name_id) VALUES (?, ?, 'en', ?)");
			updateStatement.setInt(1, conceptId);
			updateStatement.setString(2, conceptName);
			updateStatement.setInt(3, conceptNameId);
			updateStatement.executeUpdate();
			
		}
		catch (SQLException e) {
			throw new CustomChangeException("Unable to create concept " + conceptName, e);
		}
		finally {
			if (updateStatement != null) {
				try {
					updateStatement.close();
				}
				catch (SQLException e) {}
			}
		}
	}
	
	/**
	 * changes all obs which have boolean values to the new (coded) representation of boolean
	 * values.
	 * 
	 * @param connection a DatabaseConnection
	 * @param trueConceptName the concept name for boolean true values
	 * @param falseConceptName the concept name for boolean false values
	 * @throws CustomChangeException
	 */
	private void changeObs(DatabaseConnection connection, String trueConceptName, String falseConceptName)
	                                                                                                      throws CustomChangeException {
		PreparedStatement updateStatement = null;
		
		try {
			
			/* replace value_numerical boolean values by coded boolean values */
			updateStatement = connection
			        .prepareStatement("UPDATE obs SET value_coded = ?, value_numeric = NULL WHERE value_numeric != 0 AND concept_id IN (SELECT concept_id FROM concept WHERE datatype_id = 10)");
			updateStatement.setInt(1, trueConceptId);
			updateStatement.executeUpdate();
			
			updateStatement = connection
			        .prepareStatement("UPDATE obs SET value_coded = ?, value_numeric = NULL WHERE value_numeric = 0 AND concept_id IN (SELECT concept_id FROM concept WHERE datatype_id = 10)");
			updateStatement.setInt(1, falseConceptId);
			updateStatement.executeUpdate();
		}
		catch (SQLException e) {
			throw new CustomChangeException("Unable to change obs", e);
		}
		finally {
			if (updateStatement != null) {
				try {
					updateStatement.close();
				}
				catch (SQLException e) {}
			}
		}
	}
	
	/**
	 * Inserts global properties 'Concept.true' and 'Concept.false' into the global_property table
	 * 
	 * @param connection a DatabaseConnection
	 * @param trueConceptId the concept id for true boolean concept
	 * @param falseConceptId the concept id for false boolean concept
	 * @throws CustomChangeException
	 */
	private void createGlobalProperties(DatabaseConnection connection, String trueConceptName, String falseConceptName)
	                                                                                                                   throws CustomChangeException {
		//set the concept_ids for the newly created boolean concepts
		trueConceptId = getInt(connection, "SELECT concept_id FROM concept_name WHERE name = '" + trueConceptName + "'");
		falseConceptId = getInt(connection, "SELECT concept_id FROM concept_name WHERE name = '" + falseConceptName + "'");
		
		if (trueConceptId < 1 || falseConceptId < 1)
			throw new CustomChangeException("Can't create global properties for true/false concepts with invalid conceptIds");
		PreparedStatement updateStatement = null;
		
		try {
			updateStatement = connection
			        .prepareStatement("INSERT INTO global_property (property, property_value, description, uuid) VALUES (?, ?, ?, ?)");
			updateStatement.setString(1, OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT);
			updateStatement.setInt(2, trueConceptId);
			updateStatement.setString(3, "Concept id of the concept defining the TRUE boolean concept");
			updateStatement.setString(4, UUID.randomUUID().toString());
			updateStatement.executeUpdate();
			
			updateStatement.setString(1, OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT);
			updateStatement.setInt(2, falseConceptId);
			updateStatement.setString(3, "Concept id of the concept defining the FALSE boolean concept");
			updateStatement.setString(4, UUID.randomUUID().toString());
			updateStatement.executeUpdate();
		}
		catch (SQLException e) {
			throw new CustomChangeException("Unable to create global properties for concept ids defining boolean concepts",
			        e);
		}
		finally {
			if (updateStatement != null) {
				try {
					updateStatement.close();
				}
				catch (SQLException e) {}
			}
		}
	}
	
	/**
	 * returns an integer resulting from the execution of an sql statement
	 * 
	 * @param connection a DatabaseConnection
	 * @param sql the sql statement to execute
	 * @return integer resulting from the execution of the sql statement
	 * @throws CustomChangeException
	 */
	private int getInt(DatabaseConnection connection, String sql) throws CustomChangeException {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			int result;
			
			if (rs.next()) {
				result = rs.getInt(1);
			} else {
				throw new CustomChangeException("no result in getInt");
			}
			
			if (rs.next()) {
				throw new CustomChangeException("multiple results in getInt");
			}
			
			return result;
		}
		catch (SQLException e) {
			throw new CustomChangeException("Unable to get int", e);
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {}
			}
		}
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#getConfirmationMessage()
	 */
	public String getConfirmationMessage() {
		return "Finished creating boolean concepts";
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setFileOpener(liquibase.FileOpener)
	 */
	public void setFileOpener(FileOpener fileOpener) {
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setUp()
	 */
	public void setUp() throws SetupException {
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#validate(liquibase.database.Database)
	 */
	public void validate(Database database) throws InvalidChangeDefinitionException {
	}
}
