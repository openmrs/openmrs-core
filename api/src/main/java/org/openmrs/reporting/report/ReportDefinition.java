/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting.report;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.openmrs.Cohort;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.data.CohortDefinition;
import org.openmrs.reporting.data.DatasetDefinition;
import org.openmrs.reporting.export.DataExportReportObject;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class ReportDefinition extends AbstractReportObject implements Serializable {
	
	/* Serial version ID*/
	private static final long serialVersionUID = 1087599736245536124L;
	
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
	public static final String TYPE_NAME = "Report Definition";
	
	/* Subtype (classifier) */
	public static final String SUB_TYPE_NAME = "Report Definition";
	
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
	 * @return Set<ReportElementDefinition> all Report Elements in the system
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
