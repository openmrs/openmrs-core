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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsConstants;

/**
 * Adds yes/no boolean concepts and changes all boolean obs values to match these concepts
 */
public class BooleanConceptChangeSet implements CustomTaskChange {
	
	private static Log log = LogFactory.getLog(BooleanConceptChangeSet.class);
	
	private Integer trueConceptId;
	
	private Integer falseConceptId;
	
	//string values for boolean concepts
	private static Map<String, String[]> trueNames = new HashMap<String, String[]>();
	
	private static Map<String, String[]> falseNames = new HashMap<String, String[]>();
	
	// how to say True and Yes in OpenMRS core languages
	static {
		// names may not include spaces, or else the logic to create concept words will break
		
		trueNames.put("en", new String[] { "True", "Yes" });
		trueNames.put("fr", new String[] { "Vrai", "Oui" });
		trueNames.put("es", new String[] { "Verdadero", "Sí" });
		trueNames.put("it", new String[] { "Vero", "Sì" });
		trueNames.put("pt", new String[] { "Verdadeiro", "Sim" });
		
		falseNames.put("en", new String[] { "False", "No" });
		falseNames.put("fr", new String[] { "Faux", "Non" });
		falseNames.put("es", new String[] { "Falso", "No" });
		falseNames.put("it", new String[] { "Falso", "No" });
		falseNames.put("pt", new String[] { "Falso", "Não" });
	}
	
	/**
	 * @see liquibase.change.custom.CustomTaskChange#execute(liquibase.database.Database)
	 */
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		
		// try to find existing concepts with the right names
		trueConceptId = findConceptByName(connection, trueNames);
		falseConceptId = findConceptByName(connection, falseNames);
		
		// if they don't exist, create them
		if (trueConceptId == null) {
			trueConceptId = createConcept(connection, trueNames);
		}
		if (falseConceptId == null) {
			falseConceptId = createConcept(connection, falseNames);
		}
		
		// create the global properties
		final boolean trueFalseGlobalPropertiesPresent = getInt(connection,
		    "SELECT COUNT(*) FROM global_property WHERE property IN ('" + OpenmrsConstants.GLOBAL_PROPERTY_TRUE_CONCEPT
		            + "', '" + OpenmrsConstants.GLOBAL_PROPERTY_FALSE_CONCEPT + "')") == 2;
		if (!trueFalseGlobalPropertiesPresent) {
			createGlobalProperties(connection, trueConceptId, falseConceptId);
		}
		
