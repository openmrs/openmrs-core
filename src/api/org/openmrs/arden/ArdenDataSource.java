package org.openmrs.arden;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;

public interface ArdenDataSource {

	public Obs getPatientObsForConcept(Concept concept, Patient patient);
	public Obs getLastPatientObsForConcept(Concept concept, Patient patient, int howMany);
}
