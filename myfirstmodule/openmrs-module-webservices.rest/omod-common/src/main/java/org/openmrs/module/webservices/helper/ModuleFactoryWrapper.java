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

import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleFileParser;
import org.openmrs.module.ModuleUtil;
import org.openmrs.module.web.WebModuleUtil;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * ModuleFactoryWrapper wraps static methods of ModuleFactory to make code operating on modules
 * testable
 */
public class ModuleFactoryWrapper {
	
	public static final String STOP_MODULE_SKIP_REFRESH_EXCEPTION_MESSAGE = "Failed to stop module without WAC refresh";
	
	public Module parseModuleFile(MultipartFile file) throws IOException {
		return new ModuleFileParser(file.getInputStream()).parse();
	}
	
	public Module getModuleById(String id) {
		return ModuleFactory.getModuleById(id);
	}
	
	public void refreshWebApplicationContext(ServletContext context) {
		WebModuleUtil.refreshWAC(context, false, null);
	}
	
	public Collection<Module> getLoadedModules() {
		return ModuleFactory.getLoadedModules();
	}

	public List<Extension> getExtensions(String pointId) {
		return ModuleFactory.getExtensions(pointId);
	}
	
	public boolean isModuleStarted(Module module) {
		return module.isStarted();
	}
	
	public boolean isModuleStopped(Module module) {
		return !isModuleStarted(module);
	}
	
	public void unloadModule(Module module) {
		ModuleFactory.unloadModule(module);
	}
	
	public File insertModuleFile(Module module, String filename) throws FileNotFoundException {
		return ModuleUtil.insertModuleFile(new FileInputStream(module.getFile()), filename);
	}
	
	public Module loadModule(File moduleFile) {
		return ModuleFactory.loadModule(moduleFile);
	}
	
	public List<Module> stopModuleAndGetDependent(Module module) {
		return ModuleFactory.stopModule(module, false, true);
	}
	
	public void stopModule(Module module, ServletContext servletContext) {
		ModuleFactory.stopModule(module);
		WebModuleUtil.stopModule(module, servletContext);
		if (module.isStarted()) {
			throw new RuntimeException("Failed to stop module: " + module.getName() + ", " + module.getStartupErrorMessage());
		}
	}
	
	public boolean startModule(Module module, ServletContext servletContext) {
		return startModule(module, servletContext, false);
	}
	
	public boolean startModule(Module module, ServletContext servletContext, boolean delayRefreshContext) {
		ModuleFactory.startModule(module);
		return WebModuleUtil.startModule(module, servletContext, delayRefreshContext);
	}
	
	public boolean startModuleSkipRefresh(Module module, ServletContext servletContext) {
		return startModule(module, servletContext, true);
	}
	
	/**
	 * It's hacky method to workaround the fact that before 2.x platform
	 * {@link WebModuleUtil#stopModule(Module, ServletContext, boolean)} was private. It is
	 * essential to support uploading modules 1.x platform via REST api
	 */
	public void stopModuleSkipRefresh(Module module, ServletContext servletContext) {
		ModuleFactory.stopModule(module);
		try {
			Method stopModule = WebModuleUtil.class.getDeclaredMethod("stopModule", Module.class, ServletContext.class,
			    Boolean.TYPE);
			stopModule.setAccessible(true);
			stopModule.invoke(null, module, servletContext, true);
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(STOP_MODULE_SKIP_REFRESH_EXCEPTION_MESSAGE, e);
		}
		catch (InvocationTargetException e) {
			throw new RuntimeException(STOP_MODULE_SKIP_REFRESH_EXCEPTION_MESSAGE, e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(STOP_MODULE_SKIP_REFRESH_EXCEPTION_MESSAGE, e);
		}
	}
	
	/**
	 * hack to work around the change in ModuleFactory API
	 */
	@SuppressWarnings("unchecked")
	public Collection<Module> getModulesInStartupOrder(Collection<Module> modules) {
		try {
			try {
				Method getModulesInStartupOrder = ModuleFactory.class.getDeclaredMethod("getModulesInStartupOrder",
				    Collection.class);
				return (Collection<Module>) getModulesInStartupOrder.invoke(null, modules);
			}
			catch (NoSuchMethodException e) {
				try {
					Method getModulesInStartOrder = ModuleFactory.class.getDeclaredMethod("getModulesInStartOrder");
					return (Collection<Module>) getModulesInStartOrder.invoke(null);
				}
				catch (NoSuchMethodException e1) {
					throw new RuntimeException(e1);
				}
			}
		}
		catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void checkPrivilege() throws APIAuthenticationException {
		if (!Context.hasPrivilege(PrivilegeConstants.MANAGE_MODULES)) {
			throw new APIAuthenticationException("Privilege required: " + PrivilegeConstants.MANAGE_MODULES);
		}
	}
}
