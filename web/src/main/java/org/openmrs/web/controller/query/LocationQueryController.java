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
package org.openmrs.web.controller.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This class contains ajax-accessible queries relating to Locations
 */
@Controller
public class LocationQueryController {
	
	@RequestMapping("/q/locationHierarchy")
	public @ResponseBody
	List<Map<String, Object>> getHierarchyAsJson(@RequestParam("selectLeafOnly") boolean selectLeafOnly,
	        @RequestParam(value = "selectableTags", required = false) List<String> selectableTags,
	        @RequestParam(value = "startFromTag", required = false) String startFromTag,
	        @RequestParam(value = "includeNullOption", required = false) Boolean includeNullOption) throws IOException {
		HierarchyOptions options = new HierarchyOptions();
		options.selectOnlyLeaves = selectLeafOnly;
		options.selectableTags = selectableTags;
		options.startFromTag = startFromTag;
		options.includeNullOption = includeNullOption == null ? true : includeNullOption;
		return getHierarchy(options); // returning a POJO will be handled by a spring Converter
	}
	
	/**
	 * Gets JSON formatted for jstree jquery plugin [ { data: ..., children: ...}, ... ]
	 * 
	 * @return
	 * @throws IOException
	 */
	private List<Map<String, Object>> getHierarchy(HierarchyOptions options) throws IOException {
		// TODO find a way to fetch all locations at once to avoid n+1 lazy-loads
		List<Location> rootNodes = new ArrayList<Location>();
		if (options.startFromTag != null) {
			LocationTag tag = Context.getLocationService().getLocationTagByName(options.startFromTag);
			rootNodes.addAll(Context.getLocationService().getLocationsByTag(tag));
		} else {
			for (Location loc : Context.getLocationService().getAllLocations()) {
				if (loc.getParentLocation() == null) {
					rootNodes.add(loc);
				}
			}
		}
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if (options.includeNullOption) {
			list.add(toJsonHelper(null, options));
		}
		for (Location loc : rootNodes) {
			list.add(toJsonHelper(loc, options));
		}
		return list;
	}
	
	/**
	 * { data: "Location's name (tags)", children: [ recursive calls to this method, ... ] }
	 * 
	 * @param loc
	 * @return
	 */
	private Map<String, Object> toJsonHelper(Location loc, HierarchyOptions options) {
		if (loc == null) {
			String none = Context.getMessageSourceService().getMessage("general.none");
			Map<String, Object> attrs = new HashMap<String, Object>();
			attrs.put("id", 0);
			attrs.put("name", none);
			attrs.put("rel", "nulloption");
			
			Map<String, Object> ret = new LinkedHashMap<String, Object>();
			ret.put("attributes", attrs);
			ret.put("data", none);
			return ret;
			
		} else {
			String nodeType = isSelectable(loc, options) ? "selectable" : "default";
			
			Map<String, Object> attrs = new HashMap<String, Object>();
			attrs.put("id", loc.getLocationId());
			attrs.put("name", loc.getName());
			attrs.put("rel", nodeType);
			
			Map<String, Object> ret = new LinkedHashMap<String, Object>();
			ret.put("attributes", attrs);
			StringBuilder sb = new StringBuilder(loc.getName());
			if (loc.getTags() != null && loc.getTags().size() > 0) {
				sb.append(" (");
				for (Iterator<LocationTag> i = loc.getTags().iterator(); i.hasNext();) {
					LocationTag t = i.next();
					sb.append(t.getName());
					if (i.hasNext())
						sb.append(", ");
				}
				sb.append(")");
			}
			ret.put("data", sb.toString());
			if (loc.getChildLocations() != null && loc.getChildLocations().size() > 0) {
				List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();
				for (Location child : loc.getChildLocations())
					children.add(toJsonHelper(child, options));
				ret.put("children", children);
			}
			return ret;
		}
	}
	
	/**
	 * Can this node be selected given the specified options?
	 */
	private boolean isSelectable(Location loc, HierarchyOptions options) {
		if (options.selectOnlyLeaves && loc.getChildLocations() != null && loc.getChildLocations().size() > 0)
			return false;
		if (options.selectableTags != null && options.selectableTags.size() > 0) {
			for (String tag : options.selectableTags)
				if (loc.hasTag(tag))
					return true;
			return false;
		}
		return true;
	}
	
	class HierarchyOptions {
		
		public boolean includeNullOption = true;
		
		public boolean selectOnlyLeaves;
		
		public List<String> selectableTags;
		
		public String startFromTag;
	}
	
}
