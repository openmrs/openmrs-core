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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import liquibase.FileOpener;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.DatabaseUtil;

/**
 * This change set is executed in conjunction with a change made to Patient Programs which
 * automatically will complete a Patient Program if a Workflow within that Program transitions 
 * to a state marked as final.  It is intended to warn administrators when they upgrade that
 * they should carefully review any States marked as final, particularly those also marked as initial
 */
public class ProgramValidatorChangeSet implements CustomTaskChange {
	
	protected final static Log log = LogFactory.getLog(ProgramValidatorChangeSet.class);

	/**
	 * @see CustomTaskChange#execute(Database)
	 */
	public void execute(Database database) throws CustomChangeException, UnsupportedChangeException {
		Connection conn = database.getConnection().getUnderlyingConnection();
		List<String> messages = new ArrayList<String>();
		
		// Warn if any states are configured as both initial and terminal
		StringBuilder message = new StringBuilder();
		message.append("Starting now, when you transition a patient into a state that is configured as terminal, ");
		message.append("then that whole program enrollment will be marked as completed.<br/>");
		message.append("Please check that programs, workflows, and states are configured.<br/>");
		message.append("This check will highlight two things: ");
		message.append("<ul><li>states that are marked as both initial and terminal ");
		message.append("(if you start someone in that state their program enrollment will be instantly closed)</li>");
		message.append("<li>workflows that have no initial states (because you don't have a state to start people in)</li>");
		message.append("</ul><br/>");
		message.append("The following states are configured as both initial and terminal:<br/>");

		StringBuilder query = new StringBuilder();
		query.append(" select 	s.concept_id, min(n.name) as name ");
		query.append(" from 	program_workflow_state s, concept_name n ");
		query.append(" where 	s.concept_id = n.concept_id and initial = 1 and terminal = 1 ");
		query.append(" group by s.concept_id ");
		List<List<Object>> results = DatabaseUtil.executeSQL(conn, query.toString(), true);
		if (results.isEmpty()) {
			message.append("None found.");
		}
		else {
			for (List<Object> row : results) {
				message.append(row.get(1).toString() + "<br/>");
			}
		}
		
		// Warn if any workflows have no initial states
		message.append("<br/>The following workflows have no initial states...<br/>");
		query = new StringBuilder();
		query.append(" select 		w.concept_id, s.initial, count(*) as num ");
		query.append(" from			program_workflow w, program_workflow_state s ");
		query.append(" where		w.program_workflow_id = s.program_workflow_id ");
		query.append(" group by 	w.concept_id, s.initial ");
		
		results = DatabaseUtil.executeSQL(conn, query.toString(), true);
		List<Integer> missingInitial = new ArrayList<Integer>();
		for (List<Object> row : results) {
			missingInitial.add(Integer.valueOf(row.get(0).toString()));
		}
		for (List<Object> row : results) {
			Integer conceptId = Integer.valueOf(row.get(0).toString());
			boolean isInitial = row.get(1).toString().equals("1");
			int num = Integer.parseInt(row.get(2).toString());
			if (isInitial && num > 0) {
				missingInitial.remove(conceptId);
			}
		}
		if (missingInitial.isEmpty()) {
			message.append("None found.");
		}
		else {
			for (Integer conceptId : missingInitial) {
				String sql = "select min(name) from concept_name where concept_id = " + conceptId;
				String name = DatabaseUtil.executeSQL(conn, sql, true).get(0).get(0).toString();
				message.append(name + "<br/>");
			}
		}
		messages.add(message.toString());
		
		DatabaseUpdater.reportUpdateWarnings(messages);
	}

	/**
	 * @see CustomChange#getConfirmationMessage()
	 */
	@Override
	public String getConfirmationMessage() {
		return "Finished validating programs";
	}

	/**
	 * @see CustomChange#setFileOpener(FileOpener)
	 */
	@Override
	public void setFileOpener(FileOpener fo) {
	}

	/**
	 * @see CustomChange#setUp()
	 */
	@Override
	public void setUp() throws SetupException {
	}

	/**
	 * @see CustomChange#validate(Database)
	 */
	@Override
	public void validate(Database db) throws InvalidChangeDefinitionException {
	}
}
