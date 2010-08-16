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
package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ApproximateDate;
import org.springframework.util.StringUtils;

/**
 * TODO comment!
 */
public class ApproximateDateEditor extends PropertyEditorSupport {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private SimpleDateFormat dateFormat = null;
	
	public ApproximateDateEditor() {
	}
	
	public ApproximateDateEditor(SimpleDateFormat sdf) {
		this.dateFormat = sdf;
	}
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			try {
				Date d = this.dateFormat.parse(text);
				ApproximateDate ad = new ApproximateDate(d, 0);
				setValue(ad);
			}
			catch (Exception ex) {
				log.error("Error setting text" + text, ex);
			}
		} else {
			setValue(null);
		}
	}
	
	@Override
	public String getAsText() {
		ApproximateDate d = (ApproximateDate) getValue();
		if (d == null)
			return "";
		return this.dateFormat.format(d.getDate());
	}
		
}
