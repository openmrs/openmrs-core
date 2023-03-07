/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.api.APIException;
import org.openmrs.module.webservices.docs.swagger.core.property.EnumProperty;
import org.openmrs.module.webservices.helper.TaskAction;
import org.openmrs.module.webservices.helper.TaskServiceWrapper;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Creatable;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.IllegalRequestException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.TaskDefinition;

import java.util.ArrayList;
import java.util.Collection;

@Resource(name = RestConstants.VERSION_1 + "/taskaction", supportedClass = TaskAction.class, supportedOpenmrsVersions = {
        "1.8.* - 9.*" })
public class TaskActionResource1_8 extends BaseDelegatingResource<TaskAction> implements Creatable {
	
	private TaskServiceWrapper taskServiceWrapper = new TaskServiceWrapper();
	
	public void setTaskServiceWrapper(TaskServiceWrapper taskServiceWrapper) {
		this.taskServiceWrapper = taskServiceWrapper;
	}
	
	@Override
	public Object create(SimpleObject post, RequestContext context) throws ResponseException {
		TaskAction action = newDelegate();
		setConvertedProperties(action, post, getCreatableProperties(), true);
		
		Collection<TaskDefinition> taskDefinitions;
		if (action.isAllTasks()) {
			taskDefinitions = taskServiceWrapper.getRegisteredTasks();
			action.setTasks(new ArrayList<TaskDefinition>(taskDefinitions));
		} else {
			taskDefinitions = action.getTasks();
		}
		
		TaskAction.Action actionType = action.getAction();
		if (actionType != TaskAction.Action.RESCHEDULEALLTASKS && (taskDefinitions == null || taskDefinitions.isEmpty())) {
			throw new IllegalRequestException("Cannot execute action " + actionType + " on empty set of task definitions.");
		}
		
		switch (action.getAction()) {
			case SCHEDULETASK:
				scheduleTasks(taskDefinitions);
				break;
			case RESCHEDULETASK:
				reScheduleTasks(taskDefinitions);
				break;
			case DELETE:
				deleteTasks(taskDefinitions);
				break;
			case SHUTDOWNTASK:
				shutDownTasks(taskDefinitions);
				break;
			case RESCHEDULEALLTASKS:
				reScheduleAllTasks();
				break;
			case RUNTASK:
				runTasks(taskDefinitions);
				break;
		}
		return ConversionUtil.convertToRepresentation(action, Representation.DEFAULT);
	}
	
	private void scheduleTasks(Collection<TaskDefinition> taskDefs) {
		for (TaskDefinition taskDef : taskDefs) {
			try {
				taskServiceWrapper.scheduleTask(taskDef);
			}
			catch (SchedulerException e) {
				throw new APIException("Errors occurred while scheduling task", e);
			}
		}
	}
	
	private void shutDownTasks(Collection<TaskDefinition> taskDefs) {
		for (TaskDefinition taskDef : taskDefs) {
			try {
				taskServiceWrapper.shutDownTask(taskDef);
			}
			catch (SchedulerException e) {
				throw new APIException("Errors occurred while shutdowning task", e);
			}
		}
	}
	
	// Stop and start a set of scheduled tasks.
	private void reScheduleTasks(Collection<TaskDefinition> taskDefs) {
		for (TaskDefinition taskDef : taskDefs) {
			try {
				taskServiceWrapper.reScheduleTask(taskDef);
			}
			catch (SchedulerException e) {
				throw new APIException("Errors occurred while rescheduling task", e);
			}
		}
	}
	
	// Stop and start all the tasks.
	private void reScheduleAllTasks() {
		try {
			taskServiceWrapper.reScheduleAllTasks();
		}
		catch (SchedulerException e) {
			throw new APIException("Errors occurred while rescheduling all tasks", e);
		}
		
	}
	
	private void deleteTasks(Collection<TaskDefinition> taskDefs) {
		for (TaskDefinition taskDef : taskDefs) {
			try {
				taskServiceWrapper.deleteTask(taskDef);
			}
			catch (SchedulerException e) {
				throw new APIException("Errors occurred while deleting task", e);
			}
		}
	}
	
	private void runTasks(Collection<TaskDefinition> taskDefs) {
		for (TaskDefinition taskDef : taskDefs) {
			try {
				taskServiceWrapper.runTask(taskDef);
			}
			catch (SchedulerException e) {
				throw new APIException("Errors occurred while running task", e);
			}
		}
	}
	
	@Override
	public TaskAction newDelegate() {
		return new TaskAction();
	}
	
	@Override
	public TaskAction save(TaskAction delegate) {
		throw new UnsupportedOperationException("TaskAction cannot be saved");
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("tasks", Representation.REF);
		description.addProperty("action", "action");
		return description;
	}
	
	@Override
	public TaskAction getByUniqueId(String uniqueId) {
		throw new UnsupportedOperationException("TaskAction can not get by id");
	}
	
	@Override
	protected void delete(TaskAction delegate, String reason, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("TaskAction can not be deleted");
	}
	
	@Override
	public void purge(TaskAction delegate, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("TaskAction can not be purged");
	}
	
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("tasks");
		description.addProperty("allTasks");
		description.addRequiredProperty("action", "action");
		return description;
	}
	
	@Override
	public Model getCREATEModel(Representation rep) {
		ModelImpl model = new ModelImpl();
		model.property("tasks", new ArrayProperty(new StringProperty()));
		model.property("allTasks", new BooleanProperty());
		model.property("action", new EnumProperty(TaskAction.Action.class));
		model.required("action");
		return model;
	}
	
	/**
	 * Converter does not handle getters starting with 'is' instead of 'get'
	 */
	@PropertyGetter("allModules")
	public Boolean isAllModules(TaskAction action) {
		return action.isAllTasks();
	}
	
}
