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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;

/**
 * A scheduled task that automatically closes all unvoided open visits of the specified visit
 * type(s).
 * 
 * @since 1.9
 */
public class AutoCloseOpenVisitsTask extends AbstractTask {
	
	private static final Log log = LogFactory.getLog(AutoCloseOpenVisitsTask.class);
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#execute()
	 */
	@Override
	public void execute() {
		if (isTaskEnabled() && !isExecuting) {
			if (log.isDebugEnabled())
				log.debug("Starting Auto Close Open Visits Task...");
			
			startExecuting();
			try {
				List<VisitType> visitTypesToStop = new ArrayList<VisitType>();
				String gpValue = Context.getAdministrationService().getGlobalProperty(
				    OpenmrsConstants.GP_VISIT_TYPES_TO_AUTO_CLOSE);
				
				VisitService vs = Context.getVisitService();
				if (StringUtils.isNotBlank(gpValue)) {
					String[] visitTypeNames = StringUtils.split(gpValue.trim(), ",");
					for (int i = 0; i < visitTypeNames.length; i++) {
						String currName = visitTypeNames[i];
						visitTypeNames[i] = currName.trim().toLowerCase();
					}
					
					List<VisitType> allVisitTypes = vs.getAllVisitTypes();
					for (VisitType visitType : allVisitTypes) {
						if (ArrayUtils.contains(visitTypeNames, visitType.getName().toLowerCase()))
							visitTypesToStop.add(visitType);
					}
				}
				
				if (visitTypesToStop.size() > 0) {
					int counter = 0;
					Visit lastStoppedVisit = vs.stopNextActiveVisit(new Visit(0), visitTypesToStop);
					while (isExecuting && lastStoppedVisit != null) {
						if (log.isDebugEnabled())
							log.debug("Successfully stopped visit with id:" + lastStoppedVisit.getVisitId());
						
						lastStoppedVisit = vs.stopNextActiveVisit(lastStoppedVisit, visitTypesToStop);
						if (counter++ > 50) {
							//ensure changes are persisted to DB before reclaiming memory
							Context.flushSession();
							Context.clearSession();
							counter = 0;
						}
					}
				} else {
					if (log.isWarnEnabled())
						log.warn("No valid visit types have been found for auto closing, you can specify them via the '"
						        + OpenmrsConstants.GP_VISIT_TYPES_TO_AUTO_CLOSE + "' global property");
				}
			}
			catch (Exception e) {
				log.error("Error while auto stopping open visits:", e);
			}
			finally {
				stopExecuting();
			}
			
		} else if (!isTaskEnabled()) {
			if (log.isWarnEnabled())
				log.warn("Can't start Auto Close Open Visits Task because it is disabled");
		}
	}
	
	/**
	 * Checks if this task is enabled or disabled via the
	 * {@link OpenmrsConstants#GP_AUTO_CLOSE_OPEN_VISITS_TASK} global property
	 * 
	 * @return true if the task is enabled otherwise false
	 */
	private static Boolean isTaskEnabled() {
		return Boolean.valueOf(Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GP_ENABLE_AUTO_CLOSE_OPEN_VISITS_TASK, "false"));
	}
	
	/**
	 * We need to override this methods so that we can set 'isExecuting' flag to false.
	 * 
	 * @see org.openmrs.scheduler.Task#shutdown()
	 */
	@Override
	public void shutdown() {
		if (log.isDebugEnabled())
			log.debug("Shutting down Auto Close Open Visits Task...");
		super.shutdown();
		stopExecuting();
	}
}
