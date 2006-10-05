/********************************************************************
 Title : Direct Tuberculosis Contact
 Filename:  directtbcontact
 Version : 0 . 1
 Institution : Indiana University School of Medicine
 Author : Paul Biondich
 Specialist : Pediatrics
 Date :
 Validation :
 Purpose : PSF screening question that assesses direct exposure to someone with tuberculosis, which is a risk factor for TB exposure.
 Explanation : Uses new EXIST modifier.. need to discuss with group.
 Keywords : PSF, TB, tuberculosis, exposure
 Citations : Pediatric Tuberculosis Collaborative Group Report
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

public class directtbcontact implements ArdenRule{
private Context context;
private Patient patient;
private Locale locale;
private String firstname;
private ArdenDataSource dataSource;
private HashMap<String, String> userVarMap;
private DSSObject dssObj;


//Constructor
public directtbcontact(Context c, Patient p, ArdenDataSource d){
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

private Obs Pos_Risk(){
	Concept concept;
	Obs obs;

	concept = context.getConceptService().getConceptByName("TB_Risk_Positive");
	obs = dataSource.getLastPatientObsForConcept(context, concept, patient, 1);

	return obs;
}

private Obs Neg_Risk(){
	Concept concept;
	Obs obs;

	concept = context.getConceptService().getConceptByName("TB_Risk_Negative");
	obs = dataSource.getLastPatientObsForConcept(context, concept, patient, 1);

	return obs;
}

private Obs PPD(){
	Concept concept;
	Obs obs;

	concept = context.getConceptService().getConceptByName("CHICA REPORTED PPD");
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


	if ( (obs = Pos_Risk()) != null ) {
		if (obs.getValueAsBoolean() == true ) {
	
	 //conclude here
		retVal = true;
		dssObj.setConcludeVal(true);
		dssObj.addObs("Pos_Risk", obs);
		return true;

	}
	}


	if ( (obs = Neg_Risk()) != null ) {
		if (obs.getValueAsBoolean() == true ) {
	
	 //conclude here
		retVal = false;
		dssObj.setConcludeVal(false);
		dssObj.addObs("Neg_Risk", obs);
		return retVal;

	}
	}


	if ( (obs = PPD()) != null ) {
		if ((obs.getValueCoded()) != null) {
	
	 //conclude here
		retVal = true;
		dssObj.setConcludeVal(true);
		dssObj.addObs("PPD", obs);
		return retVal;

	}
	}


	 //conclude here
		retVal = true;
		dssObj.setConcludeVal(true);
		return retVal;


	}
public void initAction() {
		userVarMap.put("ActionStr", "|| firstname || has been exposed to someone with tuberculosis (TB) disease?");
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