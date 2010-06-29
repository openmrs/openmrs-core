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
package org.openmrs.api.context;

import org.openmrs.api.APIException;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleException;
import org.openmrs.module.ModuleFactory;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.timer.TimerSchedulerTask;

import sun.reflect.Reflection;

/**
 * This class allows certain tasks to run with elevated privileges. Primary use is scheduling and
 * module startup when there is no user to authenticate as.
 */
public class Daemon {
	
	/**
	 * The uuid defined for the daemon user object
	 */
	protected static final String DAEMON_USER_UUID = "A4F30A1B-5EB9-11DF-A648-37A07F9C90FB";

	private static final ThreadLocal<Boolean> isDaemonThread = new ThreadLocal<Boolean>();
	
	/**
	 * This method should not be called directly. The {@link ModuleFactory#startModule(Module)}
	 * method uses this to start the given module in a new thread that is authenticated as the
	 * daemon user
	 * 
	 * @param module the module to start
	 * @returns the module returned from {@link ModuleFactory#startModuleInternal(Module)}
	 */
	public static Module startModule(final Module module) throws ModuleException {
		
		// create a new thread and execute that task in it
		DaemonThread startModuleThread = new DaemonThread() {
			
			@Override
			public void run() {
				isDaemonThread.set(true);
				try {
					Context.openSession();
					returnedObject = ModuleFactory.startModuleInternal(module);
				}
				catch (Throwable t) {
					exceptionThrown = t;
				}
				finally {
					Context.closeSession();
				}
			}
		};
		
		startModuleThread.start();
		
		// wait for the "startModule" thread to finish
		try {
			startModuleThread.join();
		}
		catch (InterruptedException e) {
			// ignore
		}
		
		if (startModuleThread.exceptionThrown != null) {
			if (startModuleThread.exceptionThrown instanceof ModuleException)
				throw (ModuleException) startModuleThread.exceptionThrown;
			else
				throw new ModuleException("Unable to start module as Daemon", startModuleThread.exceptionThrown);
		}
		
		return (Module) startModuleThread.returnedObject;

	}
	
	/**
	 * Executes the given task in a new thread that is authenticated as the daemon user. <br/>
	 * <br/>
	 * This can only be called from {@link TimerSchedulerTask} during actual task execution
	 * 
	 * @param task the task to run
	 * @should not be called from other methods other than TimerSchedulerTask
	 */
	public static void executeScheduledTask(final Task task) throws Throwable {
		
		// quick check to make sure we're only being called by ourselves
		Class<?> callerClass = Reflection.getCallerClass(0);
		if (callerClass.isAssignableFrom(TimerSchedulerTask.class))
			throw new APIException("This method can only be called from the TimerSchedulerTask class, not "
			        + callerClass.getName());
		
		// now create a new thread and execute that task in it
		DaemonThread executeTaskThread = new DaemonThread() {
			
			@Override
			public void run() {
				isDaemonThread.set(true);
				
				try {
					Context.openSession();
					task.execute();
				}
				catch (Throwable t) {
					exceptionThrown = t;
				}
				finally {
					Context.closeSession();
				}

			}
		};
		
		executeTaskThread.start();
		
		// wait for the "executeTaskThread" thread to finish
		try {
			executeTaskThread.join();
		}
		catch (InterruptedException e) {
			// ignore
		}
		
		if (executeTaskThread.exceptionThrown != null)
			throw executeTaskThread.exceptionThrown;

	}
	
	/**
	 * @return true if the current thread was started by this class and so is a daemon thread that
	 *         has all privileges
	 * @see Context#hasPrivilege(String)
	 */
	public static boolean isDaemonThread() {
		Boolean b = isDaemonThread.get();
		if (b == null)
			return false;
		else
			return b.booleanValue();
	}
	
	/**
	 * Thread class used by the {@link Daemon#startModule(Module)} and
	 * {@link Daemon#executeScheduledTask(Task)} methods so that the returned object and the
	 * exception thrown can be returned to calling class
	 */
	private static class DaemonThread extends Thread {
		
		/**
		 * The object returned from the method called in {@link #run()}
		 */
		protected Object returnedObject = null;
		
		/**
		 * The exception thrown (if any) by the method called in {@link #run()}
		 */
		protected Throwable exceptionThrown = null;
	}

}
