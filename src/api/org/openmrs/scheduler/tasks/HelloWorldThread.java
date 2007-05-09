package org.openmrs.scheduler.tasks;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *  Implementation of the stateless task.
 */
public class HelloWorldThread implements Runnable { 

	// Logger 
	private static Log log = LogFactory.getLog( HelloWorldThread.class );

	/**
	 *  Public constructor.
	 */
	public HelloWorldThread() {  
		log.debug(" *** HelloWorldThread Constructor called");
		log.debug("HelloWorldThread was created at " + new Date());  
	}

	/**
	 *  Illustrates stateless functionality as simply as possible.  Not very 
	 *  useful in our system, except maybe as a polling thread that checks internet
	 *  connectivity by opening a connection to an external URL. 
	 *
	 *  But even that isn't very useful unless it tells someone or something 
	 *  about the connectivity (i.e. calls another service method)    
	 */
	public void run() { 
		log.debug("HelloWorldThread says Hello World!");
	}
}
