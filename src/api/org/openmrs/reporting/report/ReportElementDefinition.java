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
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.data.DatasetDefinition;
import org.openmrs.reporting.data.CohortDefinition;

public class ReportElementDefinition extends AbstractReportObject implements Serializable {

	/* */
	private static final long serialVersionUID = 4723646227057635256L;

	/* */
	public DatasetDefinition datasetDefinition = new DatasetDefinition();
	
	/* */
	public CohortDefinition cohortDefinition = new CohortDefinition();

	/**
	 * 
	 * @return
	 */
	public CohortDefinition getCohortDefinition() { 
		return cohortDefinition;		
	}
	
	/**
	 * 
	 * @param cohortDefinition
	 */
	public void setCohortDefinition(CohortDefinition cohortDefinition) { 
		this.cohortDefinition = cohortDefinition;
	}

	/**
	 * 
	 * @return
	 */
	public DatasetDefinition getDatasetDefinition() { 
		return datasetDefinition;		
	}
	
	/**
	 * 
	 * @param datasetDefinition
	 */
	public void setDatasetDefinition(DatasetDefinition datasetDefinition) { 
		this.datasetDefinition = datasetDefinition;
	}
	
}
