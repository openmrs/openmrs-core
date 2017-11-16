/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Describes when to start a task and how often it should be executed.
 */
public class Schedule {
	
	/**
	 * Schedule identifier
	 */
	private Integer id;
	
	/**
	 * Name of the schedule
	 */
	private String name;
	
	/**
	 * Description of the schedule
	 */
	private String description;
	
	/**
	 * Date and time to start the task
	 */
	private Date startTime;
	
	/**
	 * Interval used to determine the next execution time
	 */
	private long repeatInterval;
	
	/**
	 * Flag that indicates whether to begin as soon as the scheduler starts.
	 */
	private boolean startOnStartup;
	
	/**
	 * The date format pattern used to set the start date when start date is passed in as a string
	 */
	private String dateFormat;
	
	/**
	 * Default TODO Move to constants or properties
	 */
	public static final String DEFAULT_DATE_FORMAT = "MM-dd-yyyy hh:mm:ss";
	
	/**
	 * Date formatter used to format a date specified by a string
	 */
	private transient DateFormat dateFormatter;
	
	/**
	 * Constants
	 */
	public static final int MILLISECONDS_PER_SECOND = 1000;
	
	public static final int SECONDS_PER_MINUTE = 60;
	
	public static final int MINUTES_PER_HOUR = 60;
	
	public static final int HOURS_PER_DAY = 24;
	
	public static final int DAYS_PER_WEEK = 7;
	
	public static final int DAILY = MILLISECONDS_PER_SECOND * SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
	
	public static final int WEEKLY = DAILY * DAYS_PER_WEEK;
	
	/**
	 * Public no-arg constructor.
	 */
	public Schedule() {
		this(null, null, new Date(), 0);
	}
	
	/**
	 * Public constructor
	 * 
	 * @param startTime Date for when to start the task (does not need to be in the future if the
	 *            interval is specified).
	 * @param repeatInterval interval time in seconds to wait between executing task (&lt;= 0 indicates
	 *            that it should only be run once)
	 */
	public Schedule(Date startTime, long repeatInterval) {
		this(null, null, startTime, repeatInterval);
	}
	
	/**
	 * Public constructor
	 * 
	 * @param name
	 * @param description
	 * @param startTime Date for when to start the task (does not need to be in the future if the
	 *            interval is specified).
	 * @param repeatInterval interval time in seconds to wait between executing task (&lt;= 0 indicates
	 *            that it should only be run once)
	 */
	public Schedule(String name, String description, Date startTime, long repeatInterval) {
		this.name = name;
		this.description = description;
		this.startTime = startTime;
		this.repeatInterval = repeatInterval;
		this.dateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
	}
	
	/**
	 * Gets the identifier of the schedule.
	 * 
	 * @return the identifier of the schedule
	 */
	public Integer getId() {
		return this.id;
	}
	
	/**
	 * Sets the identifier of the schedule.
	 * 
	 * @param id the identifier of the schedule
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * Gets the name of the schedule.
	 * 
	 * @return the name of the schedule
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Sets the name of the schedule.
	 * 
	 * @param name the name of the schedule
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the description of the schedule.
	 * 
	 * @return the description of the schedule
	 */
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * Sets the description of the schedule.
	 * 
	 * @param description the description of the schedule
	 */
	public void setDescription(String description) {
		this.description = description;
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
	public long getRepeatInterval() {
		return repeatInterval;
	}
	
	/**
	 * Sets the number of seconds until task is executed again.
	 * 
	 * @param repeatInterval
	 */
	public void setRepeatInterval(long repeatInterval) {
		this.repeatInterval = repeatInterval;
	}
	
	/**
	 * Get the date format used to set the start time.
	 */
	public String getDateFormat() {
		return this.dateFormat;
	}
	
	/**
	 * Sets the date format used to set the start time.
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
		this.dateFormatter = new SimpleDateFormat(dateFormat);
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
	
}
