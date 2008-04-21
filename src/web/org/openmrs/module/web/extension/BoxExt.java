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

public abstract class BoxExt extends Extension {
	
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	/**
	 * The title is used as the title for the boxHeader.
	 * The returned String can be either straight up plain
	 * text or a Spring message code.
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
	 * Returns the required privilege in order to see this section.  Can be a 
	 * comma delimited list of privileges.  
	 * If the default empty string is returned, only an authenticated 
	 * user is required
	 * 
	 * @return Privilege string
	 */
	public String getRequiredPrivilege() {
		return "";
	}	
}
