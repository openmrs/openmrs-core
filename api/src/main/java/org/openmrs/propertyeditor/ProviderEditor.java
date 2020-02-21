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

import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Allows for serializing/deserializing a provider to a string so that Spring knows how to pass
 * a provider back and forth through an html form or other medium. <br>
 * 
 * @see Provider
 * @since 1.10.0
 */
public class ProviderEditor extends PropertyEditorSupport {
	
	private static final Logger log = LoggerFactory.getLogger(ProviderEditor.class);
	
	public ProviderEditor() {
	}
	
	/**
	 * <strong>Should</strong> set using id
	 * <strong>Should</strong> set using uuid
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		ProviderService ps = Context.getProviderService();
		if (StringUtils.hasText(text)) {
			try {
				setValue(ps.getProvider(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				Provider p = ps.getProviderByUuid(text);
				setValue(p);
				if (p == null) {
					log.error("Error setting provider with id or uuid: " + text, ex);
					throw new IllegalArgumentException("Provider not found: " + ex.getMessage());
				}
			}
		} else {
			setValue(null);
		}
	}
	
	@Override
	public String getAsText() {
		Provider p = (Provider) getValue();
		if (p == null) {
			return "";
		} else {
			return p.getProviderId().toString();
		}
	}
	
}
