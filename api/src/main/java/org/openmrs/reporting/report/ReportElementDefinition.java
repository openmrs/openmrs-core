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

import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.data.CohortDefinition;
import org.openmrs.reporting.data.DatasetDefinition;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class ReportElementDefinition extends AbstractReportObject implements Serializable {
	
	/* */
	private static final long serialVersionUID = 4723646227057635256L;
	
	/* */
	public DatasetDefinition datasetDefinition = new DatasetDefinition();
	
	/* */
	public CohortDefinition cohortDefinition = new CohortDefinition();
	
	/**
	 * TODO Auto generated method comment
	 * 
	 * @return CohortDefinition the CohortDefinition of this Report Element Definition
	 */
	public CohortDefinition getCohortDefinition() {
		return cohortDefinition;
	}
	
	/**
	 * @param cohortDefinition
	 */
	public void setCohortDefinition(CohortDefinition cohortDefinition) {
		this.cohortDefinition = cohortDefinition;
	}
	
	/**
	 * TODO Auto generated method comment
	 * 
	 * @return DatasetDefinition the DatasetDefinition of this Report Element Definition
	 */
	public DatasetDefinition getDatasetDefinition() {
		return datasetDefinition;
	}
	
	/**
	 * @param datasetDefinition
	 */
	public void setDatasetDefinition(DatasetDefinition datasetDefinition) {
		this.datasetDefinition = datasetDefinition;
	}
	
}
