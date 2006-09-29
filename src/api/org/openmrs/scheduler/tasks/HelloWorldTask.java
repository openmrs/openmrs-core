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
