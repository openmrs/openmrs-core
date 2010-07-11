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
package org.openmrs.scheduler.tasks;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ModuleRepository;

/**
 * Caches module meta data from online repository
 */
public class ModuleRepositoryCacheUpdateTask extends AbstractTask {
	
	private static final Log log = LogFactory.getLog(ModuleRepositoryCacheUpdateTask.class);

	@Override
	public void execute() {
		try {
			ModuleRepository.cacheModuleRepository();
		}
		catch (IOException e) {
			log.error("Couldn't execute Update Task", e);
		}
	}
}
