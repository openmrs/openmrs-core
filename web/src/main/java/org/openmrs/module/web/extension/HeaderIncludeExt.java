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
