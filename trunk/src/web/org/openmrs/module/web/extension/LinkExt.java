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

public abstract class LinkExt extends Extension {
	
	public MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}

	/**
	 * @return The message code of the label of this link
	 */
	public abstract String getLabel();

	/**
	 * @return The url that this link should go to
	 */
	public abstract String getUrl();
	
	/**
	 * @return The privilege the user must have to see this link
	 */
	public abstract String getRequiredPrivilege();

	/**
	 * This extension does not have a place for any default content to go
	 */
	public final String getOverrideContent(String bodyContent) {
		return null;
	}

}
