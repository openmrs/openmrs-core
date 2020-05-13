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

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

/**
 * Generates UUIDs for all rows in all tables in the tableNames parameter. <br/>
 * If run on MySQL, it generates SQL statements using the in-built uuid() MySQL function, otherwise
 * it uses Java's {@link UUID} class, which is less efficient.<br/>
 * <br/>
 * Expects parameter: "tableNames" : whitespace delimited list of table names to add <br/>
 * Expects parameter: "columnName" : name of the column to change. Default: "uuid" <br/>
 * Expects parameter: "idExceptions" : list of id columns that don't follow the standard naming
 * convention. Should be a pipe-separated list of key=value, where key is the name an id column
 * would have by convention, and value is the name it actually has. In this example the id of the
 * field_answer table is 'field_id' rather than 'field_answer_id', etc:
 * "field_answer_id=field_id|role_id=role|privilege_id=privilege"
 */
public class GenerateUuid implements CustomTaskChange {
	
	private static final Logger log = LoggerFactory.getLogger(GenerateUuid.class);
	
	public static final Integer TRANSACTION_BATCH_SIZE_LIMIT = 512;
	
	/**
	 * The "tableNames" parameter defined in the liquibase xml changeSet element that is calling this
	 * class (whitespace separated).
	 */
	private String tableNames = null;
	
	/**
	 * The "columnName" parameter defined in the liquibase xml changeSet element that is calling this
	 * class
	 */
	private String columnName = "uuid";
	
	/**
	 * Key-value pairs of table name ids that don't follow the convention. The key is what the
	 * convention would be and the value is what it actually is: <br>
	 * e.g. "field_answer_id=field_id|role_id=role|privilege_id=privilege"
	 */
	private String idExceptions = "";
	
	/**
	 * Set by the {@link #setUp()} method from the value of the {@link #idExceptions} parameter
	 */
	private Map<String, String> idExceptionsMap = null;
	
	/**
	 * Set by the {@link #setUp()} method from the value of the {@link #tableNames} parameter
	 */
	private String[] tableNamesArray = null;
	
	/**
	 * The sql statement to select out the ids. Generated in the {@link #setUp()} method.
	 */
	private String genericIdSql;
	
	/**
	 * The sql statement to update the rows with the uuids. Generated in the {@link #setUp()} method.
	 */
	private String genericUpdateSql;
	
	/**
	 * Adds UUIDs to all rows for the specified tables. It generates UUIDs using Java and updates one
	 * row at a time, thus it is not very efficient. When running on the MySQL database, we generate SQL
	 * statements using the uuid MySQL function, which is much faster.
	 *
	 * @see liquibase.change.custom.CustomTaskChange#execute(liquibase.database.Database)
	 */
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		boolean initialAutoCommit = true;
		try {
			initialAutoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			
			if ("mysql".equals(database.getShortName()) || "mariadb".equals(database.getShortName())) {
				String updateSql = "update %s set " + columnName + " = uuid() where " + columnName + " is null";
				for (String tablename : tableNamesArray) {
					String rawSql = String.format(updateSql, tablename);
					
					Statement statement = null;
					try {
						statement = connection.createStatement();
						statement.execute(rawSql);
						statement.close();
						connection.commit();
					}
					catch (SQLException e) {
						throw new CustomChangeException(e);
					}
					finally {
						if (statement != null) {
							try {
								statement.close();
							}
							catch (SQLException e) {
								log.warn("Failed to close the statement", e);
							}
						}
					}
					
				}
			} else {
				int transactionBatchSize = 0;
				// loop over all tables
				for (String tableName : tableNamesArray) {
					try {
						Statement idStatement = null;
						PreparedStatement updateStatement = null;
						try {
							String idSql = genericIdSql.replaceAll("tablename", tableName);
							String updateSql = genericUpdateSql.replaceAll("tablename", tableName);
							
							// hacky way to deal with tables that don't follow the tableNam_id convention
							for (Map.Entry<String, String> idException : idExceptionsMap.entrySet()) {
								idSql = idSql.replaceFirst(idException.getKey(), idException.getValue());
								updateSql = updateSql.replaceFirst(idException.getKey(), idException.getValue());
							}
							
							idStatement = connection.createStatement();
							updateStatement = connection.prepareStatement(updateSql);
							
							ResultSet ids = idStatement.executeQuery(idSql);
							while (ids.next()) {
								updateStatement.setObject(2, ids.getObject(1)); // set the primary key number
								updateStatement.setString(1, UUID.randomUUID().toString()); // set the uuid for this row
								updateStatement.executeUpdate();
								
								transactionBatchSize++;
								if (transactionBatchSize > TRANSACTION_BATCH_SIZE_LIMIT) {
									transactionBatchSize = 0;
									connection.commit();
								}
							}
							
							idStatement.close();
							updateStatement.close();
						}
						finally {
							if (idStatement != null) {
								try {
									idStatement.close();
								}
								catch (SQLException e) {
									log.warn("Failed to close statement", e);
								}
							}
							if (updateStatement != null) {
								try {
									updateStatement.close();
								}
								catch (SQLException e) {
									log.warn("Failed to close the statement", e);
								}
							}
						}
					}
					catch (DatabaseException | SQLException e) {
						throw new CustomChangeException("Unable to set uuid on table: " + tableName, e);
					}
				}
				
				connection.commit();
			}
			
			connection.setAutoCommit(initialAutoCommit);
		}
		catch (DatabaseException e) {
			throw new CustomChangeException(e);
		}
		finally {
			try {
				connection.setAutoCommit(initialAutoCommit);
			}
			catch (DatabaseException e) {
				//silently ignore so that the actual error is not hidden
			}
		}
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#getConfirmationMessage()
	 */
	@Override
	public String getConfirmationMessage() {
		return "Finished adding uuids to all rows in all tables";
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setFileOpener(ResourceAccessor)
	 */
	@Override
	public void setFileOpener(ResourceAccessor fileOpener) {
		
	}
	
	/**
	 * Get the values of the parameters passed in and set them to the local variables on this class.
	 *
	 * @see liquibase.change.custom.CustomChange#setUp()
	 */
	@Override
	public void setUp() throws SetupException {
		if (StringUtils.isBlank(tableNames)) {
			throw new SetupException("At least one table name in the 'tableNames' parameter is required");
		}
		
		tableNamesArray = StringUtils.split(tableNames);
		idExceptionsMap = OpenmrsUtil.parseParameterList(idExceptions);
		
		genericIdSql = "select tablename_id from tablename where columnName is null";
		genericIdSql = genericIdSql.replace("columnName", columnName);
		
		genericUpdateSql = "update tablename set columnName = ? where tablename_id = ?";
		genericUpdateSql = genericUpdateSql.replace("columnName", columnName);
		
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#validate(liquibase.database.Database)
	 */
	@Override
	public ValidationErrors validate(Database database) {
		return new ValidationErrors();
	}
	
	/**
	 * This is called by liquibase to set the parameter "tableNames" onto this change.
	 *
	 * @param tableNames the tableNames to set
	 */
	public void setTableNames(String tableNames) {
		this.tableNames = tableNames;
	}
	
	/**
	 * This is called by liquibase to set the parameter "columnName" onto this change.
	 *
	 * @param columnName the columnName to set
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
	/**
	 * Way to specify the table id columns that don't follow the table_name.table_name_id pattern
	 *
	 * @param idExceptions
	 */
	public void setIdExceptions(String idExceptions) {
		this.idExceptions = idExceptions;
	}
	
}
