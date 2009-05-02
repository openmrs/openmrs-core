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
package org.openmrs.scheduler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.User;

/**
 * Represents the metadata for a task that can be scheduled.
 */
public class TaskDefinition extends BaseOpenmrsMetadata {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	// Task metadata
	private Integer id;
	
	// This class must implement the schedulable interface or it will fail to start
	private String taskClass;
	
	private Task taskInstance = null;
	
	// Scheduling metadata
	private Date startTime;
	
	private Long repeatInterval; // NOW in seconds to give us ability to
	
	// support longer intervals (years, decades,
	// milleniums)
	
	private Boolean startOnStartup;
	
	private String startTimePattern;
	
	private Boolean started;
	
	// Relationships
	private Map<String, String> properties;
	
	/**
	 * Default no-arg public constructor
	 */
	public TaskDefinition() {
		this.started = new Boolean(false); // default
		this.startTime = new Date(); // makes it easier during task creation
		// as we have a default date populated
		this.properties = new HashMap<String, String>();
	}
	
	/**
	 * Public constructor
	 */
	public TaskDefinition(Integer id, String name, String description, String taskClass) {
		this();
		log.debug("Creating taskconfig: " + id);
		this.id = id;
		setName(name);
		setDescription(description);
		this.taskClass = taskClass;
	}
	
	/**
	 * Get the task identifier.
	 * 
	 * @return <code>Integer</code> identifier of the task
	 */
	public Integer getId() {
		return this.id;
	}
	
	/**
	 * Set the task identifier.
	 * 
	 * @param id
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * Get the data map used to provide the task with runtime data.
	 * 
	 * @return the data map
	 */
	public Map<String, String> getProperties() {
		return this.properties;
	}
	
	/**
	 * Set the properties of the task. This overrides any properties previously set with the
	 * setProperty(String, String) method.
	 * 
	 * @param properties <code>Map<String, String></code> of the properties to set
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	/**
	 * Get the schedulable object to be executed.
	 * 
	 * @return the schedulable object
	 */
	public String getTaskClass() {
		return this.taskClass;
	}
	
	/**
	 * Set the schedulable object to be executed.
	 * 
	 * @param taskClass <code>String</code> taskClass of a schedulable object
	 */
	public void setTaskClass(String taskClass) {
		this.taskClass = taskClass;
	}
	
	/**
	 * Get the start time for when the task should be executed.
	 * 
	 * @return long start time
	 */
	public Date getStartTime() {
		return startTime;
	}
	
	/**
	 * Set the start time for when the task should be executed. For instance, use "new Date()", if
	 * you want it to start now.
	 * 
	 * @param startTime start time for the task
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * Gets the number of seconds until task is executed again.
	 * 
	 * @return long number of seconds.
	 */
	public Long getRepeatInterval() {
		return repeatInterval;
	}
	
	/**
	 * Sets the number of seconds until task is executed again.
	 * 
	 * @param repeatInterval number of seconds, or 0 to indicate to repetition
	 */
	public void setRepeatInterval(Long repeatInterval) {
		this.repeatInterval = repeatInterval;
	}
	
	/**
	 * Get the date format used to set the start time.
	 */
	public String getStartTimePattern() {
		return this.startTimePattern;
	}
	
	/**
	 * Sets the date format used to set the start time.
	 */
	public void setStartTimePattern(String pattern) {
		this.startTimePattern = pattern;
	}
	
	/**
	 * Gets the flag that indicates whether the task should startup as soon as the scheduler starts.
	 */
	public Boolean getStartOnStartup() {
		return this.startOnStartup;
	}
	
	/**
	 * Sets the flag that indicates whether the task should startup as soon as the scheduler starts.
	 */
	public void setStartOnStartup(Boolean startOnStartup) {
		this.startOnStartup = startOnStartup;
	}
	
	/**
	 * Gets the flag that indicates whether the task has been started.
	 */
	public Boolean getStarted() {
		return this.started;
	}
	
	/**
	 * Sets the flag that indicates whether the task has been started.
	 */
	public void setStarted(Boolean started) {
		this.started = started;
	}
	
	/**
	 * Get task configuration property.
	 * 
	 * @param key the <code>String</code> key of the property to get
	 * @return the <code>String</code> value for the given key
	 */
	public String getProperty(String key) {
		return this.properties.get(key);
	}
	
	/**
	 * Set task configuration property. Only supports strings at the moment.
	 * 
	 * @param key the <code>String</code> key of the property to set
	 * @param value the <code>String</code> value of the property to set
	 */
	public void setProperty(String key, String value) {
		this.properties.put(key, value);
	}
	
	/**
	 * Convenience method that asks SchedulerUtil for it's next execution time.
	 * 
	 * @return the <code>Date</code> of the next execution
	 */
	public Date getNextExecutionTime() {
		return SchedulerUtil.getNextExecution(this);
	}
	
	/**
	 * Convenience method to calculate the seconds until the next execution time.
	 * 
	 * @return the number of seconds until the next execution
	 */
	public long getSecondsUntilNextExecutionTime() {
		return (getNextExecutionTime().getTime() - System.currentTimeMillis()) / 1000;
		
	}
	
	// ==================================   Metadata ============================
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TaskDefinition) {
			TaskDefinition other = (TaskDefinition) obj;
			if (this.getId() != null) {
				this.getId().equals(other.getId());
			}
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (this.getId() == null) {
			return super.hashCode();
		}
		
		Integer hash = 5;
		return (this.getId() * hash);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[TaskDefinition " + " id=" + getId() + " name=" + getName() + " class=" + getTaskClass() + " startTime="
		        + getStartTime() + " repeatInterval=" + this.getRepeatInterval() + " secondsUntilNext="
		        + this.getSecondsUntilNextExecutionTime() + "]";
	}
	
	/**
	 * Gets the runnable task instance associated with this definition.
	 * 
	 * @return related task, or null if none instantiated (definition hasn't been scheduled)
	 */
	public Task getTaskInstance() {
		return taskInstance;
	}
	
	/**
	 * Sets the runnable task instance associated with this definition. This should be set by the
	 * scheduler which instantiates the task.
	 * 
	 * @param taskInstance
	 */
	public void setTaskInstance(Task taskInstance) {
		this.taskInstance = taskInstance;
	}
	
	/**
	 * @deprecated use {@link #getCreator()}
	 */
	public Object getCreatedBy() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @deprecated use {@link #setCreator(User)}
	 */
	public void setCreatedBy(User authenticatedUser) {
		// TODO Auto-generated method stub
		
	}
	
}
