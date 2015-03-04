/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class DateColumnClassifier implements TableRowClassifier {
	
	public enum CombineMethod {
		YEAR("yyyy"), MONTH("yyyy-MM"), DAY("yyyy-MM-dd");
		
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
		if (value == null) {
			return valueIfNull;
		} else {
			return combineMethod.getDateFormat().format((Date) value);
		}
	}
	
}
