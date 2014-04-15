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
package org.openmrs.web.filter.util;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.generic.ResourceTool;
import org.openmrs.util.LocaleUtility;

/**
 * This class is intended for accessing {@link ResourceBundle} and formatting messages therein.
 */
@DefaultKey("l10n")
public class LocalizationTool extends ResourceTool {
	
	/**
	 * The default message resource bundle to use, this is english
	 */
	private static ResourceBundle defaultResourceBundle = null;
	
	/**
	 * Its need to override base class method to be able to change its locale property outside the
	 * class hierarchy
	 *
	 * @see org.apache.velocity.tools.generic.ResourceTool#setLocale(Locale locale)
	 */
	@Override
	public void setLocale(Locale locale) {
		super.setLocale(locale);
	}
	
	/**
	 * @return the defaultResourceBundle
	 */
	public static ResourceBundle getDefaultResourceBundle() {
		if (defaultResourceBundle == null) {
			defaultResourceBundle = CustomResourceLoader.getInstance(null).getResourceBundle(Locale.ENGLISH);
		}
		return defaultResourceBundle;
	}
	
	/**
	 * To be able to load resource bundles outside the class path we need to override this method
	 *
	 * @see org.apache.velocity.tools.generic.ResourceTool#getBundle(java.lang.String,
	 *      java.lang.Object)
	 */
	@Override
	protected ResourceBundle getBundle(String baseName, Object loc) {
		Locale locale = (loc == null) ? getLocale() : LocaleUtility.fromSpecification(String.valueOf(loc));
		if (baseName == null || locale == null) {
			return null;
		}
		//This messages_XX.properties file doesn't exist, default to messages.properties
		ResourceBundle rb = CustomResourceLoader.getInstance(null).getResourceBundle(locale);
		if (rb == null) {
			rb = getDefaultResourceBundle();
		}
		
		return rb;
	}
	
	/**
	 * @see org.apache.velocity.tools.generic.ResourceTool#get(java.lang.Object, java.lang.String[],
	 *      java.lang.Object)
	 */
	@Override
	public Object get(Object code, String[] resourceNamePrefixes, Object locale) {
		Object msg = super.get(code, resourceNamePrefixes, locale);
		//if code's translation is blank, use the english equivalent
		if (msg == null || StringUtils.isBlank(msg.toString())) {
			msg = super.get(code, resourceNamePrefixes, Locale.ENGLISH.toString());
		}
		
		return msg;
	}
}
