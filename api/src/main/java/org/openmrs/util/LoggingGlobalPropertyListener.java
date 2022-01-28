/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;

/**
 * This is a GlobalPropertyListener that updates logging levels whenever the log.level setting is updated
 *
 * @since 2.4
 */
public class LoggingGlobalPropertyListener implements GlobalPropertyListener {

    /**
     * @see GlobalPropertyListener#supportsPropertyName(String)
     */
    @Override
    public boolean supportsPropertyName(String propertyName) {
        return OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL.equals(propertyName);
    }

    /**
     * @see GlobalPropertyListener#globalPropertyChanged(GlobalProperty)
     */
    @Override
    public void globalPropertyChanged(GlobalProperty newValue) {
        OpenmrsUtil.applyLogLevels();
    }

    /**
     * @see GlobalPropertyListener#globalPropertyDeleted(String)
     */
    @Override
    public void globalPropertyDeleted(String propertyName) {
        OpenmrsUtil.applyLogLevels();
    }
}