/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.jobrunr;

import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.JobDetails;
import org.jobrunr.jobs.JobId;
import org.jobrunr.jobs.RecurringJob;
import org.jobrunr.jobs.states.EnqueuedState;
import org.jobrunr.jobs.states.JobState;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.scheduling.interval.Interval;
import org.jobrunr.storage.JobNotFoundException;
import org.jobrunr.storage.PageRequest;
import org.jobrunr.storage.StorageException;
import org.jobrunr.storage.StorageProvider;
import org.jspecify.annotations.NonNull;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.scheduler.RecurringTaskDetails;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskData;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.scheduler.TaskDetails;
import org.openmrs.scheduler.TaskState;
import org.openmrs.scheduler.db.SchedulerDAO;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.util.UuidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @since 2.9.0
 */
@Transactional
public class JobRunrSchedulerService extends BaseOpenmrsService implements SchedulerService {
	
	private static final Logger log = LoggerFactory.getLogger(JobRunrSchedulerService.class);
	
	private JobRequestScheduler jobRequestScheduler;
	
	private JobScheduler jobScheduler;
	
	private StorageProvider storageProvider;
	
	private SchedulerDAO schedulerDAO;
	
	private JobRunrServerLifecycle jobRunrServerLifecycle;
	
	public JobRunrSchedulerService(StorageProvider storageProvider, JobRequestScheduler jobRequestScheduler, 
								   JobScheduler jobScheduler, SchedulerDAO schedulerDAO) {
		this.jobRequestScheduler = jobRequestScheduler;
		this.jobScheduler = jobScheduler;
		this.storageProvider = storageProvider;
		this.schedulerDAO = schedulerDAO;
	}
	
	public void setJobRunrServerLifecycle(JobRunrServerLifecycle jobRunrServerLifecycle) {
		this.jobRunrServerLifecycle = jobRunrServerLifecycle;
	}
	
	/**
	 * JobRunr rejects recurring intervals shorter than this.
	 */
	private static final long MIN_INTERVAL_SECONDS = 5;
	
	@Override
	public void onStartup() {
		jobRunrServerLifecycle.openmrsStarted();
		int registered = 0;
		int failed = 0;
		for (TaskDefinition taskDefinition: schedulerDAO.getTasks()) {
			// Isolate failures: one misconfigured task must not leave every later task unregistered (defect 3)
			try {
				registerOnStartup(taskDefinition);
				registered++;
			}
			catch (Exception e) {
				failed++;
				log.error("Failed to register task {} [{}] on startup; continuing with remaining tasks",
					taskDefinition.getName(), taskDefinition.getUuid(), e);
			}
		}
		log.info("Scheduler startup: {} task definitions processed, {} failed to register", registered, failed);
	}
	
	private void registerOnStartup(TaskDefinition taskDefinition) throws SchedulerException {
		String name = taskDefinition.getName() != null ? taskDefinition.getName() : taskDefinition.getTaskClass();
		
		if (Boolean.TRUE.equals(taskDefinition.getStartOnStartup())) {
			enqueueStartupRun(taskDefinition, name);
		}
		
		// 2.8.x scheduled tasks flagged start_on_startup at boot; 2.9.0 also tracks started. Leave tasks that are
		// neither alone, so a task an administrator stopped is not silently restarted by a deploy.
		if (!Boolean.TRUE.equals(taskDefinition.getStarted()) && !Boolean.TRUE.equals(taskDefinition.getStartOnStartup())) {
			return;
		}
		
		if (isRecurring(taskDefinition)) {
			// Decide on the recurring registration alone. The startup run above uses the task uuid as its job id,
			// so looking the task up as a one-off job would always find it and skip registration (defect 4).
			Optional<RecurringJob> existing = getRecurringJob(taskDefinition.getUuid());
			if (!existing.isPresent()) {
				scheduleTask(taskDefinition);
				log.info("Scheduled legacy task {} [{}] to run every {}s", name, taskDefinition.getUuid(),
					taskDefinition.getRepeatInterval());
			} else if (!hasExpectedInterval(existing.get(), taskDefinition)) {
				// Heal registrations written by earlier 2.9.0 builds with a placeholder period
				log.warn("Recurring task {} [{}] is registered with schedule {} instead of every {}s; re-registering",
					name, taskDefinition.getUuid(), existing.get().getScheduleExpression(),
					taskDefinition.getRepeatInterval());
				deleteRecurringTask(taskDefinition.getUuid());
				scheduleTask(taskDefinition);
			}
		} else if (!Boolean.TRUE.equals(taskDefinition.getStartOnStartup())
			&& !getTask(taskDefinition.getUuid()).isPresent()) {
			scheduleTask(taskDefinition);
			log.info("Scheduled one-off legacy task {} [{}]", name, taskDefinition.getUuid());
		}
	}
	
