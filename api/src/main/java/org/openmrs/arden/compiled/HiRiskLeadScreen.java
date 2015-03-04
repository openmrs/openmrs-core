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
package org.openmrs.arden.compiled;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.arden.ArdenClause;
import org.openmrs.arden.ArdenDataSource;
import org.openmrs.arden.ArdenRule;
import org.openmrs.arden.ArdenValue;

public class HiRiskLeadScreen implements ArdenRule {
	
	private Patient patient;
	
	private String firstname;
	
	private ArdenDataSource dataSource;
	
	private HashMap<String, String> userVarMap;
	
	private HashMap<String, ArdenValue> valueMap;
	
	private ArdenClause ardenClause;
	
	//Constructor
	public HiRiskLeadScreen(Patient p, ArdenDataSource d) {
		
		patient = p;
		dataSource = d;
		ardenClause = new ArdenClause();
		userVarMap = new HashMap<String, String>();
		valueMap = new HashMap<String, ArdenValue>();
		firstname = patient.getPersonName().getGivenName();
		userVarMap.put("firstname", firstname);
		initAction();
	}
	
	public ArdenRule getChildren() {
		ArdenRule rule = null;
		return rule;
	}
	
	public ArdenRule getInstance() {
		ArdenRule rule = null;
		if (this != null) {
			rule = this;
		}
		return rule;
	}
	
	private ArdenValue Last_Pb() {
		Concept c = new Concept();
		c.setConceptId(31); // BLOOD LEAD LEVEL 
		//return dataSource.eval(patient, Aggregation.last(2), c, DateCriteria.within(Duration.past(Days(330))) );
		return dataSource.eval(patient, ardenClause.concept(c).last(2).within().past().Days(330));
	}
	
	private ArdenValue Qual_Pb() {
		Concept c = new Concept();
		c.setConceptId(8); // CHICA REPORTED LEAD 
		//return dataSource.eval(patient, Aggregation.last(1), c, DateCriteria.within(Duration.past(Days(330))) );
		return dataSource.eval(patient, ardenClause.concept(c).last(1).within().past().Days(330));
	}
	
	private ArdenValue EnvHx() {
		Concept c = new Concept();
		c.setConceptId(3); // ENVIRONMENTAL HISTORY 
		//return dataSource.eval(patient, Aggregation.last(1), c, DateCriteria.within(Duration.past(Days(365))) );
		return dataSource.eval(patient, ardenClause.concept(c).last(1).within().past().Days(365));
	}
	
	public boolean evaluate() {
		return evaluate_logic();
	}
	
	private boolean evaluate_logic() {
		boolean retVal = false;
		ArdenValue val;
		
		if ((val = Last_Pb()) != null) {
			if (val.getValueNumeric() >= 14) {
				//LeadRisk = "has lead level greater than 14 mg/dcl"
				userVarMap.put("LeadRisk", "has lead level greater than 14 mg/dcl");
				valueMap.put("Last_Pb", val);
				
			}
		}
		
		if ((val = Qual_Pb()) != null) {
			
			//LESS THAN 10 MG/DL
			if (val.getValueCoded() == 24) {
				
				//conclude here
				retVal = false;
				valueMap.put("Qual_Pb", val);
				return retVal;
				
			}
		}
		
		if ((val = EnvHx()) != null) {
			
			//HOME BUILT BEFORE 1960
			if (val.getValueCoded() == 4) {
				//LeadRisk = "lives in a house built before 1950"
				userVarMap.put("LeadRisk", "lives in a house built before 1950");
				valueMap.put("EnvHx", val);
				
			}
		}

		else if ((val = EnvHx()) != null) {
			
			//HOME RENOVATED BEFORE 1978
			if (val.getValueCoded() == 55) {
				//LeadRisk = "lives in a house built before 1950"
				userVarMap.put("LeadRisk", "lives in a house built before 1950");
				valueMap.put("EnvHx", val);
				
			}
		}

		else if ((val = EnvHx()) != null) {
			
			//TB EXPOSURE
			if (val.getValueCoded() == 104) {
				//LeadRisk = "lives in a house built before 1950"
				userVarMap.put("LeadRisk", "lives in a house built before 1950");
				valueMap.put("EnvHx", val);
				
			}
		}

		else {
			
			//conclude here
			retVal = false;
			return retVal;
			
		}
		
		//conclude here
		retVal = true;
		return retVal;
		
	}
	
	public void initAction() {
		userVarMap.put("ActionStr",
		    "||firstname|| reportedly ||LeadRisk||.  Drawing a blood lead level is recommended annually:");
	}
	
	public String doAction() {
		int index = 0, nindex = 0, endindex = 0, startindex = 0;
		String tempstr, variable;
		StringBuilder outStr = new StringBuilder("");
		String inStr = userVarMap.get("ActionStr");
		
		tempstr = inStr;
		
		index = tempstr.indexOf("||", nindex);
		if (index != -1) {
			if (index == 0) { // At the beginning
				nindex = tempstr.indexOf("||", index + 1);
				startindex = index + 2;
				endindex = nindex;
				variable = inStr.substring(startindex, endindex).trim();
				outStr.append(userVarMap.get(variable));
				index = tempstr.indexOf("||", nindex + 2);
			}
			while (index > 0) {
				if (nindex == 0) { // Are we starting now
					startindex = nindex;
					endindex = index;
					outStr.append(tempstr.substring(startindex, endindex));
				} else {
					startindex = nindex + 2;
					endindex = index;
					outStr.append(tempstr.substring(startindex, endindex));
				}
				nindex = tempstr.indexOf("||", index + 2);
				startindex = index + 2;
				endindex = nindex;
				variable = inStr.substring(startindex, endindex).trim();
				outStr.append(userVarMap.get(variable));
				index = tempstr.indexOf("||", nindex + 2);
			}
			outStr.append(tempstr.substring(nindex + 2));
		} else {
			outStr.append(tempstr);
		}
		return outStr.toString();
	}
	
	public void printDebug() {
		for (Map.Entry<String, ArdenValue> entry : valueMap.entrySet()) {
			System.out.println("__________________________________");
			System.out.println(entry.getKey() + ": ");
			ArdenValue val = entry.getValue();
			val.PrintObsMap();
			System.out.println("__________________________________");
		}
	}
}
