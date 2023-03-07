/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.docs.swagger;

import java.util.List;

public class Versions {
	
	private String platform;
	
	private List<ModuleVersion> modules;
	
	public Versions(String platform, List<ModuleVersion> modules) {
		this.platform = platform;
		this.modules = modules;
	}
	
	public String getPlatform() {
		return platform;
	}
	
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	
	public List<ModuleVersion> getModules() {
		return modules;
	}
	
	public void setModules(List<ModuleVersion> modules) {
		this.modules = modules;
	}
}
