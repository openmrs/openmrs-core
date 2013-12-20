package org.openmrs.util.databasechange;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.openmrs.util.DatabaseUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MigrateDrugOrderUnitsToCodedDoseUnitsChangeset implements CustomTaskChange {
	
	@Override
	public void execute(Database database) throws CustomChangeException {
		JdbcConnection connection = (JdbcConnection) database.getConnection();
		
		try {
			List<String> uniqueUnits = getUniqueUnits(connection);
			migrateUnitsToCodedValue(connection, uniqueUnits);
		}
		catch (SQLException e) {
			throw new CustomChangeException(e);
		}
		catch (DatabaseException e) {
			throw new CustomChangeException(e);
		}
	}
	
	private void migrateUnitsToCodedValue(JdbcConnection connection, List<String> uniqueUnits) throws CustomChangeException,
	        SQLException, DatabaseException {
		PreparedStatement updateDrugOrderStatement = null;
		Boolean autoCommit = null;
		try {
			autoCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);
			updateDrugOrderStatement = connection.prepareStatement("update drug_order set dose_units = ? where units = ?");
			for (String unit : uniqueUnits) {
				Integer conceptIdForUnit = DatabaseUtil.getConceptIdForUnits(connection.getUnderlyingConnection(), unit);
				if (conceptIdForUnit == null) {
					throw new CustomChangeException("No concept mapping found for unit: " + unit);
				}
				updateDrugOrderStatement.setInt(1, conceptIdForUnit);
				updateDrugOrderStatement.setString(2, unit);
				updateDrugOrderStatement.executeUpdate();
				updateDrugOrderStatement.clearParameters();
			}
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
			if (updateDrugOrderStatement != null) {
				updateDrugOrderStatement.close();
			}
		}
	}
	
	private void handleError(JdbcConnection connection, Exception e) throws DatabaseException, CustomChangeException {
		connection.rollback();
		throw new CustomChangeException(e);
	}
	
	private List<String> getUniqueUnits(JdbcConnection connection) throws CustomChangeException, SQLException {
		List<String> uniqueUnits = new ArrayList<String>();
		PreparedStatement uniqueUnitsQuery = null;
		try {
			uniqueUnitsQuery = connection.prepareStatement("Select distinct units as unique_units from drug_order");
			ResultSet resultSet = uniqueUnitsQuery.executeQuery();
			while (resultSet.next()) {
				String unit = resultSet.getString("unique_units");
				if (unit != null) {
					uniqueUnits.add(unit);
				}
			}
		}
		catch (SQLException e) {
			throw new CustomChangeException(e);
		}
		catch (DatabaseException e) {
			throw new CustomChangeException(e);
		}
		finally {
			if (uniqueUnitsQuery != null) {
				uniqueUnitsQuery.close();
			}
		}
		return uniqueUnits;
	}
	
	@Override
	public String getConfirmationMessage() {
		return "Finished migrating drug order units to coded dose units";
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
}
