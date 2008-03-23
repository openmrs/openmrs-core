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
import org.openmrs.scheduler.Schedulable;
import org.openmrs.scheduler.TaskConfig;

/**
 *  Implementation of a task that writes "Hello World" to a log file.
 *
 *  @author Justin Miranda
 *  @version 1.0
 */
public class HelloWorldTask implements Schedulable { 

	// Logger 
	private static Log log = LogFactory.getLog( HelloWorldTask.class );

	// Thread
	private Thread thread;

	// Instance of configuration information for task
	private TaskConfig taskConfig;	
	
	/**
	 * Public constructor.
	 */
	public HelloWorldTask() { 
		thread = new Thread( new HelloWorldThread() );
	}
	
	/**
	 * Initialize task.
	 * 
	 * @param config
	 */
	public void initialize(TaskConfig config) { 
		this.taskConfig = config;
	}	 
	

	/**
	 * Illustrates stateless functionality as simply as possible.  Not very 
	 * useful in our system, except maybe as a polling thread that checks internet
	 * connectivity by opening a connection to an external URL. 
	 *
	 * But even that isn't very useful unless it tells someone or something 
	 *  about the connectivity (i.e. calls another service method)    
	 */
	public void run() { 
		thread.start();
	}
}
