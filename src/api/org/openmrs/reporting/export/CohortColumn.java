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
				this.columnName = Context.getReportService().getPatientFilterById(filterId).getName();
			if (!StringUtils.hasText(valueIfTrue))
				this.valueIfTrue = Context.getReportService().getPatientFilterById(filterId).getName();
		} else { // assert patientSearchId != null
			this.patientSearchId = patientSearchId;
			if (!StringUtils.hasText(columnName))
				this.columnName = Context.getReportService().getReportObject(patientSearchId).getName();
			if (!StringUtils.hasText(valueIfTrue))
				this.valueIfTrue = Context.getReportService().getReportObject(patientSearchId).getName();
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
