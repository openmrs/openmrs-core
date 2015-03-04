/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.web.extension;

import java.util.Map;

import org.openmrs.module.Extension;

public abstract class TableRowExt extends Extension {
	
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	/**
	 * The map returns a listing of the rows to add to a table. The map key will be attempted to be
	 * used as a Spring message. The map value will be the html to insert into the table cell In
	 * order to sort the links, you should use a <code>LinkedHashMap</code>.
	 * 
	 * @return Map<String, String> of <label for cell, cell content>
	 */
	public abstract Map<String, String> getRows();
	
}
