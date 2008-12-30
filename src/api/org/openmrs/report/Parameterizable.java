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

import java.io.Serializable;
import java.util.List;

/**
 * A Cohort, ReportSchema, and Logic rules can be parameterizable. If a class extends this, it means
 * that it can use Parameters and possible have those filled out by its parent. (Meaning a parameter
 * in a cohort that is in a reportschema can give it that value) The object should assume that when
 * its being run (getPatientIds() for cohorts, getColumns for DataSetDefinition(), etc) it will
 * receive an EvaluationContext which will give values to all of its parameters
 */
public interface Parameterizable extends Serializable {
	
	/**
	 * A object that can take parameters should examine itself and its child objects to find out all
	 * of its required parameters. This method should not return null, but should return an empty
	 * list if no parameters exist
	 * 
	 * @return list of parameters that will satisfy this object
	 */
	List<Parameter> getParameters();
	
}
