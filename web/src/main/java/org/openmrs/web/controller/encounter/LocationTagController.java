/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.encounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.LocationTagEditor;
import org.openmrs.util.MetadataComparator;
import org.openmrs.validator.LocationTagValidator;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.WebRequest;

/**
 * Controller for managing {@link LocationTag}s
 */
@Controller
@SessionAttributes("locationTag")
public class LocationTagController {
	
	/**
	 * Set up automatic primitive-to-class mappings
	 *
	 * @param wdb
	 */
	@InitBinder
	public void initBinder(WebDataBinder wdb) {
		wdb.registerCustomEditor(LocationTag.class, new LocationTagEditor());
	}
	
	/**
	 * List all LocationTags
	 */
	@RequestMapping("/admin/locations/locationTag")
	public void list(ModelMap model) {
		List<LocationTag> list = new ArrayList<LocationTag>(Context.getLocationService().getAllLocationTags());
		Collections.sort(list, new MetadataComparator(Context.getLocale()));
		model.addAttribute("locationTags", list);
	}
	
	/**
	 * Add a new LocationTag (quickly, without a dedicated page)
	 */
	@RequestMapping("/admin/locations/locationTagAdd")
	public String add(@RequestParam("name") String name, @RequestParam("description") String description, WebRequest request) {
		
		if (!StringUtils.hasText(name)) {
			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
			    "LocationTag.error.name.required"), WebRequest.SCOPE_SESSION);
		} else if (Context.getLocationService().getLocationTagByName(name) != null) {
			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, Context.getMessageSourceService().getMessage(
			    "LocationTag.error.name.duplicate"), WebRequest.SCOPE_SESSION);
		} else {
			LocationTag tag = new LocationTag(name, description);
			Context.getLocationService().saveLocationTag(tag);
			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
			    "LocationTag.saved"), WebRequest.SCOPE_SESSION);
		}
		
		return "redirect:locationTag.list";
	}
	
	/**
	 * Display the edit page for LocationTag
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/admin/locations/locationTagEdit")
	public void showEdit(@RequestParam("locationTagId") LocationTag locationTag, ModelMap model) {
		model.addAttribute("locationTag", locationTag); // this will go in the session
		List<Location> locations = Context.getLocationService().getLocationsByTag(locationTag);
		if (locations != null && locations.size() > 0) {
			model.addAttribute("locations", locations);
		}
	}
	
	/**
	 * Handle submission for editing a LocationTag (for editing its name/description)
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/admin/locations/locationTagEdit")
	public String handleEditSubmission(WebRequest request, @ModelAttribute("locationTag") LocationTag locationTag,
	        BindingResult result, SessionStatus status) {
		
		new LocationTagValidator().validate(locationTag, result);
		if (result.hasErrors()) {
			return "/admin/locations/locationTagEdit";
		} else {
			Context.getLocationService().saveLocationTag(locationTag);
			request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
			    "LocationTag.saved"), WebRequest.SCOPE_SESSION);
			status.setComplete();
			return "redirect:/admin/locations/locationTag.list";
		}
	}
	
	/**
	 * Purge a locationTag
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/admin/locations/locationTagPurge")
	public String purge(WebRequest request, @RequestParam("id") LocationTag locationTag) {
		Context.getLocationService().purgeLocationTag(locationTag);
		request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
		    "LocationTag.purged"), WebRequest.SCOPE_SESSION);
		return "redirect:/admin/locations/locationTag.list";
	}
	
	/**
	 * Retire a locationTag
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/admin/locations/locationTagRetire")
	public String retire(WebRequest request, @RequestParam("id") LocationTag locationTag,
	        @RequestParam("retireReason") String retireReason) {
		Context.getLocationService().retireLocationTag(locationTag, retireReason);
		request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
		    "LocationTag.retired"), WebRequest.SCOPE_SESSION);
		return "redirect:/admin/locations/locationTag.list";
	}
	
	/**
	 * Unretire a locationTag
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/admin/locations/locationTagUnretire")
	public String unretire(WebRequest request, @RequestParam("id") LocationTag locationTag) {
		Context.getLocationService().unretireLocationTag(locationTag);
		request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, Context.getMessageSourceService().getMessage(
		    "LocationTag.unretired"), WebRequest.SCOPE_SESSION);
		return "redirect:/admin/locations/locationTag.list";
	}
	
}
