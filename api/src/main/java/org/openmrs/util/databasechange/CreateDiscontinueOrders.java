package org.openmrs.util.databasechange;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
			                + "creator, date_created, date_stopped, discontinued_by, discontinued_reason, discontinued_reason_non_coded, uuid, order_action, order_type_id, orderer) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			for (DiscontinuedOrder discontinuedOrder : discontinuedOrders) {
				insertStatement.setInt(1, discontinuedOrder.previousOrderId);
				insertStatement.setInt(2, discontinuedOrder.conceptId);
				insertStatement.setInt(3, discontinuedOrder.patientId);
				if (discontinuedOrder.encounterId != 0) {
					insertStatement.setInt(4, discontinuedOrder.encounterId);
				} else {
					insertStatement.setNull(4, Types.INTEGER);
				}
				insertStatement.setInt(5, discontinuedOrder.creator);
				insertStatement.setDate(6, discontinuedOrder.dateCreated);
				insertStatement.setDate(7, discontinuedOrder.dateStopped);
				insertStatement.setInt(8, discontinuedOrder.discontinuedById);
				if (discontinuedOrder.discontinuedReasonId != 0) {
					insertStatement.setInt(9, discontinuedOrder.discontinuedReasonId);
				} else {
					insertStatement.setNull(9, Types.INTEGER);
				}
				insertStatement.setString(10, discontinuedOrder.discontinuedReasonNonCoded);
				insertStatement.setString(11, UUID.randomUUID().toString());
				insertStatement.setString(12, "DISCONTINUE");
				insertStatement.setInt(13, discontinuedOrder.orderTypeId);
				if (discontinuedOrder.orderer != 0) {
					insertStatement.setInt(14, discontinuedOrder.orderer);
				} else {
					insertStatement.setNull(14, Types.INTEGER);
				}
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
	
	private void handleError(JdbcConnection connection, Exception e) throws DatabaseException, CustomChangeException {
		connection.rollback();
		throw new CustomChangeException(e);
	}
	
	private List<DiscontinuedOrder> getDiscontinuedOrders(JdbcConnection connection) throws CustomChangeException,
	        SQLException {
		List<DiscontinuedOrder> dc = new ArrayList<DiscontinuedOrder>();
		PreparedStatement statement = null;
		try {
			statement = connection
			        .prepareStatement("select order_id, concept_id, patient_id, encounter_id, date_stopped, discontinued_by, discontinued_reason, discontinued_reason_non_coded, order_type_id, orderer from orders where discontinued = ?");
			statement.setBoolean(1, true);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				dc.add(new DiscontinuedOrder(rs.getInt("order_id"), rs.getInt("concept_id"), rs.getInt("patient_id"), rs
				        .getInt("encounter_id"), rs.getInt("discontinued_by"), rs.getInt("discontinued_reason"), rs
				        .getString("discontinued_reason_non_coded"), rs.getDate("date_stopped"), rs.getInt("order_type_id"),
				        rs.getInt("orderer")));
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
		return dc;
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
		
		public int discontinuedById;
		
		public int discontinuedReasonId;
		
		public String discontinuedReasonNonCoded;
		
		public Date dateStopped;
		
		public int creator;
		
		public Date dateCreated;
		
		public int previousOrderId;
		
		public int orderTypeId;
		
		public int orderer;
		
		private DiscontinuedOrder(int orderId, int conceptId, int patientId, int encounterId, int discontinuedById,
		    int discontinuedReasonId, String discontinuedReasonNonCoded, Date dateStopped, int orderTypeId, int orderer) {
			this.orderId = orderId;
			this.previousOrderId = orderId;
			this.conceptId = conceptId;
			this.patientId = patientId;
			this.encounterId = encounterId;
			this.discontinuedById = discontinuedById;
			this.discontinuedReasonId = discontinuedReasonId;
			this.discontinuedReasonNonCoded = discontinuedReasonNonCoded;
			this.dateStopped = dateStopped;
			this.creator = discontinuedById;
			this.dateCreated = dateStopped;
			this.orderTypeId = orderTypeId;
			this.orderer = orderer;
		}
	}
}
