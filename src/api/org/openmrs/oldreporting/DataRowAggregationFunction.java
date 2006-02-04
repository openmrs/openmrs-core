package org.openmrs.oldreporting;

import java.util.Collection;

public interface DataRowAggregationFunction {

	public Object aggregate(Collection<DataRow> rows);

}
