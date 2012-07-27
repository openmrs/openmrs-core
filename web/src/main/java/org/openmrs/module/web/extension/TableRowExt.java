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
