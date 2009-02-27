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

public class SimpleColumnClassifier implements TableRowClassifier {
	
	private String columnName;
	
	private String valueIfNull;
	
	public SimpleColumnClassifier(String columnName, String valueIfNull) {
		this.columnName = columnName;
		this.valueIfNull = valueIfNull;
	}
	
	public String classify(TableRow row) {
		Object temp = row.get(columnName);
		return temp == null ? valueIfNull : temp.toString();
	}
	
}
