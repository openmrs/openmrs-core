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
package org.openmrs.reporting.report;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.data.CohortDefinition;
import org.openmrs.reporting.data.DatasetDefinition;
import org.openmrs.reporting.export.DataExportReportObject;

public class ReportDefinition extends AbstractReportObject implements Serializable {
	
	/* Serial version ID*/
	private static final long serialVersionUID = 1087599736245536124L;
	
	/* Logger */
	private static Log log = LogFactory.getLog(ReportDefinition.class);
	
	/* Set of report elements - table, grid, graph */
	private Set<ReportElementDefinition> reportElements = new HashSet<ReportElementDefinition>();
	
	/* 
	 * The data export and patient set objects are currently being used until the cohort and dataset 
	 * definition objects/interfaces are defined. 
	 */

	/* Data export object */
	DataExportReportObject dataExport = new DataExportReportObject();
	
	/* Default cohort */

	// TODO uncomment - was causing the following exception so I had to comment this out 
	// identifier of an instance of org.openmrs.User was altered from 2 to 1; nested exception is org.hibernate.HibernateException: identifier of an instance of org.openmrs.User was altered from 2 to 1
	// private Cohort cohort = new Cohort();
	/* Patient set */
	private Cohort patientSet = new Cohort();
	
	/* Default cohort definition - used if the report element does specify its own */
	private CohortDefinition cohortDefinition = new CohortDefinition();
	
	/* Default dataset definition - used if the report element does not specify its own */
	private DatasetDefinition datasetDefinition = new DatasetDefinition();
	
	/* Type of report object */
	public final static String TYPE_NAME = "Report Definition";
	
	/* Subtype (classifier) */
	public final static String SUB_TYPE_NAME = "Report Definition";
	
	/**
	 * Default public constructor
	 */
	public ReportDefinition() {
		super.setType(ReportDefinition.TYPE_NAME);
		super.setSubType(ReportDefinition.SUB_TYPE_NAME);
		//reportElements = new HashSet<ReportElementDefinition>();
		//cohortDefinition = new CohortDefinition();
		//datasetDefinition = new DatasetDefinition();
	}
	
	/**
	 * Gets the report elements.
	 * 
	 * @return
	 */
	public Set<ReportElementDefinition> getReportElements() {
		return reportElements;
	}
	
	/**
	 * Sets the report elements.
	 * 
	 * @param reportElements
	 */
	public void setReportElements(Set<ReportElementDefinition> reportElements) {
		this.reportElements = reportElements;
	}
	
	/**
	 * Gets the default cohort definition.
	 * 
	 * @return
	 */
	public CohortDefinition getCohortDefinition() {
		return cohortDefinition;
	}
	
	/**
	 * Sets the default cohort definition.
	 * 
	 * @param cohortDefinition
	 */
	public void setCohortDefinition(CohortDefinition cohortDefinition) {
		
	}
	
	/**
	 * Gets the default dataset definition.
	 * 
	 * @return
	 */
	public DatasetDefinition getDatasetDefinition() {
		return datasetDefinition;
	}
	
	/**
	 * Sets the default dataset definition.
	 * 
	 * @param datasetDefinition
	 */
	public void setDatasetDefinition(DatasetDefinition datasetDefinition) {
		this.datasetDefinition = datasetDefinition;
	}
	
	/**
	 * @param reportElement
	 */
	public void addReportElement(ReportElementDefinition reportElement) {
		reportElements.add(reportElement);
	}
	
	/**
	 * Gets the default data export object.
	 * 
	 * @return
	 */
	public DataExportReportObject getDataExport() {
		return dataExport;
	}
	
	/**
	 * Sets the default data export object.
	 * 
	 * @param dataExport
	 */
	public void setDataExport(DataExportReportObject dataExport) {
		this.dataExport = dataExport;
	}
	
	/**
	 * Gets the default patient set.
	 * 
	 * @return the default patient set for this report
	 */
	public Cohort getPatientSet() {
		return patientSet;
	}
	
	/**
	 * Sets the default patient set.
	 * 
	 * @param patientSet
	 */
	public void setPatientSet(Cohort patientSet) {
		this.patientSet = patientSet;
	}
	
	/**
	 * Gets the default cohort.
	 * 
	 * @return the default cohort for this report public Cohort getCohort() { return cohort; }
	 */
	
	/**
	 * Sets the default cohort.
	 * 
	 * @param cohort public void setCohort(Cohort cohort) { this.cohort = cohort; }
	 */
	
	/**
	 * Compares the object IDs and indicates whether the given object is equal to this object.
	 * 
	 * @param obj object to compare
	 * @return true if object id's are equals, false otherwise
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ReportDefinition) {
			ReportDefinition reportDef = (ReportDefinition) obj;
			return (this.getReportObjectId().equals(reportDef.getReportObjectId()));
		}
		return false;
	}
	
	/**
	 * Returns hash code representation of object.
	 * 
	 * @return an integer hash code that represents the identity of the object
	 */
	public int hashCode() {
		if (this.getReportObjectId() == null)
			return super.hashCode();
		int hash = 5;
		hash = 31 * this.getReportObjectId() + hash;
		return hash;
	}
	
	/**
	 * Converts object to string
	 */
	@Override
	public String toString() {
		return new StringBuffer().append("Report Definition #").append(getReportObjectId()).append("\nReport Elements: ")
		        .append(reportElements)
		        //.append("\nCohort: ").append(cohort)
		        .append("\nData Export: ").append(dataExport).append("\nCohort [NOT USED YET]: ").append(cohortDefinition)
		        .append("\nDataset [NOT USED YET]: ").append(datasetDefinition).toString();
	}
	
}
