/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.tasks;

import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionException;

/**
 * Legacy task definition for Observation Archiving. This class is registered in the database task
 * config and executes the Spring-managed handler.
 *
 * @since 3.0.0
 */
public class ObsArchivingTask extends AbstractTask {

	private static final Logger log = LoggerFactory.getLogger(ObsArchivingTask.class);

	@Override
	public void execute() {
		log.info("Starting Observation Archiving Task...");
		try {
			ObsArchivingTaskHandler handler = Context.getRegisteredComponent("obsArchivingTaskHandler",
			    ObsArchivingTaskHandler.class);
			if (handler != null) {
				handler.execute(null, null);
			} else {
				log.error("ObsArchivingTaskHandler bean not found.");
			}
		} catch (APIException | DataAccessException | TransactionException | NumberFormatException e) {
			log.error("Error occurred during Observation Archiving Task execution", e);
		}
		log.info("Observation Archiving Task completed.");
	}
}
