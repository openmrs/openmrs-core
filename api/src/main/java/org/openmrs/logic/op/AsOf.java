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
 * The AsOf operator works with a date object to test whether an expression will yield result after
 * a certain date position<br>
 * <br>
 * Example: <br>
 * -
 * <code>logicService.parse("'CD4 COUNT'").asOf(Context.getDateformat().parse("2009/12/04");</code><br>
 * The above will give us a criteria to check if there's "CD4 COUNT" observations as of 12/04/2009
 */
public class AsOf implements Operator {
	
	@Override
	public String toString() {
		return "AS OF";
	}
	
}
