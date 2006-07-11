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
package org.openmrs.arden.compiled;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

public class  HiRiskLeadScreen{
private Context context;
private Patient patient;
private Locale locale;
private String firstname;
private HashMap<String, String> userVarMap;


//Constructor
public  HiRiskLeadScreen(Context c, Integer pid, Locale l){
	context = c;
	locale = l;
	patient = c.getPatientService().getPatient(pid);
	userVarMap = new HashMap <String, String>();
	firstname = patient.getPatientName().getGivenName();
	userVarMap.put("firstname", firstname);
	initAction();}


public Obs getObsForConceptForPatient(Concept concept, Locale locale, Patient patient) {
	Set <Obs> MyObs;
	Obs obs = new Obs();
	{		MyObs = context.getObsService().getObservations(patient, concept);
		Iterator iter = MyObs.iterator();
		if(iter.hasNext()) {
			while(iter.hasNext())	{
				obs = (Obs) iter.next();
				System.out.println(obs.getValueAsString(locale));
			}
				return obs;
		}
		else {
			return null;
		}
	}
}

public boolean run() {
	boolean retVal = false;
	if(evaluate()) {
		String str = action();
		System.out.println(str);
	}
	return retVal;
}

public boolean evaluate_Last_Pb(){
	Concept concept;
	boolean retVal = false;
	Obs obs;

	concept = context.getConceptService().getConceptByName("BLOOD LEAD LEVEL");
	obs = getObsForConceptForPatient(concept,locale, patient);
	if(obs != null) {
		double Last_Pb = obs.getValueNumeric();
		if (Last_Pb >= 14) {
		retVal = true;
		}
	}

	return retVal;
}

public boolean evaluate_Qual_Pb(){
	Concept concept;
	boolean retVal = false;
	Obs obs;

	concept = context.getConceptService().getConceptByName("Qualitative_Blood_Lead");
	obs = getObsForConceptForPatient(concept,locale, patient);
	if(obs != null) {
		boolean Qual_Pb = obs.getValueAsBoolean();
		if (Qual_Pb == true) {
		retVal = true;
		}
	}

	return retVal;
}

public boolean evaluate_HousePre50(){
	Concept concept;
	boolean retVal = false;
	Obs obs;

	concept = context.getConceptService().getConceptByName("HouseBltPre1950");
	obs = getObsForConceptForPatient(concept,locale, patient);
	if(obs != null) {
		String HousePre50 = obs.getValueText();
		if (HousePre50.equals("YES")) {
		retVal = true;
		}
	}

	return retVal;
}

public boolean evaluate_RenovatedPre78(){
	Concept concept;
	boolean retVal = false;
	Obs obs;

	concept = context.getConceptService().getConceptByName("RenovatedPre78");
	obs = getObsForConceptForPatient(concept,locale, patient);
	if(obs != null) {
		String RenovatedPre78 = obs.getValueText();
		if (RenovatedPre78.equals("YES")) {
		retVal = true;
		}
	}

	return retVal;
}

public boolean evaluate_HiPbSibFriend(){
	Concept concept;
	boolean retVal = false;
	Obs obs;

	concept = context.getConceptService().getConceptByName("HiPbSibFriend");
	obs = getObsForConceptForPatient(concept,locale, patient);
	if(obs != null) {
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


	if(evaluate_Last_Pb()) {
		//LeadRisk = "has lead level greater than 14 mg/dcl"
		userVarMap.put("LeadRisk", "has lead level greater than 14 mg/dcl");
	}
	if(evaluate_Qual_Pb()) {
		retVal = false;
		return retVal;

	}
	if(evaluate_HousePre50()) {
		//LeadRisk = "lives in a house built before 1950"
		userVarMap.put("LeadRisk", "lives in a house built before 1950");
	}
	else if(evaluate_RenovatedPre78()) {
		//LeadRisk = "lives in a pre-1978 house undergoing renovation"
		userVarMap.put("LeadRisk", "lives in a pre-1978 house undergoing renovation");
	}
	else if(evaluate_HiPbSibFriend()) {
		//LeadRisk = "has a friend or sibling with elevated blood lead"
		userVarMap.put("LeadRisk", "has a friend or sibling with elevated blood lead");
	}
	else {
		retVal = true;
		return retVal;

	}
 //conclude here
		retVal = true;
		return retVal;

	}
public void initAction() {
		userVarMap.put("ActionStr", "||firstname|| reportedly ||LeadRisk||.  Drawing a blood lead level is recommended annually:");
}

public String action() {
	int index = 0, nindex = 0, endindex = 0, startindex = 0;
	String tempstr, variable, outStr = "";
	String inStr = userVarMap.get("ActionStr");

	tempstr = inStr;

	index = tempstr.indexOf("||", nindex);
	if(index != -1) {
		if(index == 0) { // At the beginning
			nindex = tempstr.indexOf("||", index+1);
			startindex = index + 2;
			endindex = nindex;
			variable = inStr.substring(startindex, endindex);
			outStr += userVarMap.get(variable);
			index = tempstr.indexOf("||", nindex+2);
		}
		while(index > 0){
			if(nindex == 0){ // Are we starting now
				startindex = nindex;
				endindex = index;
				outStr += tempstr.substring(startindex, endindex);
			}
			else {
				startindex = nindex + 2;
				endindex = index;
				outStr += tempstr.substring(startindex, endindex);
			}
			nindex = tempstr.indexOf("||", index+2);
			startindex = index + 2;
			endindex = nindex;
			variable = inStr.substring(startindex, endindex);
			outStr += userVarMap.get(variable);
			index = tempstr.indexOf("||", nindex+2);
		}
		outStr += tempstr.substring(nindex+2);
	}
	else {
		outStr += tempstr;
	}
	return outStr;
}
}