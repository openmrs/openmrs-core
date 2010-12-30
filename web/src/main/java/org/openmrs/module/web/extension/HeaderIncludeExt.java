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

import java.util.List;

import org.openmrs.module.Extension;

/**
 * The header include extension allows a module developer to include extra files (css, javascript)
 * within the header of every page. This could be used to include a new javascript library, CSS
 * styles, etc. In order to include headers, a module developer should implement the
 * getHeaderFiles() method and return a collection of file names ("myjavascript.js", "mycss.css")
 * that reside in the modules resources directory (i.e. "web/module/resources").
 */
public abstract class HeaderIncludeExt extends Extension {
	
	/**
	 * @see org.openmrs.module.Extension#getMediaType()
	 */
	@Override
	public MEDIA_TYPE getMediaType() {
		return MEDIA_TYPE.html;
	}
	
	/**
	 * Returns references to header files in the module resource directory.
	 * 
	 * @return a collection of header files
	 */
	public abstract List<String> getHeaderFiles();
	
}
