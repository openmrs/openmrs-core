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
package org.openmrs.scheduler.timer;

import java.util.TimerTask;

import org.openmrs.scheduler.Schedulable;

public class TimerTaskWrapper extends TimerTask { 
		/**	 *  The task that will be executed by the JDK timer. 	 */	private Schedulable schedulable; 	/**	 *  Public constructor	 */	public TimerTaskWrapper(Schedulable schedulable) { 
		this.schedulable = schedulable;	}

	/**	 *  The action to be performed by this timer task.  Required by JDK Timer Task interface.	 */	public void run() { 
		schedulable.run();	}

}
