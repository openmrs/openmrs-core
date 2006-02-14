package org.openmrs.oldreporting;

import java.util.Date;


public class CalendarClassifier implements DataRowClassifier {

	public enum Method { YEAR, MONTH, DAY }
	
	private String dataItemName;
	private Method method;
	
	public CalendarClassifier(String dataItemName, Method method) {
		this.dataItemName = dataItemName;
		this.method = method;
	}
	
	public Date classify(DataRow row) {
		Date date = (Date) row.get(dataItemName);
		if (method == Method.YEAR) {
			return new Date(date.getYear(), 0, 1);
		} else if (method == Method.MONTH) {
			return new Date(date.getYear(), date.getMonth(), 1);
		} else {
			return new Date(date.getYear(), date.getMonth(), date.getDay());
		}
	}

}
