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
package org.openmrs.layout.web.address;

import java.io.Serializable;

import org.openmrs.layout.web.LayoutSupport;
import org.openmrs.layout.web.LayoutTemplate;

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
