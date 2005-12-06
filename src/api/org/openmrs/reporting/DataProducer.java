package org.openmrs.reporting;

public interface DataProducer<T> {

	/**
	 * @return the input DataSet
	 */
	public <U extends T> DataSet<U> produceData(DataSet<U> dataSet);
	
}
