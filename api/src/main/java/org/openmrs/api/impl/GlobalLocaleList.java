/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;

/**
 * A utility class which caches the current list of allowed locales, rebuilding the list whenever
 * the global properties are updated.
 */
public class GlobalLocaleList implements GlobalPropertyListener {
	
	private Set<Locale> allowedLocales = null;
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyChanged(org.openmrs.GlobalProperty)
	 */
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		allowedLocales = new LinkedHashSet<>();
		for (String allowedLocaleString : newValue.getPropertyValue().split(",")) {
			Locale allowedLocale = LocaleUtility.fromSpecification(allowedLocaleString.trim());
			if (allowedLocale != null) {
				allowedLocales.add(allowedLocale);
			}
		}
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyDeleted(java.lang.String)
	 */
	@Override
	public void globalPropertyDeleted(String propertyName) {
		allowedLocales = null;
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#supportsPropertyName(java.lang.String)
	 */
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST.equals(propertyName);
	}
	
	/**
	 * Gets the current list of allowed locales.
	 * 
	 * @return List&lt;Locale&gt; object with allowed Locales defined by the administrator
	 */
	public Set<Locale> getAllowedLocales() {
		return allowedLocales;
	}
	
}
