package org.openmrs.arden;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

public class DefaultArdenDataSource implements ArdenDataSource {

	private String Name = "Default";
	public Obs getPatientObsForConcept(Concept concept, Patient patient) {
		Set <Obs> MyObs;
		Obs obs = new Obs();
		{		MyObs = Context.getObsService().getObservations(patient, concept);
			Iterator iter = MyObs.iterator();
			if(iter.hasNext()) {
				while(iter.hasNext())	{
					obs = (Obs) iter.next();
					System.out.println(obs.getValueAsString(Context.getLocale()) + " --- " + obs.getObsDatetime());
				}
					return obs;
			}
			else {
				return null;
			}
		}
	}
	
	public Obs getLastPatientObsForConcept(Concept concept, Patient patient, int howMany) {
		List <Obs> MyObs;
		Obs obs = new Obs();
		{	
			MyObs = Context.getObsService().getLastNObservations(howMany, patient, concept);
			Iterator iter = MyObs.iterator();
			if(iter.hasNext()) {
				while(iter.hasNext())	{
					obs = (Obs) iter.next();
					System.out.println(obs.getValueAsString(Context.getLocale())
							+ " --- " + obs.getObsDatetime());
				}
					return obs;
			}
			else {
				return null;
			}
		}
}	
}
