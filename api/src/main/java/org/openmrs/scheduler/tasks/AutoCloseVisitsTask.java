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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;

/**
 * A scheduled task that automatically closes all unvoided active visits that match the visit
 * type(s) set as the value of the global property
 * {@link OpenmrsConstants#GP_VISIT_TYPES_TO_AUTO_CLOSE}
 *
 * @since 1.9
 */
public class AutoCloseVisitsTask extends AbstractTask {
	
	private static final Log log = LogFactory.getLog(AutoCloseVisitsTask.class);
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#execute()
	 */
	@Override
	public void execute() {
		if (!isExecuting) {
			if (log.isDebugEnabled()) {
				log.debug("Starting Auto Close Visits Task...");
			}
			
			startExecuting();
			try {
				Context.getVisitService().stopVisits(new Date());
			}
			catch (Exception e) {
				log.error("Error while auto closing visits:", e);
			}
			finally {
				stopExecuting();
			}
		}
	}
}
