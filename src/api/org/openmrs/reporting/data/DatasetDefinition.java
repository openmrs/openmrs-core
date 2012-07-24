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
