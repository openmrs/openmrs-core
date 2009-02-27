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
