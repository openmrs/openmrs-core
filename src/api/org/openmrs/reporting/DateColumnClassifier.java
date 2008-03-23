/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
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
