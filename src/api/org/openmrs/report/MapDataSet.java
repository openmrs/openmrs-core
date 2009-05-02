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
