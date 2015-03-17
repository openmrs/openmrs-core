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
	
	public static final TransformOperator LAST = new Last();
	
	public static final TransformOperator FIRST = new First();
	
	public static final TransformOperator DISTINCT = new Distinct();
	
	public static final TransformOperator EXISTS = new Exists();
	
	public static final TransformOperator NOT_EXISTS = new NotExists();
	
	public static final TransformOperator COUNT = new Count();
	
	public static final TransformOperator AVERAGE = new Average();
	
}
