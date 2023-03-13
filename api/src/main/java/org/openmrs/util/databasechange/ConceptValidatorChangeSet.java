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

import java.sql.BatchUpdateException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * This change set is run just after the conversion of core concept name tags to concept name types'
 * it runs through all the rows in the concept table and checks if all its conceptNames conform to
 * the constraints added with the conversion of the tags.
 */
public class ConceptValidatorChangeSet implements CustomTaskChange {
	
	private static final Logger log = LoggerFactory.getLogger(ConceptValidatorChangeSet.class);
	
	//List to store warnings
	private List<String> updateWarnings = new LinkedList<>();
	
	//List to store info messages
	private List<String> logMessages = new LinkedList<>();
	
	//A set to store unique concept names that have been updated and changes have to be persisted to the database
	private Set<ConceptName> updatedConceptNames = new HashSet<>();
	
	private Locale defaultLocale = new Locale("en");
	
	private List<Locale> allowedLocales = null;
	
	/**
	 * @see CustomTaskChange#execute(Database)
	 */
	@Override
	public void execute(Database database) throws CustomChangeException {
		
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		//In the liquibase changelog file, there is a precondition that checks if this is a fresh installation
		//with no rows in the concept table or if it has some active concepts, we don't need to check again.
		
		//validate all concepts and try to fix those that can be fixed, note that updates are not committed inside this methods
		validateAndCleanUpConcepts(connection);
		
		//commit as a batch update
		if (!updatedConceptNames.isEmpty()) {
			runBatchUpdate(connection);
		} else {
			log.debug("No concept names to update");
		}
		
		if (!logMessages.isEmpty() || !updateWarnings.isEmpty()) {
			writeWarningsToFile();
		}
		
		if (!updateWarnings.isEmpty()) {
			DatabaseUpdater.reportUpdateWarnings(updateWarnings);
		}
		
		//we need this memory in case the lists are large
		updateWarnings = null;
		updatedConceptNames = null;
		logMessages = null;
	}
	
