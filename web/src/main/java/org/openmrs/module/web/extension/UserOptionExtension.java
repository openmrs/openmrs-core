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
