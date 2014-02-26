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
