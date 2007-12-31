package org.openmrs.reporting;

import java.util.Date;

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.User;
import org.openmrs.api.context.Context;
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
	
	/**
	 * Find the name from the given concept object.  If no name exists, load from the database 
	 * and then return the name
	 * @param concept
	 * @return name of the concept
	 */
	public String getConceptName(Concept concept) {
		if (concept == null)
			return "[CONCEPT]";
		
		String cName = "";
		try {
			ConceptName conceptName = concept.getName();
			if (conceptName == null)
				conceptName = Context.getConceptService().getConcept(concept.getConceptId()).getName();
			cName = conceptName.getName();
		} catch ( Exception e ) {
			cName = "";
		}
		
		return cName;
	}
}
