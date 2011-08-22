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
package org.openmrs.web.controller.location;

import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for listing all location attribute types.
 * 
 * @since 1.9
 */
@Controller
public class LocationAttributeTypeListController {
	
	/**
	 * Show existing
	 */
	@RequestMapping("/admin/locations/locationAttributeTypes")
	public void list(Model model) {
		model.addAttribute("attributeTypes", Context.getLocationService().getAllLocationAttributeTypes());
	}
	
}
