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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.openmrs.Cohort;
import org.openmrs.Location;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.util.OpenmrsUtil;

/**
 * Facilitates printing a data export so that a patient is listed out on multiple lines to allow for
 * all obs values selected to be included.
 * 
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class RowPerObsDataExportReportObject extends DataExportReportObject implements Serializable {
	
	public static final long serialVersionUID = 123123999L;
	
	private List<Integer> patientIds = new Vector<Integer>();
	
	private Location location;
	
	// cohort and cohortDefinition should really be of type Cohort and PatientFilter, but this is temporary, and I want to avoid the known bug with xml serialization of ReportObjects  
	private Integer cohortId;
	
	private Integer cohortDefinitionId;
	
	private Integer patientSearchId;
	
	private boolean isAllPatients = false;
	
	List<ExportColumn> columns = new Vector<ExportColumn>();
	
	RowPerObsColumn rowPerObsColumn = null;
	
	public static final String TYPE_NAME = "Obs Per Row Data Export";
	
	public static final String SUB_TYPE_NAME = "Obs Per Row Data Export";
	
	/**
	 * Default Constructor
	 */
	public RowPerObsDataExportReportObject() {
		super.setType(RowPerObsDataExportReportObject.TYPE_NAME);
		super.setSubType(RowPerObsDataExportReportObject.SUB_TYPE_NAME);
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof RowPerObsDataExportReportObject) {
			RowPerObsDataExportReportObject c = (RowPerObsDataExportReportObject) obj;
			return (this.getReportObjectId().equals(c.getReportObjectId()));
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getReportObjectId() == null)
			return super.hashCode();
		int hash = 5;
		hash = 31 * this.getReportObjectId() + hash;
		return hash;
	}
	
	/**
	 * Append a simple column
	 * 
	 * @param columnName
	 * @param columnValue
	 */
	public void addSimpleColumn(String columnName, String columnValue) {
		columns.add(new SimpleColumn(columnName, columnValue));
	}
	
	/**
	 * Set the concept based column
	 * 
	 * @param columnName the string to identify this row (the header)
	 * @param conceptId the conceptid for this column
	 * @param extras String[] data from the obs to add (obsDatetime, encounterType, etc)
	 */
	public void setRowPerObsColumn(String columnName, String conceptId, String[] extras) {
		rowPerObsColumn = new RowPerObsColumn(columnName, conceptId, extras);
	}
	
	/**
	 * Append a calculated column
	 * 
	 * @param columnName
	 * @param columnValue
	 */
	public void addCalculatedColumn(String columnName, String columnValue) {
		columns.add(new CalculatedColumn(columnName, columnValue));
	}
	
	/**
	 * Append a cohort column
	 * 
	 * @param columnName
	 * @param cohortId only one of this or filterId should be non-null
	 * @param filterId only one of this or cohortId should be non-null
	 */
	public void addCohortColumn(String columnName, Integer cohortId, Integer filterId, Integer patientSearchId,
	                            String valueIfTrue, String valueIfFalse) {
		columns.add(new CohortColumn(columnName, cohortId, filterId, patientSearchId, valueIfTrue, valueIfFalse));
	}
	
	/**
	 * Add a patient to the list to be run on
	 * 
	 * @param p
	 */
	public void addPatientId(Integer p) {
		patientIds.add(p);
	}
	
	/**
	 * Generate a template according to this reports columns Assumes there is a patientSet object
	 * available
	 * 
	 * @return template string to be evaluated
	 */
	public String generateTemplate() {
		StringBuilder sb = new StringBuilder();
		
		// print out the column headers
		if (columns.size() >= 1) {
			sb.append(columns.get(0).getTemplateColumnName());
			for (int i = 1; i < columns.size(); i++) {
				sb.append("$!{fn.getSeparator()}");
				sb.append(columns.get(i).getTemplateColumnName());
			}
			sb.append("$!{fn.getSeparator()}");
		}
		sb.append(rowPerObsColumn.getTemplateColumnName());
		
		sb.append("\n");
		
		// print out the data
		
		// loop over all the patient ids
		sb.append("$!{fn.setPatientSet($patientSet)}");
		
		// the extras array used by the rowPerObsColumn 
		sb.append("#set($arr = [");
		String[] extras = rowPerObsColumn.getExtras();
		if (extras != null) {
			for (Integer x = 0; x < extras.length; x++) {
				sb.append("'" + extras[x] + "'");
				if (!x.equals(extras.length - 1))
					sb.append(",");
			}
		}
		sb.append("])");
		
		sb.append("#foreach($patientId in $patientSet.memberIds)\n");
		{
			sb.append("$!{fn.setPatientId($patientId)}");
			
			// if a null is returned from getObsWithValues, velocity wouldn't
			// set obsValues to null...it just ignores it.
			// the workaround is to set obsValues to one empty string value
			// so that the simple columns are printed out, just not any obs
			sb.append("#set($obsValues = [''])");
			// get the obs for this patient and then loop over them.
			sb.append("#set($obsValues = $fn.getObsWithValues($fn.getConcept('" + rowPerObsColumn.getConceptIdOrName()
			        + "'), $arr))");
			sb.append("#foreach($vals in $obsValues)");
			{
				if (columns.size() >= 1) {
					sb.append(columns.get(0).toTemplateString());
					for (int i = 1; i < columns.size(); i++) {
						sb.append("$!{fn.getSeparator()}");
						sb.append(columns.get(i).toTemplateString());
					}
					sb.append("$!{fn.getSeparator()}");
				}
				
				// now add the concept column stuff
				sb.append(rowPerObsColumn.toTemplateString());
			}
			sb.append("\n\n#end");
		}
		// Removed a newline at the end of the string -- the second newline was causing a problem with BIRT 
		sb.append("\n#end\n");
		
		return sb.toString();
	}
	
	/**
	 * Generate the patientSet according to this report's characteristics
	 * 
	 * @return patientSet to be used with report template
	 */
	public Cohort generatePatientSet(EvaluationContext context) {
		PatientSetService pss = Context.getPatientSetService();
		
		Set<Integer> patientIdSet = new HashSet<Integer>();
		
		if (getPatientIds() == null || getPatientIds().size() == 0) {
			patientIdSet.addAll(Context.getPatientSetService().getAllPatients().getMemberIds());
			setAllPatients(true);
		} else {
			patientIdSet.addAll(patientIds);
		}
		
		if (location != null && !location.equals(""))
			patientIdSet.retainAll(pss.getPatientsHavingLocation(getLocation()).getMemberIds());
		
		if (cohortId != null) {
			// hack to hydrate this
			Cohort cohort = Context.getCohortService().getCohort(cohortId);
			if (cohort != null)
				patientIdSet.retainAll(cohort.getMemberIds());
		}
		
		if (cohortDefinitionId != null) {
			PatientFilter cohortDefinition = (PatientFilter) Context.getReportObjectService().getReportObject(
			    cohortDefinitionId);
			if (cohortDefinition != null) {
				Cohort c = new Cohort("Cohort from Definition", "cohort from cohortdefinitionid: " + cohortDefinitionId,
				        patientIdSet);
				c = cohortDefinition.filter(c, context);
				patientIdSet = c.getMemberIds();
			}
		}
		
		if (patientSearchId != null) {
			PatientSearchReportObject search = (PatientSearchReportObject) Context.getReportObjectService().getReportObject(
			    patientSearchId);
			PatientFilter cohortDefinition = OpenmrsUtil.toPatientFilter(search.getPatientSearch(), null);
			org.openmrs.Cohort c = new Cohort("Cohort from patientSearch",
			        "cohort from patientSearchId: " + patientSearchId, patientIdSet);
			c = cohortDefinition.filter(c, context);
			patientIdSet = c.getMemberIds();
		}
		
		return new Cohort("Cohort from selected groups", "", patientIdSet);
	}
	
	@Override
	public String toString() {
		return "Data Export #" + getReportObjectId();
	}
	
	public List<ExportColumn> getColumns() {
		return columns;
	}
	
	public void setColumns(List<ExportColumn> columns) {
		this.columns = columns;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public List<Integer> getPatientIds() {
		return patientIds;
	}
	
	public void setPatientIds(List<Integer> patientIds) {
		this.patientIds = patientIds;
	}
	
	public Integer getCohortDefinitionId() {
		return cohortDefinitionId;
	}
	
	public void setCohortDefinitionId(Integer cohortDefinitionId) {
		this.cohortDefinitionId = cohortDefinitionId;
	}
	
	public Integer getCohortId() {
		return cohortId;
	}
	
	public void setCohortId(Integer cohortId) {
		this.cohortId = cohortId;
	}
	
	public Integer getPatientSearchId() {
		return patientSearchId;
	}
	
	public void setPatientSearchId(Integer patientSearchId) {
		this.patientSearchId = patientSearchId;
	}
	
	public boolean isAllPatients() {
		return isAllPatients;
	}
	
	public void setAllPatients(boolean isAllPatients) {
		this.isAllPatients = isAllPatients;
	}
	
	public RowPerObsColumn getRowPerObsColumn() {
		return rowPerObsColumn;
	}
	
	public void setRowPerObsColumn(RowPerObsColumn rowPerObsColumn) {
		this.rowPerObsColumn = rowPerObsColumn;
	}
	
}
