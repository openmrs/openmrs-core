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
            for (String unit : uniqueUnits) {
                    migrateUnitsToCodedValue(connection, unit);
            }
        } catch (SQLException e) {
            throw new CustomChangeException(e);
        }
    }

    private void migrateUnitsToCodedValue(JdbcConnection connection, String unit) throws CustomChangeException, SQLException {
        Integer conceptIdForUnit = DatabaseUtil.getConceptIdForUnits(connection.getUnderlyingConnection(), unit);
        PreparedStatement updateDrugOrderStatement = null;
        try {
            updateDrugOrderStatement = connection.prepareStatement("update drug_order set dose_units = ? where units = ?");
            updateDrugOrderStatement.setInt(1, conceptIdForUnit);
            updateDrugOrderStatement.setString(2, unit);
            updateDrugOrderStatement.executeUpdate();
        } catch (DatabaseException e) {
            throw new CustomChangeException(e);
        } catch (SQLException e) {
            throw new CustomChangeException(e);
        } finally {
            if(updateDrugOrderStatement != null) {
                updateDrugOrderStatement.close();
            }
        }
    }

    private List<String> getUniqueUnits(JdbcConnection connection) throws CustomChangeException, SQLException {
        List<String> uniqueUnits = new ArrayList<String>();
        PreparedStatement uniqueUnitsQuery = null;
        try {
            uniqueUnitsQuery = connection.prepareStatement("Select distinct units as unique_units from drug_order");
            ResultSet resultSet = uniqueUnitsQuery.executeQuery();
            while (resultSet.next()) {
                String unit = resultSet.getString("unique_units");
                if(unit != null) {
                    uniqueUnits.add(unit);
                }
            }
        } catch (SQLException e) {
            throw new CustomChangeException(e);
        } catch (DatabaseException e) {
            throw new CustomChangeException(e);
        } finally {
            if(uniqueUnitsQuery != null) {
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
