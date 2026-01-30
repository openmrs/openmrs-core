/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate.search.bridge;

import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;
import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeFromIndexedValueContext;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;

/**
 * Indexes {@link java.util.Locale} as string.
 * 
 * @since 2.8.0
 */
public class LocaleValueBridge implements ValueBridge<Locale, String> {

	@Override
	public String toIndexedValue(Locale locale, ValueBridgeToIndexedValueContext valueBridgeToIndexedValueContext) {
		return locale.toString();
	}

	@Override
	public Locale fromIndexedValue(String value, ValueBridgeFromIndexedValueContext context) {
		return value == null ? null : LocaleUtils.toLocale(value);
	}

	@Override
	public String parse(String value) {
		return LocaleUtils.toLocale(value).toString();
	}
}
