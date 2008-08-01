package org.openmrs.scheduler;

import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;


/**
 *  Stateful task 
 *
 */
public abstract class StatefulTask extends AbstractTask { 

	// Private context that provides some state
	private Context context;
	
	/**
	 *  Set the context for this task.
	 */
	public void setContext(Context context) { 
		this.context = context;
	}
	
	/**
	 * Returns the context.
	 * 
	 * @return
	 */
	public Context getContext() { 
		return this.context;
	}
	

}

