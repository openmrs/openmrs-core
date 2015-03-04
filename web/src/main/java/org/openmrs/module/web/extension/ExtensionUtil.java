/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
