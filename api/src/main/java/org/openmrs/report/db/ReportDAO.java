/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.report.db;

import java.util.List;

import org.openmrs.report.ReportSchemaXml;

/**
 * The database methods involved with saving objects in the report package to the database
 * 
 * @see org.openmrs.api.ReportService
 * @deprecated see reportingcompatibility module
 */
@Deprecated
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
