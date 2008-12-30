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
package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;

/**
 * A utility class which caches the current list of allowed locales, rebuilding the list whenever
 * the global properties are updated.
 */
public class GlobalLocaleList implements GlobalPropertyListener {
	
	private List<Locale> allowedLocales = null;
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyChanged(org.openmrs.GlobalProperty)
	 */
	public void globalPropertyChanged(GlobalProperty newValue) {
		allowedLocales = new ArrayList<Locale>();
		for (String allowedLocaleString : newValue.getPropertyValue().split(",")) {
			try {
				Locale allowedLocale = LocaleUtility.fromSpecification(allowedLocaleString.trim());
				if (allowedLocale != null) {
					allowedLocales.add(allowedLocale);
				}
			}
			catch (Exception e) {
				// bad locale spec? just ignore it. the UI should take care of
				// guiding the user.
			}
		}
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyDeleted(java.lang.String)
	 */
	public void globalPropertyDeleted(String propertyName) {
		allowedLocales = new ArrayList<Locale>();
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#supportsPropertyName(java.lang.String)
	 */
	public boolean supportsPropertyName(String propertyName) {
		return OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST.equals(propertyName);
	}
	
	/**
	 * Gets the current list of allowed locales.
	 * 
	 * @return
	 */
	public List<Locale> getAllowedLocales() {
		if (allowedLocales == null) {
			allowedLocales = new ArrayList<Locale>();
			allowedLocales.add(Locale.ENGLISH);
		}
		return allowedLocales;
	}
	
}
