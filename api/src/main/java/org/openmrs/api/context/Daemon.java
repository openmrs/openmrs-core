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
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.api.db.hibernate.HibernateContextDAO;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleException;
import org.openmrs.module.ModuleFactory;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.timer.TimerSchedulerTask;
import org.openmrs.util.OpenmrsThreadPoolHolder;
import org.springframework.context.support.AbstractRefreshableApplicationContext;

/**
 * This class allows certain tasks to run with elevated privileges. Primary use is scheduling and
 * module startup when there is no user to authenticate as.
 */
public final class Daemon {
	
	/**
	 * The uuid defined for the daemon user object
	 */
	static final String DAEMON_USER_UUID = "A4F30A1B-5EB9-11DF-A648-37A07F9C90FB";
	
	private static final ThreadLocal<Boolean> isDaemonThread = new ThreadLocal<>();
	
	private static final ThreadLocal<User> daemonThreadUser = new ThreadLocal<>();
	
	/**
	 * private constructor to override the default constructor to prevent it from being instantiated.
	 */
	private Daemon() {
	}
	
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
	 * If a non-null application context is passed in, it gets refreshed to make the module's
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
		StackTraceElement[] stack = new Exception().getStackTrace();
		if (stack.length < 2) {
			throw new APIException("Could not determine where startModule() was called from");
		}
		StackTraceElement caller = stack[1];
		String callerClass = caller.getClassName();
		
		if (!Daemon.class.getName().equals(callerClass) && !ModuleFactory.class.getName().equals(callerClass)) {
			throw new APIException("Module.factory.only", new Object[] { callerClass });
		}
		
		Future<Module> moduleStartFuture = runInDaemonThreadInternal(() -> ModuleFactory.startModuleInternal(module, isOpenmrsStartup, applicationContext));
		
		// wait for the "startModule" thread to finish
		try {
			return moduleStartFuture.get();
		}
		catch (InterruptedException e) {
			// ignore
		} catch (ExecutionException e) {
			if (e.getCause() instanceof ModuleException) {
				throw (ModuleException) e.getCause();
			} else {
				throw new ModuleException("Unable to start module " + module.getName(), e);
			}
		}

