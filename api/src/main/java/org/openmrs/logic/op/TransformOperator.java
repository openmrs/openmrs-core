/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logic.op;

/**
 * Marker for all transform operator.
 */
public interface TransformOperator extends Operator {
	
	TransformOperator LAST = new Last();
	
	TransformOperator FIRST = new First();
	
	TransformOperator DISTINCT = new Distinct();
	
	TransformOperator EXISTS = new Exists();
	
	TransformOperator NOT_EXISTS = new NotExists();
	
	TransformOperator COUNT = new Count();
	
	TransformOperator AVERAGE = new Average();
	
}
