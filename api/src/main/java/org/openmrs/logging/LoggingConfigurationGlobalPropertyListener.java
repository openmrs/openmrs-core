/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logging;

import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.util.OpenmrsConstants;

/**
 * This is a GlobalPropertyListener that updates logging levels whenever any of the log-related settings supported by core
 * are updated.
 * <p/>
 * Note that changing <tt>log.level</tt> will result in an in-memory change to the logger (which should be preserved across
 * reloads of logging configuration). Modifying other settings will result in the logging configuration being fully reloaded,
 * which may result in log-file rollovers, etc.
 * 
 * @since 2.4
 */
public class LoggingConfigurationGlobalPropertyListener implements GlobalPropertyListener {
	
	private volatile String logLayout = null;
	
	private volatile String logLocation = null;

	/**
	 * @see GlobalPropertyListener#supportsPropertyName(String) 
	 */
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return
			OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL.equals(propertyName) ||
			OpenmrsConstants.GP_LOG_LAYOUT.equals(propertyName) ||
			OpenmrsConstants.GP_LOG_LOCATION.equals(propertyName);
	}

	/**
	 * @see GlobalPropertyListener#globalPropertyChanged(GlobalProperty) 
	 */
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		switch (newValue.getProperty()) {
			case OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL:
				OpenmrsLoggingUtil.applyLogLevels();
				return;
			case OpenmrsConstants.GP_LOG_LAYOUT:
				if (logLayout != null && logLayout.equals(newValue.getPropertyValue())) {
					return;
				}
				
				logLayout = newValue.getPropertyValue();
				OpenmrsLoggingUtil.reloadLoggingConfiguration();
				return;
			case OpenmrsConstants.GP_LOG_LOCATION:
				if (logLocation != null && logLocation.equals(newValue.getPropertyValue())) {
					return;
				}
				
				logLocation = newValue.getPropertyValue();
				OpenmrsLoggingUtil.reloadLoggingConfiguration();
		}
	}

	/**
	 * @see GlobalPropertyListener#globalPropertyDeleted(String) 
	 */
	@Override
	public void globalPropertyDeleted(String propertyName) {
		switch (propertyName) {
			case OpenmrsConstants.GP_LOG_LAYOUT:
				logLayout = null;
				break;
			case OpenmrsConstants.GP_LOG_LOCATION:
				logLocation = null;
		}
		
		OpenmrsLoggingUtil.reloadLoggingConfiguration();
	}
}
