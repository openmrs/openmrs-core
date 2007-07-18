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
