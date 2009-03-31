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
package org.openmrs.reporting.export;

import java.io.Serializable;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

public class ConceptColumn implements ExportColumn, Serializable {
	
	public static final long serialVersionUID = 987654323L;
	
	private String columnType = "concept";
	
	private String columnName = "";
	
	private String modifier = "";
	
	private Integer modifierNum = null;
	
	private Integer conceptId = null;
	
	private String conceptName = "";
	
	private String[] extras = null;
	
	public ConceptColumn() {
	}
	
	public ConceptColumn(String columnName, String modifier, Integer modifierNum, String conceptId, String[] extras) {
		this.columnName = columnName;
		this.modifier = modifier;
		this.modifierNum = modifierNum;
		try {
			this.conceptId = Integer.valueOf(conceptId);
		}
		catch (NumberFormatException e) {
			this.conceptName = conceptId; // for backwards compatibility to pre 1.0.43
		}
		this.extras = extras;
	}
	
	private String toSingleTemplateString(int conceptId) {
		String s = "";
		if (extras == null)
			extras = new String[] {};
		
		if (DataExportReportObject.MODIFIER_LAST_NUM.equals(modifier)
		        || DataExportReportObject.MODIFIER_FIRST_NUM.equals(modifier)) {
			Integer num = modifierNum == null ? 1 : modifierNum;
			
			s += "#set($arr = [";
			for (Integer x = 0; x < extras.length; x++) {
				s += "'" + extras[x] + "'";
				if (!x.equals(extras.length - 1))
					s += ",";
			}
			s += "])";
			
			if (DataExportReportObject.MODIFIER_LAST_NUM.equals(modifier))
				s += "#set($obsValues = $fn.getLastNObsWithValues(" + num + ", '" + conceptId + "', $arr))";
			else if (DataExportReportObject.MODIFIER_FIRST_NUM.equals(modifier))
				s += "#set($obsValues = $fn.getFirstNObsWithValues(" + num + ", '" + conceptId + "', $arr))";
			s += "#foreach($vals in $obsValues)";
			s += "#if($velocityCount > 1)";
			s += "$!{fn.getSeparator()}";
			s += "#end";
			s += "#foreach($val in $vals)";
			s += "#if($velocityCount > 1)";
			s += "$!{fn.getSeparator()}";
			s += "#end";
			s += "$!{fn.getValueAsString($val)}";
			s += "#end";
			s += "#end\n";
		} else {
			String function = " ";
			if (DataExportReportObject.MODIFIER_ANY.equals(modifier))
				function += "$fn.getLastObs";
			else if (DataExportReportObject.MODIFIER_FIRST.equals(modifier))
				function += "$fn.getFirstObs";
			else if (DataExportReportObject.MODIFIER_LAST.equals(modifier))
				function += "$fn.getLastObs";
			else
				throw new APIException("Unknown modifier: " + modifier);
			
			if (extras.length < 1) {
				function = "$!{fn.getValueAsString(" + function;
				function += "('" + conceptId + "'))}";
				s += function; // if we don't have extras, just call the normal function and print it
			} else {
				
				s += "#set($arr = [";
				for (Integer x = 0; x < extras.length; x++) {
					s += "'" + extras[x] + "'";
					if (!x.equals(extras.length - 1))
						s += ",";
				}
				s += "])";
				
				function += "WithValues('" + conceptId + "', $arr)";
				
				s += "#set($obsRow =" + function + ")";
				s += "#foreach($val in $obsRow)";
				s += "#if($velocityCount > 1)";
				s += "$!{fn.getSeparator()}";
				s += "#end";
				s += "$!{fn.getValueAsString($val)}";
				s += "#end\n";
			}
		}
		
		return s;
	}
	
