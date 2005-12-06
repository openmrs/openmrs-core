package org.openmrs.reporting;

public interface DataProducer<T> {

	/**
	 * @return the input DataSet
	 */
	public DataSet<T> produceData(DataSet<T> dataSet);
	
}
