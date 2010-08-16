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
		if (readType != null && !readType.equals("") && !readType.equalsIgnoreCase("exist")) {
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
				w.append("\n\t\t\tResult " + key + "=new Result((String) parameters.get(\"" + key + "\")");
			} else {
				w.append("\n\t\t\tResult " + key + "=context.read(\n\t\t\t\tpatient,context.getLogicDataSource(\""
				        + this.datasource + "\"),\n\t\t\t\tnew LogicCriteriaImpl(\"" + cn.trim() + "\")");
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
