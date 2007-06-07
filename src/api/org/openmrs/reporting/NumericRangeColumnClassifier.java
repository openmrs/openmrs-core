package org.openmrs.reporting;


public class NumericRangeColumnClassifier implements TableRowClassifier {

	private String columnName;
	private double[] points; // { 15, 40, 80 } means <15, 15-40, 40-80, >80
	private String[] labels;
	private String missingLabel;
	
	public NumericRangeColumnClassifier(String columnName, String missingLabel) {
		this.columnName = columnName;
		this.missingLabel = missingLabel;
		points = new double[0];
		labels = new String[0];
	}
	
	public void addCutoff(Number num, String label) {
		double lastPoint = 0;
		if (points.length > 0) {
			lastPoint = points[points.length - 1];
			if (num.doubleValue() <= lastPoint)
				throw new IllegalArgumentException("The cutoff you're trying to add (" + num + ") is <= the last cutoff (" + lastPoint + ")");
		}
		if (label == null) {
			if (points.length == 0)
				label = "< " + num;
			else
				label = "[" + lastPoint + ", " + num + ")";
		}

		double[] temp = new double[points.length + 1];
		for (int i = 0; i < points.length; ++i)
			temp[i] = points[i];
		temp[points.length] = num.doubleValue();
		points = temp;

		String[] temp2 = new String[labels.length + 1];
		for (int i = 0; i < labels.length; ++i)
			temp2[i] = labels[i];
		temp2[labels.length] = label;
		labels = temp2;
	}
	
	public void addLastLabel(String label) {
		String[] temp = new String[labels.length + 1];
		for (int i = 0; i < labels.length; ++i)
			temp[i] = labels[i];
		temp[labels.length] = label;
		labels = temp;
	}
	
	public String classify(TableRow row) {
		Object o = row.get(columnName);
		if (o == null)
			return missingLabel;
		double value;
		if (o instanceof Number)
			value = ((Number) o).doubleValue();
		else
			value = Double.valueOf(o.toString());
		if (o instanceof Number)
		for (int i = 0; i < points.length; ++i) {
			if (value < points[i])
				return labels[i];
		}
		// TODO: if labels.length == points.length, then add a default last label
		return labels[points.length];
	}

}
