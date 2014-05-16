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
package org.openmrs.annotation;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleUtil;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Prevents creating a bean if profile is not matched. It returns true if a bean should not be created.
 */
public class OpenmrsProfileExcludeFilter implements TypeFilter {
	
	/**
	 * @param metadataReader
	 * @param metadataReaderFactory
	 * @return
	 * @throws IOException
	 *
	 * @should not include bean for openmrs from 1_6 to 1_7
	 * @should include bean for openmrs 1_10 and later
	 * @should not include bean for openmrs 1_8 and later if module missing
	 * @should include bean for openmrs 1_8 and later
	 */
	@Override
	public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
		Map<String, Object> openmrsProfileAttributes = metadataReader.getAnnotationMetadata().getAnnotationAttributes(
		    "org.openmrs.annotation.OpenmrsProfile");
		if (openmrsProfileAttributes != null) {
			return !matchOpenmrsProfileAttributes(openmrsProfileAttributes);
		} else {
			return false; //do not exclude
		}
	}
	
	public boolean matchOpenmrsProfileAttributes(Map<String, Object> openmrsProfile) {
		Object openmrsVersion = openmrsProfile.get("openmrsVersion");
		if (StringUtils.isNotBlank((String) openmrsVersion)) {
			if (!ModuleUtil.matchRequiredVersions(OpenmrsConstants.OPENMRS_VERSION_SHORT, (String) openmrsVersion)) {
				return false;
			}
		}
		
		String[] modules = (String[]) openmrsProfile.get("modules");
		
		for (String moduleAndVersion : modules) {
			String[] splitModuleAndVersion = moduleAndVersion.split(":");
			String moduleId = splitModuleAndVersion[0];
			String moduleVersion = splitModuleAndVersion[1];
			
			boolean moduleMatched = false;
			for (Module module : ModuleFactory.getStartedModules()) {
				if (module.getModuleId().equals(moduleId)) {
					if (ModuleUtil.matchRequiredVersions(module.getVersion(), moduleVersion)) {
						moduleMatched = true;
						break;
					}
				}
			}
			
			if (!moduleMatched) {
				return false;
			}
		}
		
		return true;
	}
}
