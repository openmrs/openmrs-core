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
 * The First operator will return a subset result of the entire result returned by the criteria<br />
 * <br />
 * Example: <br />
 * - <code>logicService.parse("'CD4 COUNT'").first(2);</code><br />
 * The above will give us a criteria to get the first two "CD4 COUNT" observations
 * 
 * @see Last
 */
public class First implements TransformOperator {
	
	public String toString() {
		return "FIRST";
	}
	
}
