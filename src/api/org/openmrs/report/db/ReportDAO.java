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
package org.openmrs.report.db;

import java.util.List;

import org.openmrs.report.ReportSchemaXml;

/**
 * The database methods involved with saving objects in the report package to
 * the database
 * 
 * @see org.openmrs.api.ReportService
 */
public interface ReportDAO {

	/**
	 * @see org.openmrs.api.ReportService#getReportSchemaXml(java.lang.Integer)
	 */
	public ReportSchemaXml getReportSchemaXml(Integer reportSchemaXmlId);

	/**
	 * @see org.openmrs.api.ReportService#saveReportSchemaXml(org.openmrs.report.ReportSchemaXml)
	 */
	public void saveReportSchemaXml(ReportSchemaXml reportSchemaXml);

	/**
	 * @see org.openmrs.api.ReportService#deleteReportSchemaXml(org.openmrs.report.ReportSchemaXml)
	 */
	public void deleteReportSchemaXml(ReportSchemaXml reportSchemaXml);

	/**
     * @see org.openmrs.api.ReportService#getReportSchemaXmls()
     */
    public List<ReportSchemaXml> getReportSchemaXmls();

}
