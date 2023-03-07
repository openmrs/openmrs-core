/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web;

import org.openmrs.module.Extension;
import org.openmrs.module.Module;
import org.openmrs.module.webservices.helper.ModuleFactoryWrapper;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MockModuleFactoryWrapper extends ModuleFactoryWrapper {
	
	public List<Module> loadedModules = new ArrayList<Module>();

	public List<Extension> loadedExtensions = new ArrayList<Extension>();

	public List<Module> startedModules = new ArrayList<Module>();
	
	public Module loadModuleMock;
	
	@Override
	public Module getModuleById(String id) {
		for (Module module : loadedModules) {
			if (module.getModuleId().equals(id)) {
				return module;
			}
		}
		return null;
	}
	
	@Override
	public Module parseModuleFile(MultipartFile file) throws IOException {
		return new Module("ParsedModule", "atlas", "name", "author", "description", "version");
	}
	
	@Override
	public void refreshWebApplicationContext(ServletContext context) {
		//do nothing
	}
	
	@Override
	public Collection<Module> getLoadedModules() {
		return loadedModules;
	}

	@Override
	public List<Extension> getExtensions(String pointId) {
		List<Extension> foundExtensions = new ArrayList<Extension>();
		for (Extension loadedExtension : loadedExtensions) {
			if (loadedExtension.getPointId().equals(pointId)) {
				foundExtensions.add(loadedExtension);
			}
		}
		return foundExtensions;
	}

	@Override
	public boolean isModuleStarted(Module module) {
		return startedModules.contains(module);
	}
	
	@Override
	public void unloadModule(Module module) {
		startedModules.remove(module);
		loadedModules.remove(module);
	}
	
	@Override
	public File insertModuleFile(Module module, String filename) {
		return new File("fake");
	}
	
	@Override
	public Module loadModule(File moduleFile) {
		return loadModuleMock;
	}
	
	@Override
	public List<Module> stopModuleAndGetDependent(Module module) {
		return Collections.singletonList(new Module("dependent", "atlas", "name", "author", "description", "version"));
	}
	
	@Override
	public void stopModule(Module module, ServletContext servletContext) {
		startedModules.remove(module);
	}
	
	@Override
	public boolean startModule(Module module, ServletContext servletContext) {
		return startModule(module, servletContext, false);
	}
	
	@Override
	public boolean startModule(Module module, ServletContext servletContext, boolean delayRefreshContext) {
		if (!loadedModules.contains(module)) {
			loadedModules.add(module);
		}
		return startedModules.add(module);
	}
	
	@Override
	public void stopModuleSkipRefresh(Module module, ServletContext servletContext) {
		startedModules.remove(module);
	}
	
	@Override
	public Collection<Module> getModulesInStartupOrder(Collection<Module> modules) {
		return modules;
	}
}
