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
import io.swagger.models.properties.StringProperty;
import org.apache.commons.io.FileUtils;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleException;
import org.openmrs.module.webservices.helper.ModuleFactoryWrapper;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Uploadable;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingReadableResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.web.WebUtil;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/module", supportedClass = Module.class, supportedOpenmrsVersions = { "1.8.* - 9.*" })
public class ModuleResource1_8 extends BaseDelegatingReadableResource<Module> implements Uploadable {
	
	private ModuleFactoryWrapper moduleFactoryWrapper = new ModuleFactoryWrapper();
	
	private String moduleActionLink = ModuleActionResource1_8.class.getAnnotation(Resource.class).name();
	
	public void setModuleFactoryWrapper(ModuleFactoryWrapper moduleFactoryWrapper) {
		this.moduleFactoryWrapper = moduleFactoryWrapper;
	}
	
	@Override
	public Module getByUniqueId(String uniqueId) {
		moduleFactoryWrapper.checkPrivilege();
		return moduleFactoryWrapper.getModuleById(uniqueId);
	}
	
	@Override
	public Module newDelegate() {
		return null;
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("packageName");
			description.addProperty("author");
			description.addProperty("version");
			description.addProperty("started");
			description.addProperty("startupErrorMessage");
			description.addProperty("requireOpenmrsVersion");
			description.addProperty("awareOfModules");
			description.addProperty("requiredModules");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addLink("action", RestConstants.URI_PREFIX + moduleActionLink);
			description.addSelfLink();
			return description;
		} else if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("version");
			description.addProperty("started");
			description.addProperty("startupErrorMessage");
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addLink("action", RestConstants.URI_PREFIX + moduleActionLink);
			description.addSelfLink();
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl model = (ModelImpl) super.getGETModel(rep);
		if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			model
			        .property("uuid", new StringProperty())
			        .property("display", new StringProperty())
			        .property("name", new StringProperty())
			        .property("description", new StringProperty())
			        .property("started", new BooleanProperty()) //FIXME check type
			        .property("startupErrorMessage", new StringProperty()); //FIXME add-link: action
		}
		if (rep instanceof FullRepresentation) {
			model
			        .property("packageName", new StringProperty())
			        .property("author", new StringProperty())
			        .property("version", new StringProperty())
			        .property("requireOpenmrsVersion", new StringProperty())
			        .property("awareOfModules", new ArrayProperty(new StringProperty())) //FIXME check type
			        .property("requiredModules", new ArrayProperty(new StringProperty()));
		} else if (rep instanceof RefRepresentation) {
			model
			        .property("uuid", new StringProperty())
			        .property("display", new StringProperty());
		}
		return model;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public NeedsPaging<Module> doGetAll(RequestContext context) throws ResponseException {
		moduleFactoryWrapper.checkPrivilege();
		return new NeedsPaging<Module>(new ArrayList<Module>(moduleFactoryWrapper.getLoadedModules()), context);
	}
	
	@PropertyGetter("uuid")
	public static String getUuid(Module instance) {
		return instance.getModuleId();
	}
	
	@PropertyGetter("display")
	public static String getDisplay(Module instance) {
		return instance.getName();
	}
	
	@Override
	public Object upload(MultipartFile file, RequestContext context) throws ResponseException, IOException {
		moduleFactoryWrapper.checkPrivilege();
		
		File moduleFile = null;
		Module module = null;
		
		try {
			if (file == null || file.isEmpty()) {
				throw new IllegalArgumentException("Uploaded OMOD file cannot be empty");
			} else {
				String filename = WebUtil.stripFilename(file.getOriginalFilename());
				Module tmpModule = moduleFactoryWrapper.parseModuleFile(file);
				Module existingModule = moduleFactoryWrapper.getModuleById(tmpModule.getModuleId());
				ServletContext servletContext = context.getRequest().getSession().getServletContext();
				List<Module> dependentModulesStopped = new ArrayList<Module>();
				
				if (existingModule != null) {
					dependentModulesStopped = moduleFactoryWrapper.stopModuleAndGetDependent(existingModule);
					for (Module depMod : dependentModulesStopped) {
						moduleFactoryWrapper.stopModuleSkipRefresh(depMod, servletContext);
					}
					
					moduleFactoryWrapper.stopModuleSkipRefresh(existingModule, servletContext);
					moduleFactoryWrapper.unloadModule(existingModule);
				}
				
				moduleFile = moduleFactoryWrapper.insertModuleFile(tmpModule, filename);
				module = moduleFactoryWrapper.loadModule(moduleFile);
				moduleFactoryWrapper.startModule(module, servletContext);
				
				if (existingModule != null && dependentModulesStopped.size() > 0
				        && moduleFactoryWrapper.isModuleStarted(module)) {
					startModules(dependentModulesStopped, existingModule, servletContext);
				}
				return getByUniqueId(tmpModule.getModuleId());
			}
		}
		finally {
			if (module == null && moduleFile != null) {
				FileUtils.deleteQuietly(moduleFile);
			}
		}
	}
	
	private void startModules(Collection<Module> modules, Module existingModule, ServletContext servletContext) {
		boolean needsRefresh = false;
		if (modules.size() > 1) {
			modules = moduleFactoryWrapper.getModulesInStartupOrder(modules);
		}
		
		for (Module module : modules) {
			if (moduleFactoryWrapper.isModuleStopped(module) && module.getModuleId() != existingModule.getModuleId()) {
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
}
