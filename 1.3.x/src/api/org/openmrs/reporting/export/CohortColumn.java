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

import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class CohortColumn implements ExportColumn, Serializable {

	private static final long serialVersionUID = 1L;
	
	private String columnType = "cohort";
	private String columnName = "";
	private String valueIfTrue = "Yes";
	private String valueIfFalse = "No";
	private Integer cohortId;
	private Integer filterId;
	private Integer patientSearchId;

	public CohortColumn() {	}
	
	public CohortColumn(String columnName, Integer cohortId, Integer filterId, Integer patientSearchId, String valueIfTrue, String valueIfFalse) {
		this.columnName = columnName;
		this.valueIfTrue = valueIfTrue;
		this.valueIfFalse = StringUtils.hasText(valueIfFalse) ? valueIfFalse : "";
		if (cohortId != null) {
			this.cohortId = cohortId;
			if (!StringUtils.hasText(columnName))
				this.columnName = Context.getCohortService().getCohort(cohortId).getName();
			if (!StringUtils.hasText(valueIfTrue))
				this.valueIfTrue = Context.getCohortService().getCohort(cohortId).getName();
		} else if (filterId != null) {
			this.filterId = filterId;
			if (!StringUtils.hasText(columnName))
				this.columnName = Context.getReportObjectService().getPatientFilterById(filterId).getName();
			if (!StringUtils.hasText(valueIfTrue))
				this.valueIfTrue = Context.getReportObjectService().getPatientFilterById(filterId).getName();
		} else { // assert patientSearchId != null
			this.patientSearchId = patientSearchId;
			if (!StringUtils.hasText(columnName))
				this.columnName = Context.getReportObjectService().getReportObject(patientSearchId).getName();
			if (!StringUtils.hasText(valueIfTrue))
				this.valueIfTrue = Context.getReportObjectService().getReportObject(patientSearchId).getName();
		}
	}
	
	public String getColumnName() {
		return this.columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getTemplateColumnName() {
		return columnName;
	}

	public String toTemplateString() {
		if (cohortId != null)
			return "$!{fn.getCohortMembership(" + cohortId + ", '" + valueIfTrue + "', '" + valueIfFalse + "')}";
		else if (filterId != null)
			return "$!{fn.getCohortDefinitionMembership(" + filterId + ", '" + valueIfTrue + "', '" + valueIfFalse + "')}";
		else
			return "$!{fn.getPatientSearchMembership(" + patientSearchId + ", '" + valueIfTrue + "', '" + valueIfFalse + "')}";
	}
	
	public Integer getCohortId() {
		return cohortId;
	}

	public void setCohortId(Integer cohortId) {
		this.cohortId = cohortId;
	}

	public Integer getFilterId() {
		return filterId;
	}

	public void setFilterId(Integer filterId) {
		this.filterId = filterId;
	}

	public Integer getPatientSearchId() {
    	return patientSearchId;
    }

	public void setPatientSearchId(Integer patientSearchId) {
    	this.patientSearchId = patientSearchId;
    }

	public String getColumnType() {
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public String getValueIfFalse() {
		return valueIfFalse;
	}

	public void setValueIfFalse(String valueIfFalse) {
		this.valueIfFalse = valueIfFalse;
	}

	public String getValueIfTrue() {
		return valueIfTrue;
	}

	public void setValueIfTrue(String valueIfTrue) {
		this.valueIfTrue = valueIfTrue;
	}
	
}
