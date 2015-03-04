/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting.data;

import java.io.Serializable;

import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.export.DataExportReportObject;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class DatasetDefinition extends AbstractReportObject implements Serializable {
	
	/* Serial version ID */
	private static final long serialVersionUID = 6756236465896337596L;
	
	/* Type of report object */
	public final static String TYPE_NAME = "Dataset Definition";
	
	/* Subtype (classifier) */
	public final static String SUB_TYPE_NAME = "Dataset Definition";
	
	/* Data export object */
	DataExportReportObject dataExport = new DataExportReportObject();
	
	/* Data export location */
	private String datasetLocation;
	
	/**
	 * Default public constructor
	 */
	public DatasetDefinition() {
		this.dataExport = new DataExportReportObject();
	}
	
	/**
	 * Default public constructor
	 */
	public DatasetDefinition(DataExportReportObject dataExport) {
		this.dataExport = dataExport;
	}
	
	/**
	 * @return DataExportReportObject
	 */
	public DataExportReportObject getDataset() {
		return dataExport;
	}
	
	/**
	 * @param dataExport
	 */
	public void setDataset(DataExportReportObject dataExport) {
		this.dataExport = dataExport;
	}
	
	/**
	 * Get the file location of the dataset export.
	 * 
	 * @return String the file location of the dataset export
	 */
	public String getDatasetLocation() {
		return datasetLocation;
	}
	
	/**
	 * Sets the location of the dataset export.
	 * 
	 * @param datasetLocation
	 */
	public void setDatasetLocation(String datasetLocation) {
		this.datasetLocation = datasetLocation;
	}
	
}
