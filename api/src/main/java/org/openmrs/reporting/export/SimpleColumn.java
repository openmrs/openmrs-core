/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting.export;

import java.io.Serializable;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class SimpleColumn implements ExportColumn, Serializable {
	
	public static final long serialVersionUID = 987654322L;
	
	private String columnType = "simple";
	
	private String columnName = "";
	
	private String returnValue = "";
	
	public SimpleColumn() {
	}
	
	public SimpleColumn(String columnName, String columnValue) {
		this.columnName = columnName;
		returnValue = columnValue;
	}
	
	public String toTemplateString() {
		return returnValue;
	}
	
	public String getColumnType() {
		return columnType;
	}
	
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
	
	public String getTemplateColumnName() {
		return columnName;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
	public String getReturnValue() {
		return returnValue;
	}
	
	public void setReturnValue(String returnValue) {
		this.returnValue = returnValue;
	}
	
}
