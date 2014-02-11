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

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.util.Clock;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.SystemClock;

/**
 * A scheduled task that automatically closes all unvoided active visits that match the visit
 * started before number of days set as the value of the global property
 * {@link OpenmrsConstants#GP_VISIT_AUTO_CLOSE_MINIMUM_NUMBER_OF_DAYS}
 * type(s) set as the value of the global property
 * {@link OpenmrsConstants#GP_VISIT_TYPES_TO_AUTO_CLOSE}
 *
 * @since 1.9
 */
public class AutoCloseVisitsTask extends AbstractTask {
	
	private static final Log log = LogFactory.getLog(AutoCloseVisitsTask.class);
	
	private AdministrationService administrationService;
	
	private VisitService visitService;
	
	private Clock clock;
	
	public AutoCloseVisitsTask(AdministrationService administrationService, VisitService visitService, Clock clock) {
		this.administrationService = administrationService;
		this.visitService = visitService;
		this.clock = clock;
	}
	
	public AutoCloseVisitsTask() {
		this(Context.getAdministrationService(), Context.getVisitService(), new SystemClock());
	}
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#execute()
	 */
	@Override
	public void execute() {
		if (!isExecuting) {
			if (log.isDebugEnabled())
				log.debug("Starting Auto Close Visits Task...");
			
			startExecuting();
			try {
				String minimumNumberOfDaysProperty = administrationService
				        .getGlobalProperty(OpenmrsConstants.GP_VISIT_AUTO_CLOSE_MINIMUM_NUMBER_OF_DAYS);
				Integer minimumNumberOfDays = NumberUtils.toInt(minimumNumberOfDaysProperty,
				    OpenmrsConstants.GP_VISIT_AUTO_CLOSE_MINIMUM_NUMBER_OF_DAYS_DEFAULT_VALUE);
				visitService.stopVisits(DateUtils.addDays(clock.getCurrentTime(), -1 * minimumNumberOfDays));
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
