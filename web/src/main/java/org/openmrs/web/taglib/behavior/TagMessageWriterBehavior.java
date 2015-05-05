/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
