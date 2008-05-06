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
package org.openmrs.module.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleConstants;
import org.openmrs.module.ModuleException;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleUtil;
import org.openmrs.module.web.WebModuleUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.openmrs.web.WebUtil;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ModuleListController extends SimpleFormController {

	/**
	 * Logger for this class and subclasses
	 */
	protected static final Log log = LogFactory
			.getLog(ModuleListController.class);
	
	/**
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 * by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {

		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_MODULES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_MODULES);
		
		HttpSession httpSession = request.getSession();
		String moduleId = ServletRequestUtils.getStringParameter(request, "moduleId", "");
		String view = getFormView();
		String success = "";
		String error = "";
		MessageSourceAccessor msa = getMessageSourceAccessor();
		
		String action = ServletRequestUtils.getStringParameter(request, "action", "");
		if (ServletRequestUtils.getStringParameter(request, "start.x", null) != null)
			action = "start";
		else if (ServletRequestUtils.getStringParameter(request, "stop.x", null) != null)
			action = "stop";
		else if (ServletRequestUtils.getStringParameter(request, "unload.x", null) != null)
			action = "unload";
		
		
		// handle module upload
		if ("upload".equals(action) && request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
			MultipartFile multipartModuleFile = multipartRequest.getFile("moduleFile");
			if (multipartModuleFile != null && !multipartModuleFile.isEmpty()) {
				// double check upload permissions
				if (!ModuleUtil.allowAdmin()) {
					error = msa.getMessage("Module.disallowUploads", new String[] {ModuleConstants.RUNTIMEPROPERTY_ALLOW_ADMIN});
				}
				else {
					String filename = WebUtil.stripFilename(multipartModuleFile.getOriginalFilename());
					InputStream inputStream = null;
					File moduleFile = null;
					Module module = null;
					try {
						inputStream = multipartModuleFile.getInputStream();
						moduleFile = ModuleUtil.insertModuleFile(inputStream, filename);
						module = ModuleFactory.loadModule(moduleFile);
					}
					catch (ModuleException me) {
						log.warn("Unable to load and start module", me);
						error = me.getMessage();
					}
					finally {
						// clean up the module repository folder
						try {
							if (inputStream != null)
								inputStream.close();
						}
						catch (IOException io) {
							log.warn("Unable to close temporary input stream", io);
						}
						
						if (module == null && moduleFile != null)
							moduleFile.delete();
					}
					
					// if we didn't have trouble loading the module, start it
					if (module != null) {
						ModuleFactory.startModule(module);
						WebModuleUtil.startModule(module, getServletContext(), false);
						if (module.isStarted())
							success = msa.getMessage("Module.loadedAndStarted", new String[] {module.getName()});
						else
							success = msa.getMessage("Module.loaded", new String[] {module.getName()});
					}
				}
			}
		}
		
		else if (moduleId.equals("")) {
			ModuleUtil.checkForModuleUpdates();
		}
		else if (action.equals(msa.getMessage("Module.installUpdate"))) {
			// download and install update
			if (!ModuleUtil.allowAdmin()) {
				error = msa.getMessage("Module.disallowAdministration", new String[] {ModuleConstants.RUNTIMEPROPERTY_ALLOW_ADMIN});
			}
			Module mod = ModuleFactory.getModuleById(moduleId);
			if (mod.getDownloadURL() != null) {
				ModuleFactory.stopModule(mod);
				WebModuleUtil.stopModule(mod, getServletContext());
				Module newModule = ModuleFactory.updateModule(mod);
				WebModuleUtil.startModule(newModule, getServletContext(), false);
			}
		}
		else { // moduleId is not empty
			if (!ModuleUtil.allowAdmin()) {
				error = msa.getMessage("Module.disallowAdministration", new String[] {ModuleConstants.RUNTIMEPROPERTY_ALLOW_ADMIN});
			}
			else {
				log.debug("Module id: " + moduleId);
				Module mod = ModuleFactory.getModuleById(moduleId);
				
				// Argument to pass to the success/error message
				Object[] args = new Object[] { moduleId };
				
				if (mod == null)
					error = msa.getMessage("Module.invalid", args);
				else {
					if ("stop".equals(action)) {
						mod.clearStartupError();
						ModuleFactory.stopModule(mod);
						WebModuleUtil.stopModule(mod, getServletContext());
						success = msa.getMessage("Module.stopped", args);
					}
					else if ("start".equals(action)) {
						ModuleFactory.startModule(mod);
						WebModuleUtil.startModule(mod, getServletContext(), false);
						if (mod.isStarted())
							success = msa.getMessage("Module.started", args);
						else
							error = msa.getMessage("Module.not.started", args);
					}
					else if ("unload".equals(action)) {
						ModuleFactory.stopModule(mod); // stop the module so that when the web stop is done properly
						WebModuleUtil.stopModule(mod, getServletContext());
						ModuleFactory.unloadModule(mod);
						success = msa.getMessage("Module.unloaded", args);
					}
				}
			}
		}
		
		view = getSuccessView();

		if (!success.equals(""))
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
		
		if (!error.equals(""))
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);

		return new ModelAndView(new RedirectView(view));
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time. It tells
	 * Spring the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		Collection<Module> modules = ModuleFactory.getLoadedModules();
		
		log.info("Returning " + modules.size() + " modules");

		return modules;
	}
	
	@Override
	protected Map<String, String> referenceData(HttpServletRequest request) throws Exception {
		
		Map<String, String> map = new HashMap<String, String>();
		MessageSourceAccessor msa = getMessageSourceAccessor();
		
		map.put("allowAdmin", ModuleUtil.allowAdmin().toString());
		map.put("disallowUploads", msa.getMessage("Module.disallowUploads", new String[] {ModuleConstants.RUNTIMEPROPERTY_ALLOW_ADMIN}));
		
		return map;
	}
	
}
