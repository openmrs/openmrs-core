/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib;

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;

public class AuthTag extends TagSupport {
	
	public static final long serialVersionUID = 11233L;
	
	private final Log log = LogFactory.getLog(getClass());
	
	public int doStartTag() {
		log.debug("setting authenticatedUser with value: " + Context.getAuthenticatedUser());
		
		// sets a null value when not authenticated
		pageContext.setAttribute("authenticatedUser", Context.getAuthenticatedUser());
		
		return EVAL_BODY_INCLUDE;
	}
	
}
