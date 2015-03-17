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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Form;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.Extension.MEDIA_TYPE;
import org.openmrs.module.web.FormEntryContext;
import org.openmrs.module.web.extension.FormEntryHandler;
import org.openmrs.util.OpenmrsUtil;

/**
 * Controller for the PersonFormEntry portlet Provides a map telling which url to hit to enter each
 * form
 */
public class PersonFormEntryPortletController extends PortletController {
	
	/**
	 * @see org.openmrs.web.controller.PortletController#populateModel(javax.servlet.http.HttpServletRequest,
	 *      java.util.Map)
	 */
	@Override
	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {
		if (model.containsKey("formToEntryUrlMap")) {
			return;
		}
		Person person = (Person) model.get("person");
		if (person == null) {
			throw new IllegalArgumentException("This portlet may only be used in the context of a Person");
		}
		FormEntryContext fec = new FormEntryContext(person);
		Map<Form, FormEntryHandler> entryUrlMap = new TreeMap<Form, FormEntryHandler>(new Comparator<Form>() {
			
			public int compare(Form left, Form right) {
				int temp = left.getName().toLowerCase().compareTo(right.getName().toLowerCase());
				if (temp == 0) {
					temp = OpenmrsUtil.compareWithNullAsLowest(left.getVersion(), right.getVersion());
				}
				if (temp == 0) {
					temp = OpenmrsUtil.compareWithNullAsGreatest(left.getId(), right.getId());
				}
				return temp;
			}
		});
		List<Extension> handlers = ModuleFactory.getExtensions("org.openmrs.module.web.extension.FormEntryHandler",
		    MEDIA_TYPE.html);
		if (handlers != null) {
			for (Extension ext : handlers) {
				FormEntryHandler handler = (FormEntryHandler) ext;
				Collection<Form> toEnter = handler.getFormsModuleCanEnter(fec);
				if (toEnter != null) {
					for (Form form : toEnter) {
						entryUrlMap.put(form, handler);
					}
				}
			}
		}
		model.put("formToEntryUrlMap", entryUrlMap);
		model.put("anyUpdatedFormEntryModules", handlers != null && handlers.size() > 0);
		
		// determine whether it's need to show disclaimer on jsp page or not
		// as current user does not have enough permissions to view at least one
		// type of encounters
		model.put("showDisclaimer", !Context.getEncounterService().canViewAllEncounterTypes(Context.getAuthenticatedUser()));
	}
	
}
