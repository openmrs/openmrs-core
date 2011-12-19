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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.ConceptServiceImpl;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.util.OpenmrsConstants;

/**
 * A utility class for updating concept words in a scheduled task.
 */
public class ConceptIndexUpdateTask extends AbstractTask {
	
	private Log log = LogFactory.getLog(ConceptIndexUpdateTask.class);
	
	private boolean shouldExecute = true;
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#execute()
	 */
	@Override
	public void execute() {
		if (!isExecuting) {
			isExecuting = true;
			shouldExecute = true;
			AdministrationService as = Context.getAdministrationService();
			ConceptService cs = Context.getConceptService();
			GlobalProperty gp = as
			        .getGlobalPropertyObject(OpenmrsConstants.GP_CONCEPT_INDEX_UPDATE_TASK_LAST_UPDATED_CONCEPT);
			if (gp == null)
				gp = new GlobalProperty(OpenmrsConstants.GP_CONCEPT_INDEX_UPDATE_TASK_LAST_UPDATED_CONCEPT);
			
			if (log.isDebugEnabled())
				log.debug("Updating concept words ... ");
			try {
				Concept currentConcept = null; // assumes that all conceptIds are positive
				//check if we have a saved last updated concept id
				try {
					currentConcept = cs.getConcept(Integer.valueOf(gp.getPropertyValue()));
				}
				catch (NumberFormatException e) {
					//do nothing, most likely there was none
				}
				
				if (currentConcept == null)
					currentConcept = new Concept(0);
				
				currentConcept = cs.getNextConcept(currentConcept);
				int counter = 0;
				while (currentConcept != null && shouldExecute) {
					if (log.isDebugEnabled())
						log.debug("updateConceptWords() : current concept: " + currentConcept);
					cs.updateConceptIndex(currentConcept);
					
					// keep memory consumption low
					if (counter++ > 25) {
						gp.setPropertyValue(currentConcept.getConceptId().toString());
						as.saveGlobalProperty(gp);
						
						//persist to DB prior to releasing memory
						Context.flushSession();
						Context.clearSession();
						counter = 0;
					}
					
					currentConcept = cs.getNextConcept(currentConcept);
				}
				
				//we have reached the end, get rid of the GP
				if (currentConcept == null)
					as.purgeGlobalProperty(gp);
			}
			catch (APIException e) {
				log.error("ConceptWordUpdateTask failed, because:", e);
				throw e;
			}
			finally {
				isExecuting = false;
				shouldExecute = false;
				SchedulerService ss = Context.getSchedulerService();
				TaskDefinition conceptWordUpdateTaskDef = ss.getTaskByName(ConceptServiceImpl.CONCEPT_WORD_UPDATE_TASK_NAME);
				conceptWordUpdateTaskDef.setStarted(false);
				//This was need when upgrading to version1.8
				//Otherwise it will always return false
				if (conceptWordUpdateTaskDef.getStartOnStartup())
					conceptWordUpdateTaskDef.setStartOnStartup(false);
				ss.saveTask(conceptWordUpdateTaskDef);
				log.debug("Task set to stopped.");
			}
		}
	}
	
	/**
	 * @see org.openmrs.scheduler.Task#initialize(org.openmrs.scheduler.TaskDefinition)
	 */
	@Override
	public void initialize(TaskDefinition config) {
		// do nothing
	}
	
	/**
	 * @see org.openmrs.scheduler.Task#shutdown()
	 */
	@Override
	public void shutdown() {
		shouldExecute = false;
	}
	
}