	/**
	 * Enqueues the run-on-startup execution, at most once across replicas booting at the same time.
	 * <p>
	 * The task uuid is the job id, so every replica targets the same {@code jobrunr_jobs} row. The row is
	 * written with JobRunr's optimistic locking rather than read, deleted and re-inserted: a finished run
	 * from a previous boot is replaced by an update conditional on the version just read, and a missing
	 * row by an insert that fails on the primary key if another replica got there first. Whichever replica
	 * loses the race sees the conflict and leaves the other's run alone, so a queued or running job is
	 * never deleted and the task does not run twice.
	 */
	private void enqueueStartupRun(TaskDefinition taskDefinition, String name) {
		UUID id = UUID.fromString(taskDefinition.getUuid());
		String scheduledBy = taskDefinition.getCreator() != null ? taskDefinition.getCreator().getSystemId() : "daemon";
		JobDetails jobDetails = new JobDetails(new JobRequestAdapter(taskDefinition, scheduledBy));
		
		Job run;
		try {
			Job previous = storageProvider.getJobById(id);
			StateName state = previous.getState();
			if (state == StateName.ENQUEUED || state == StateName.PROCESSING || state == StateName.SCHEDULED) {
				log.debug("Startup run of {} already queued by another node ({})", name, state);
				return;
			}
			// Same id and version as the finished run, so the save only succeeds if no other node changed it
			List<JobState> history = new ArrayList<>(previous.getJobStates());
			history.add(new EnqueuedState());
			run = new Job(id, previous.getVersion(), jobDetails, history, new ConcurrentHashMap<>());
		}
		catch (JobNotFoundException e) {
			run = new Job(id, 0, jobDetails, new ArrayList<>(Collections.singletonList(new EnqueuedState())),
				new ConcurrentHashMap<>());
		}
		run.setJobName(name);
		
		try {
			storageProvider.save(run);
			log.info("Scheduled legacy task {} [{}] to run on startup", name, taskDefinition.getUuid());
		}
		catch (StorageException e) {
			// ConcurrentJobModificationException on the versioned update, or a duplicate key on the insert:
			// another node wrote this row after we read it, so its run stands
			if (jobExists(id)) {
				log.debug("Startup run of {} was queued concurrently by another node", name, e);
			} else {
				throw e;
			}
		}
	}
	
	private boolean jobExists(UUID id) {
		try {
			storageProvider.getJobById(id);
			return true;
		}
		catch (JobNotFoundException e) {
			return false;
		}
	}
	
	private static boolean isRecurring(TaskDefinition task) {
		return task.getRepeatInterval() != null && task.getRepeatInterval() > 0;
	}
	
	private static boolean hasExpectedInterval(RecurringJob recurringJob, TaskDefinition task) {
		return Duration.ofSeconds(task.getRepeatInterval()).toString().equals(recurringJob.getScheduleExpression());
	}

	@Override
	public void onShutdown() {
		// the webapp never closes its application context, so the lifecycle would not stop the server
		jobRunrServerLifecycle.stop();
	}

	@Override
	public String getStatus(Integer id) {
		TaskDefinition task = getTask(id);
		if (task != null && Boolean.TRUE.equals(task.getStarted())) {
			return "Scheduled";
		}
		return "Not Running";
	}

	@Override
	public void shutdownTask(TaskDefinition task) throws SchedulerException {
		if (task != null) {
			getTask(task.getUuid()).ifPresent(t -> deleteTask(task.getUuid()));
			getRecurringTask(task.getUuid()).ifPresent(t -> deleteRecurringTask(task.getUuid()));
			task.setStarted(false);
			saveTaskDefinition(task);
		}
	}

