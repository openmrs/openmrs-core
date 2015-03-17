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

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import org.openmrs.Order;

public class CreateDiscontinueOrders implements CustomTaskChange {
	
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		try {
			List<DiscontinuedOrder> discontinuedOrders = getDiscontinuedOrders(connection);
			createDiscontinueOrders(connection, discontinuedOrders);
		}
		catch (SQLException e) {
			throw new CustomChangeException(e);
		}
		catch (DatabaseException e) {
			throw new CustomChangeException(e);
		}
	}
	
	private void createDiscontinueOrders(JdbcConnection connection, List<DiscontinuedOrder> discontinuedOrders)
	        throws CustomChangeException, SQLException, DatabaseException {
		final int batchSize = 1000;
		int index = 0;
		PreparedStatement insertStatement = null;
		Boolean autoCommit = null;
		try {
			autoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			insertStatement = connection
			        .prepareStatement("Insert into orders(previous_order_id, concept_id, patient_id, encounter_id, "
			                + "creator, date_created, discontinued_reason, discontinued_reason_non_coded, "
			                + "uuid, order_action, orderer, order_number, order_type_id, start_date, auto_expire_date) "
			                + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			for (DiscontinuedOrder discontinuedOrder : discontinuedOrders) {
				insertStatement.setInt(1, discontinuedOrder.previousOrderId);
				insertStatement.setInt(2, discontinuedOrder.conceptId);
				insertStatement.setInt(3, discontinuedOrder.patientId);
				setIntOrNull(insertStatement, 4, discontinuedOrder.encounterId);
				insertStatement.setInt(5, discontinuedOrder.discontinuedById);
				insertStatement.setDate(6, new Date(System.currentTimeMillis()));
				setIntOrNull(insertStatement, 7, discontinuedOrder.discontinuedReasonId);
				insertStatement.setString(8, discontinuedOrder.discontinuedReasonNonCoded);
				insertStatement.setString(9, UUID.randomUUID().toString());
				insertStatement.setString(10, Order.Action.DISCONTINUE.name());
				setIntOrNull(insertStatement, 11, discontinuedOrder.discontinuedById);
				insertStatement.setString(12, discontinuedOrder.orderNumber);
				insertStatement.setInt(13, discontinuedOrder.orderTypeId);
				insertStatement.setDate(14, discontinuedOrder.dateActivated);
				insertStatement.setDate(15, discontinuedOrder.dateActivated);
				insertStatement.addBatch();
				
				if (index % batchSize == 0) {
					insertStatement.executeBatch();
				}
				index++;
			}
			insertStatement.executeBatch();
			connection.commit();
		}
		catch (DatabaseException e) {
			handleError(connection, e);
		}
		catch (SQLException e) {
			handleError(connection, e);
		}
		finally {
			if (autoCommit != null) {
				connection.setAutoCommit(autoCommit);
			}
			if (insertStatement != null) {
				insertStatement.close();
			}
		}
	}
	
	private void setIntOrNull(PreparedStatement statement, int index, Integer value) throws SQLException {
		if (value == null || value == 0) {
			statement.setNull(index, Types.INTEGER);
		} else {
			statement.setInt(index, value);
		}
	}
	
	private void handleError(JdbcConnection connection, Exception e) throws DatabaseException, CustomChangeException {
		connection.rollback();
		throw new CustomChangeException(e);
	}
	
	private List<DiscontinuedOrder> getDiscontinuedOrders(JdbcConnection connection) throws CustomChangeException,
	        SQLException {
		List<DiscontinuedOrder> dcOrders = new ArrayList<DiscontinuedOrder>();
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement("select order_id, concept_id, patient_id, encounter_id, date_stopped, "
			        + "discontinued_by, discontinued_reason, discontinued_reason_non_coded, order_type_id "
			        + "from orders where discontinued = ?");
			statement.setBoolean(1, true);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				dcOrders.add(new DiscontinuedOrder(rs.getInt("order_id"), rs.getInt("concept_id"), rs.getInt("patient_id"),
				        rs.getInt("encounter_id"), rs.getInt("discontinued_by"), rs.getInt("discontinued_reason"), rs
				                .getString("discontinued_reason_non_coded"), rs.getDate("date_stopped"), rs
				                .getInt("order_type_id")));
			}
		}
		catch (SQLException e) {
			throw new CustomChangeException(e);
		}
		catch (DatabaseException e) {
			throw new CustomChangeException(e);
		}
		finally {
			if (statement != null) {
				statement.close();
			}
		}
		return dcOrders;
	}
	
	@Override
	public String getConfirmationMessage() {
		return "Finished creating discontinue orders for discontinued orders";
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
	
	private static class DiscontinuedOrder {
		
		public int orderId;
		
		public int conceptId;
		
		public int patientId;
		
		public int encounterId;
		
		public int discontinuedReasonId;
		
		public String discontinuedReasonNonCoded;
		
		public Date dateActivated;
		
		public int discontinuedById;
		
		public Date dateCreated;
		
		public int previousOrderId;
		
		public String orderNumber;
		
		public int orderTypeId;
		
		public Date date;
		
		private DiscontinuedOrder(int orderId, int conceptId, int patientId, int encounterId, int discontinuedById,
		    int discontinuedReasonId, String discontinuedReasonNonCoded, Date dateStopped, int orderTypeId) {
			this.orderId = orderId;
			this.previousOrderId = orderId;
			this.conceptId = conceptId;
			this.patientId = patientId;
			this.encounterId = encounterId;
			this.discontinuedReasonId = discontinuedReasonId;
			this.discontinuedReasonNonCoded = discontinuedReasonNonCoded;
			this.dateActivated = dateStopped;
			this.discontinuedById = discontinuedById;
			this.dateCreated = dateStopped;
			this.orderNumber = String.valueOf(orderId).concat("-DC");
			this.orderTypeId = orderTypeId;
		}
	}
}
