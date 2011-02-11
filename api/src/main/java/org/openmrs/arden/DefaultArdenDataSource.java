/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.arden;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;

public class DefaultArdenDataSource implements ArdenDataSource {
	
	private String Name = "Default";
	
	/*
	 * This to be moved to Data Access layer...
	 */

	private Set<Obs> getPatientObsForConcept(Patient patient, Concept concept) {
		return (Set<Obs>) Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
	}
	
	private List<Obs> getLastPatientObsForConcept(Patient patient, Concept concept, int howMany) {
		return Context.getObsService().getObservations(Collections.singletonList((Person)patient), null, Collections.singletonList(concept), null, null, null, null, new Integer(howMany), null, null, null, false);
		
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
		if (c.getPredicate() == ArdenClause.Predicate.last) {
			obs = getLastPatientObsForConcept(p, c.getConcept(), 1);
			retVal.addObs(obs);
		}
		return retVal;
	}
}
