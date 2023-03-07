/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openmrs.api.APIException;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleException;
import org.openmrs.module.ModuleUtil;
import org.openmrs.module.webservices.docs.swagger.core.property.EnumProperty;
import org.openmrs.module.webservices.helper.ModuleAction;
import org.openmrs.module.webservices.helper.ModuleFactoryWrapper;
import org.openmrs.module.webservices.helper.ModuleAction.Action;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Creatable;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.IllegalRequestException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.util.ResourceUtils;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/moduleaction", supportedClass = ModuleAction.class, supportedOpenmrsVersions = {
        "1.8.* - 9.*" })
public class ModuleActionResource1_8 extends BaseDelegatingResource<ModuleAction> implements Creatable {
	
	/**
	 * ModuleFactoryWrapper is used for testing purposes.
	 */
	private ModuleFactoryWrapper moduleFactoryWrapper = new ModuleFactoryWrapper();
	
	public void setModuleFactoryWrapper(ModuleFactoryWrapper moduleFactoryWrapper) {
		this.moduleFactoryWrapper = moduleFactoryWrapper;
	}
	
	/**
	 * Overriding create directly, because ModuleFactory requires ServletContext to execute any
	 * action
	 */
	@Override
	public Object create(SimpleObject post, RequestContext context) throws ResponseException {
		moduleFactoryWrapper.checkPrivilege();
		
		ModuleAction action = newDelegate();
		setConvertedProperties(action, post, getCreatableProperties(), true);
		String installUri = action.getInstallUri();
		
		Collection<Module> modules;
		if (action.isAllModules() != null && action.isAllModules()) {
			modules = moduleFactoryWrapper.getLoadedModules();
			action.setModules(new ArrayList<Module>(modules));
		} else {
			modules = action.getModules();
		}
		
		ServletContext servletContext = getServletContext(context);
		
		if (modules == null || modules.isEmpty()) {
			throw new IllegalRequestException("Cannot execute action " + action.getAction() + " on empty set of modules.");
		} else {
			if (action.getAction() == Action.INSTALL) {
				if (installUri == null || !ResourceUtils.isUrl(installUri)) {
					throw new IllegalRequestException("The installUri needs to be a URL for this action to be performed");
				}
			}
			else {
				if (action.isAllModules() == null || !action.isAllModules()) {
					// ensure all specified modules exist
					// ensure they're not trying to modify the REST module
					for (Module module : modules) {
						// if they specified a module that's not loaded, it will show up here as null
						if (module == null) {
							throw new IllegalRequestException(
							        "One or more of the modules you specified are not loaded on this server");
						}
						if (module.getModuleId().equals(RestConstants.MODULE_ID)) {
							throw new IllegalRequestException("You are not allowed to modify " + module.getModuleId()
							        + " via this REST call");
						}
					}
				}
				
				// even if they said allModule=true, don't touch the REST module
				Module restModule = moduleFactoryWrapper.getModuleById(RestConstants.MODULE_ID);
				modules.remove(restModule);
			}
			
			switch (action.getAction()) {
				case START:
					startModules(modules, servletContext);
					break;
				case STOP:
					stopModules(modules, servletContext, true);
					break;
				case RESTART:
					restartModules(modules, servletContext);
					break;
				case UNLOAD:
					unloadModules(modules, servletContext);
					break;
				case INSTALL:
					Module module = installModule(modules, installUri, servletContext);
					modules.clear();
					modules.add(module);
					action.setModules(new ArrayList<Module>(modules));
					break;
			}
		}
		
		return ConversionUtil.convertToRepresentation(action, Representation.DEFAULT);
	}
	
	private Module installModule(Collection<Module> modules, String installUri, ServletContext servletContext) {
		List<Module> moduleList = new ArrayList<Module>(modules);
		List<Module> dependentModulesStopped = new ArrayList<Module>();
		Module existingModule = moduleList.get(0);
		Module tempModule = null;
		File moduleFile = null;
		
		try {
			if (existingModule != null) {
				dependentModulesStopped = moduleFactoryWrapper.stopModuleAndGetDependent(existingModule);
				for (Module depMod : dependentModulesStopped) {
					moduleFactoryWrapper.stopModuleSkipRefresh(depMod, servletContext);
				}
				
				moduleFactoryWrapper.stopModuleSkipRefresh(existingModule, servletContext);
				moduleFactoryWrapper.unloadModule(existingModule);
			}
			
			URL downloadUrl = new URL(installUri);
			String fileName = FilenameUtils.getName(downloadUrl.getPath());
			InputStream inputStream = ModuleUtil.getURLStream(downloadUrl);
			moduleFile = ModuleUtil.insertModuleFile(inputStream, fileName);
			tempModule = moduleFactoryWrapper.loadModule(moduleFile);
			moduleFactoryWrapper.startModule(tempModule, servletContext);
			if (existingModule != null && !dependentModulesStopped.isEmpty()
			        && moduleFactoryWrapper.isModuleStarted(tempModule)) {
				startModules(dependentModulesStopped, servletContext);
			}
			return tempModule;
		}
		catch (MalformedURLException e) {
			throw new APIException(e.getMessage(), e);
		}
		catch (IOException e) {
			throw new APIException(e.getMessage(), e);
		}
		finally {
			if (tempModule == null && moduleFile != null) {
				FileUtils.deleteQuietly(moduleFile);
			}
		}
	}
	
