package org.openmrs.oldreporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class GroupingSeriesTransformer implements SeriesTransformer {

	private String keyTitle;
	private String valueTitle;
	private DataRowClassifier classifier;
	private DataRowAggregationFunction aggregation;
	
	public GroupingSeriesTransformer() { }
	
	public GroupingSeriesTransformer(String keyTitle, String valueTitle, DataRowClassifier classifier, DataRowAggregationFunction aggregation) {
		this.keyTitle = keyTitle;
		this.valueTitle = valueTitle;
		this.classifier = classifier;
		this.aggregation = aggregation;
	}


	public DataSeries transform(DataTable input) {
		Map<Object, List<DataRow>> map = new HashMap<Object, List<DataRow>>();
		for (DataRow row : input.getRows()) {
			Object classification = classifier.classify(row);
			List<DataRow> temp = map.get(classification);
			if (temp == null) {
				temp = new ArrayList<DataRow>();
				map.put(classification, temp);
			}
			temp.add(row);
		}
		boolean canSort = true;
		for (Object o : map.keySet()) {
			if (!(o instanceof Comparable)) {
				canSort = false;
				break;
			}
		}
		Map ret = canSort ? new TreeMap() : new HashMap();
		for (Map.Entry<Object, List<DataRow>> e : map.entrySet()) {
			List<DataRow> rows = e.getValue();
			Object o = aggregation.aggregate(rows);
			ret.put(e.getKey(), o);
		}
		DataSeries series = new DataSeries(keyTitle, valueTitle);
		series.setData(ret);
		return series;
	}

}
