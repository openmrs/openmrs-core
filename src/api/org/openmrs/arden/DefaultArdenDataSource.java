package org.openmrs.arden;

import java.util.Iterator;
import java.util.Set;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.arden.ArdenDataSource;
import org.openmrs.api.context.*;

public class DefaultArdenDataSource implements ArdenDataSource {

	private String Name = "Default";
	public Obs getPatientObsForConcept(Context context, Concept concept, Patient patient) {
		Set <Obs> MyObs;
		Obs obs = new Obs();
		{	
			MyObs = context.getObsService().getObservations(patient, concept);
			Iterator iter = MyObs.iterator();
			if(iter.hasNext()) {
				while(iter.hasNext())	{
					obs = (Obs) iter.next();
					System.out.println(obs.getValueAsString(context.getLocale())
							+ " --- " + obs.getObsDatetime());
				}
					return obs;
			}
			else {
				return null;
			}
		}
		
	}
	
	public Obs getLastPatientObsForConcept(Context context, Concept concept, Patient patient, int howMany) {
				List <Obs> MyObs;
				Obs obs = new Obs();
				{	
					MyObs = context.getObsService().getLastNObservations(howMany, patient, concept);
					Iterator iter = MyObs.iterator();
					if(iter.hasNext()) {
						while(iter.hasNext())	{
							obs = (Obs) iter.next();
							System.out.println(obs.getValueAsString(context.getLocale())
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
