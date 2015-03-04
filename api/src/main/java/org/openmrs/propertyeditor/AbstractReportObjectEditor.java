/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.AbstractReportObject;
import org.springframework.util.StringUtils;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class AbstractReportObjectEditor extends PropertyEditorSupport {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * Public constructor
	 */
	public AbstractReportObjectEditor() {
	}
	
	/**
	 * 
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		log.debug("Setting report object text " + text);
		if (StringUtils.hasText(text)) {
			try {
				setValue(Context.getReportObjectService().getReportObject(Integer.valueOf(text)));
				log.debug("value: " + getValue());
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("Report object not found: " + ex.getMessage());
			}
		} else {
			setValue(null);
		}
	}
	
	/**
	 * 
	 */
	public String getAsText() {
		log.debug("Getting cohort text " + getValue());
		String text = "";
		AbstractReportObject obj = (AbstractReportObject) getValue();
		if (obj != null && obj.getReportObjectId() != null) {
			text = String.valueOf(obj.getReportObjectId());
		}
		log.debug("Text: " + text);
		return text;
	}
	
}
