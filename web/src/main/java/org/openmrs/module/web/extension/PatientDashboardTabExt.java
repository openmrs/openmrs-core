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

public abstract class PatientDashboardTabExt extends Extension {
	
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	/**
	 * The visible name of this tab. This can return either a string or a spring message code
	 * 
	 * @return
	 */
	public abstract String getTabName();
	
	/**
	 * Tabs should be named uniquely. (The id will probably just be the name without spaces)
	 * 
	 * @return
	 */
	public abstract String getTabId();
	
	/**
	 * Tab views are controlled by privileges. The user must have this privilege to be able to view
	 * this tab
	 * 
	 * @return
	 */
	public abstract String getRequiredPrivilege();
	
	/**
	 * Get the name of the portlet that will be imported for the tab content
	 * 
	 * @return html code
	 */
	public abstract String getPortletUrl();
	
	/**
	 * This extension does not have a place for any default content to go
	 */
	@Override
	public final String getOverrideContent(String bodyContent) {
		return null;
	}
	
}
