package org.openmrs.api.impl;

public interface PersonPatientSharable<T> {
	Boolean getVoided();	
	Boolean getPreferred();
	void setPreferred(Boolean preferred);
}
