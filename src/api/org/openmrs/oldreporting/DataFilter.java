package org.openmrs.oldreporting;

public interface DataFilter<T> {

	public <U extends T> DataSet<T> filter(DataSet<U> input);
	
	public String getDescription();
	
}
