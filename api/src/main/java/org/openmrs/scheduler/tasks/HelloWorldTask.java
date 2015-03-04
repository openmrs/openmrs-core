/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.tasks;

/**
 * Implementation of a task that writes "Hello World" to a log file.
 * 
 * @version 1.0
 */
public class HelloWorldTask extends AbstractTask {
	
	// Thread
	private Thread thread;
	
	private boolean started;
	
	/**
	 * Public constructor.
	 */
	public HelloWorldTask() {
		thread = new Thread(new HelloWorldThread());
	}
	
	/**
	 * Illustrates stateless functionality as simply as possible. Not very useful in our system,
	 * except maybe as a polling thread that checks internet connectivity by opening a connection to
	 * an external URL. But even that isn't very useful unless it tells someone or something about
	 * the connectivity (i.e. calls another service method)
	 */
	public void execute() {
		synchronized (thread) {
			if (!started) {
				thread.start();
				started = true;
			}
		}
	}
}
