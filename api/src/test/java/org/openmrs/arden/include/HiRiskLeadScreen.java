/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
/********************************************************************
 Title : HiRiskLeadScreen
 Filename:  HiRiskLeadScreen
 Version : 1 . 0
 Institution : iCHSR
 Author : Steve Downs
 Specialist : Pediatrics
 Validation :
 Purpose : PWS prompt to obtain blood lead if child has a risk factor
 Explanation :
 Keywords : lead risk based screening PWS
 Citations :
 Links :

 ********************************************************************/
package org.openmrs.arden.include;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

public class HiRiskLeadScreen {
	
	private Patient patient;
	
	private Locale locale;
	
	private String firstname;
	
	private HashMap<String, String> userVarMap;
	
	//Constructor
	public HiRiskLeadScreen(Integer pid, Locale l) {
		locale = l;
		patient = Context.getPatientService().getPatient(pid);
		userVarMap = new HashMap<String, String>();
		firstname = patient.getPersonName().getGivenName();
	}
	
	public Obs getObsForConceptForPatient(Concept concept, Locale locale, Patient patient) {
		List<Obs> MyObs;
		Obs obs = new Obs();
		{
			MyObs = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
			Iterator iter = MyObs.iterator();
			if (iter.hasNext()) {
				while (iter.hasNext()) {
					obs = (Obs) iter.next();
					//System.out.println(obs.getValueAsString(locale));
				}
				return obs;
			} else {
				return null;
			}
		}
	}
	
	public boolean run() {
		boolean retVal = false;
		if (evaluate()) {
			action();
			String str = userVarMap.get("ActionStr");
			//System.out.println(str);
		}
		return retVal;
	}
	
	public boolean evaluate_Last_Pb() {
		Concept concept;
		boolean retVal = false;
		Obs obs;
		
		concept = Context.getConceptService().getConceptByName("BLOOD LEAD LEVEL");
		obs = getObsForConceptForPatient(concept, locale, patient);
		if (obs != null) {
			double Last_Pb = obs.getValueNumeric();
			if (Last_Pb >= 14) {
				retVal = true;
			}
		}
		
		return retVal;
	}
	
	public boolean evaluate_Qual_Pb() {
		Concept concept;
		boolean retVal = false;
		Obs obs;
		
		concept = Context.getConceptService().getConceptByName("Qualitative_Blood_Lead");
		obs = getObsForConceptForPatient(concept, locale, patient);
		if (obs != null) {
			boolean Qual_Pb = obs.getValueAsBoolean();
			if (Qual_Pb == true) {
				retVal = true;
			}
		}
		
		return retVal;
	}
	
	public boolean evaluate_HousePre50() {
		Concept concept;
		boolean retVal = false;
		Obs obs;
		
		concept = Context.getConceptService().getConceptByName("HouseBltPre1950");
		obs = getObsForConceptForPatient(concept, locale, patient);
		if (obs != null) {
			String HousePre50 = obs.getValueText();
			if (HousePre50.equals("YES")) {
				retVal = true;
			}
		}
		
		return retVal;
	}
	
	public boolean evaluate_RenovatedPre78() {
		Concept concept;
		boolean retVal = false;
		Obs obs;
		
		concept = Context.getConceptService().getConceptByName("RenovatedPre78");
		obs = getObsForConceptForPatient(concept, locale, patient);
		if (obs != null) {
			String RenovatedPre78 = obs.getValueText();
			if (RenovatedPre78.equals("YES")) {
				retVal = true;
			}
		}
		
		return retVal;
	}
	
	public boolean evaluate_HiPbSibFriend() {
		Concept concept;
		boolean retVal = false;
		Obs obs;
		
		concept = Context.getConceptService().getConceptByName("HiPbSibFriend");
		obs = getObsForConceptForPatient(concept, locale, patient);
		if (obs != null) {
			String HiPbSibFriend = obs.getValueText();
			if (HiPbSibFriend.equals("YES")) {
				retVal = true;
			}
		}
		
		return retVal;
	}
	
	public boolean evaluate() {
		Concept concept;
		boolean retVal = false;
		Obs obs;
		
		if (evaluate_Last_Pb()) {
			//LeadRisk = "has lead level greater than 14 mg/dcl"
			userVarMap.put("LeadRisk", "has lead level greater than 14 mg/dcl");
		}
		if (evaluate_Qual_Pb()) {
			retVal = false;
			return retVal;
			
		}
		if (evaluate_HousePre50()) {
			//LeadRisk = "lives in a house built before 1950"
			userVarMap.put("LeadRisk", "lives in a house built before 1950");
		} else if (evaluate_RenovatedPre78()) {
			//LeadRisk = "lives in a pre-1978 house undergoing renovation"
			userVarMap.put("LeadRisk", "lives in a pre-1978 house undergoing renovation");
		} else if (evaluate_HiPbSibFriend()) {
			//LeadRisk = "has a friend or sibling with elevated blood lead"
			userVarMap.put("LeadRisk", "has a friend or sibling with elevated blood lead");
		} else {
			retVal = true;
			return retVal;
			
		}
		//conclude here
		retVal = true;
		return retVal;
		
	}
	
	public boolean action() {
		boolean retVal = false;
		{
			userVarMap.put("ActionStr",
			    "||firstname|| reportedly ||LeadRisk||.  Drawing a blood lead level is recommended annually:");
		}
		return retVal;
	}
	
}
