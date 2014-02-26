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
package org.openmrs.web.taglib.behavior;

import org.openmrs.web.taglib.OpenmrsMessageTag;

/**
 * Represents interface of JSTL tag output writing behavior.
 */
public interface TagMessageWriterBehavior {
	
	/**
	 * Implementations should override this method to customize how translated messages are rendered on JSP pages via
	 * {@link OpenmrsMessageTag}. They may enclose passed in resolvedText with HTML span tags, for example.
	 * 
	 * @param resolvedText
	 *            the resolved message string to be customized
	 * @param code
	 *            the code (key) used when looking up the message
	 * @param locale
	 *            the locale of the fallback message text provided by the tag
	 * @param fallbackText
	 *            the fallback text provided by tag if given code is not specified or can not be resolved
	 * @return customization created off passed in tag attributes.
	 */
	public String renderMessage(String resolvedText, String code, String locale, String fallbackText);
	
}
