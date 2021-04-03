/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.annotation;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleUtil;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

/**
 * Prevents creating a bean if profile is not matched. It returns true if a bean should not be created.
 */
public class OpenmrsProfileExcludeFilter implements TypeFilter {

	/**
	 * @param metadataReader
	 * @param metadataReaderFactory
	 * @return whether this filter matches
	 * @throws IOException
	 *
	 * <strong>Should</strong> not include bean for openmrs from 1_6 to 1_7
	 * <strong>Should</strong> include bean for openmrs 1_10 and later
	 * <strong>Should</strong> not include bean for openmrs 1_8 and later if module missing
	 * <strong>Should</strong> include bean for openmrs 1_8 and later
	 */
	@Override
	public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
		Map<String, Object> openmrsProfileAttributes = metadataReader.getAnnotationMetadata().getAnnotationAttributes(
				"org.openmrs.annotation.OpenmrsProfile");
		if (openmrsProfileAttributes != null) {
			return !matchOpenmrsProfileAttributes(openmrsProfileAttributes);
		} else {
			//do not exclude
			return false;
		}
	}

	public boolean matchOpenmrsProfileAttributes(Map<String, Object> openmrsProfile) {
		Object openmrsPlatformVersion = openmrsProfile.get("openmrsPlatformVersion");
		if (StringUtils.isBlank((String) openmrsPlatformVersion)) {
			//Left for backwards compatibility
			openmrsPlatformVersion = openmrsProfile.get("openmrsVersion");
		}

		if (StringUtils.isNotBlank((String) openmrsPlatformVersion)
				&& !ModuleUtil.matchRequiredVersions(OpenmrsConstants.OPENMRS_VERSION_SHORT, (String) openmrsPlatformVersion)) {
			return false;
		}

		String[] modules = (String[]) openmrsProfile.get("modules");

		for (String moduleAndVersion : modules) {
			if ("!".equals(moduleAndVersion.substring(0, 1))) {
				if (ModuleFactory.isModuleStarted(moduleAndVersion.substring(1))) {
					return false;
				}
			}
			else {
				String[] splitModuleAndVersion = moduleAndVersion.split(":");
				String moduleId = splitModuleAndVersion[0];
				String moduleVersion = splitModuleAndVersion[1];

				boolean moduleMatched = false;
				for (Module module : ModuleFactory.getStartedModules()) {
					if (module.getModuleId().equals(moduleId)
							&& ModuleUtil.matchRequiredVersions(module.getVersion(), moduleVersion)) {
						moduleMatched = true;
						break;
					}
				}

				if (!moduleMatched) {
					return false;
				}
			}
		}

		return true;
	}
}