	/**
	 * This method is called by the execute {@link #execute(Database)} method to run through all
	 * concept and their conceptNames and validates them, It also tries to fix any constraints that
	 * are being violated.
	 *
	 * @param connection The database connection
	 */
	private void validateAndCleanUpConcepts(JdbcConnection connection) {
		List<Integer> conceptIds = getAllUnretiredConceptIds(connection);
		allowedLocales = getAllowedLocalesList(connection);
		//default locale(if none, then 'en') is always the last in the list.
		defaultLocale = allowedLocales.get(allowedLocales.size() - 1);
		//a map to store all duplicates names found for each locale
		Map<Locale, Set<String>> localeDuplicateNamesMap = null;
		
		for (Integer conceptId : conceptIds) {
			
			Map<Locale, List<ConceptName>> localeConceptNamesMap = getLocaleConceptNamesMap(connection, conceptId);
			if (localeConceptNamesMap == null) {
				updateWarnings.add("No names added for concept with id: " + conceptId);
				continue;
			}
			
			boolean hasFullySpecifiedName = false;
			List<ConceptName> namesWithNoLocale = null;
			
			//for each locale
			for (Map.Entry<Locale, List<ConceptName>> e : localeConceptNamesMap.entrySet()) {
				Locale conceptNameLocale = e.getKey();
				boolean fullySpecifiedNameForLocaleFound = false;
				boolean preferredNameForLocaleFound = false;
				boolean shortNameForLocaleFound = false;
				//map to hold a name and a list of conceptNames that are found as duplicates
				Map<String, List<ConceptName>> nameDuplicateConceptNamesMap = new HashMap<>();
				
				//for each name in the locale
				for (ConceptName nameInLocale : e.getValue()) {
					if (StringUtils.isBlank(nameInLocale.getName())) {
						updateWarnings.add("ConceptName with id " + nameInLocale.getConceptNameId() + " ("
						        + nameInLocale.getName() + ") is null, white space character or empty string");
					}
					//if the concept name has no locale, wonder why this would be the case but there was no not-null constraint originally
					if (conceptNameLocale == null) {
						if (namesWithNoLocale == null) {
							namesWithNoLocale = new LinkedList<>();
						}
						
						namesWithNoLocale.add(nameInLocale);
						continue;
					}
					
					//The concept's locale should be among the allowed locales listed in global properties
					if (!allowedLocales.contains(conceptNameLocale)) {
						updateWarnings.add("ConceptName with id: " + nameInLocale.getConceptNameId() + " ("
						        + nameInLocale.getName() + ") has a locale (" + conceptNameLocale
						        + ") that isn't listed among the allowed ones by the system admin");
					}
					
					if (nameInLocale.getLocalePreferred() != null) {
						if (nameInLocale.getLocalePreferred() && !preferredNameForLocaleFound) {
							if (nameInLocale.isIndexTerm()) {
								nameInLocale.setLocalePreferred(false);
								reportUpdatedName(nameInLocale, "Preferred name '" + nameInLocale.getName()
								        + "' in locale '" + conceptNameLocale.getDisplayName()
								        + "' has been dropped as the preferred name because it is a search term");
							} else if (nameInLocale.isShort()) {
								nameInLocale.setLocalePreferred(false);
								reportUpdatedName(nameInLocale, "Preferred name '" + nameInLocale.getName()
								        + "' in locale '" + conceptNameLocale.getDisplayName()
								        + "' has been dropped as the preferred name because it is a short name");
							} else {
								preferredNameForLocaleFound = true;
							}
						}
						//should have one preferred name per locale
						else if (nameInLocale.getLocalePreferred() && preferredNameForLocaleFound) {
							//drop this name as locale preferred so that we have only one
							nameInLocale.setLocalePreferred(false);
							reportUpdatedName(
							    nameInLocale,
							    "Preferred name '"
							            + nameInLocale.getName()
							            + "' in locale '"
							            + conceptNameLocale.getDisplayName()
							            + "' has been dropped as the preferred name because there is already another preferred name in the same locale");
						}
					} else {
						//Enforce not-null on locale preferred field constraint from the database table
						nameInLocale.setLocalePreferred(false);
						reportUpdatedName(nameInLocale, "The locale preferred property of name '" + nameInLocale.getName()
						        + "' in locale '" + conceptNameLocale.getDisplayName()
						        + "' has been updated to false from null");
					}
					
					if (nameInLocale.isFullySpecifiedName()) {
						if (!hasFullySpecifiedName) {
							hasFullySpecifiedName = true;
						}
						if (!fullySpecifiedNameForLocaleFound) {
							fullySpecifiedNameForLocaleFound = true;
						} else {
							nameInLocale.setConceptNameType(null);
							reportUpdatedName(nameInLocale, "The name '" + nameInLocale.getName() + "' in locale '"
							        + conceptNameLocale.getDisplayName()
							        + "' has been converted from fully specified to a synonym");
						}
					}
					
					if (nameInLocale.isShort()) {
						if (!shortNameForLocaleFound) {
							shortNameForLocaleFound = true;
						}
						//should have one short name per locale
						else {
							nameInLocale.setConceptNameType(null);
							reportUpdatedName(nameInLocale, "The name '" + nameInLocale.getName() + "' in locale '"
							        + conceptNameLocale.getDisplayName()
							        + "' has been converted from a short name to a synonym");
						}
					}
					
					if ((nameInLocale.isFullySpecifiedName() || nameInLocale.isPreferred())
					        && !isNameUniqueInLocale(connection, nameInLocale, conceptId)) {
						if (localeDuplicateNamesMap == null) {
							localeDuplicateNamesMap = new HashMap<>();
						}
						if (!localeDuplicateNamesMap.containsKey(conceptNameLocale)) {
							localeDuplicateNamesMap.put(conceptNameLocale, new HashSet<>());
						}
						
						localeDuplicateNamesMap.get(conceptNameLocale).add(nameInLocale.getName());
					}
					
					String name = nameInLocale.getName().toLowerCase();
					if (!nameDuplicateConceptNamesMap.containsKey(name)) {
						nameDuplicateConceptNamesMap.put(name, new ArrayList<>());
					}
					
					nameDuplicateConceptNamesMap.get(name).add(nameInLocale);
					
				}//close for each name
				
				//No duplicate names allowed for the same locale and concept
				for (Map.Entry<String, List<ConceptName>> entry : nameDuplicateConceptNamesMap.entrySet()) {
					//no duplicates found for the current name
					if (entry.getValue().size() < 2) {
						continue;
					}
					
					logMessages.add("The name '" + entry.getKey() + "' was found multiple times for the concept with id '"
					        + conceptId + "' in locale '" + conceptNameLocale.getDisplayName() + "'");					
				}
				
				//if this locale has no preferred name found, set one
				if (!preferredNameForLocaleFound) {
					//find the fully specified name and set it as the locale preferred
					for (ConceptName cn : localeConceptNamesMap.get(conceptNameLocale)) {
						if (cn.isFullySpecifiedName()) {
							cn.setLocalePreferred(true);
							preferredNameForLocaleFound = true;
							break;
						}
					}
					
					//if there was no fully specified name found, mark one of the synonyms as locale preferred
					if (!preferredNameForLocaleFound) {
						for (ConceptName cn : localeConceptNamesMap.get(conceptNameLocale)) {
							if (cn.isSynonym()) {
								cn.setLocalePreferred(true);
								break;
							}
						}
					}
				}
				
			}//close for each locale
			
			//Make the first name found the fully specified name if none exists
			if (!hasFullySpecifiedName) {
				hasFullySpecifiedName = setFullySpecifiedName(conceptId, localeConceptNamesMap);
			}
			
			//set default locale for names with no locale, if there was no fully specified name for the current concept,
			//set the first name found as the fully specified and drop locale preferred mark and short name concept name type
			if (!CollectionUtils.isEmpty(namesWithNoLocale)) {
				for (ConceptName conceptName : namesWithNoLocale) {
					conceptName.setLocale(defaultLocale);
					reportUpdatedName(conceptName, "The locale for ConceptName with id " + conceptName.getConceptNameId()
					        + " (" + conceptName.getName() + ") has been set to '" + defaultLocale.getDisplayName() + "'");
					if (!hasFullySpecifiedName) {
						conceptName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
						hasFullySpecifiedName = true;
						reportUpdatedName(conceptName, "ConceptName with id " + conceptName.getConceptNameId() + " ("
						        + conceptName.getName() + ") in locale '" + defaultLocale.getDisplayName()
						        + "' has been set as the fully specified name for concept with id : " + conceptId);
					}
					//convert to a synonym and should not be preferred, this will avoid inconsistencies, in case
					//already short, fully specified and preferred names exist
					else {
						conceptName.setLocalePreferred(false);
						reportUpdatedName(conceptName, "ConceptName with id " + conceptName.getConceptNameId() + " ("
						        + conceptName.getName() + ") is no longer marked as preferred because it had no locale");
						if (conceptName.isFullySpecifiedName() || conceptName.isShort()) {
							conceptName.setConceptNameType(null);
							reportUpdatedName(conceptName, "The name '" + conceptName.getName() + "' in locale '"
							        + conceptName.toString() + "' has been converted to a synonym because it had no locale");
						}
					}
					
				}
			}
			
			if (!hasFullySpecifiedName) {
				updateWarnings.add("Concept with id: " + conceptId + " has no fully specified name");
			}
			
		}
		
		if (!MapUtils.isEmpty(localeDuplicateNamesMap)) {
			for (Map.Entry<Locale, Set<String>> entry : localeDuplicateNamesMap.entrySet()) {
				//no duplicates found in the locale
				if (CollectionUtils.isEmpty(entry.getValue())) {
					continue;
				}
				
				for (String duplicateName : entry.getValue()) {
					updateWarnings.add("Concept Name '" + duplicateName + "' was found multiple times in locale '"
					        + entry.getKey() + "'");
				}
			}
		}
		
		logMessages.add("Number of Updated ConceptNames: " + updatedConceptNames.size());
	}
	
