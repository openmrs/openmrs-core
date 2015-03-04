/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting;

import java.util.Date;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;
import org.openmrs.util.OpenmrsConstants;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public abstract class AbstractPatientFilter extends AbstractReportObject implements PatientFilter {
	
	public AbstractPatientFilter() {
		// do nothing
		super.setType(OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTFILTER);
	}
	
	public AbstractPatientFilter(Integer reportObjectId, String name, String description, String type, String subType,
	    User creator, Date dateCreated, User changedBy, Date dateChanged, Boolean voided, User voidedBy, Date dateVoided,
	    String voidReason) {
		super(reportObjectId, name, description, type, subType, creator, dateCreated, changedBy, dateChanged, voided,
		        voidedBy, dateVoided, voidReason);
	}
	
	/**
	 * Basic implementation of filterInverse that delegates to PatientSetService.getAllPatients()
	 * and this class's filter() method. Subclasses may override this method if they have a way of
	 * doing so more efficiently (since getAllPatients can be very expensive).
	 * 
	 * @param input
	 * @param context
	 * @return
	 */
	public Cohort filterInverse(Cohort input, EvaluationContext context) {
		Cohort filterResult = filter(input, context);
		if (input != null) {
			return Cohort.subtract(input, filterResult);
		} else if (context != null) {
			return Cohort.subtract(context.getBaseCohort(), filterResult);
		} else {
			return Context.getPatientSetService().getInverseOfCohort(filterResult);
		}
	}
	
	/**
	 * Find the name from the given concept object. If no name exists, load from the database and
	 * then return the name
	 * 
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
		}
		catch (Exception e) {
			cName = "";
		}
		
		return cName;
	}
}
