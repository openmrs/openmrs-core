package org.openmrs.reporting;

public interface DataFilter<T> {

	public <U extends T> DataSet<T> filter(DataSet<U> input);
	
}
