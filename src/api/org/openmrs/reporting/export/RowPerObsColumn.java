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

/**
 * Used with the RowPerObsDataExport to output data for one concept answered multiple times per
 * patient. The output will repeat patients in rows in order to list off all observations for the
 * given concept Example output:
 * 
 * <pre>
 * PatientId, Obs Value, Obs Date
 * 123,       55.3,      1/1/2000
 * 123,       60.3,      2/5/2000
 * 123,       62.0,      3/3/2000
 * 4393,      34.0,      1/2/2000
 * 4393,      35.0,      1/7/2000
 * 4400,      12.0,      1/1/2000
 * </pre>
 * 
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class RowPerObsColumn implements ExportColumn, Serializable {
	
	public static final long serialVersionUID = 987654323L;
	
	private String columnType = "rowPerObs";
	
	private String columnName = "";
	
	private Integer conceptId = null;
	
	private String conceptName = "";
	
	private String[] extras = null;
	
	public RowPerObsColumn() {
	}
	
	/**
	 * Convenience constructor to build the column with all values at once
	 * 
	 * @param columnName
	 * @param conceptId
	 * @param extras
	 */
	public RowPerObsColumn(String columnName, String conceptId, String[] extras) {
		this.columnName = columnName;
		try {
			this.conceptId = Integer.valueOf(conceptId);
		}
		catch (NumberFormatException e) {
			this.conceptName = conceptId; // for backwards compatibility to pre 1.0.43
		}
		this.extras = extras;
	}
	
	/**
	 * @see org.openmrs.reporting.export.ExportColumn#toTemplateString()
	 */
	public String toTemplateString() {
		String s = "#foreach($val in $vals)";
		s += "#if($velocityCount > 1)";
		s += "$!{fn.getSeparator()}";
		s += "#end";
		s += "$!{fn.getValueAsString($val)}";
		s += "#end";
		
		return s;
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
	
	public String getTemplateColumnName() {
		String s = columnName;
		s += getExtrasTemplateColumnNames(false);
		
		return s;
	}
	
	private String getExtrasTemplateColumnNames(boolean appendCount) {
		String s = "";
		if (extras != null) {
			for (String ext : extras) {
				s += "$!{fn.getSeparator()}";
				s += columnName + "_" + ext;
				if (appendCount)
					s += "_($velocityCount)";
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
	
	public String[] getExtras() {
		return extras;
	}
	
	public void setExtras(String[] extras) {
		this.extras = extras;
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
