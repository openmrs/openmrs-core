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

import org.apache.commons.lang.StringUtils;

import java.io.Writer;

/*
 * @@ This class represents a statement in Data slot - read
 */

public class MLMObjectElement {
	
	public static final String PARAMETERS_DATASOURCE = "Parameters";
	
	private String conceptName;
	
	private String readType; // Exist, Last, First etc
	
	private int howMany; // how many to read
	
	private boolean hasWhere;
	
	private String whereType;
	
	private String durationType;
	
	private String durationVal;
	
	private String durationOp; // TODO
	
	private String datasource = "obs";
	
	public MLMObjectElement() {
	}
	
	public void setWhere(String type) {
		hasWhere = true;
		whereType = type.trim();
	}
	
	public void setDuration(String type, String val, String op) {
		durationType = type.trim();
		durationVal = val.trim();
		op = op.trim();
		if (op.toUpperCase().startsWith("MONTH")) {
			durationOp = "months";
		} else if (op.toUpperCase().startsWith("YEAR")) {
			durationOp = "years";
		} else if (op.toUpperCase().startsWith("DAY")) {
			durationOp = "days";
		}
	}
	
	public void setWhere(boolean val) {
		hasWhere = val;
	}
	
	private String getConcept(String conceptName) {
		String cn;
		int len;
		int index;
		if (conceptName == null) {
			return conceptName;
		}
		index = conceptName.indexOf("from"); // First substring
		if (index != -1) {
			cn = conceptName.substring(1, index);
			datasource = conceptName.substring(index + 4, conceptName.length() - 1).trim();
		} else {
			len = conceptName.length();
			if (conceptName.contains("{")) {
				cn = conceptName.substring(1, len - 1);
			} else {
				cn = conceptName.substring(0, len);
			}
		}
		return cn;
	}
	
	private String getReadTypeAsString() {
		String retVal = "";
		if (StringUtils.isNotEmpty(readType) && !readType.equalsIgnoreCase("exist")) {
			retVal = "." + readType;
		} else {
			retVal = ".last"; // TODO: for now default
		}
		
		if (howMany > 1) {
			retVal += "(" + howMany + ")";
		} else {
			retVal += "()";
		}
		
		return retVal;
	}
	
	public String getReadType() {
		return this.readType;
	}
	
	public boolean writeEvaluate(String key, Writer w) throws Exception {
		boolean retVal = true;
		
		String cn = conceptName;
		
		if (cn != null) {
			/***************************************************************************************
			 * 
			 **************************************************************************************/
			if (this.datasource.equalsIgnoreCase(PARAMETERS_DATASOURCE)) {
				w.append("\n\t\t\tResult " + key + "=new Result((String) parameters.get(\"" + cn + "\")");
			} else {
				w.append("\n\t\t\tResult " + key
				        + "=context.read(\n\t\t\t\tpatient.getPatientId(),context.getLogicDataSource(\"" + this.datasource
				        + "\"),\n\t\t\t\tnew LogicCriteriaImpl(\"" + cn.trim() + "\")");
			}
			if (hasWhere) {
				if (whereType.equals("withinPreceding")) {
					w.append(".within(Duration." + durationOp + "(-" + durationVal + "))");
				}
			}
			
			//right the read type
			if (readType != null && readType.length() > 0) {
				w.append(this.getReadTypeAsString());
			}
			w.append(");");
			w.append("\n\t\t\tresultLookup.put(\"" + key + "\"," + key + ");");
			
		}
		return retVal;
	}
	
	private String getConceptName() {
		
		return conceptName;
	}
	
	public void setReadType(String readType) {
		this.readType = readType.trim();
	}
	
	public void setHowMany(int howMany) {
		this.howMany = howMany;
	}
	
	public void setConceptName(String conceptName) {
		this.conceptName = this.getConcept(conceptName).trim();
	}
	
}
