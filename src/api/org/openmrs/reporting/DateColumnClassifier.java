package org.openmrs.reporting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DateColumnClassifier implements TableRowClassifier {
	
	public enum CombineMethod {
		YEAR ("yyyy"),
		MONTH ("yyyy-MM"),
		DAY ("yyyy-MM-dd");

		private DateFormat df;
		
		private CombineMethod(String dateFormat) {
			df = new SimpleDateFormat(dateFormat);
		}
		
		public DateFormat getDateFormat() {
			return df;
		}
	}
	
	private String columnName;
	private CombineMethod combineMethod;
	private String valueIfNull;
	
	public DateColumnClassifier(String columnName, CombineMethod combineMethod, String valueIfNull) {
		this.columnName = columnName;
		this.combineMethod = combineMethod;
		this.valueIfNull = valueIfNull;
	}
	
	public String classify(TableRow row) {
		Object value = row.get(columnName);
		if (value == null)
			return valueIfNull;
		else
			return combineMethod.getDateFormat().format((Date) value);
	}

}
