package org.openmrs.web.filter.update;


import javax.servlet.ServletException;

public class UpdateRequiredCheckerFromModel extends UpdateRequiredChecker {
	@Override
	public boolean isUpdateRequired(UpdateFilterModel updateFilterModel, boolean initializationRequired, boolean runtimePropertiesFound) throws ServletException {
		try {
			// this pings the DatabaseUpdater.updatesRequired which also
			// considers a db lock to be a 'required update'
			if (updateFilterModel.updateRequired) {
				return true;
			} else if (updateFilterModel.changes == null) {
				return false;
			} else {
				return !updateFilterModel.changes.isEmpty();
			}
		}
		catch (Exception e) {
			throw new ServletException("Unable to determine if updates are required", e);
		}
	}
}
