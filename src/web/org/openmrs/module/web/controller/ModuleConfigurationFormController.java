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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller that backs the /admin/modules/moduleConfiguration.form page. This controller makes a
 * dynamic form based on the global properties available in a module
 */
public class ModuleConfigurationFormController extends SimpleFormController {

	private final static Log log = LogFactory.getLog(ModuleConfigurationFormController.class);
	
	private final String PROP_NAME = "property";
	
	private final String PROP_VAL_NAME = "value";
	
	private final String PROP_DESC_NAME = "description";

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
	                             BindException errors) throws Exception {

		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_MODULES))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_MODULES);

		MessageSourceAccessor msa = getMessageSourceAccessor();
		
		String action = request.getParameter("action");
		if (action == null) {
			action = "cancel";
		}
		
		List<GlobalProperty> formBackingObject = (List<GlobalProperty>) command;
		Map<String, GlobalProperty> formBackingObjectMap = new HashMap<String, GlobalProperty>();
		for (GlobalProperty gp : formBackingObject) {
			formBackingObjectMap.put(gp.getProperty(), gp);
		}

		if (action.equals(msa.getMessage("general.save"))) {
			
			if (Context.isAuthenticated()) {
				AdministrationService as = Context.getAdministrationService();
				HttpSession httpSession = request.getSession();
				
				String keys[] = request.getParameterValues(PROP_NAME);
				String values[] = request.getParameterValues(PROP_VAL_NAME);
				String descriptions[] = request.getParameterValues(PROP_DESC_NAME);

				List<GlobalProperty> gpList = new ArrayList<GlobalProperty>();
				
				String moduleId = request.getParameter("moduleId");
				
				Module module = null;

				if (moduleId != null) {
					module = ModuleFactory.getModuleById(moduleId);
				}

				for (int i = 0; i < keys.length; i++) {
					String propName = keys[i];
					String propValue = values[i];
					String propDescription = descriptions[i];
					
					GlobalProperty gp = formBackingObjectMap.get(propName);
					
					if (gp != null) {
						gp.setPropertyValue(propValue);
						gp.setDescription(propDescription);
					} else {
						gp = new GlobalProperty(propName, propValue, propDescription);
					}

					gpList.add(gp);
				}
				
				try {
					for (GlobalProperty gp : gpList) {
						as.saveGlobalProperty(gp);
					}
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, msa.getMessage("Module.configured",
					    new String[] { module != null ? module.getName() : "" }));
				}
				catch (Exception e) {
					log.error("Error saving properties", e);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, msa.getMessage("GlobalProperty.not.saved"));
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, e.getMessage());
				}
			}
		}
		
		return new ModelAndView(new RedirectView(getSuccessView()));
	}

	@Override
	public Map referenceData(HttpServletRequest request) {
		Map<String, Object> map = new HashMap<String, Object>();

		String moduleId = request.getParameter("moduleId");
		
		Module module = null;
		
		String moduleName = "";
		
		try {
			module = getModuleFromModuleId(moduleId);
			
			moduleName = module.getName();
		}
		catch (Exception e) {
			log.error(e);
		}
		
		map.put("moduleName", moduleName);

		return map;
	}
	
	@Override
	public Object formBackingObject(HttpServletRequest request) {
		List<GlobalProperty> gpList = new ArrayList<GlobalProperty>();
		
		if (Context.isAuthenticated()) {
			AdministrationService as = Context.getAdministrationService();
			
			String moduleId = request.getParameter("moduleId");
			
			Module module = null;

			try {
				module = getModuleFromModuleId(moduleId);
				
				List<GlobalProperty> globalProperties = module.getGlobalProperties();
				
				for (GlobalProperty gp : globalProperties) {
					String property = gp.getProperty();
					GlobalProperty dbgp = as.getGlobalPropertyObject(property);
					if (dbgp == null) {
						// Not available in database then directly use
						dbgp = gp;
					}
					gpList.add(dbgp);
				}
			}
			catch (Exception e) {
				log.error(e);
			}
		}
		
		return gpList;
	}
	
	private Module getModuleFromModuleId(String moduleId) throws Exception {
		if (moduleId != null) {
			Module module = ModuleFactory.getModuleById(moduleId);
			
			if (module != null) {
				return module;
			} else {
				throw new Exception("Invalid Module Id");
			}
		} else {
			throw new Exception("Module Id required but not found");
		}
	}
}
