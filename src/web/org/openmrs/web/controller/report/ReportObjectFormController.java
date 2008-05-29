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
package org.openmrs.web.controller.report;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.ConceptEditor;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.propertyeditor.UserEditor;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.EmptyReportObject;
import org.openmrs.reporting.ReportObjectFactory;
import org.openmrs.reporting.ReportObjectService;
import org.openmrs.web.WebConstants;
import org.openmrs.web.taglib.HtmlIncludeTag;
import org.springframework.beans.propertyeditors.CharacterEditor;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ReportObjectFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());
    
	/**
	 * 
	 * Allows for Integers to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */

    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, true));
        binder.registerCustomEditor(Long.class, new CustomNumberEditor(Long.class, true));
        binder.registerCustomEditor(Float.class, new CustomNumberEditor(Float.class, true));
        binder.registerCustomEditor(Double.class, new CustomNumberEditor(Double.class, true));
        binder.registerCustomEditor(Boolean.class, new CustomBooleanEditor("t", "f", true));
        binder.registerCustomEditor(Character.class, new CharacterEditor(true));
        // TODO: check this for dates in both locales
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("dd/MM/yyyy"), true));
        binder.registerCustomEditor(Concept.class, new ConceptEditor());
        binder.registerCustomEditor(Location.class, new LocationEditor());
        binder.registerCustomEditor(User.class, new UserEditor());
    }

	/** 
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		String view = getFormView();
		
		if (Context.isAuthenticated()) {
			AbstractReportObject reportObject = (AbstractReportObject)obj;

			Context.getAdministrationService().updateReportObject(reportObject);
			view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ReportObject.saved");
		}
		
		return new ModelAndView(new RedirectView(view));
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj, BindException be) throws Exception {
		String submitted = ServletRequestUtils.getStringParameter(request, "submitted", "");

		if ( submitted.length() > 0 ) {
			AbstractReportObject reportObject = (AbstractReportObject)obj;
			
			// no matter what, we want to do some minimal validation
			if ( reportObject.getName() == null ) be.rejectValue("name", "error.name");
			else if ( reportObject.getName().length() <= 0 ) be.rejectValue("name", "error.name");
			
			if ( reportObject.getType() == null ) be.rejectValue("type", "error.reportObject.type.required");

			if ( reportObject.getSubType() == null ) be.rejectValue("subType", "error.reportObject.subType.required");
		}

		
		// TODO: This is NOT the place to be clearing out hte mapping of html included files.  
		// Is this even needed?? In what situations?
		
		// not adding data, but need to take out HtmlIncludeMap before we display this form, in case it is a re-showing of
		if ( request.getAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_MAP_KEY) != null ) {
			log.debug("\n\nREMOVING HTMLINCLUDEMAP FROM REQUEST\n\n");
			request.removeAttribute(HtmlIncludeTag.OPENMRS_HTML_INCLUDE_MAP_KEY);
		}

		return super.processFormSubmission(request, response, obj, be);
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#onBind(javax.servlet.http.HttpServletRequest, java.lang.Object)
	 */
	@Override
	protected void onBind(HttpServletRequest request, Object obj, BindException be) throws Exception {

		ReportObjectService rs = (ReportObjectService)Context.getReportObjectService();

		AbstractReportObject reportObject = (AbstractReportObject)obj;
		String setType = reportObject.getType();
		String setSubType = reportObject.getSubType();
		if ( setType != null && setSubType != null ) {
			if ( !ReportObjectFactory.getInstance().isSubTypeOfType(setType, setSubType) ) {
				((AbstractReportObject)obj).setSubType(null);
			}
		}

		String currentClassName = this.getCommand(request).getClass().getName();
		String correspondingValidatorName = rs.getReportObjectValidatorByClass(currentClassName);
		Validator v = null;
		if ( correspondingValidatorName.length() > 0 ) {
			try {
				Class cls = Class.forName(correspondingValidatorName);
				Constructor ct = cls.getConstructor();
				v = (Validator)ct.newInstance();
				this.setValidator(v);
			} catch ( Throwable t ) {
				log.error("Could not instantiate validator \"" + correspondingValidatorName + "\" for ReportObject class " + currentClassName);
			}
		} else {
			try {
				Class cls = Class.forName(currentClassName + "Validator");
				Constructor ct = cls.getConstructor();
				v = (Validator)ct.newInstance();
				this.setValidator(v);
			} catch ( Throwable t ) {
				log.debug("Could not instantiate default validator \"" + currentClassName + "Validator\" for ReportObject class " + currentClassName + "; Using default validator: " + rs.getDefaultReportObjectValidator());
				Class cls = Class.forName(rs.getDefaultReportObjectValidator());
				Constructor ct = cls.getConstructor();
				v = (Validator)ct.newInstance();
				this.setValidator(v);
			}
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Map referenceData(HttpServletRequest request, Object obj, Errors err) throws Exception {
		Map addedData = new HashMap();
		
		ReportObjectService rs = Context.getReportObjectService();

		List<String> availableTypes = rs.getReportObjectTypes();
		addedData.put("availableTypes", availableTypes.iterator());
		
		String selectedType = ServletRequestUtils.getStringParameter(request, "type", "");

		if ( selectedType.length() > 0 ) {
			List<String> availableSubTypes = rs.getReportObjectSubTypes(selectedType);
			addedData.put("availableSubTypes", availableSubTypes.iterator());
		}
		
		Map extendedObjectInfo = new HashMap();
		Map transientObjects = new HashMap();
		for ( Field field : obj.getClass().getDeclaredFields() ) {
			String fieldName = field.getName();
			int modifiers = field.getModifiers();
			if ( (modifiers & (0x0 | Modifier.TRANSIENT)) == (0x0 | Modifier.TRANSIENT) ) {
				log.debug("OBJECT IS TRANSIENT, SO NOT TRYING TO EDIT");
				transientObjects.put(fieldName, "true");
			} else {
				Method m = obj.getClass().getMethod("get" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1), (Class[])null);
				Object fieldObj = m.invoke(obj, (Object[])null);
				extendedObjectInfo.put(fieldName, fieldObj);
			}

		}
		addedData.put("extendedObjectInfo", extendedObjectInfo);
		addedData.put("transientObjects", transientObjects);
	
		
		
		return addedData;
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#isFormChangeRequest(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected boolean isFormChangeRequest(HttpServletRequest request) {
		String type = ServletRequestUtils.getStringParameter(request, "type", "");
		String subType = ServletRequestUtils.getStringParameter(request, "subType", "");
		String submitted = ServletRequestUtils.getStringParameter(request, "submitted", "");
		
		boolean isChange = (type.length() == 0 || subType.length() == 0 || submitted.length() == 0 );
		
		return isChange;
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		AbstractReportObject reportObject = null;
		ReportObjectService rs = Context.getReportObjectService();

		//Context.getClass().getDeclaredField("some").getGenericType().
		
		if (Context.isAuthenticated()) {
			int reportObjectId = ServletRequestUtils.getIntParameter(request, "reportObjectId", 0);
	    	if (reportObjectId > 0) 
	    		reportObject = rs.getReportObject(Integer.valueOf(reportObjectId));	
		}
		
		if (reportObject == null)
			reportObject = new EmptyReportObject();
		
		String presetType = ServletRequestUtils.getStringParameter(request, "type", "");
		if ( presetType.length() > 0 ) reportObject.setType(presetType);
    	
		String presetSubType = ServletRequestUtils.getStringParameter(request, "subType", "");
		if ( presetSubType.length() > 0 && ReportObjectFactory.getInstance().isSubTypeOfType(presetType, presetSubType) ) {
			reportObject.setSubType(presetSubType);
			String className = rs.getReportObjectClassBySubType(presetSubType);
			if ( className.length() > 0 ) {
				try {
					Class cls = Class.forName(className);
					Constructor ct = cls.getConstructor();
					reportObject = (AbstractReportObject)ct.newInstance();
					reportObject.setType(presetType);
					reportObject.setSubType(presetSubType);
				} catch (Throwable e) {
					log.error("Unable to generate subtype class for ReportObject", e);
				}
			}
		}
		
		return reportObject;
    }
    
}