		// now change all the existing obs
		changeObs(connection);
	}
	
	/**
	 * Finds a concept that has any of the the given names in the given locale. If you have a
	 * concept named 'True' in 'en_US' and you search for 'True' in 'en' this will be returned.
	 *
	 * @param connection
	 * @param names a Map from (2-letter) locale to all possible names in that locale
	 * @return a concept id.
	 * @throws CustomChangeException
	 */
	private Integer findConceptByName(JdbcConnection connection, Map<String, String[]> names) throws CustomChangeException {
		for (Map.Entry<String, String[]> e : names.entrySet()) {
			String locale = e.getKey();
			for (String name : e.getValue()) {
				Integer ret = getInt(connection, "select concept_id from concept_name where name = '" + name
				        + "' and locale like '" + locale + "%'");
				if (ret != null) {
					return ret;
				}
			}
		}
		return null;
	}
	
	/**
	 * creates a concept
	 *
	 * @param connection a DatabaseConnection
	 * @param names a Map from locale to names in that locale, which will be added to the new
	 *            concept
	 * @throws CustomChangeException
	 */
	private Integer createConcept(JdbcConnection connection, Map<String, String[]> names) throws CustomChangeException {
		PreparedStatement updateStatement = null;
		
		try {
			int conceptId = getInt(connection, "SELECT MAX(concept_id) FROM concept");
			conceptId++;
			updateStatement = connection
			        .prepareStatement("INSERT INTO concept (concept_id, short_name, description, datatype_id, class_id, retired, is_set, creator, date_created, uuid) VALUES (?, '', '', 4, 11, FALSE, FALSE, 1, NOW(), ?)");
			updateStatement.setInt(1, conceptId);
			updateStatement.setString(2, UUID.randomUUID().toString());
			updateStatement.executeUpdate();
			
			boolean preferredDoneAlready = false; // only tag one name as preferred
			
			int conceptNameId = getInt(connection, "SELECT MAX(concept_name_id) FROM concept_name");
			for (Map.Entry<String, String[]> e : names.entrySet()) {
				String locale = e.getKey();
				for (String name : e.getValue()) {
					conceptNameId++;
					updateStatement = connection
					        .prepareStatement("INSERT INTO concept_name (concept_name_id, concept_id, locale, name, creator, date_created, uuid) VALUES (?, ?, ?, ?, 1, NOW(), ?)");
					updateStatement.setInt(1, conceptNameId);
					updateStatement.setInt(2, conceptId);
					updateStatement.setString(3, locale);
					updateStatement.setString(4, name);
					updateStatement.setString(5, UUID.randomUUID().toString());
					updateStatement.executeUpdate();
					
					// Tag the first english name as preferred. This is ugly, but it's not feasible to
					// fix this before refactoring concept_name_tags.
					if (!preferredDoneAlready && "en".equals(locale)) {
						updateStatement = connection
						        .prepareStatement("INSERT INTO concept_name_tag_map (concept_name_id, concept_name_tag_id) VALUES (?, 4)");
						updateStatement.setInt(1, conceptNameId);
						updateStatement.executeUpdate();
						preferredDoneAlready = true;
					}
					
					updateStatement = connection
					        .prepareStatement("INSERT INTO concept_word (concept_id, word, locale, concept_name_id) VALUES (?, ?, ?, ?)");
					updateStatement.setInt(1, conceptId);
					updateStatement.setString(2, name);
					updateStatement.setString(3, locale);
					updateStatement.setInt(4, conceptNameId);
					updateStatement.executeUpdate();
					
				}
			}
			
			return conceptId;
		}
		catch (DatabaseException e) {
			throw new CustomChangeException("Unable to create concept with names " + names, e);
		}
		catch (SQLException e) {
			throw new CustomChangeException("Unable to create concept with names " + names, e);
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
	private void changeObs(JdbcConnection connection) throws CustomChangeException {
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
		catch (DatabaseException e) {
			throw new CustomChangeException("Unable to change obs", e);
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
	private void createGlobalProperties(JdbcConnection connection, Integer trueConceptId, Integer falseConceptId)
	        throws CustomChangeException {
		if (trueConceptId == null || trueConceptId < 1 || falseConceptId == null || falseConceptId < 1) {
			throw new CustomChangeException("Can't create global properties for true/false concepts with invalid conceptIds");
		}
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
		catch (DatabaseException e) {
			throw new CustomChangeException("Unable to create global properties for concept ids defining boolean concepts",
			        e);
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
	private Integer getInt(JdbcConnection connection, String sql) throws CustomChangeException {
		Statement stmt = null;
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			Integer result = null;
			
			if (rs.next()) {
				result = rs.getInt(1);
			} else {
				// this is okay, we just return null in this case
				log.debug("Query returned no results: " + sql);
			}
			
			if (rs.next()) {
				log.warn("Query returned multiple results when we expected just one: " + sql);
			}
			
			return result;
		}
		catch (DatabaseException e) {
			throw new CustomChangeException("Unable to get int", e);
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
	@Override
	public String getConfirmationMessage() {
		return "Finished creating boolean concepts";
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setFileOpener(liquibase.ResourceAccessor)
	 */
	@Override
	public void setFileOpener(ResourceAccessor fileOpener) {
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setUp()
	 */
	@Override
	public void setUp() throws SetupException {
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#validate(liquibase.database.Database)
	 */
	@Override
	public ValidationErrors validate(Database database) {
		return new ValidationErrors();
	}
}
