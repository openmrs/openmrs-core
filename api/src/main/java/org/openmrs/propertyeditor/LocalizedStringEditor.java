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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.LocalizedString;
import org.openmrs.util.LocalizedStringUtil;

/**
 * Used to get/set the attribute(which is of type {@link LocalizedString}) of any object.
 * 
 * @since 1.9
 */
public class LocalizedStringEditor extends PropertyEditorSupport {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public LocalizedStringEditor() {
	}
	
	/**
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 * @should set value to the localized string object with the specified string
	 * @should set value to null if given empty string
	 * @should set value to null if given null value
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		log.debug("setting text: " + text);
		if (StringUtils.isBlank(text))
			setValue(null);
		else
			setValue(LocalizedString.valueOf(text));
	}
	
	/**
	 * @see java.beans.PropertyEditorSupport#getAsText()
	 * @should return a serialized string when editor has a value
	 * @should return empty string when editor has a null value
	 */
	@Override
	public String getAsText() {
		LocalizedString localizedString = (LocalizedString) getValue();
		if (localizedString == null)
			return "";
		else
			return LocalizedStringUtil.serialize(localizedString);
	}
	
}
