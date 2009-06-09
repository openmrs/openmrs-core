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
import java.util.Map;
import java.util.UUID;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.util.OpenmrsUtil;

/**
 * Uses Java's {@link UUID} class to generate UUIDs for all rows in all tables in the tableNames
 * parameter. <br/>
 * <br/>
 * This class should only be used if you are not using MySQL, Oracle, MsSql, or some other dbms that
 * has a UUID-like function. <br/>
 * <br/>
 * Expects parameter: "tableNames" : whitespace delimited list of table names to add <br/>
 * Expects parameter: "columnName" : name of the column to change. Default: "uuid"
 */
public class GenerateUuid implements CustomTaskChange {
	
	/**
	 * The "tableNames" parameter defined in the liquibase xml changeSet element that is calling
	 * this class (whitespace separated).
	 */
	private String tableNames = null;
	
	/**
	 * The "columnName" parameter defined in the liquibase xml changeSet element that is calling
	 * this class
	 */
	private String columnName = "uuid";
	
	/**
	 * Key-value pairs of table name ids that don't follow the convention. The key is what the
	 * convention would be and the value is what it actually is: <br/>
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
	 * The sql statement to update the rows with the uuids. Generated in the {@link #setUp()}
	 * method.
	 */
	private String genericUpdateSql;
	
	/**
	 * Does the work of adding UUIDs to all rows.
	 * 
	 * @see liquibase.change.custom.CustomTaskChange#execute(liquibase.database.Database)
	 */
	public void execute(Database database) throws CustomChangeException, UnsupportedChangeException {
		
		if (tableNamesArray == null || tableNamesArray.length == 0)
			throw new CustomChangeException("At least one table name in the 'tableNames' parameter is required", null);
		
		DatabaseConnection connection = database.getConnection();
		
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
					
					// Map<Integer, UUID> uuids = new HashMap<Integer, UUID>();
					
					ResultSet ids = idStatement.executeQuery(idSql);
					while (ids.next()) {
						updateStatement.setInt(2, ids.getInt(1)); // set the primary key number
						updateStatement.setString(1, UUID.randomUUID().toString()); // set the uuid for this row
						updateStatement.executeUpdate();
					}
				}
				finally {
					if (idStatement != null)
						idStatement.close();
					if (updateStatement != null)
						updateStatement.close();
				}
				
			}
			catch (SQLException e) {
				throw new CustomChangeException("Unable to set uuid on table: " + tableName, e);
			}
		}
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#getConfirmationMessage()
	 */
	public String getConfirmationMessage() {
		return "Finished adding uuids to all rows in all tables";
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setFileOpener(liquibase.FileOpener)
	 */
	public void setFileOpener(FileOpener fileOpener) {
		
	}
	
	/**
	 * Get the values of the parameters passed in and set them to the local variables on this class.
	 * 
	 * @see liquibase.change.custom.CustomChange#setUp()
	 */
	public void setUp() throws SetupException {
		
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
	public void validate(Database database) throws InvalidChangeDefinitionException {
		
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
