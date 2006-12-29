package org.openmrs.module.web.controller;

import java.io.File;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.Module;
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

		// handle module upload
		if (request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
			MultipartFile multipartModuleFile = multipartRequest.getFile("moduleFile");
			if (multipartModuleFile != null && !multipartModuleFile.isEmpty()) {
				String filename = WebUtil.stripFilename(multipartModuleFile.getOriginalFilename());
				File moduleFile = ModuleUtil.insertModuleFile(multipartModuleFile.getInputStream(), filename);
				Module module = ModuleFactory.loadModule(moduleFile);
				ModuleFactory.startModule(module);
				WebModuleUtil.startModule(module, getServletContext());
				if (module.isStarted())
					success = msa.getMessage("Module.loadedAndStarted", new String[] {module.getName()});
				else
					success = msa.getMessage("Module.loaded", new String[] {module.getName()});
			}
		}
		
		if (moduleId.equals("")) {
			ModuleUtil.checkForModuleUpdates();
		}
		else { // moduleId is not empty
			log.debug("Module id: " + moduleId);
			String action = ServletRequestUtils.getStringParameter(request, "action", "");
			Module mod = ModuleFactory.getModuleById(moduleId);
			
			// Argument to pass to the success/error message
			Object[] args = new Object[] { moduleId };
			
			if (mod == null)
				error = msa.getMessage("Module.invalid", args);
			else {
				if ("stop".equals(action)) {
					ModuleFactory.stopModule(mod);
					WebModuleUtil.stopModule(mod, getServletContext());
					success = msa.getMessage("Module.stopped", args);
				}
				else if ("start".equals(action)) {
					ModuleFactory.startModule(mod);
					WebModuleUtil.startModule(mod, getServletContext());
					if (mod.isStarted())
						success = msa.getMessage("Module.started", args);
					else
						error = msa.getMessage("Module.not.started", args);
				}
				else if ("unload".equals(action)) {
					WebModuleUtil.stopModule(mod, getServletContext());
					ModuleFactory.unloadModule(mod);
					success = msa.getMessage("Module.unloaded", args);
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

}
