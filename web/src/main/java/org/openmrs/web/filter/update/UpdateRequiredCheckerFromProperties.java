package org.openmrs.web.filter.update;

public class UpdateRequiredCheckerFromProperties extends UpdateRequiredChecker {
	@Override
	public boolean isUpdateRequired(UpdateFilterModel updateFilterModel, boolean initializationRequired, boolean runtimePropertiesFound) {
		return !initializationRequired && !runtimePropertiesFound;
	}
}
