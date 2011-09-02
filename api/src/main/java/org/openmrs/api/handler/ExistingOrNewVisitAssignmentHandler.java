package org.openmrs.api.handler;

import java.util.Calendar;
import java.util.Locale;

import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;

/**
 * This handler assigns an encounter to an existing visit, where appropriate, or creates a new one.
 * 
 * @see EncounterVisitHandler
 */
public class ExistingOrNewVisitAssignmentHandler extends ExistingVisitAssignmentHandler {
	
	/**
	 * @see org.openmrs.api.handler.ExistingVisitAssignmentHandler#getDisplayName(java.util.Locale)
	 */
	@Override
	public String getDisplayName(Locale locale) {
		return Context.getMessageSourceService().getMessage("visit.assignmentHandler.assignToExistingVisitOrNew", null,
		    locale);
	}
	
	/**
	 * @see org.openmrs.api.handler.ExistingVisitAssignmentHandler#beforeCreateEncounter(org.openmrs.Encounter)
	 * 
	 * @should assign existing visit if match found
	 * @should assign new visit if no match found
	 */
	@Override
	public void beforeCreateEncounter(Encounter encounter) {
		
		//Do the default assignment to an existing visit.
		super.beforeCreateEncounter(encounter);
		
		//Do nothing if the encounter already belongs to a visit.
		if (encounter.getVisit() != null)
			return;
		
		Visit visit = new Visit();
		visit.setStartDatetime(encounter.getEncounterDatetime());
		visit.setCreator(encounter.getCreator());
		visit.setDateCreated(encounter.getDateCreated());
		visit.setLocation(encounter.getLocation());
		visit.setPatient(encounter.getPatient());
		
		//TODO Is is correct?
		visit.setVisitType(Context.getVisitService().getAllVisitTypes().get(0));
		
		//set stop date time to last minute of the encounter day.
		Calendar calender = Calendar.getInstance();
		calender.setTime(encounter.getEncounterDatetime());
		calender.set(Calendar.HOUR_OF_DAY, 23);
		calender.set(Calendar.MINUTE, 59);
		calender.set(Calendar.SECOND, 59);
		
		visit.setStopDatetime(calender.getTime());
		
		encounter.setVisit(visit);
	}
}
