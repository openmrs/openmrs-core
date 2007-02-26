package org.openmrs.reporting;

import java.util.Date;

import org.openmrs.User;
import org.openmrs.util.OpenmrsConstants;

public abstract class AbstractPatientFilter extends AbstractReportObject {

	public AbstractPatientFilter()
	{
		// do nothing
		super.setType(OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTFILTER);
	}

	public AbstractPatientFilter(Integer reportObjectId, String name, String description, String type, String subType, 
			User creator, Date dateCreated, User changedBy, Date dateChanged, Boolean voided, User voidedBy,
			Date dateVoided, String voidReason )
	{
		super(reportObjectId, name, description, type, subType, creator, dateCreated, changedBy, dateChanged, voided, voidedBy,
				dateVoided, voidReason);
	}

}
