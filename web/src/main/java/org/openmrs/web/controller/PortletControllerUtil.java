/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.Person;
import org.openmrs.module.Extension;
import org.openmrs.module.Extension.MEDIA_TYPE;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.web.extension.FormEntryHandler;

/**
 * Contains utility method(s) to be used by PortletControllers
 *
 * @since 1.9
 */
public class PortletControllerUtil {
	
	private static final Log log = LogFactory.getLog(PortletControllerUtil.class);
	
	/**
	 * Adds encounter formToEdit and formToView Url maps to the specified model
	 *
	 * @param model the model to which to add the maps
	 */
	public static void addFormToEditAndViewUrlMaps(Map<String, Object> model) {
		if (log.isDebugEnabled()) {
			log.debug("In PortletControllerUtil....");
		}
		
		if (model.containsKey("formToEditUrlMap")) {
			return;
		}
		
		Person person = (Person) model.get("person");
		if (person == null) {
			throw new IllegalArgumentException("This portlet may only be used in the context of a Person");
		}
		
		Map<Form, String> viewUrlMap = new HashMap<Form, String>();
		Map<Form, String> editUrlMap = new HashMap<Form, String>();
		List<Extension> handlers = ModuleFactory.getExtensions("org.openmrs.module.web.extension.FormEntryHandler",
		    MEDIA_TYPE.html);
		if (handlers != null) {
			for (Extension ext : handlers) {
				FormEntryHandler handler = (FormEntryHandler) ext;
				{ // view
					Collection<Form> toView = handler.getFormsModuleCanView();
					if (toView != null) {
						if (handler.getViewFormUrl() == null) {
							throw new IllegalArgumentException("form entry handler " + handler.getClass()
							        + " is trying to handle viewing forms but specifies no URL");
						}
						for (Form form : toView) {
							viewUrlMap.put(form, handler.getViewFormUrl());
						}
					}
				}
				{ // edit
					Collection<Form> toEdit = handler.getFormsModuleCanEdit();
					if (toEdit != null) {
						if (handler.getEditFormUrl() == null) {
							throw new IllegalArgumentException("form entry handler " + handler.getClass()
							        + " is trying to handle editing forms but specifies no URL");
						}
						for (Form form : toEdit) {
							editUrlMap.put(form, handler.getEditFormUrl());
						}
					}
				}
			}
		}
		model.put("formToViewUrlMap", viewUrlMap);
		model.put("formToEditUrlMap", editUrlMap);
	}
	
}
