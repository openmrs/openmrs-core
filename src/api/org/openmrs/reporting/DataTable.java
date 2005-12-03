package org.openmrs.reporting;

import java.util.List;
import java.util.Map;

/**
 * This might no longer be necessary, because of PatientDataSet
 * @author djazayeri
 */
public interface DataTable {

	public List<String> getColumnNames();

	public int getRowCount();
	
	public Map<String, Object> getRow(int rowNum);

	public List getColumn(String columnName);
	
}
