package org.openmrs.oldreporting;


public interface SeriesTransformer<K, V> {

	public DataSeries<K, V> transform(DataTable input);
	
}
