/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.report;

import java.util.Map;

/**
 * DataSet which is key-value pairs, instead of a full two-dimensional table
 * 
 * @deprecated see reportingcompatibility module
 */
@SuppressWarnings("unchecked")
@Deprecated
public interface MapDataSet<T extends Object> extends DataSet {
	
	Map<String, T> getData();
	
}
