package org.openmrs.scheduler.timer;

import java.util.TimerTask;

import org.openmrs.scheduler.Schedulable;

public class TimerTaskWrapper extends TimerTask { 
	/** *  The task that will be executed by the JDK timer.  */private Schedulable schedulable; 	/**	 *  Public constructor	 */	public TimerTaskWrapper(Schedulable schedulable) { 
		this.schedulable = schedulable;	}

	/**	 *  The action to be performed by this timer task.  Required by JDK Timer Task interface.	 */	public void run() { 
		schedulable.run();	}


}