	@Override
	public Task scheduleTask(TaskDefinition task) throws SchedulerException {
		if (task != null) {
			try {
				String name = task.getName();
				if (name == null) {
					name = task.getTaskClass();
				}
				String scheduledBy = task.getCreator() != null ? task.getCreator().getSystemId() : "daemon";
				
				if (isRecurring(task)) {
					registerRecurring(task, name, scheduledBy);
				} else if (task.getStartTime() != null){
					Instant runAt = task.getStartTime().toInstant();
					JobId jobId = jobRequestScheduler.schedule(UUID.fromString(task.getUuid()), runAt, 
						new JobRequestAdapter(task, scheduledBy));
					updateJobWithName(jobId, name);
				} else {
					JobId jobId = jobRequestScheduler.enqueue(UUID.fromString(task.getUuid()),
						new JobRequestAdapter(task, scheduledBy));
					updateJobWithName(jobId, name);
				}
				task.setStarted(true);
				if (task.getId() != null) {
					schedulerDAO.updateTask(task);
				} else {
					schedulerDAO.createTask(task);
				}
			}
			catch (Exception e) {
				throw new SchedulerException("Failed to schedule task", e);
			}
		}
		return null;
	}

	/**
	 * Registers a recurring job that always repeats at the task's configured interval.
	 * <p>
	 * JobRunr computes interval runs as {@code createdAt + n * interval}, so anchoring {@code createdAt} at the
	 * task's start time keeps the 2.8.x semantics of {@code start_time} (it fixes the phase of the schedule,
	 * e.g. "daily at 02:00") without the placeholder period and fix-up job used previously, which could never
	 * run because the fix-up executed without a Hibernate session.
	 */
	private void registerRecurring(TaskDefinition task, String name, String scheduledBy) {
		long intervalSeconds = task.getRepeatInterval();
		if (intervalSeconds < MIN_INTERVAL_SECONDS) {
			throw new IllegalArgumentException("Task " + name + " has repeat interval " + intervalSeconds
				+ "s; the minimum supported interval is " + MIN_INTERVAL_SECONDS + "s");
		}
		Instant anchor = task.getStartTime() != null ? task.getStartTime().toInstant() : Instant.now();
		RecurringJob recurringJob = new RecurringJob(task.getUuid(),
			new JobDetails(new JobRequestAdapter(task, scheduledBy)),
			new Interval(Duration.ofSeconds(intervalSeconds)), ZoneId.systemDefault(), anchor);
		recurringJob.setJobName(name);
		storageProvider.saveRecurringJob(recurringJob);
	}
	
	/**
	 * Fix-up job target created by earlier 2.9.0 builds. Retained only so that jobs already serialized in
	 * {@code jobrunr_jobs} still resolve; new registrations no longer schedule it.
	 * <p>
	 * It is invoked by JobRunr directly on the service target, outside the transactional proxy and outside
	 * {@link org.openmrs.api.context.Daemon}, so it opens its own session.
	 *
	 * @param uuid the task uuid
	 * @deprecated recurring tasks are now registered with their real interval directly; kept only for jobs
	 *             already serialized by earlier 2.9.0-SNAPSHOT builds
	 */
	@Deprecated
	public void scheduleRecurrently(String uuid) {
		Context.openSession();
		try {
			TaskDefinition task = getTaskByUuid(uuid);
			// Fix-up jobs from earlier builds have random ids, so shutdownTask() does not delete them. Do not let
			// one re-create the recurring job of a task that has since been stopped or deleted.
			if (task == null || !Boolean.TRUE.equals(task.getStarted())) {
				log.info("Ignoring obsolete fix-up job for task [{}]: the task is not started", uuid);
				return;
			}
			if (isRecurring(task)) {
				String scheduledBy = task.getCreator() != null ? task.getCreator().getSystemId() : "daemon";
				registerRecurring(task, task.getName() != null ? task.getName() : task.getTaskClass(), scheduledBy);
			}
		}
		finally {
			Context.closeSession();
		}
	}

	@Override
	public Task rescheduleTask(TaskDefinition task) throws SchedulerException {
		shutdownTask(task);
		return scheduleTask(task);
	}

	@Override
	public void rescheduleAllTasks() throws SchedulerException {
		for (TaskDefinition task : getScheduledTasks()) {
			try {
				rescheduleTask(task);
			}
			catch (Exception e) {
				log.error("Failed to reschedule task " + task.getName(), e);
			}
		}
	}

	@Override
	public Collection<TaskDefinition> getScheduledTasks() {
		return getRegisteredTasks().stream()
				.filter(t -> Boolean.TRUE.equals(t.getStarted()))
				.collect(Collectors.toList());
	}

	@Override
	public Collection<TaskDefinition> getRegisteredTasks() {
		return schedulerDAO.getTasks();
	}

	@Override
	public TaskDefinition getTask(Integer id) {
		return schedulerDAO.getTask(id);
	}

