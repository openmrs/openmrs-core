package org.openmrs.reporting.data;

import java.io.Serializable;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.export.DataExportReportObject;

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
	 * 
	 * @return
	 */
	public DataExportReportObject getDataset() { 
		return dataExport;
	}
	
	/**
	 * 
	 * @param dataExport
	 */
	public void setDataset(DataExportReportObject dataExport) { 
		this.dataExport = dataExport;
	}
	
	
	/**
	 * Get the file location of the dataset export.
	 * @return
	 */
	public String getDatasetLocation() { 
		return datasetLocation;
	}
	
	/**
	 * Sets the location of the dataset export.
	 * @param datasetLocation
	 */
	public void setDatasetLocation(String datasetLocation) { 
		this.datasetLocation = datasetLocation;
	}
	
}
