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

import org.openmrs.module.webservices.helper.TaskServiceWrapper;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Resource for Task Definitions, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/taskdefinition", supportedClass = TaskDefinition.class, supportedOpenmrsVersions = {
        "1.8.* - 2.3.*" })
public class TaskDefinitionResource1_8 extends MetadataDelegatingCrudResource<TaskDefinition> {
	
	private TaskServiceWrapper taskServiceWrapper = new TaskServiceWrapper();
	
	public void setTaskServiceWrapper(TaskServiceWrapper taskServiceWrapper) {
		this.taskServiceWrapper = taskServiceWrapper;
	}
	
	@Override
	public TaskDefinition newDelegate() {
		return new TaskDefinition();
	}
	
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("taskClass");
			description.addProperty("startTime");
			description.addProperty("started");
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			description.addSelfLink();
			return description;
		} else if (rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("taskClass");
			description.addProperty("startTime");
			description.addProperty("lastExecutionTime");
			description.addProperty("repeatInterval");
			description.addProperty("startOnStartup");
			description.addProperty("startTimePattern");
			description.addProperty("started");
			description.addProperty("properties");
			description.addSelfLink();
			description.addLink("ref", ".?v=" + RestConstants.REPRESENTATION_REF);
			return description;
		} else if (rep instanceof RefRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("taskClass");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("name");
		description.addRequiredProperty("description");
		description.addRequiredProperty("taskClass");
		description.addRequiredProperty("startTime");
		description.addRequiredProperty("repeatInterval");
		description.addRequiredProperty("startOnStartup");
		description.addRequiredProperty("properties");
		return description;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#save(java.lang.Object)
	 */
	@Override
	public TaskDefinition save(TaskDefinition taskDefinition) {
		taskServiceWrapper.saveTaskDefinition(taskDefinition);
		return taskDefinition;
	}
	
	@Override
	public TaskDefinition getByUniqueId(String uniqueId) {
		TaskDefinition taskDefinition = taskServiceWrapper.getTaskByName(uniqueId);
		if (taskDefinition == null) {
			taskDefinition = taskServiceWrapper.getTaskById(Integer.parseInt(uniqueId));
		}
		return taskDefinition;
	}
	
	@Override
	public void purge(TaskDefinition delegate, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException("TaskAction cannot be purged");
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
	 */
	@Override
	public NeedsPaging<TaskDefinition> doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<TaskDefinition>(new ArrayList<TaskDefinition>(taskServiceWrapper.getRegisteredTasks()),
		        context);
	}
	
	@PropertyGetter("uuid")
	public static String getUuid(TaskDefinition instance) {
		return instance.getUuid();
	}
	
	@PropertyGetter("display")
	public static String getDisplay(TaskDefinition instance) {
		return instance.getName();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doSearch(org.openmrs.module.webservices.rest.web.RequestContext)
	 *      It will return only the scheduled tasks, if query contains string as scheduled otherwise
	 *      it will return the registered tasks
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String query = context.getParameter("q");
		List<TaskDefinition> taskDefinitions = null;
		if (query == "registered") {
			taskDefinitions = (ArrayList<TaskDefinition>) taskServiceWrapper.getRegisteredTasks();
		} else {
			taskDefinitions = (ArrayList<TaskDefinition>) taskServiceWrapper.getScheduledTasks();
		}
		return new NeedsPaging<TaskDefinition>(taskDefinitions, context);
	}
	
}
