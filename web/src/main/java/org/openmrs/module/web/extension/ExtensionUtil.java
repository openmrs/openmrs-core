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
package org.openmrs.module.web.extension;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.openmrs.module.Extension;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.web.extension.provider.Link;

/**
 * Facilitates processing extensions.
 */
public class ExtensionUtil {
	
	private ExtensionUtil() {
		
	}
	
	/**
	 * Searches for all modules implementing {@link AddEncounterToVisitExtension} and returns the
	 * set of links.
	 * 
	 * @return the set of Links
	 * @should return empty set if there is no AddEncounterToVisitExtension
	 * @should return links if there are AddEncounterToVisitExtensions
	 */
	public static Set<Link> getAllAddEncounterToVisitLinks() {
		List<Extension> extensions = ModuleFactory
		        .getExtensions("org.openmrs.module.web.extension.AddEncounterToVisitExtension");
		
		if (extensions == null) {
			return Collections.emptySet();
		}
		
		Set<Link> links = new TreeSet<Link>(new Comparator<Link>() {
			
			@Override
			public int compare(Link o1, Link o2) {
				return o1.getLabel().compareTo(o2.getLabel());
			}
		});
		
		for (Extension extension : extensions) {
			AddEncounterToVisitExtension ext = (AddEncounterToVisitExtension) extension;
			
			Set<Link> tmpLinks = ext.getAddEncounterToVisitLinks();
			if (tmpLinks != null) {
				links.addAll(tmpLinks);
			}
		}
		
		return links;
	}
}