	private void restartModules(Collection<Module> modules, ServletContext servletContext) {
		stopModules(modules, servletContext, false);
		startModules(modules, servletContext);
	}
	
	private void unloadModules(Collection<Module> modules, ServletContext servletContext) {
		boolean needsRefresh = false;
		for (Module module : modules) {
			if (moduleFactoryWrapper.isModuleStarted(module)) {
				moduleFactoryWrapper.stopModuleSkipRefresh(module, servletContext);
				needsRefresh = true;
			}
			moduleFactoryWrapper.unloadModule(module);
		}
		
		if (needsRefresh) {
			moduleFactoryWrapper.refreshWebApplicationContext(servletContext);
		}
	}
	
	/**
	 * @param modules modules to stop
	 * @param servletContext ServletContext is required by WebModuleUtil to perform operation
	 */
	private void stopModules(Collection<Module> modules, ServletContext servletContext, boolean refreshContext) {
		for (Module module : modules) {
			if (moduleFactoryWrapper.isModuleStarted(module)) {
				moduleFactoryWrapper.stopModuleSkipRefresh(module, servletContext);
			}
		}
		if (refreshContext) {
			moduleFactoryWrapper.refreshWebApplicationContext(servletContext);
		}
	}
	
	/**
	 * @param modules modules to start
	 * @param servletContext ServletContext is required by WebModuleUtil to perform operation
	 */
	private void startModules(Collection<Module> modules, ServletContext servletContext) {
		boolean needsRefresh = false;
		if (modules.size() > 1) {
			modules = moduleFactoryWrapper.getModulesInStartupOrder(modules);
		}
		
		for (Module module : modules) {
			if (moduleFactoryWrapper.isModuleStopped(module)) {
				needsRefresh = moduleFactoryWrapper.startModuleSkipRefresh(module, servletContext) || needsRefresh;
			}
		}
		//check if any module has been started, doesn't refresh WAC if all modules failed to start
		if (needsRefresh) {
			moduleFactoryWrapper.refreshWebApplicationContext(servletContext);
		}
		
		findAndThrowStartupErrors(modules);
	}
	
	private void findAndThrowStartupErrors(Collection<Module> modules) {
		List<Exception> errors = new ArrayList<Exception>();
		for (Module module : modules) {
			if (moduleFactoryWrapper.isModuleStopped(module) && module.getStartupErrorMessage() != null) {
				//module actions are executed in other thread, so we need to explicitly check and throw them
				errors.add(new ModuleException(module.getStartupErrorMessage()));
			}
		}
		
		if (!errors.isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder();
			for (Exception error : errors) {
				stringBuilder.append(error.getMessage()).append("; ");
			}
			throw new ModuleException(stringBuilder.toString());
		}
	}
	
	@Override
	public ModuleAction newDelegate() {
		return new ModuleAction();
	}
	
	@Override
	public ModuleAction save(ModuleAction delegate) {
		throw new UnsupportedOperationException("ModuleAction cannot be saved");
	}
	
	@Override
	public ModuleAction getByUniqueId(String uniqueId) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	protected void delete(ModuleAction delegate, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public void purge(ModuleAction delegate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("modules", Representation.REF);
		description.addProperty("action", "action");
		return description;
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("modules");
		description.addProperty("allModules");
		description.addProperty("installUri");
		description.addRequiredProperty("action", "action");
		return description;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		return ((ModelImpl) super.getGETModel(rep))
		        .property("modules", new ArrayProperty(new RefProperty("#/definitions/ModuleGetRef")))
		        .property("action", new EnumProperty(ModuleAction.Action.class));
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		return new ModelImpl()
		        .property("modules", new ArrayProperty(new StringProperty().example("moduleId")))
		        .property("allModules", new BooleanProperty())
		        .property("action", new EnumProperty(ModuleAction.Action.class))
		        .property("installUri", new StringProperty())
		        .required("action");
	}
	
	/**
	 * Converter does not handle getters starting with 'is' instead of 'get'
	 */
	@PropertyGetter("allModules")
	public Boolean isAllModules(ModuleAction action) {
		return action.isAllModules();
	}
	
	private ServletContext getServletContext(RequestContext context) {
		return context.getRequest().getSession().getServletContext();
	}
}