	@Override
	public TaskDefinition getTaskByUuid(String uuid) {
		return schedulerDAO.getTaskByUuid(uuid);
	}

	@Override
	public TaskDefinition getTaskByName(String name) {
		try {
			return schedulerDAO.getTaskByName(name);
		} catch (ObjectRetrievalFailureException e) {
			log.warn("getTaskByName({}) failed, because: {}", name, e.toString());
			return null;
		}
	}

	@Override
	public void deleteTask(Integer id) {
		TaskDefinition task = getTask(id);
		if (task != null) {
			if (Boolean.TRUE.equals(task.getStarted())) {
				try {
					shutdownTask(task);
				} catch (SchedulerException e) {
					throw new APIException(e);
				}
			}
			schedulerDAO.deleteTask(id);
		}
	}

	@Override
	public void saveTaskDefinition(TaskDefinition task) {
		if (task.getId() != null) {
			schedulerDAO.updateTask(task);
		} else {
			schedulerDAO.createTask(task);
		}
	}

	@Override
	public void scheduleIfNotRunning(TaskDefinition taskDef) {
		if (taskDef != null && !Boolean.TRUE.equals(taskDef.getStarted())) {
			try {
				scheduleTask(taskDef);
			} catch (SchedulerException e) {
				throw new APIException(e);
			}
		}
	}
	
