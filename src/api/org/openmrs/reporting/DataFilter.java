package org.openmrs.reporting;

public interface DataFilter<T> {

	public DataSet<T> filter(DataSet<T> input);
	
}
