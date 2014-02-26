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
import java.util.Arrays;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
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
		if (extras == null) {
			this.extras = new String[0];
		} else {
			this.extras = Arrays.copyOf(extras, extras.length);
		}
	}
	
	private String toSingleTemplateString(int conceptId) {
		StringBuilder s = new StringBuilder("");
		if (extras == null) {
			extras = new String[] {};
		}
		
		if (DataExportReportObject.MODIFIER_LAST_NUM.equals(modifier)
		        || DataExportReportObject.MODIFIER_FIRST_NUM.equals(modifier)) {
			Integer num = modifierNum == null ? 1 : modifierNum;
			
			s.append("#set($arr = [");
			for (Integer x = 0; x < extras.length; x++) {
				s.append("'");
				s.append(extras[x]);
				s.append("'");
				if (!x.equals(extras.length - 1)) {
					s.append(",");
				}
			}
			s.append("])");
			
			if (DataExportReportObject.MODIFIER_LAST_NUM.equals(modifier)) {
				s.append("#set($obsValues = $fn.getLastNObsWithValues(").append(num).append(", '").append(conceptId).append(
				    "', $arr))");
			} else if (DataExportReportObject.MODIFIER_FIRST_NUM.equals(modifier)) {
				s.append("#set($obsValues = $fn.getFirstNObsWithValues(").append(num).append(", '").append(conceptId)
				        .append("', $arr))");
			}
			s.append("#foreach($vals in $obsValues)").append("#if($velocityCount > 1)").append("$!{fn.getSeparator()}")
			        .append("#end").append("#foreach($val in $vals)").append("#if($velocityCount > 1)").append(
			            "$!{fn.getSeparator()}").append("#end").append("$!{fn.getValueAsString($val)}").append("#end")
			        .append("#end\n");
		} else {
			String function = " ";
			if (DataExportReportObject.MODIFIER_ANY.equals(modifier)) {
				function += "$fn.getLastObs";
			} else if (DataExportReportObject.MODIFIER_FIRST.equals(modifier)) {
				function += "$fn.getFirstObs";
			} else if (DataExportReportObject.MODIFIER_LAST.equals(modifier)) {
				function += "$fn.getLastObs";
			} else {
				throw new APIException("Unknown modifier: " + modifier);
			}
			
			if (extras.length < 1) {
				function = "$!{fn.getValueAsString(" + function;
				function += "('" + conceptId + "'))}";
				s.append(function); // if we don't have extras, just call the normal function and print it
			} else {
				
				s.append("#set($arr = [");
				for (Integer x = 0; x < extras.length; x++) {
					s.append("'").append(extras[x]).append("'");
					if (!x.equals(extras.length - 1)) {
						s.append(",");
					}
				}
				s.append("])");
				
				function += "WithValues('" + conceptId + "', $arr)";
				
				s.append("#set($obsRow =" + function + ")").append("#foreach($val in $obsRow)").append(
				    "#if($velocityCount > 1)").append("$!{fn.getSeparator()}").append("#end").append(
				    "$!{fn.getValueAsString($val)}").append("#end\n");
			}
		}
		
		return s.toString();
	}
	
	public String toTemplateString() {
		Concept concept = Context.getConceptService().getConcept(conceptId);
		StringBuilder toReturn;
		
		if (!concept.isSet()) {
			toReturn = new StringBuilder(toSingleTemplateString(concept.getConceptId()));
		} else {
			List<Concept> setMembers = Context.getConceptService().getConceptsByConceptSet(concept);
			toReturn = new StringBuilder("");
			boolean firstMember = true;
			for (Concept setMember : setMembers) {
				if (firstMember) {
					toReturn.append(toSingleTemplateString(setMember.getConceptId()));
					firstMember = false;
				} else {
					toReturn.append("$!{fn.getSeparator()}");
					toReturn.append(toSingleTemplateString(setMember.getConceptId()));
				}
			}
		}
		
		return toReturn.toString();
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
	 * Convenience method used by {@link #getTemplateColumnName()} to print out for just the given
	 * concept. This is used for all normal columns and then for the each set member of a column
	 *
	 * @param columnName the conceptName to act on
	 * @return string for this one concept
	 */
	private String getTemplateSingleConceptColumnName(String columnName) {
		String s = "\"" + columnName + "\"";
		s += getExtrasTemplateColumnNames(columnName, false);
		
		if (DataExportReportObject.MODIFIER_LAST_NUM.equals(modifier)
		        || DataExportReportObject.MODIFIER_FIRST_NUM.equals(modifier)) {
			
			if (modifierNum == null || modifierNum < 2) {
				s += "#foreach($o in []) ";
			} else {
				s += "#foreach($o in [1.." + (modifierNum - 1) + "]) ";
			}
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
		StringBuilder toReturn;
		if (!concept.isSet()) {
			toReturn = new StringBuilder(getTemplateSingleConceptColumnName(columnName));
		} else {
			List<Concept> setMembers = Context.getConceptService().getConceptsByConceptSet(concept);
			toReturn = new StringBuilder("");
			boolean firstMember = true;
			for (Concept setMember : setMembers) {
				if (firstMember) {
					toReturn.append(getTemplateSingleConceptColumnName(setMember.getName().getName()));
					firstMember = false;
				} else {
					toReturn.append("$!{fn.getSeparator()}").append(
					    getTemplateSingleConceptColumnName(setMember.getName().getName()));
				}
			}
		}
		
		return toReturn.toString();
	}
	
	/**
	 * Get the extras template for the given conceptName
	 *
	 * @param columnName optional column name to use instead of conceptName
	 * @param appendCount the extra label to append to the name
	 * @return template column string for this concept
	 */
	private String getExtrasTemplateColumnNames(String columnName, boolean appendCount) {
		StringBuilder s = new StringBuilder("");
		if (extras != null) {
			for (String ext : extras) {
				s.append("$!{fn.getSeparator()}").append("\"").append(columnName).append("_").append(ext);
				if (appendCount) {
					s.append("_($velocityCount)");
				}
				s.append("\"");
			}
		}
		return s.toString();
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
		if (extras == null) {
			this.extras = new String[0];
		} else {
			this.extras = Arrays.copyOf(extras, extras.length);
		}
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
		if (conceptId != null) {
			return conceptId.toString();
		} else {
			return conceptName;
		}
	}
	
}
