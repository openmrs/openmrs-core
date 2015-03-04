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

public abstract class AdministrationSectionExt extends Extension {
	
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	/**
	 * The title is used as the larger text above the links on the admin screen. The returned String
	 * can be either straight up plain text or a Spring message code.
	 * 
	 * @return String title
	 */
	public abstract String getTitle();
	
	/**
	 * Returns the required privilege in order to see this section. Can be a comma delimited list of
	 * privileges. If the default empty string is returned, only an authenticated user is required
	 * 
	 * @return Privilege string
	 */
	public String getRequiredPrivilege() {
		return "";
	}
	
	/**
	 * The links are appear under the <code>getTitle<code> heading on the 
	 * admin screen.  Links can be either absolute or relative.  Title of the 
	 * links can be either plain text or Spring message codes.
	 * 
	 * In order to sort the links, you should use a <code>LinkedHashMap</code>.
	 * 
	 * @return Map<String, String> of <link, title>
	 */
	public abstract Map<String, String> getLinks();
	
}
