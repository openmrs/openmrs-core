package org.openmrs.arden;

import java.util.*;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;


public class DefaultArdenDataSource implements ArdenDataSource {

	private String Name = "Default";
	/*
	 * This to be moved to Data Access layer...
	 */
	
	private Set<Obs> getPatientObsForConcept(Patient patient, Concept concept) {
		return Context.getObsService().getObservations(patient, concept);
	}
	
	private List<Obs> getLastPatientObsForConcept(Patient patient, Concept concept, int howMany) {
		return Context.getObsService().getLastNObservations(howMany, patient, concept);
		
	}
	
/*	
	private Obs getPatientObsForConcept(Concept concept, Patient patient) {
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
	
	private Obs getLastPatientObsForConcept(Concept concept, Patient patient, int howMany) {
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
*/
	public ArdenValue eval(Patient p, ArdenClause c) {
		ArdenValue retVal = new ArdenValue(p, Context.getLocale());
		List<Obs> obs;
				/*
		 *  To something with the clause now...
		 */
		if(c.getPredicate()== ArdenClause.Predicate.last) {
			obs = getLastPatientObsForConcept(p,c.getConcept(),1);
			retVal.addObs(obs);
		}
		return retVal;
	}
}
