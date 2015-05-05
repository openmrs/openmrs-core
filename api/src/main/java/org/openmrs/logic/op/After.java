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
 * The After operator works with a date object to tests whether an expression will yield result
 * after a certain date position.<br />
 * <br />
 * Example: <br />
 * -
 * <code>logicService.parse("'CD4 COUNT'").after(Context.getDateformat().parse("2009/12/04");</code><br />
 * The above will give us a criteria to check if there's "CD4 COUNT" observations after 12/04/2009
 * 
 * @see Before
 */
public class After implements ComparisonOperator {
	
	public String toString() {
		return "AFTER";
	}
	
}
