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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

/**
 * Allows for serializing/deserializing a provider to a string so that Spring knows how to pass
 * a provider back and forth through an html form or other medium. <br/>
 * 
 * @see Provider
 * @since 1.10.0
 */
public class ProviderEditor extends PropertyEditorSupport {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public ProviderEditor() {
	}
	
	/**
	 * @should set using id
	 * @should set using uuid
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
