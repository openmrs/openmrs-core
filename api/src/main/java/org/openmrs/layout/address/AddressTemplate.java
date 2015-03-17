/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.layout.address;

import java.io.Serializable;

import org.openmrs.layout.LayoutSupport;
import org.openmrs.layout.LayoutTemplate;

/**
 * @since 1.12
 */
public class AddressTemplate extends LayoutTemplate implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public AddressTemplate(String string) {
		super(string);
	}
	
	public String getLayoutToken() {
		return "IS_ADDR_TOKEN";
	}
	
	public String getNonLayoutToken() {
		return "IS_NOT_ADDR_TOKEN";
	}
	
	@Override
	public LayoutSupport<?> getLayoutSupportInstance() {
		return AddressSupport.getInstance();
	}
	
	public String getCodeName() {
		if (this.codeName == null) {
			this.codeName = "default";
			return this.codeName;
		} else {
			return this.codeName;
		}
	}
}
