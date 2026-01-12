/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl

import org.openmrs.GlobalProperty
import org.openmrs.api.GlobalPropertyListener
import org.openmrs.util.LocaleUtility
import org.openmrs.util.OpenmrsConstants
import java.util.Locale

/**
 * A utility class which caches the current list of allowed locales, rebuilding the list whenever
 * the global properties are updated.
 */
class GlobalLocaleList : GlobalPropertyListener {
    
    var allowedLocales: Set<Locale>? = null
        private set
    
    /**
     * @see org.openmrs.api.GlobalPropertyListener.globalPropertyChanged
     */
    override fun globalPropertyChanged(newValue: GlobalProperty) {
        allowedLocales = newValue.propertyValue
            .split(",")
            .mapNotNull { LocaleUtility.fromSpecification(it.trim()) }
            .toSet()
    }
    
    /**
     * @see org.openmrs.api.GlobalPropertyListener.globalPropertyDeleted
     */
    override fun globalPropertyDeleted(propertyName: String) {
        allowedLocales = null
    }
    
    /**
     * @see org.openmrs.api.GlobalPropertyListener.supportsPropertyName
     */
    override fun supportsPropertyName(propertyName: String): Boolean {
        return OpenmrsConstants.GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST == propertyName
    }
}
