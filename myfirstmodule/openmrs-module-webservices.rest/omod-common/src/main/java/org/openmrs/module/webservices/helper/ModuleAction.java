/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.helper;

import org.openmrs.module.Module;

import java.util.List;

public class ModuleAction {
	
	public enum Action {
		START, STOP, RESTART, UNLOAD, INSTALL;
	}
	
	public ModuleAction() {
	}
	
	public ModuleAction(Action action, List<Module> modules, boolean startAll, String installUri) {
		this.modules = modules;
		this.action = action;
		this.allModules = startAll;
	}
	
	private List<Module> modules;
	
	private Action action;
	
	private Boolean allModules;
	
	private String installUri;
	
	public void setModules(List<Module> modules) {
		this.modules = modules;
	}
	
	public List<Module> getModules() {
		return modules;
	}
	
	public void setAction(Action type) {
		this.action = type;
	}
	
	public Action getAction() {
		return action;
	}
	
	public Boolean isAllModules() {
		return allModules;
	}
	
	public void setAllModules(Boolean allModules) {
		this.allModules = allModules;
	}
	
	public void setInstallUri(String installUri) {
		this.installUri = installUri;
	}
	
	public String getInstallUri() {
		return installUri;
	}
}
