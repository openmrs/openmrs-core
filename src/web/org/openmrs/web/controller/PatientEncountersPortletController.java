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
package org.openmrs.web.controller;

import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Form;
import org.openmrs.Person;
import org.openmrs.module.Extension;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.web.FormEntryContext;
import org.openmrs.module.web.extension.FormEntryHandler;
import org.openmrs.module.web.extension.provider.Link;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.web.util.UrlPathHelper;


/**
 * Controller for the patientEncounters portlet.
 * 
 * Provides a map telling which forms have their view and edit links overridden by form entry modules  
 */
public class PatientEncountersPortletController extends PortletController {

	/**
     * @see org.openmrs.web.controller.PortletController#populateModel(javax.servlet.http.HttpServletRequest, java.util.Map)
     */
    @Override
    protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
    	Person person = (Person) model.get("person");
    	if (person == null)
    		throw new IllegalArgumentException("This portlet may only be used in the context of a Person");
    	FormEntryContext fec = new FormEntryContext(person);
    	
    	Map<Form, String> viewUrlMap = new HashMap<Form, String>();
    	Map<Form, String> editUrlMap = new HashMap<Form, String>();
    	Map<Form, FormEntryHandler> entryUrlMap = new TreeMap<Form, FormEntryHandler>(new Comparator<Form>() {
    		public int compare(Form left, Form right) {
    			int temp = left.getName().toLowerCase().compareTo(right.getName().toLowerCase());
    			if (temp == 0)
    				temp = OpenmrsUtil.compareWithNullAsLowest(left.getVersion(), right.getVersion());
	            return temp;
            }
    	});
    	List<Extension> handlers = ModuleFactory.getExtensions("formentryHandler");
    	if (handlers != null) {
    		for (Extension ext : handlers) {
    			FormEntryHandler handler = (FormEntryHandler) ext;
    			{ // enter
    				Collection<Form> toEnter = handler.getFormsModuleCanEnter(fec);
    				if (toEnter != null) {
    					for (Form form : toEnter) {
    						entryUrlMap.put(form, handler);
    					}
    				}
    			}
    			{ // view
    				Collection<Form> toView = handler.getFormsModuleCanView();
    				if (toView != null) {
    					if (handler.getViewFormUrl() == null)
        					throw new IllegalArgumentException("form entry handler " + handler.getClass() + " is trying to handle viewing forms but specifies no URL");
    					for (Form form : toView) {
    						viewUrlMap.put(form, handler.getViewFormUrl());
    					}
    				}
    			}
    			{ // edit
    				Collection<Form> toEdit = handler.getFormsModuleCanEdit();
    				if (toEdit != null) {
        				if (handler.getEditFormUrl() == null)
        					throw new IllegalArgumentException("form entry handler " + handler.getClass() + " is trying to handle editing forms but specifies no URL");
    					for (Form form : toEdit) {
    						editUrlMap.put(form, handler.getEditFormUrl());
    					}
    				}
    			}
    		}
    	}
    	model.put("formToEntryUrlMap", entryUrlMap);
    	model.put("formToViewUrlMap", viewUrlMap);
    	model.put("formToEditUrlMap", editUrlMap);
    }

	
	
}
