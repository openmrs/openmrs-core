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

import org.openmrs.module.Extension;

public abstract class BoxExt extends Extension {
	
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	/**
	 * @return The message code of the label of this link
	 */
	public abstract String getPortletUrl();
	
	/**
	 * The title is used as the title for the boxHeader. The returned String can be either straight
	 * up plain text or a Spring message code.
	 * 
	 * @return String title
	 */
	public abstract String getTitle();
	
	/**
	 * Returns the content used for the box.
	 * 
	 * @return Content string
	 */
	public abstract String getContent();
	
	/**
	 * Returns the required privilege in order to see this section. Can be a comma delimited list of
	 * privileges. If the default empty string is returned, only an authenticated user is required
	 * 
	 * @return Privilege string
	 */
	public String getRequiredPrivilege() {
		return "";
	}
}
