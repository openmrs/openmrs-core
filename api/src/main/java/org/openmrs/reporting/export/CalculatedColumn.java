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
public class CalculatedColumn extends SimpleColumn implements ExportColumn, Serializable {
	
	public static final long serialVersionUID = 987654324L;
	
	public CalculatedColumn() {
		super();
		setColumnType("calculated");
	}
	
	public CalculatedColumn(String columnName, String columnValue) {
		super(columnName, columnValue);
		setColumnType("calculated");
	}
	
}
