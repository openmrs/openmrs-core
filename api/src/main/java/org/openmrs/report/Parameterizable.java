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

import java.io.Serializable;
import java.util.List;

/**
 * A Cohort, ReportSchema, and Logic rules can be parameterizable. If a class extends this, it means
 * that it can use Parameters and possible have those filled out by its parent. (Meaning a parameter
 * in a cohort that is in a reportschema can give it that value) The object should assume that when
 * its being run (getPatientIds() for cohorts, getColumns for DataSetDefinition(), etc) it will
 * receive an EvaluationContext which will give values to all of its parameters
 * 
 * @deprecated see reportingcompatibility module
 */
@Deprecated
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
