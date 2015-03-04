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

public abstract class UserOptionExtension extends PortletExt {
	
	/**
	 * page views are controlled by privileges. The user must have this privilege to be able to view
	 * @return the privilege name.
	 */
	public abstract String getRequiredPrivilege();
	
	/** 
	 * @return the tab unique name without spaces.
	 */
	public abstract String getTabId();
	
	/**
	 * @return The visible name of this tab. This can return either a string or a spring message code
	 */
	public abstract String getTabName();
}
