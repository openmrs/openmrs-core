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
import org.openmrs.module.web.extension.provider.Link;
import org.openmrs.module.web.extension.provider.LinkProvider;

/**
 *
 */
public abstract class LinkProviderExtension extends Extension implements LinkProvider {
	
	public abstract List<Link> getLinks();
	
	/**
	 * @see org.openmrs.module.Extension#getMediaType()
	 */
	public MEDIA_TYPE getMediaType() {
		return MEDIA_TYPE.html;
	}
	
}
