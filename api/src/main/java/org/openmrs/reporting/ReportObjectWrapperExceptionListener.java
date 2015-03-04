/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting;

import java.beans.ExceptionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class ReportObjectWrapperExceptionListener implements ExceptionListener {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public ReportObjectWrapperExceptionListener() {
		// do nothing
	}
	
	public void exceptionThrown(Exception e) {
		log.debug("Error deserializing report object: ", e);
	}
	
}
