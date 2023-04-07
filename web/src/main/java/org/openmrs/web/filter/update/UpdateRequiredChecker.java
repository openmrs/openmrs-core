package org.openmrs.web.filter.update;

import javax.servlet.ServletException;

public abstract class UpdateRequiredChecker {
	
	public abstract boolean isUpdateRequired(UpdateFilterModel updateFilterModel, boolean initializationRequired,
	        boolean runtimePropertiesFound) throws ServletException;
	
}
