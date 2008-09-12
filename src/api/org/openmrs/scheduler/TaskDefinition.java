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
import org.openmrs.User;

/**
 * Represents the metadata for a task that can be scheduled.
 * 
 * @author Justin Miranda
 */
public class TaskDefinition {

	private Log log = LogFactory.getLog(this.getClass());

	// Task metadata
	private Integer id;
	private String name;
	private String description;
	private String taskClass; // This class must implement the schedulable
								// interface or it will fail to start

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

	// Metadata fields
	private User createdBy;
	private Date dateCreated;
	private User changedBy;
	private Date dateChanged;

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
		this.name = name;
		this.description = description;
		this.taskClass = taskClass;
	}



	/**
	 * Get the task identifier.
	 * 
	 * @return the task identifier
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
	 * Get the name of the task.
	 * 
	 * @return the name of the task
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Set the name of the task.
	 * 
	 * @param name of the task
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the description of the task.
	 * 
	 * @return the description of the task
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Set the name of the task.
	 * 
	 * @param name of the task
	 */
	public void setDescription(String description) {
		this.description = description;
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
	 * Set the name of the task.
	 * 
	 * @param name of the task
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
	 * @param schedulable schedulable object
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
	 * Set the start time for when the task should be executed. For instance,
	 * use "new Date()", if you want it to start now.
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
	 * @param
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
	 * Gets the flag that indicates whether we start on scheduler startup.
	 */
	public Boolean getStartOnStartup() {
		return this.startOnStartup;
	}

	/**
	 * Sets the flag that indicates whether we start on scheduler startup.
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
	 * @param key
	 * @return
	 */
	public String getProperty(String key) {
		return this.properties.get(key);
	}

	/**
	 * Set task configuration property. Only supports strings at the moment.
	 * 
	 * @param key
	 * @param value
	 */
	public void setProperty(String key, String value) {
		this.properties.put(key, value);
	}

	
	/**
	 * Convenience method that asks SchedulerUtil for 
	 * it's next execution time.  
	 * 
	 * @return
	 */
	public Date getNextExecutionTime() { 
		return SchedulerUtil.getNextExecution(this);
	}
	
	/**
	 * Convenience method to calculate the seconds until the next execution time.
	 * 
	 * @return
	 */
	public long getSecondsUntilNextExecutionTime() { 
		return (getNextExecutionTime().getTime() - System.currentTimeMillis()) / 1000;
		
	}
	
	// ==================================   Metadata ============================
	
	
	/**
	 * @return Returns the creator.
	 */
	public User getCreatedBy() {
		return this.createdBy;
	}

	/**
	 * @param creator The creator to set.
	 */
	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the changedBy.
	 */
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy The changedBy to set.
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	/**
	 * @return Returns the dateChanged.
	 */
	public Date getDateChanged() {
		return this.dateChanged;
	}

	/**
	 * @param dateChanged The dateChanged to set.
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
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
    	return 
    		"[TaskDefinition " + 
    			" id=" + getId() + 
    			" name=" + getName() + 
    			" class=" + getTaskClass() + 
    			" startTime=" + getStartTime() + 
    			" repeatInterval=" + this.getRepeatInterval() + 
    			" secondsUntilNext=" + this.getSecondsUntilNextExecutionTime() + 
    		"]";
    }
	
	
	
	

}