	public String toTemplateString() {
		Concept concept = Context.getConceptService().getConcept(conceptId);
		String toReturn;
		
		if (!concept.isSet()) {
			toReturn = toSingleTemplateString(concept.getConceptId());
		} else {
			List<Concept> setMembers = Context.getConceptService().getConceptsByConceptSet(concept);
			toReturn = "";
			boolean firstMember = true;
			for (Concept setMember : setMembers) {
				if (firstMember) {
					toReturn += toSingleTemplateString(setMember.getConceptId());
					firstMember = false;
				} else {
					toReturn += "$!{fn.getSeparator()}";
					toReturn += toSingleTemplateString(setMember.getConceptId());
				}
			}
		}
		
		return toReturn;
		
	}
	
	public String getColumnType() {
		return columnType;
	}
	
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	/**
	 * Convenience method used by {@link #getTemplateColumnName()} to print
	 * out for just the given concept.  This is used for all normal columns
	 * and then for the each set member of a column
	 * 
	 * @param columnName the conceptName to act on
	 * @return string for this one concept 
	 */
	private String getTemplateSingleConceptColumnName(String columnName) {
		String s = "\"" + columnName + "\"";
		s += getExtrasTemplateColumnNames(columnName, false);
		
		if (DataExportReportObject.MODIFIER_LAST_NUM.equals(modifier)
		        || DataExportReportObject.MODIFIER_FIRST_NUM.equals(modifier)) {
			
			if (modifierNum == null || modifierNum < 2)
				s += "#foreach($o in []) ";
			else
				s += "#foreach($o in [1.." + (modifierNum - 1) + "]) ";
			s += "$!{fn.getSeparator()}";
			s += "\"";
			s += columnName + "_($velocityCount)";
			s += "\"";
			s += getExtrasTemplateColumnNames(columnName, true);
			s += "#end\n";
		}
		
		return s;
	}
	
	/**
	 * @see org.openmrs.reporting.export.ExportColumn#getTemplateColumnName()
	 */
	public String getTemplateColumnName() {
		Concept concept = Context.getConceptService().getConcept(conceptId);
		String toReturn;
		if (!concept.isSet()) {
			toReturn = getTemplateSingleConceptColumnName(columnName);
		} else {
			List<Concept> setMembers = Context.getConceptService().getConceptsByConceptSet(concept);
			toReturn = "";
			boolean firstMember = true;
			for (Concept setMember : setMembers) {
				if (firstMember) {
					toReturn += getTemplateSingleConceptColumnName(setMember.getName().getName());
					firstMember = false;
				} else {
					toReturn += "$!{fn.getSeparator()}";
					toReturn += getTemplateSingleConceptColumnName(setMember.getName().getName());
				}
			}
		}
		
		return toReturn;
	}
	
	/**
	 * Get the extras template for the given conceptName
	 * 
	 * @param columnName optional column name to use instead of conceptName
	 * @param appendCount the extra label to append to the name
	 * @return template column string for this concept
	 */
	private String getExtrasTemplateColumnNames(String columnName, boolean appendCount) {
		String s = "";
		if (extras != null) {
			for (String ext : extras) {
				s += "$!{fn.getSeparator()}";
				s += "\"";
				s += columnName + "_" + ext;
				if (appendCount)
					s += "_($velocityCount)";
				s += "\"";
			}
		}
		return s;
	}
	
	//// left for backwards compatibility to pre 1.0.43
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
	public String getConceptName() {
		return conceptName;
	}
	
	///////
	
	public Integer getConceptId() {
		return conceptId;
	}
	
	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}
	
	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
	}
	
	public String getModifier() {
		return modifier;
	}
	
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}
	
	public String[] getExtras() {
		return extras;
	}
	
	public void setExtras(String[] extras) {
		this.extras = extras;
	}
	
	public Integer getModifierNum() {
		return modifierNum;
	}
	
	public void setModifierNum(Integer modifierNum) {
		this.modifierNum = modifierNum;
	}
	
	// returns conceptId if not null, conceptName otherwise
	// convenience method for backwards compatibility to pre 1.0.43
	public String getConceptIdOrName() {
		if (conceptId != null)
			return conceptId.toString();
		else
			return conceptName;
	}
	
}