	/**
	 * Method writes the log messages and error warnings to the application data directory
	 */
	private void writeWarningsToFile() {
		
		String lineSeparator = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder("WARNINGS:").append(lineSeparator);
		for (String warning : updateWarnings) {
			sb.append(lineSeparator).append(warning);
		}
		
		sb.append(lineSeparator).append(lineSeparator).append("NOTIFICATIONS:").append(lineSeparator);
		
		for (String message : logMessages) {
			sb.append(lineSeparator).append(message);
		}
		
		DatabaseUpdater.writeUpdateMessagesToFile(sb.toString());
		
	}
	
	/**
	 * Sets the fully specified name from available names
	 *
	 * @param localeConceptNamesMap, list of all concept names for the concept
	 * @return
	 */
	private boolean setFullySpecifiedName(int conceptId, Map<Locale, List<ConceptName>> localeConceptNamesMap) {
		
		//Pick the first name in any locale by searching in order from the allowed locales
		for (Locale allowedLoc : allowedLocales) {
			List<ConceptName> possibleFullySpecNames = localeConceptNamesMap.get(allowedLoc);
			if (CollectionUtils.isEmpty(possibleFullySpecNames)) {
				continue;
			}
			
			//try the synonyms
			for (ConceptName cn : possibleFullySpecNames) {
				if (cn.isSynonym()) {
					cn.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
					reportUpdatedName(cn, "ConceptName with id " + cn.getConceptNameId() + " (" + cn.getName()
					        + ") in locale '" + allowedLoc.getDisplayName()
					        + "' has been set as the fully specified name for concept with id : " + conceptId);
					return true;
				}
			}
			
			//try the short names
			for (ConceptName cn : possibleFullySpecNames) {
				if (cn.isShort()) {
					cn.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
					reportUpdatedName(cn, "ConceptName with id " + cn.getConceptNameId() + " (" + cn.getName()
					        + ") in locale '" + allowedLoc.getDisplayName()
					        + "' has been changed from short to fully specified name for concept with id : " + conceptId);
					return true;
				}
			}
		}
		
		//pick a name randomly from the conceptName map
		for (Map.Entry<Locale, List<ConceptName>> entry : localeConceptNamesMap.entrySet()) {
			Locale locale = entry.getKey();
			if (locale != null) {
				ConceptName fullySpecName = entry.getValue().get(0);
				fullySpecName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
				reportUpdatedName(fullySpecName, "ConceptName with id " + fullySpecName.getConceptNameId() + " ("
				        + fullySpecName.getName() + ") in locale '" + locale.getDisplayName()
				        + "' has been set as the fully specified name for concept with id : " + conceptId);
				return true;
			}
		}
		
		//most probably this concept has no names added to it yet
		return false;
	}
	
