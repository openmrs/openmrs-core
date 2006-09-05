package org.openmrs.arden;

import java.util.Iterator;
import java.util.Set;

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
		{		MyObs = context.getObsService().getObservations(patient, concept);
			Iterator iter = MyObs.iterator();
			if(iter.hasNext()) {
				while(iter.hasNext())	{
					obs = (Obs) iter.next();
					System.out.println(obs.getValueAsString(context.getLocale()));
				}
					return obs;
			}
			else {
				return null;
			}
		}
	}	
}
