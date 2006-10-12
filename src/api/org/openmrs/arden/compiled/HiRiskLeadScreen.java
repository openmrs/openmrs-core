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
import org.openmrs.api.context.Context;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.arden.*;
import org.openmrs.arden.compiled.*;
import java.util.Map;

public class HiRiskLeadScreen implements ArdenRule{
private Patient patient;
private Locale locale;
private String firstname;
private ArdenDataSource dataSource;
private HashMap<String, String> userVarMap;
private HashMap<String, ArdenValue> valueMap;
private ArdenClause ardenClause;


//Constructor
public HiRiskLeadScreen(Patient p, ArdenDataSource d){

	locale = Context.getLocale();
	patient = p;
	dataSource = d;
	ardenClause = new ArdenClause();
	userVarMap = new HashMap <String, String>();
	valueMap = new HashMap <String, ArdenValue>();
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

private ArdenValue Last_Pb(){
	Concept concept;
	concept = Context.getConceptService().getConceptByName("BLOOD LEAD LEVEL");
	return dataSource.eval(patient, ardenClause.concept(concept).latest(1));
}

private ArdenValue Qual_Pb(){
	Concept concept;
	concept = Context.getConceptService().getConceptByName("Qualitative_Blood_Lead");
	return dataSource.eval(patient, ardenClause.concept(concept).latest(1));
}

private ArdenValue HousePre50(){
	Concept concept;
	concept = Context.getConceptService().getConceptByName("HouseBltPre1950");
	return dataSource.eval(patient, ardenClause.concept(concept).latest(1));
}

private ArdenValue RenovatedPre78(){
	Concept concept;
	concept = Context.getConceptService().getConceptByName("RenovatedPre78");
	return dataSource.eval(patient, ardenClause.concept(concept).latest(1));
}

private ArdenValue HiPbSibFriend(){
	Concept concept;
	concept = Context.getConceptService().getConceptByName("HiPbSibFriend");
	return dataSource.eval(patient, ardenClause.concept(concept).latest(1));
}


public boolean evaluate() {
			return evaluate_logic();
}

private boolean evaluate_logic() {
	boolean retVal = false;
	ArdenValue val;

	if ( (val = Last_Pb()) != null ) {
		if (val.getValueNumeric() >= 14 ) {
			//LeadRisk = "has lead level greater than 14 mg/dcl"
		userVarMap.put("LeadRisk", "has lead level greater than 14 mg/dcl");
		valueMap.put("Last_Pb", val);

	}
	}


	if ( (val = Qual_Pb()) != null ) {
		if (val.getValueAsBoolean() == true ) {
	
	 //conclude here
		retVal = false;
		valueMap.put("Qual_Pb", val);
		return retVal;

	}
	}


	if ( (val = HousePre50()) != null ) {
		if (val.getValueText() != null && val.getValueText().equals("YES") ) {
			//LeadRisk = "lives in a house built before 1950"
		userVarMap.put("LeadRisk", "lives in a house built before 1950");
		valueMap.put("HousePre50", val);

	}
	}

	else if ( (val = RenovatedPre78()) != null ) {
		if (val.getValueText() != null && val.getValueText().equals("YES") ) {
			//LeadRisk = "lives in a pre-1978 house undergoing renovation"
		userVarMap.put("LeadRisk", "lives in a pre-1978 house undergoing renovation");
		valueMap.put("RenovatedPre78", val);

	}
	}

	else if ( (val = HiPbSibFriend()) != null ) {
		if (val.getValueText() != null && val.getValueText().equals("YES") ) {
			//LeadRisk = "has a friend or sibling with elevated blood lead"
		userVarMap.put("LeadRisk", "has a friend or sibling with elevated blood lead");
		valueMap.put("HiPbSibFriend", val);

	}
	}

	else {

	 //conclude here
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

public String doAction() {
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
public void printDebug(){
	for (Map.Entry<String,ArdenValue> entry : valueMap.entrySet()) {
		System.out.println("__________________________________");
		System.out.println (entry.getKey () + ": ");
		ArdenValue val = entry.getValue ();
		val.PrintObsMap();
		System.out.println("__________________________________");
		}
	}
}