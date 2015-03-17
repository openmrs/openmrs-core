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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Shows the location hierarchy, in tree form
 */
@Controller
public class HierarchyController {
	
	@RequestMapping("/admin/locations/hierarchy")
	public void showHierarchy(ModelMap model) throws IOException {
		model.addAttribute("json", getHierarchyAsJson());
		model.addAttribute("locationWidgetType", Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_LOCATION_WIDGET_TYPE, "default"));
	}
	
	@RequestMapping("/admin/locations/changeLocationWidgetType")
	public String setWidgetType(@RequestParam("locationWidgetType") String widgetType) {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOCATION_WIDGET_TYPE, widgetType));
		return "redirect:hierarchy.list";
	}
	
	/**
	 * Gets JSON formatted for jstree jquery plugin [ { data: ..., children: ...}, ... ]
	 *
	 * @return
	 * @throws IOException
	 */
	private String getHierarchyAsJson() throws IOException {
		// TODO fetch all locations at once to avoid n+1 lazy-loads
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (Location loc : Context.getLocationService().getAllLocations()) {
			if (loc.getParentLocation() == null) {
				list.add(toJsonHelper(loc));
			}
		}
		
		// If this gets slow with lots of locations then switch out ObjectMapper for the
		// stream-based version. (But the TODO above is more likely to be a performance hit.)
		StringWriter w = new StringWriter();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(w, list);
		return w.toString();
	}
	
	/**
	 * { data: "Location's name (tags)", children: [ recursive calls to this method, ... ] }
	 *
	 * @param loc
	 * @return
	 */
	private Map<String, Object> toJsonHelper(Location loc) {
		Map<String, Object> ret = new LinkedHashMap<String, Object>();
		StringBuilder sb = new StringBuilder(getName(loc));
		if (loc.getTags() != null && loc.getTags().size() > 0) {
			sb.append(" (");
			for (Iterator<LocationTag> i = loc.getTags().iterator(); i.hasNext();) {
				LocationTag t = i.next();
				sb.append(getName(t));
				if (i.hasNext()) {
					sb.append(", ");
				}
			}
			sb.append(")");
		}
		ret.put("data", sb.toString());
		if (loc.getChildLocations() != null && loc.getChildLocations().size() > 0) {
			List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();
			for (Location child : loc.getChildLocations()) {
				children.add(toJsonHelper(child));
			}
			ret.put("children", children);
		}
		return ret;
	}
	
	private String getName(BaseOpenmrsMetadata element) {
		String name = StringEscapeUtils.escapeHtml(element.getName());
		name = StringEscapeUtils.escapeJavaScript(name);
		return element.isRetired() ? "<strike>" + name + "</strike>" : name;
	}
	
}
