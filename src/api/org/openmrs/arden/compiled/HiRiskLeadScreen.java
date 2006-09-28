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

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.HashMap;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.arden.*;

public class HiRiskLeadScreen implements ArdenRule{
private Context context;
private Patient patient;
private Locale locale;
private String firstname;
private ArdenDataSource dataSource;
private HashMap<String, String> userVarMap;
private DSSObject dssObj;


//Constructor
public HiRiskLeadScreen(Context c, Patient p, ArdenDataSource d){
	context = c;
	locale = c.getLocale();
	patient = p;
	dataSource = d;
	userVarMap = new HashMap <String, String>();
	firstname = patient.getPatientName().getGivenName();
	userVarMap.put("firstname", firstname);
	initAction();
	}


public ArdenRule getChildren() {
	ArdenRule rule = null;
	return rule;
}

public ArdenRule getInstance() {
	ArdenRule rule = null;
	if (this != null){
		rule = this;
	}
		return rule;
}

private Obs Last_Pb(){
	Concept concept;
	Obs obs;

	concept = context.getConceptService().getConceptByName("BLOOD LEAD LEVEL");
	obs = dataSource.getLastPatientObsForConcept(context, concept, patient, 1);

	return obs;
}

private Obs Qual_Pb(){
	Concept concept;
	Obs obs;

	concept = context.getConceptService().getConceptByName("Qualitative_Blood_Lead");
	obs = dataSource.getLastPatientObsForConcept(context, concept, patient, 1);

	return obs;
}

private Obs HousePre50(){
	Concept concept;
	Obs obs;

	concept = context.getConceptService().getConceptByName("HouseBltPre1950");
	obs = dataSource.getLastPatientObsForConcept(context, concept, patient, 1);

	return obs;
}

private Obs RenovatedPre78(){
	Concept concept;
	Obs obs;

	concept = context.getConceptService().getConceptByName("RenovatedPre78");
	obs = dataSource.getLastPatientObsForConcept(context, concept, patient, 1);

	return obs;
}

private Obs HiPbSibFriend(){
	Concept concept;
	Obs obs;

	concept = context.getConceptService().getConceptByName("HiPbSibFriend");
	obs = dataSource.getLastPatientObsForConcept(context, concept, patient, 1);

	return obs;
}


public DSSObject evaluate() {
	if(evaluate_logic()) {
			dssObj.setPrintString(action());
			return dssObj;
	}
	else {
			return null;
	}

}

private boolean evaluate_logic() {
	boolean retVal = false;
	Obs obs;
	dssObj = new DSSObject(context,locale, patient);


	if ( (obs = Last_Pb()) != null ) {
		if (obs.getValueNumeric() >= 14 ) {
			//LeadRisk = "has lead level greater than 14 mg/dcl"
		userVarMap.put("LeadRisk", "has lead level greater than 14 mg/dcl");
		dssObj.addObs("BLOOD LEAD LEVEL", obs);

	}
	}


	if ( (obs = Qual_Pb()) != null ) {
		if (obs.getValueAsBoolean() == true ) {
	
	 //conclude here
		retVal = false;
		dssObj.setConcludeVal(false);
		dssObj.addObs("Qualitative_Blood_Lead", obs);
		return retVal;

	}
	}


	if ( (obs = HousePre50()) != null ) {
		if (obs.getValueText().equals("YES") ) {
			//LeadRisk = "lives in a house built before 1950"
		userVarMap.put("LeadRisk", "lives in a house built before 1950");
		dssObj.addObs("HouseBltPre1950", obs);

	}
	}

	else if ( (obs = RenovatedPre78()) != null ) {
		if (obs.getValueText().equals("YES") ) {
			//LeadRisk = "lives in a pre-1978 house undergoing renovation"
		userVarMap.put("LeadRisk", "lives in a pre-1978 house undergoing renovation");
		dssObj.addObs("RenovatedPre78", obs);

	}
	}

	else if ( (obs = HiPbSibFriend()) != null ) {
		if (obs.getValueText().equals("YES") ) {
			//LeadRisk = "has a friend or sibling with elevated blood lead"
		userVarMap.put("LeadRisk", "has a friend or sibling with elevated blood lead");
		dssObj.addObs("HiPbSibFriend", obs);

	}
	}

	else {

	 //conclude here
		retVal = true;
		dssObj.setConcludeVal(true);
		return retVal;

	}

		return retVal;

	}
public void initAction() {
		userVarMap.put("ActionStr", "||firstname|| reportedly ||LeadRisk||.  Drawing a blood lead level is recommended annually:");
}

private String action() {
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
			variable = inStr.substring(startindex, endindex).trim();
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
			variable = inStr.substring(startindex, endindex).trim();
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