		return module;
	}

	/**
	 * This method should not be called directly, only {@link ContextDAO#createUser(User, String, List)} can
	 * legally invoke this method.
	 * 
	 * @param user A new user to be created.
	 * @param password The password to set for the new user.
	 * @param roleNames A list of role names to fetch the roles to add to the user.
	 * @return The newly created user
	 * 
	 * <strong>Should</strong> only allow the creation of new users, not the edition of existing ones
	 * 
	 * @since 2.3.0
	 */
	public static User createUser(User user, String password, List<String> roleNames) throws Exception {
		// quick check to make sure we're only being called by ourselves
		StackTraceElement[] stack = new Exception().getStackTrace();
		if (stack.length < 2) {
			throw new APIException("Could not determine where createUser() was called from");
		}
		StackTraceElement caller = stack[1];
		String callerClass = caller.getClassName();
		
		if (!HibernateContextDAO.class.getName().equals(callerClass)) {
			throw new APIException("Context.DAO.only", new Object[] { callerClass });
		}

		// create a new thread and execute that task in it
		Future<User> userFuture = runInDaemonThreadInternal(() -> {
			if ((user.getId() != null && Context.getUserService().getUser(user.getId()) != null) || Context.getUserService().getUserByUuid(user.getUuid()) != null || Context.getUserService().getUserByUsername(user.getUsername()) != null || (user.getEmail() != null && Context.getUserService().getUserByUsernameOrEmail(user.getEmail()) != null) ) {
				throw new APIException("User.creating.already.exists", new Object[] { user.getDisplayString() });
			}

			if (!CollectionUtils.isEmpty(roleNames)) {
				List<Role> roles = roleNames.stream().map(roleName -> Context.getUserService().getRole(roleName)).collect(Collectors.toList());
				roles.forEach(user::addRole);
			}

			return Context.getUserService().createUser(user, password);
		});

		// wait for the 'create user' thread to finish
		try {
			return userFuture.get();
		}
		catch (InterruptedException e) {
			// ignore
		}
		catch (ExecutionException e) {
			if (e.getCause() instanceof Exception) {
				throw (Exception) e.getCause();
			} else {
				throw new RuntimeException(e.getCause());
			}
		}

		return null;
	}	
	
	/**
	 * Executes the given task in a new thread that is authenticated as the daemon user. <br>
	 * <br>
	 * This can only be called from {@link TimerSchedulerTask} during actual task execution
	 *
	 * @param task the task to run
	 * <strong>Should</strong> not be called from other methods other than TimerSchedulerTask
	 * <strong>Should</strong> not throw error if called from a TimerSchedulerTask class
	 */
	public static void executeScheduledTask(final Task task) throws Exception {
		// quick check to make sure we're only being called by ourselves
		StackTraceElement[] stack = new Exception().getStackTrace();
		if (stack.length < 2) {
			throw new APIException("Could not determine where executeScheduledTask() was called from");
		}
		StackTraceElement caller = stack[1];
		String callerClass = caller.getClassName();
		
		Class<?> callerClazz;
		try {
			callerClazz = Class.forName(callerClass);
		} catch (ClassNotFoundException e) {
			throw new APIException("Could not determine where executeScheduledTask() was called from", e);
		}
		
		if (!TimerSchedulerTask.class.isAssignableFrom(callerClazz)) {
			throw new APIException("Scheduler.timer.task.only", new Object[] { callerClass });
		}
		
		Future<?> scheduleTaskFuture = runInDaemonThreadInternal(() -> TimerSchedulerTask.execute(task));
		
		// wait for the "executeTaskThread" thread to finish
		try {
			scheduleTaskFuture.get();
		}
		catch (InterruptedException e) {
			// ignore
		} catch (ExecutionException e) {
			if (e.getCause() instanceof Exception) {
				throw (Exception) e.getCause();
			} else {
				throw new RuntimeException(e.getCause());
			}
		}
	}
	
	/**
	 * Call this method if you are inside a Daemon thread (for example in a Module activator or a
	 * scheduled task) and you want to start up a new parallel Daemon thread. You may only call this
	 * method from a Daemon thread.
	 *
	 * @param runnable what to run in a new thread
	 * @return the newly spawned {@link Thread}
	 * @deprecated As of 2.7.0, consider using {@link #runNewDaemonTask(Runnable)} instead
	 */
	@Deprecated
	public static Thread runInNewDaemonThread(final Runnable runnable) {
		// make sure we're already in a daemon thread
		if (!isDaemonThread()) {
			throw new APIAuthenticationException("Only daemon threads can spawn new daemon threads");
		}

		// the previous implementation ensured that Thread.start() was called before this function returned
		// since we cannot guarantee that the executor will run the thread when `execute()` is called, we need another
		// mechanism to ensure the submitted Runnable was actually started.
		final CountDownLatch countDownLatch = new CountDownLatch(1);
		
		// we should consider making DaemonThread public, so the caller can access returnedObject and exceptionThrown
		DaemonThread thread = new DaemonThread() {
			
			@Override
			public void run() {
				isDaemonThread.set(true);
				try {
					Context.openSession();
					countDownLatch.countDown();
					//Suppressing sonar issue "squid:S1217"
					//We intentionally do not start a new thread yet, rather wrap the run call in a session.
					runnable.run();
				}
				finally {
					try {
						Context.closeSession();
					} finally {
						isDaemonThread.remove();
						daemonThreadUser.remove();
					}
				}
			}
		};
		
		OpenmrsThreadPoolHolder.threadExecutor.execute(thread);

		// do not return until the thread is actually started to emulate the previous behaviour
		try {
			countDownLatch.await();
		} catch (InterruptedException ignored) {
		}

		return thread;
	}

	/**
	 * Call this method if you are inside a Daemon thread (for example in a Module activator or a
	 * scheduled task) and you want to start up a new parallel Daemon thread. You may only call this
	 * method from a Daemon thread.
	 *
	 * @param callable what to run in a new thread
	 * @return a future that completes when the task is done;
	 * @since 2.7.0
	 */
	@SuppressWarnings({"squid:S1217", "unused"})
	public static <T> Future<T> runInNewDaemonThread(final Callable<T> callable) {
		// make sure we're already in a daemon thread
		if (!isDaemonThread()) {
			throw new APIAuthenticationException("Only daemon threads can spawn new daemon threads");
		}

		return runInDaemonThreadInternal(callable);
	}

	/**
	 * Call this method if you are inside a Daemon thread (for example in a Module activator or a
	 * scheduled task) and you want to start up a new parallel Daemon thread. You may only call this
	 * method from a Daemon thread.
	 *
	 * @param runnable what to run in a new thread
	 * @return a future that completes when the task is done;
	 * @since 2.7.0
	 */
	@SuppressWarnings({"squid:S1217", "unused"})
	public static Future<?> runNewDaemonTask(final Runnable runnable) {
		// make sure we're already in a daemon thread
		if (!isDaemonThread()) {
			throw new APIAuthenticationException("Only daemon threads can spawn new daemon threads");
		}
		
		return runInDaemonThreadInternal(runnable);
	}
	
	/**
	 * @return true if the current thread was started by this class and so is a daemon thread that
	 *         has all privileges
	 * @see Context#hasPrivilege(String)
	 */
	public static boolean isDaemonThread() {
		Boolean b = isDaemonThread.get();
		if (b == null || !b) {
			// Allow functions in Daemon and WebDaemon to be treated as a DaemonThread
			StackTraceElement[] stack = new Exception().getStackTrace();
			if (stack.length < 3) {
				throw new APIException("Could not determine where change password was called from");
			}
			StackTraceElement caller = stack[2];
			String callerClass = caller.getClassName();
			
			return Daemon.class.getName().equals(callerClass) || "org.openmrs.web.WebDaemon".equals(callerClass);
		} else {
			return true;
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
		StackTraceElement[] stack = new Exception().getStackTrace();
		if (stack.length < 2) {
			throw new APIException("Could not determine where change password was called from");
		}
		StackTraceElement caller = stack[1];
		String callerClass = caller.getClassName();
		
		if (!ServiceContext.class.getName().equals(callerClass)) {
			throw new APIException("Service.context.only", new Object[] { callerClass });
		}
		
		Future<?> future = runInDaemonThreadInternal(service::onStartup);
		
		// wait for the "onStartup" thread to finish
		try {
			future.get();
		}
		catch (InterruptedException e) {
			// ignore
		}
		catch (ExecutionException e) {
			if (e.getCause() instanceof ModuleException) {
				throw (ModuleException) e.getCause();
			} else {
				throw new ModuleException("Unable to run onStartup() method of service {}", service.getClass().getSimpleName(), e);
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
	 * @deprecated Since 2.7.0 use {@link #runInDaemonThreadWithoutResult(Runnable, DaemonToken)} instead
	 */
	@Deprecated
	@SuppressWarnings({"squid:S1217", "unused"})
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
					try {
						Context.closeSession();
					} finally {
						isDaemonThread.remove();
						daemonThreadUser.remove();
					}
				}
			}
		};

		OpenmrsThreadPoolHolder.threadExecutor.execute(thread);
		return thread;
	}

	/**
	 * Executes the given runnable in a new thread that is authenticated as the daemon user.
	 *
	 * @param callable an object implementing the {@link Callable<T>} interface to be run
	 * @param token the token required to run code as the daemon user
	 * @return the newly spawned {@link Thread}
	 * @since 2.7.0
	 */
	@SuppressWarnings({"squid:S1217", "unused"})
	public static <T> Future<T> runInDaemonThread(final Callable<T> callable, DaemonToken token) {
		if (!ModuleFactory.isTokenValid(token)) {
			throw new ContextAuthenticationException("Invalid token");
		}
		
		return runInDaemonThreadInternal(callable);
	}

	/**
	 * Executes the given runnable in a new thread that is authenticated as the daemon user.
	 *
	 * @param runnable an object implementing the {@link Runnable} interface to be run
	 * @param token the token required to run code as the daemon user
	 * @return the newly spawned {@link Thread}
	 * @since 2.7.0
	 */
	@SuppressWarnings("squid:S1217")
	public static Future<?> runInDaemonThreadWithoutResult(final Runnable runnable, DaemonToken token) {
		if (!ModuleFactory.isTokenValid(token)) {
			throw new ContextAuthenticationException("Invalid token");
		}

		return runInDaemonThreadInternal(runnable);
	}
	
	/**
	 * Executes the given runnable in a new thread that is authenticated as the daemon user and wait
	 * for the thread to finish.
	 *
	 * @param runnable an object implementing the {@link Runnable} interface.
	 * @param token the token required to run code as the daemon user
	 * @since 2.7.0
	 */
	public static void runInDaemonThreadAndWait(final Runnable runnable, DaemonToken token) {
		Future<?> daemonThread = runInDaemonThreadWithoutResult(runnable, token);
		
		try {
			daemonThread.get();
		}
		catch (InterruptedException | ExecutionException e) {
			// Ignored
		}
	}
	
	private static <T> Future<T> runInDaemonThreadInternal(Callable<T> callable) {
		return OpenmrsThreadPoolHolder.threadExecutor.submit(() -> {
			isDaemonThread.set(true);
			try {
				Context.openSession();
				return callable.call();
			}
			finally {
				try {
					Context.closeSession();
				} finally {
					isDaemonThread.remove();
					daemonThreadUser.remove();
				}
			}
		});
	}
	
	private static Future<?> runInDaemonThreadInternal(Runnable runnable) {
		// for Threads, we used to guarantee that Thread.start() was called before the function returned
		// since we cannot guarantee that the executor actually started executing the thread, we use a CountDownLatch
		// to emulate this behaviour when the user submits a Thread. Other runnables are unaffected.
		CountDownLatch countDownLatch = getCountDownLatch(runnable instanceof Thread);

		Future<?> result = OpenmrsThreadPoolHolder.threadExecutor.submit(() -> {
			isDaemonThread.set(true);
			try {
				Context.openSession();
				countDownLatch.countDown();
				runnable.run();
			}
			finally {
				try {
					Context.closeSession();
				} finally {
					isDaemonThread.remove();
					daemonThreadUser.remove();
				}
			}
		});
		
		try {
			countDownLatch.await();
		} catch (InterruptedException ignored) {}
		
		return result;
	}

	private static CountDownLatch getCountDownLatch(boolean isThread) {
		return isThread ? new CountDownLatch(1) : new CountDownLatch(0);
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
	 * However, this is not the preferred method for checking to see if the current thread is a daemon thread,
	 * 				rather use {@link #isDaemonThread()}.
	 * isDaemonThread is preferred for checking to see if you are in that thread or if the current thread is daemon.
	 *
	 * @param user user whom we are checking if daemon
	 * @return true if user is Daemon
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