	@Override
	public Optional<TaskDetails> getTask(String uuid) {
		try {
			Job job = storageProvider.getJobById(JobId.parse(uuid));
			if (!hasPrivileges(job.getJobDetails())) {
				return Optional.empty();
			} else {
				return Optional.of(new JobRunrTaskDetails(job));
			}
		} catch (JobNotFoundException e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<RecurringTaskDetails> getRecurringTask(String uuid) {
		return storageProvider.getRecurringJobs().stream()
			.filter(r -> r.getId().equals(uuid) && hasPrivileges(r.getJobDetails()))
			.findFirst().map(JobRunrRecurringTaskDetails::new);
	}
	
	@Override
	public void deleteTask(String uuid) {
		if (getTask(uuid).isPresent()) {
			JobId jobId = JobId.parse(uuid);
			jobRequestScheduler.delete(jobId);
			storageProvider.deletePermanently(jobId.asUUID());
		}
	}
	
	@Override
	public void deleteRecurringTask(String uuid) {
		if (getRecurringTask(uuid).isPresent()) {
			jobRequestScheduler.delete(uuid);
			// Delete any already scheduled task as well
			getTasks(TaskState.SCHEDULED, Instant.now()).forEach(
				t -> t.getRecurringTaskUuid().ifPresent(taskUuid -> {
					if (taskUuid.equals(uuid)) {
						deleteTask(t.getUuid());
					}
				})
			);
		}
	}
	
	@Override
	public Stream<TaskDetails> getTasks(TaskState state, Instant before) {
		return StreamSupport.stream(new Spliterators.AbstractSpliterator<TaskDetails>(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL) {
			private int offset = 0;
			private Iterator<Job> currentBatch;
			private final boolean isSchedulerManager = Context.hasPrivilege(PrivilegeConstants.MANAGE_SCHEDULER);
			private final User user = Context.getAuthenticatedUser();
			private final String userSystemId = user != null ? user.getSystemId() : null;

			@Override
			public boolean tryAdvance(Consumer<? super TaskDetails> action) {
				if (currentBatch == null || !currentBatch.hasNext()) {
					int limit = 100;
					List<Job> jobs = storageProvider.getJobs(StateName.valueOf(state.name()), before, PageRequest.ascOnUpdatedAt(offset, limit));
					if (jobs == null || jobs.isEmpty()) {
						return false;
					}
					if (!isSchedulerManager) {
						// List only tasks for the currently authenticated user
						currentBatch = jobs.stream().filter(job -> userSystemId != null && isScheduledBy(job.getJobDetails(), userSystemId)).iterator();
					} else {
						currentBatch = jobs.iterator();
					}
					offset += jobs.size();
				}
				
				if (currentBatch.hasNext()) {
					action.accept(new JobRunrTaskDetails(currentBatch.next()));
					return true;
				}
				return false;
			}
		}, false);
	}
	
	public boolean hasPrivileges(JobDetails jobDetails) {
		if (Context.hasPrivilege(PrivilegeConstants.MANAGE_SCHEDULER)) {
			return true;
		}
		User user = Context.getAuthenticatedUser();
		return user != null && isScheduledBy(jobDetails, user.getSystemId());
	}

	public boolean isScheduledBy(JobDetails jobDetails, String userSystemId) {
		return jobDetails.getJobParameters().stream()
			.anyMatch(jp -> {
				if (jp.getObject() instanceof JobRequestAdapter) {
					JobRequestAdapter jobRequestAdapter = (JobRequestAdapter) jp.getObject();
					return jobRequestAdapter.getUserSystemId().equals(userSystemId);
				}
				return false;
			});
	}
	
	@Override
	public Stream<RecurringTaskDetails> getRecurringTasks() {
		boolean isSchedulerManager = Context.hasPrivilege(PrivilegeConstants.MANAGE_SCHEDULER);
		User user = Context.getAuthenticatedUser();
		String userSystemId = user != null ? user.getSystemId() : null;
		return storageProvider.getRecurringJobs().stream().filter(
			j -> isSchedulerManager || (userSystemId != null && isScheduledBy(j.getJobDetails(), userSystemId)))
			.map(JobRunrRecurringTaskDetails::new);
	}

	@Override
	public Stream<RecurringTaskDetails> getRecurringTasksByName(String name) {
		return getRecurringTasks().filter(task -> task.getName().equals(name));
	}

	@Override
	public TaskDetails schedule(TaskData taskData) {
		return schedule(taskData, taskData.getClass().getSimpleName());
	}

	@Override
	public TaskDetails schedule(TaskData taskData, String name) {
		return schedule(UuidUtil.newUuidString(), taskData, name);
	}

	@Override
	public TaskDetails schedule(String uuid, TaskData taskData) {
		return schedule(uuid, taskData, taskData.getClass().getSimpleName());
	}

	@Override
	public TaskDetails schedule(String uuid, TaskData taskData, String name) {
		String scheduledBy = getScheduledBySystemId();
		JobId jobId = jobRequestScheduler.enqueue(UUID.fromString(uuid), new JobRequestAdapter(taskData, scheduledBy));
		Job job = updateJobWithName(jobId, name);
		return new JobRunrTaskDetails(job);
	}
	
	private Job updateJobWithName(JobId jobId, String name) {
		try {
			Job job = storageProvider.getJobById(jobId);
			
			if (name != null) {
				job.setJobName(name);
				job = storageProvider.save(job);
			}
			return job;
		} catch (JobNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private RecurringJob updateRecurringJobWithName(String uuid, String name) {
		RecurringJob recurringJob = storageProvider.getRecurringJobs().stream()
			.filter(rj -> rj.getId().equals(uuid)).findFirst()
			.orElseThrow(() -> new APIException("Job " + uuid + " not found"));
		
		if (name != null) {
			recurringJob.setJobName(name);
			storageProvider.saveRecurringJob(recurringJob);
		}
		return recurringJob;
	}
	
	@Override
	public TaskDetails schedule(TaskData taskData, Instant runAt) {
		return schedule(taskData, runAt, taskData.getClass().getSimpleName());
	}

	@Override
	public TaskDetails schedule(TaskData taskData, Instant runAt, String name) {
		return schedule(UuidUtil.newUuidString(), taskData, runAt, name);
	}

	@Override
	public TaskDetails schedule(String uuid, TaskData taskData, Instant runAt) {
		return schedule(uuid, taskData, runAt, taskData.getClass().getSimpleName());
	}

	@Override
	public TaskDetails schedule(String uuid, TaskData taskData, Instant runAt, String name) {
		String scheduledBy = getScheduledBySystemId();
		JobId jobId = jobRequestScheduler.schedule(UUID.fromString(uuid), runAt, new JobRequestAdapter(taskData, scheduledBy));
		Job job = updateJobWithName(jobId, name);
		return new JobRunrTaskDetails(job);
	}

	@Override
	public TaskDetails schedule(TaskData taskData, ZonedDateTime runAt) {
		return schedule(taskData, runAt, taskData.getClass().getSimpleName());
	}

	@Override
	public TaskDetails schedule(TaskData taskData, ZonedDateTime runAt, String name) {
		return schedule(UuidUtil.newUuidString(), taskData, runAt, name);
	}

	@Override
	public TaskDetails schedule(String uuid, TaskData taskData, ZonedDateTime runAt) {
		return schedule(uuid, taskData, runAt, taskData.getClass().getSimpleName());
	}

	@Override
	public TaskDetails schedule(String uuid, TaskData taskData, ZonedDateTime runAt, String name) {
		String scheduledBy = getScheduledBySystemId();
		JobId jobId = jobRequestScheduler.schedule(UUID.fromString(uuid), runAt, new JobRequestAdapter(taskData, scheduledBy));
		Job job = updateJobWithName(jobId, name);
		return new JobRunrTaskDetails(job);
	}

	@Override
	public RecurringTaskDetails scheduleRecurrently(TaskData taskData, String cron) {
		return scheduleRecurrently(taskData, cron, taskData.getClass().getSimpleName());
	}
	
	@Override
	public RecurringTaskDetails scheduleRecurrently(TaskData taskData, String cron, String name) {
		return scheduleRecurrently(UuidUtil.newUuidString(), taskData, cron, name);
	}

	@Override
	public RecurringTaskDetails scheduleRecurrently(String uuid, TaskData taskData, String cron) {
		return scheduleRecurrently(uuid, taskData, cron, taskData.getClass().getSimpleName());
	}
	
	@Override
	public RecurringTaskDetails scheduleRecurrently(String uuid, TaskData taskData, String cron, String name) {
		String scheduledBy = getScheduledBySystemId();
		Optional<RecurringJob> existingJob = getRecurringJob(uuid);
		RecurringJob job = existingJob.orElseGet(() -> {
			String jobId = jobRequestScheduler.scheduleRecurrently(uuid, cron,
				new JobRequestAdapter(taskData, scheduledBy));
			return updateRecurringJobWithName(jobId, name);
		});
		
		return new JobRunrRecurringTaskDetails(job);
	}

	private @NonNull Optional<RecurringJob> getRecurringJob(String uuid) {
		return storageProvider.getRecurringJobs().stream().filter(rj -> rj.getId().equals(uuid)).findAny();
	}

	@Override
	public RecurringTaskDetails scheduleRecurrently(TaskData taskData, String cron, ZoneId zoneId) {
		return scheduleRecurrently(taskData, cron, zoneId, taskData.getClass().getSimpleName());
	}

	@Override
	public RecurringTaskDetails scheduleRecurrently(TaskData taskData, String cron, ZoneId zoneId, String name) {
		return scheduleRecurrently(UuidUtil.newUuidString(), taskData, cron, zoneId, name);
	}

	@Override
	public RecurringTaskDetails scheduleRecurrently(String uuid, TaskData taskData, String cron, ZoneId zoneId) {
		return scheduleRecurrently(uuid, taskData, cron, zoneId, taskData.getClass().getSimpleName());
	}

	@Override
	public RecurringTaskDetails scheduleRecurrently(String uuid, TaskData taskData, String cron, ZoneId zoneId, String name) {
		String scheduledBy = getScheduledBySystemId();
		Optional<RecurringJob> existingJob = getRecurringJob(uuid);
		RecurringJob job = existingJob.orElseGet(() -> {
			String jobId = jobRequestScheduler.scheduleRecurrently(uuid, cron, zoneId,
				new JobRequestAdapter(taskData, scheduledBy));
			return updateRecurringJobWithName(jobId, name);
		});
		return new JobRunrRecurringTaskDetails(job);
	}

	@Override
	public RecurringTaskDetails scheduleRecurrently(TaskData taskData, Duration interval) {
		return scheduleRecurrently(taskData, interval, taskData.getClass().getSimpleName());
	}
	
	@Override
	public RecurringTaskDetails scheduleRecurrently(TaskData taskData, Duration interval, String name) {
		return scheduleRecurrently(UuidUtil.newUuidString(), taskData, interval, name);
	}

	@Override
	public RecurringTaskDetails scheduleRecurrently(String uuid, TaskData taskData, Duration interval) {
		return scheduleRecurrently(uuid, taskData, interval, taskData.getClass().getSimpleName());
	}
	
	@Override
	public RecurringTaskDetails scheduleRecurrently(String uuid, TaskData taskData, Duration interval, String name) {
		String scheduledBy = getScheduledBySystemId();
		Optional<RecurringJob> existingJob = getRecurringJob(uuid);
		RecurringJob job = existingJob.orElseGet(() -> {
			String jobId = jobRequestScheduler.scheduleRecurrently(uuid, interval,
				new JobRequestAdapter(taskData, scheduledBy));
			return updateRecurringJobWithName(jobId, name);
		});
		return new JobRunrRecurringTaskDetails(job);
	}
	
	private String getScheduledBySystemId() {
		User user = Context.getAuthenticatedUser();
		return user != null ? user.getSystemId() : "daemon";
	}
}