	/**
	 * Adds the specified concept name to the list of concept names to be updated and also adds the
	 * specified message to the list of messages/warnings to be reported after the database updater
	 * has terminated
	 *
	 * @param updatedName the name that has been updated
	 * @param updateMessage the message to report
	 */
	private void reportUpdatedName(ConceptName updatedName, String updateMessage) {
		updatedConceptNames.add(updatedName);
		logMessages.add(updateMessage);
	}
	
	/**
	 * Fetches all conceptIds for un retired concepts from the database.
	 *
	 * @param connection The database connection
	 * @return A list of all fetched conceptIds
	 */
	private List<Integer> getAllUnretiredConceptIds(JdbcConnection connection) {
		
		LinkedList<Integer> conceptIds = null;
		Statement stmt = null;
		
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT concept_id FROM concept WHERE retired = '0'");
			
			while (rs.next()) {
				if (conceptIds == null) {
					conceptIds = new LinkedList<>();
				}
				
				conceptIds.add(rs.getInt("concept_id"));
			}
		}
		catch (DatabaseException | SQLException e) {
			log.warn("Error generated", e);
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					log.warn("Failed to close the statement object");
				}
			}
		}
		
		return conceptIds;
	}
	
	/**
	 * Checks if the conceptName is unique among all unvoided preferred and fully specified names
	 * across all other un-retired concepts except the concept it is associated to.
	 *
	 * @param connection The datbase Connection
	 * @param conceptName The conceptName to be validated
	 * @return true if the conceptName is unique, otherwise false
	 */
	private boolean isNameUniqueInLocale(JdbcConnection connection, ConceptName conceptName, int conceptId) {
		
		int duplicates = getInt(connection,
		    "SELECT count(*) FROM concept_name cn, concept c WHERE cn.concept_id = c.concept_id  AND (cn.concept_name_type = '"
		            + ConceptNameType.FULLY_SPECIFIED
		            + "' OR cn.locale_preferred = '1') AND cn.voided = '0' AND cn.name = '"
		            + HibernateUtil.escapeSqlWildcards(conceptName.getName(), connection.getUnderlyingConnection())
		            + "' AND cn.locale = '"
		            + HibernateUtil.escapeSqlWildcards(conceptName.getLocale().toString(), connection
		                    .getUnderlyingConnection()) + "' AND c.retired = '0' AND c.concept_id != " + conceptId);
		
		return duplicates == 0;
	}
	
	/**
	 * Retrieves the list of allowed locales from the database, sets the default locale, english and
	 * the default locale will be added to the list allowed locales if not yet included
	 *
	 * @param connection The database connection
	 * @return A list of allowed locales
	 */
	@SuppressWarnings("unchecked")
	private List<Locale> getAllowedLocalesList(JdbcConnection connection) {
		Statement stmt = null;
		ListOrderedSet allowedLocales = new ListOrderedSet();
		
		try {
			//get the default locale
			stmt = connection.createStatement();
			ResultSet rsDefaultLocale = stmt.executeQuery("SELECT property_value FROM global_property WHERE property = '"
			        + OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE + "'");
			
			if (rsDefaultLocale.next()) {
				String defaultLocaleStr = rsDefaultLocale.getString("property_value");
				if (!StringUtils.isBlank(defaultLocaleStr) && defaultLocaleStr.length() > 1) {
					Locale defaultLocaleGP = LocaleUtility.fromSpecification(defaultLocaleStr);
					if (defaultLocaleGP != null) {
						defaultLocale = defaultLocaleGP;
					}
				} else {
					updateWarnings.add("'" + defaultLocaleStr
					        + "' is an invalid value for the global property default locale");
				}
			}
			
			allowedLocales.add(defaultLocale);
			
			//get the locale.allowed.list
			ResultSet rsAllowedLocales = stmt.executeQuery("SELECT property_value FROM global_property WHERE property = '"
			        + OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST + "'");
			
			if (rsAllowedLocales.next()) {
				String allowedLocaleStr = rsAllowedLocales.getString("property_value");
				if (!StringUtils.isBlank(allowedLocaleStr)) {
					String[] localesArray = allowedLocaleStr.split(",");
					for (String localeStr : localesArray) {
						if (localeStr.trim().length() > 1) {
							allowedLocales.add(LocaleUtility.fromSpecification(localeStr.trim()));
						} else {
							updateWarnings.add("'" + localeStr
							        + "' is an invalid value for the global property locale.allowed.list");
						}
					}
				}
			} else {
				log.warn("The global property '" + OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST + "' isn't set");
			}
		}
		catch (DatabaseException | SQLException e) {
			log.warn("Error generated", e);
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					log.warn("Failed to close the statement object");
				}
			}
		}
		
		//if it isn't among
		allowedLocales.add(new Locale("en"));
		
		return allowedLocales.asList();
	}
	
	/**
	 * Convenience Method that fetches all non-voided concept names from the database associated to
	 * a concept with a matching concept id, stores the names in a map with locales as the keys and
	 * the lists of conceptNames in each locale as the values i.e &lt;Locale List&lt;ConceptNames&gt;&gt;.
	 *
	 * @param connection a DatabaseConnection
	 * @param conceptId the conceptId for the conceptNames to fetch
	 * @return a map of Locale with ConceptNames in them associated to the concept identified by the
	 *         given conceptId
	 */
	private Map<Locale, List<ConceptName>> getLocaleConceptNamesMap(JdbcConnection connection, int conceptId) {
		PreparedStatement pStmt = null;
		Map<Locale, List<ConceptName>> localeConceptNamesMap = null;
		
		try {
			pStmt = connection
			        .prepareStatement("SELECT concept_name_id, name, concept_name_type, locale, locale_preferred FROM concept_name WHERE voided = '0' AND concept_id = ?");
			pStmt.setInt(1, conceptId);
			ResultSet rs = pStmt.executeQuery();
			
			while (rs.next()) {
				if (localeConceptNamesMap == null) {
					localeConceptNamesMap = new HashMap<>();
				}
				ConceptName conceptName = new ConceptName();
				conceptName.setConceptNameId(rs.getInt("concept_name_id"));
				conceptName.setName(rs.getString("name"));
				
				String cnType = rs.getString("concept_name_type");
				if (!StringUtils.isBlank(cnType)) {
					ConceptNameType conceptNameType = null;
					if (cnType.equals(ConceptNameType.FULLY_SPECIFIED.toString())) {
						conceptNameType = ConceptNameType.FULLY_SPECIFIED;
					} else if (cnType.equals(ConceptNameType.SHORT.toString())) {
						conceptNameType = ConceptNameType.SHORT;
					} else if (cnType.equals(ConceptNameType.INDEX_TERM.toString())) {
						conceptNameType = ConceptNameType.INDEX_TERM;
					}
					conceptName.setConceptNameType(conceptNameType);
				}
				String localeString = rs.getString("locale");
				conceptName.setLocale(!StringUtils.isBlank(localeString) ? LocaleUtility.fromSpecification(localeString)
				        : null);
				conceptName.setLocalePreferred(rs.getBoolean("locale_preferred"));
				conceptName.setVoided(false);
				
				if (!localeConceptNamesMap.containsKey(conceptName.getLocale())) {
					localeConceptNamesMap.put(conceptName.getLocale(), new LinkedList<>());
				}
				
				localeConceptNamesMap.get(conceptName.getLocale()).add(conceptName);
			}
		}
		catch (DatabaseException | SQLException e) {
			log.warn("Error generated", e);
		}
		finally {
			if (pStmt != null) {
				try {
					pStmt.close();
				}
				catch (SQLException e) {
					log.warn("Failed to close the prepared statement object");
				}
			}
		}
		
		return localeConceptNamesMap;
	}
	
	/**
	 * Executes all the changes to the concept names as a batch update.
	 *
	 * @param connection The database connection
	 */
	private void runBatchUpdate(JdbcConnection connection) {
		PreparedStatement pStmt = null;
		
		try {
			connection.setAutoCommit(false);
			pStmt = connection
			        .prepareStatement("UPDATE concept_name SET locale = ?, concept_name_type = ?, locale_preferred = ?, voided = ?, date_voided = ?, void_reason = ?, voided_by = ? WHERE concept_name_id = ?");
			
			Integer userId = DatabaseUpdater.getAuthenticatedUserId();
			//is we have no authenticated user(for API users), set as Daemon
			if (userId == null || userId < 1) {
				userId = getInt(connection, "SELECT min(user_id) FROM users");
				//leave it as null rather than setting it to 0
				if (userId < 1) {
					userId = null;
				}
			}
			
			for (ConceptName conceptName : updatedConceptNames) {
				pStmt.setString(1, conceptName.getLocale().toString());
				pStmt.setString(2, (conceptName.getConceptNameType() != null) ? conceptName.getConceptNameType().toString()
				        : null);
				pStmt.setBoolean(3, conceptName.getLocalePreferred());
				pStmt.setBoolean(4, conceptName.getVoided());
				pStmt.setDate(5, conceptName.getVoided() ? new Date(System.currentTimeMillis()) : null);
				pStmt.setString(6, conceptName.getVoidReason());
				// "Not all databases allow for a non-typed Null to be sent to the backend", so we can't use setInt
				pStmt.setObject(7, (conceptName.getVoided() && userId != null) ? userId : null, Types.INTEGER);
				pStmt.setInt(8, conceptName.getConceptNameId());
				
				pStmt.addBatch();
			}
			
			try {
				int[] updateCounts = pStmt.executeBatch();
				for (int updateCount : updateCounts) {
					if (updateCount > -1) {
						log.debug("Successfully executed: updateCount=" + updateCount);
					} else if (updateCount == Statement.SUCCESS_NO_INFO) {
						log.debug("Successfully executed; No Success info");
					} else if (updateCount == Statement.EXECUTE_FAILED) {
						log.warn("Failed to execute update");
					}
				}
				
				log.debug("Committing updates...");
				connection.commit();
			}
			catch (BatchUpdateException be) {
				log.warn("Error generated while processsing batch update", be);
				int[] updateCounts = be.getUpdateCounts();

				for (int updateCount : updateCounts) {
					if (updateCount > -1) {
						log.warn("Executed with exception: updateCount=" + updateCount);
					} else if (updateCount == Statement.SUCCESS_NO_INFO) {
						log.warn("Executed with exception; No Success info");
					} else if (updateCount == Statement.EXECUTE_FAILED) {
						log.warn("Failed to execute update with exception");
					}
				}
				
				try {
					log.warn("Rolling back batch", be);
					connection.rollback();
				}
				catch (Exception rbe) {
					log.warn("Error generated while rolling back batch update", be);
				}
			}
		}
		catch (SQLException | DatabaseException e) {
			log.warn("Error generated", e);
		}
		finally {
			//reset to auto commit mode
			try {
				connection.setAutoCommit(true);
			}
			catch (DatabaseException e) {
				log.warn("Failed to reset auto commit back to true", e);
			}
			
			if (pStmt != null) {
				try {
					pStmt.close();
				}
				catch (SQLException e) {
					log.warn("Failed to close the prepared statement object");
				}
			}
		}
	}
	
	/**
	 * returns an integer resulting from the execution of an sql statement
	 *
	 * @param connection a DatabaseConnection
	 * @param sql the sql statement to execute
	 * @return integer resulting from the execution of the sql statement
	 */
	private int getInt(JdbcConnection connection, String sql) {
		Statement stmt = null;
		int result = 0;
		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			
			if (rs.next()) {
				result = rs.getInt(1);
			} else {
				log.warn("No row returned by getInt() method");
			}
			
			if (rs.next()) {
				log.warn("Multiple rows returned by getInt() method");
			}
			
			return result;
		}
		catch (DatabaseException | SQLException e) {
			log.warn("Error generated", e);
		}
		finally {
			if (stmt != null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					log.warn("Failed to close the statement object");
				}
			}
		}
		
		return result;
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#getConfirmationMessage()
	 */
	@Override
	public String getConfirmationMessage() {
		return "Finished validating concepts";
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setFileOpener(ResourceAccessor)
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
