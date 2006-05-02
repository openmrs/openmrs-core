package org.openmrs.oldreporting;

import java.util.List;

public interface DataProducer<T> {

	/**
	 * @return the input DataSet
	 */
	public <U extends T> DataSet<U> produceData(DataSet<U> dataSet);
	
	public List<String> columnsProduced();
	
	public List<String> columnsToDisplay();
	
}
