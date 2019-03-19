/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleException;
import org.openmrs.module.ModuleFactory;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.timer.TimerSchedulerTask;
import org.openmrs.util.OpenmrsSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractRefreshableApplicationContext;

/**
 * This class allows certain tasks to run with elevated privileges. Primary use is scheduling and
 * module startup when there is no user to authenticate as.
 */
public class Daemon {

	private static final Logger log = LoggerFactory.getLogger(Daemon.class);
	
	/**
	 * The uuid defined for the daemon user object
	 */
	protected static final String DAEMON_USER_UUID = "A4F30A1B-5EB9-11DF-A648-37A07F9C90FB";
	
	protected static final ThreadLocal<Boolean> isDaemonThread = new ThreadLocal<>();
	
	protected static final ThreadLocal<User> daemonThreadUser = new ThreadLocal<>();
	
	/**
	 * @see #startModule(Module, boolean, AbstractRefreshableApplicationContext)
	 */
	public static Module startModule(Module module) throws ModuleException {
		return startModule(module, false, null);
	}
	
	/**
	 * This method should not be called directly. The {@link ModuleFactory#startModule(Module)}
	 * method uses this to start the given module in a new thread that is authenticated as the
	 * daemon user. <br>
	 * If a non null application context is passed in, it gets refreshed to make the module's
	 * services available
	 *
	 * @param module the module to start
	 * @param isOpenmrsStartup Specifies whether this module is being started at application startup
	 *            or not
	 * @param applicationContext the spring application context instance to refresh
	 * @return the module returned from {@link ModuleFactory#startModuleInternal(Module)}
	 */
	public static Module startModule(final Module module, final boolean isOpenmrsStartup,
	        final AbstractRefreshableApplicationContext applicationContext) throws ModuleException {
		// create a new thread and execute that task in it
		DaemonThread startModuleThread = new DaemonThread() {
			
			@Override
			public void run() {
				isDaemonThread.set(true);
				try {
					Context.openSession();
					returnedObject = ModuleFactory.startModuleInternal(module, isOpenmrsStartup, applicationContext);
				}
				catch (Exception e) {
					exceptionThrown = e;
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
			if (startModuleThread.exceptionThrown instanceof ModuleException) {
				throw (ModuleException) startModuleThread.exceptionThrown;
			} else {
				throw new ModuleException("Unable to start module as Daemon", startModuleThread.exceptionThrown);
			}
		}

		return (Module) startModuleThread.returnedObject;
	}

	/**
	 * This method should not be called directly, only {@link ContextDAO#createUser(User, String)} can
	 * legally invoke {@link #createUser(User, String)}.
	 * 
	 * @param user A new user to be created.
	 * @param password The password to set for the new user.
	 * @param roleNames A list of role names to fetch the roles to add to the user.
	 * @return The newly created user
	 * 
	 * @should only allow the creation of new users, not the edition of existing ones
	 * 
	 * @since 2.3.0
	 */
	public static User createUser(User user, String password, List<String> roleNames) throws Exception {

		// quick check to make sure we're only being called by ourselves
		Class<?> callerClass = new OpenmrsSecurityManager().getCallerClass(0);
		if (!ContextDAO.class.isAssignableFrom(callerClass)) {
			throw new APIException("Context.DAO.only", new Object[] { callerClass.getName() });
		}

		// create a new thread and execute that task in it
		DaemonThread createUserThread = new DaemonThread() {

			@Override
			public void run() {
				isDaemonThread.set(true);
				try {
					Context.openSession();

					if ( (user.getId() != null && Context.getUserService().getUser(user.getId()) != null) || Context.getUserService().getUserByUuid(user.getUuid()) != null || Context.getUserService().getUserByUsername(user.getUsername()) != null || (user.getEmail() != null && Context.getUserService().getUserByUsernameOrEmail(user.getEmail()) != null) ) {
						throw new APIException("User.creating.already.exists", new Object[] { user.getDisplayString() });
					}

					if (!CollectionUtils.isEmpty(roleNames)) {
						List<Role> roles = roleNames.stream().map(roleName -> Context.getUserService().getRole(roleName)).collect(Collectors.toList()); 
						roles.forEach(role -> user.addRole(role));
					}

					returnedObject = Context.getUserService().createUser(user, password);
				}
				catch (Exception e) {
					exceptionThrown = e;
				}
				finally {
					Context.closeSession();
				}
			}
		};

		createUserThread.start();

		// wait for the 'create user' thread to finish
		try {
			createUserThread.join();
		}
		catch (InterruptedException e) {
			// ignore
		}

		if (createUserThread.exceptionThrown != null) {
			throw createUserThread.exceptionThrown;
		}

		return (User) createUserThread.returnedObject;
	}	
	
	/**
	 * Executes the given task in a new thread that is authenticated as the daemon user. <br>
	 * <br>
	 * This can only be called from {@link TimerSchedulerTask} during actual task execution
	 *
	 * @param task the task to run
	 * @should not be called from other methods other than TimerSchedulerTask
	 * @should not throw error if called from a TimerSchedulerTask class
	 */
	public static void executeScheduledTask(final Task task) throws Exception {
		
		// quick check to make sure we're only being called by ourselves
		Class<?> callerClass = new OpenmrsSecurityManager().getCallerClass(0);
		if (!TimerSchedulerTask.class.isAssignableFrom(callerClass)) {
			throw new APIException("Scheduler.timer.task.only", new Object[] { callerClass.getName() });
		}
		
		// now create a new thread and execute that task in it
		DaemonThread executeTaskThread = new DaemonThread() {
			
			@Override
			public void run() {
				isDaemonThread.set(true);
				
				try {
					Context.openSession();
					TimerSchedulerTask.execute(task);
				}
				catch (Exception e) {
					exceptionThrown = e;
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
		
		if (executeTaskThread.exceptionThrown != null) {
			throw executeTaskThread.exceptionThrown;
		}
		
	}
	
	/**
	 * Call this method if you are inside a Daemon thread (for example in a Module activator or a
	 * scheduled task) and you want to start up a new parallel Daemon thread. You may only call this
	 * method from a Daemon thread.
	 *
	 * @param runnable what to run in a new thread
	 * @return the newly spawned {@link Thread}
	 * @should throw error if called from a non daemon thread
	 * @should not throw error if called from a daemon thread
	 */
	@SuppressWarnings("squid:S1217")
	public static Thread runInNewDaemonThread(final Runnable runnable) {
		// make sure we're already in a daemon thread
		if (!isDaemonThread()) {
			throw new APIAuthenticationException("Only daemon threads can spawn new daemon threads");
		}
		
		// we should consider making DaemonThread public, so the caller can access returnedObject and exceptionThrown
		DaemonThread thread = new DaemonThread() {
			
			@Override
			public void run() {
				isDaemonThread.set(true);
				try {
					Context.openSession();
					//Suppressing sonar issue "squid:S1217"
					//We intentionally do not start a new thread yet, rather wrap the run call in a session.
					runnable.run();
				}
				finally {
					Context.closeSession();
				}
			}
		};
		
		thread.start();
		return thread;
	}
	
	/**
	 * @return true if the current thread was started by this class and so is a daemon thread that
	 *         has all privileges
	 * @see Context#hasPrivilege(String)
	 */
	public static boolean isDaemonThread() {
		Boolean b = isDaemonThread.get();
		if (b == null) {
			return false;
		} else {
			return b;
		}
	}
	
	/**
	 * Calls the {@link OpenmrsService#onStartup()} method, as a daemon, for an instance
	 * implementing the {@link OpenmrsService} interface.
	 *
	 * @param service instance implementing the {@link OpenmrsService} interface.
	 * @since 1.9
	 */
	public static void runStartupForService(final OpenmrsService service) throws ModuleException {
		
		DaemonThread onStartupThread = new DaemonThread() {
			
			@Override
			public void run() {
				isDaemonThread.set(true);
				try {
					Context.openSession();
					service.onStartup();
				}
				catch (Exception e) {
					exceptionThrown = e;
				}
				finally {
					Context.closeSession();
				}
			}
		};
		
		onStartupThread.start();
		
		// wait for the "onStartup" thread to finish
		try {
			onStartupThread.join();
		}
		catch (InterruptedException e) {
			// ignore
			log.error("Thread was interrupted", e);
		}
		
		if (onStartupThread.exceptionThrown != null) {
			if (onStartupThread.exceptionThrown instanceof ModuleException) {
				throw (ModuleException) onStartupThread.exceptionThrown;
			} else {
				throw new ModuleException("Unable to run onStartup() method as Daemon", onStartupThread.exceptionThrown);
			}
		}
	}
	
	/**
	 * Executes the given runnable in a new thread that is authenticated as the daemon user.
	 *
	 * @param runnable an object implementing the {@link Runnable} interface.
	 * @param token the token required to run code as the daemon user
	 * @return the newly spawned {@link Thread}
	 * @since 1.9.2
	 */
	@SuppressWarnings("squid:S1217")
	public static Thread runInDaemonThread(final Runnable runnable, DaemonToken token) {
		if (!ModuleFactory.isTokenValid(token)) {
			throw new ContextAuthenticationException("Invalid token " + token);
		}
		
		DaemonThread thread = new DaemonThread() {
			
			@Override
			public void run() {
				isDaemonThread.set(true);
				try {
					Context.openSession();
					//Suppressing sonar issue "squid:S1217"
					//We intentionally do not start a new thread yet, rather wrap the run call in a session.
					runnable.run();
				}
				finally {
					Context.closeSession();
				}
			}
		};
		
		thread.start();
		return thread;
	}
	
	/**
	 * Executes the given runnable in a new thread that is authenticated as the daemon user and wait
	 * for the thread to finish.
	 *
	 * @param runnable an object implementing the {@link Runnable} interface.
	 * @param token the token required to run code as the daemon user
	 * @since 1.9.2
	 */
	public static void runInDaemonThreadAndWait(final Runnable runnable, DaemonToken token) {
		Thread daemonThread = runInDaemonThread(runnable, token);
		
		try {
			daemonThread.join();
		}
		catch (InterruptedException e) {
			//Ignore
		}
	}
	
	/**
	 * Thread class used by the {@link Daemon#startModule(Module)} and
	 * {@link Daemon#executeScheduledTask(Task)} methods so that the returned object and the
	 * exception thrown can be returned to calling class
	 */
	protected static class DaemonThread extends Thread {
		
		/**
		 * The object returned from the method called in {@link #run()}
		 */
		protected Object returnedObject = null;
		
		/**
		 * The exception thrown (if any) by the method called in {@link #run()}
		 */
		protected Exception exceptionThrown = null;
		
		/**
		 * Gets the exception thrown (if any) by the method called in {@link #run()}
		 *
		 * @return the thrown exception (if any).
		 */
		public Exception getExceptionThrown() {
			return exceptionThrown;
		}
	}
	
	/**
	 * Checks whether user is Daemon.
	 * However this is not the preferred method for checking to see if the current thread is a daemon thread,
	 * 				rather use Daemon.isDeamonThread().
	 * isDaemonThread is preferred for checking to see if you are in that thread or if the current thread is daemon.
	 *
	 * @param user user whom we are checking if daemon
	 * @return true if user is Daemon
	 * @should return true for a daemon user
	 * @should return false if the user is not a daemon
	 */
	public static boolean isDaemonUser(User user) {
		return DAEMON_USER_UUID.equals(user.getUuid());
	}
	
	/**
	 * @return the current thread daemon user or null if not assigned
	 * @since 2.0.0, 1.12.0, 1.11.6, 1.10.4, 1.9.11
	 */
	public static User getDaemonThreadUser() {
		if (isDaemonThread()) {
			User user = daemonThreadUser.get();
			if (user == null) {
				user = Context.getContextDAO().getUserByUuid(DAEMON_USER_UUID);
				daemonThreadUser.set(user);
			}
			return user;
		} else {
			return null;
		}
	}

	public static String getDaemonUserUuid() {
		return DAEMON_USER_UUID;
	}
}
