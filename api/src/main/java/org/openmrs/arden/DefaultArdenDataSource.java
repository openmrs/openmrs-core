/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
		return Context.getObsService().getObservations(Collections.singletonList((Person) patient), null,
		    Collections.singletonList(concept), null, null, null, null, Integer.valueOf(howMany), null, null, null, false);
		
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
