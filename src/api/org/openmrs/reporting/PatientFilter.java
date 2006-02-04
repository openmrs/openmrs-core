package org.openmrs.reporting;

import java.util.Locale;

import org.openmrs.api.context.Context;

public interface PatientFilter {

	public PatientSet filter(Context context, PatientSet input);
	
	public String getDescription();
	
}
