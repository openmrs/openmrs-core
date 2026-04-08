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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.JobDetails;
import org.jobrunr.jobs.JobId;
import org.jobrunr.jobs.RecurringJob;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.storage.JobNotFoundException;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.navigation.OffsetBasedPageRequest;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.scheduler.RecurringTaskDetails;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.SchedulerUtil;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskData;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.scheduler.TaskDetails;
import org.openmrs.scheduler.TaskState;
import org.openmrs.scheduler.db.SchedulerDAO;
import org.openmrs.util.PrivilegeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @since 2.9.x
 */
@Service("schedulerService")
@Transactional
public class JobRunrSchedulerService extends BaseOpenmrsService implements SchedulerService {

	private static final Logger log = LoggerFactory.getLogger(JobRunrSchedulerService.class);

	private JobRequestScheduler jobRequestScheduler;

	private JobScheduler jobScheduler;

	private StorageProvider storageProvider;

	private SchedulerDAO schedulerDAO;

	public JobRunrSchedulerService(StorageProvider storageProvider, JobRequestScheduler jobRequestScheduler,
	    JobScheduler jobScheduler, SchedulerDAO schedulerDAO) {
		this.jobRequestScheduler = jobRequestScheduler;
		this.jobScheduler = jobScheduler;
		this.storageProvider = storageProvider;
		this.schedulerDAO = schedulerDAO;
	}

	@Override
	public void onStartup() {
		for (TaskDefinition taskDefinition : schedulerDAO.getTasks()) {
			if (Boolean.TRUE.equals(taskDefinition.getStartOnStartup())) {
				JobId jobId = jobRequestScheduler.enqueue(UUID.fromString(taskDefinition.getUuid()),
				    new JobRequestAdapter(taskDefinition, taskDefinition.getCreator().getSystemId()));
				String name = taskDefinition.getName();
				if (name == null) {
					name = taskDefinition.getTaskClass();
				}
				updateJobWithName(jobId, name);
				log.info("Scheduled legacy task {} [{}] to run on startup", name, taskDefinition.getUuid());
			}

			Optional<RecurringTaskDetails> recurringTask = getRecurringTask(taskDefinition.getUuid());
			if (!recurringTask.isPresent()) {
				Optional<TaskDetails> task = getTask(taskDefinition.getUuid());
				if (!task.isPresent()) {
					try {
						scheduleTask(taskDefinition);
						log.info("Scheduled legacy task {} [{}] to run on schedule", taskDefinition.getName(),
						    taskDefinition.getUuid());
					} catch (SchedulerException e) {
						throw new APIException(e);
					}
				}
			}
		}
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
	public Task scheduleTask(TaskDefinition legacyTask) throws SchedulerException {
		if (legacyTask != null) {
			// Reload task from DB, to get a session attached version
			final TaskDefinition task = schedulerDAO.getTask(legacyTask.getId());
			try {
				String name = task.getName();
				if (name == null) {
					name = task.getTaskClass();
				}
				String scheduledBy = task.getCreator() != null ? task.getCreator().getSystemId() : "daemon";

				if (task.getRepeatInterval() != null && task.getRepeatInterval() > 0) {
					if (task.getStartTime() == null) {
						String recurringJobId = jobRequestScheduler.scheduleRecurrently(task.getUuid(),
						    Duration.ofSeconds(task.getRepeatInterval()), new JobRequestAdapter(task, scheduledBy));
						updateRecurringJobWithName(recurringJobId, name);
					} else {
						Date nextExecution = SchedulerUtil.getNextExecution(task);
						JobId jobId = jobScheduler.schedule(UUID.randomUUID(), nextExecution.toInstant(),
						    (JobRunrSchedulerService service) -> service.scheduleRecurrently(task.getUuid()));
						updateJobWithName(jobId, task.getName());
						// Create a placeholder recurring task that will be updated by the above task to the correct interval
						String recurringJobId = jobRequestScheduler.scheduleRecurrently(task.getUuid(),
						    Duration.between(Instant.now(), nextExecution.toInstant()).plus(1, ChronoUnit.DAYS),
						    new JobRequestAdapter(task, scheduledBy));
						updateRecurringJobWithName(recurringJobId, name);
					}
				} else if (task.getStartTime() != null) {
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
			} catch (Exception e) {
				throw new SchedulerException("Failed to schedule task", e);
			}
		}
		return null;
	}

	/**
	 * Helper task to schedule recurrently at a given time.
	 *
	 * @param uuid
	 */
	public void scheduleRecurrently(String uuid) {
		TaskDefinition task = getTaskByUuid(uuid);
		if (task != null) {
			String scheduledBy = task.getCreator() != null ? task.getCreator().getSystemId() : "daemon";
			String jobId = jobRequestScheduler.scheduleRecurrently(task.getUuid(),
			    Duration.ofSeconds(task.getRepeatInterval()), new JobRequestAdapter(task, scheduledBy));
			updateRecurringJobWithName(jobId, task.getName());
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
			} catch (Exception e) {
				log.error("Failed to reschedule task " + task.getName(), e);
			}
		}
	}

	@Override
	public Collection<TaskDefinition> getScheduledTasks() {
		return getRegisteredTasks().stream().filter(t -> Boolean.TRUE.equals(t.getStarted())).collect(Collectors.toList());
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
		return schedulerDAO.getTaskByName(name);
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
			} else if (StateName.DELETED.equals(job.getState())) {
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
		        .filter(r -> r.getId().equals(uuid) && hasPrivileges(r.getJobDetails())).findFirst()
		        .map(JobRunrRecurringTaskDetails::new);
	}

	@Override
	public void deleteTask(String uuid) {
		if (getTask(uuid).isPresent()) {
			jobRequestScheduler.delete(JobId.parse(uuid));
		}
	}

	@Override
	public void deleteRecurringTask(String uuid) {
		if (getRecurringTask(uuid).isPresent()) {
			jobRequestScheduler.deleteRecurringJob(uuid);
		}
	}

	@Override
	public Stream<TaskDetails> getTasks(TaskState state, Instant before) {
		return StreamSupport.stream(
		    new Spliterators.AbstractSpliterator<TaskDetails>(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL) {

			    private int offset = 0;

			    private Iterator<Job> currentBatch;

			    private final boolean isSchedulerManager = isSchedulerManager(Context.getAuthenticatedUser());

			    private final String userSystemId = Context.getAuthenticatedUser().getSystemId();

			    @Override
			    public boolean tryAdvance(Consumer<? super TaskDetails> action) {
				    if (currentBatch == null || !currentBatch.hasNext()) {
					    int limit = 100;
					    List<Job> jobs = storageProvider.getJobs(StateName.valueOf(state.name()),
					        new OffsetBasedPageRequest("updatedAt:ASC", offset, limit)).getItems();
					    if (jobs == null || jobs.isEmpty()) {
						    return false;
					    }
					    // Filter tasks based on scheduled by or updated at
					    currentBatch = jobs.stream()
					            .filter(job -> (isSchedulerManager || isScheduledBy(job.getJobDetails(), userSystemId))
					                    && (before == null || job.getUpdatedAt().isBefore(before)))
					            .iterator();
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

	public boolean isSchedulerManager(User user) {
		return user.hasPrivilege(PrivilegeConstants.MANAGE_SCHEDULER);
	}

	public boolean hasPrivileges(JobDetails jobDetails) {
		User user = Context.getAuthenticatedUser();
		return isSchedulerManager(user) || isScheduledBy(jobDetails, user.getSystemId());
	}

	public boolean isScheduledBy(JobDetails jobDetails, String userSystemId) {
		return jobDetails.getJobParameters().stream().anyMatch(jp -> {
			if (jp.getObject() instanceof JobRequestAdapter) {
				JobRequestAdapter jobRequestAdapter = (JobRequestAdapter) jp.getObject();
				return jobRequestAdapter.getUserSystemId().equals(userSystemId);
			}
			return false;
		});
	}

	@Override
	public Stream<RecurringTaskDetails> getRecurringTasks() {
		User user = Context.getAuthenticatedUser();
		boolean isSchedulerManager = isSchedulerManager(user);
		return storageProvider.getRecurringJobs().stream()
		        .filter(j -> isSchedulerManager || isScheduledBy(j.getJobDetails(), user.getSystemId()))
		        .map(JobRunrRecurringTaskDetails::new);
	}

	@Override
	public TaskDetails schedule(TaskData taskData) {
		return schedule(taskData.getClass().getSimpleName(), taskData);
	}

	@Override
	public TaskDetails schedule(String name, TaskData taskData) {
		String scheduledBy = getScheduledBySystemId();
		JobId jobId = jobRequestScheduler.enqueue(new JobRequestAdapter(taskData, scheduledBy));
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
		RecurringJob recurringJob = storageProvider.getRecurringJobs().stream().filter(rj -> rj.getId().equals(uuid))
		        .findFirst().orElseThrow(() -> new APIException("Job " + uuid + " not found"));

		if (name != null) {
			recurringJob.setJobName(name);
			storageProvider.saveRecurringJob(recurringJob);
		}
		return recurringJob;
	}

	@Override
	public TaskDetails schedule(TaskData taskData, Instant runAt) {
		return schedule(taskData.getClass().getSimpleName(), taskData, runAt);
	}

	@Override
	public TaskDetails schedule(String name, TaskData taskData, Instant runAt) {
		String scheduledBy = getScheduledBySystemId();
		JobId jobId = jobRequestScheduler.schedule(runAt, new JobRequestAdapter(taskData, scheduledBy));
		Job job = updateJobWithName(jobId, name);
		return new JobRunrTaskDetails(job);
	}

	@Override
	public TaskDetails schedule(TaskData taskData, ZonedDateTime runAt) {
		return schedule(taskData.getClass().getSimpleName(), taskData, runAt);
	}

	@Override
	public TaskDetails schedule(String name, TaskData taskData, ZonedDateTime runAt) {
		String scheduledBy = getScheduledBySystemId();
		JobId jobId = jobRequestScheduler.schedule(runAt, new JobRequestAdapter(taskData, scheduledBy));
		Job job = updateJobWithName(jobId, name);
		return new JobRunrTaskDetails(job);
	}

	@Override
	public RecurringTaskDetails scheduleRecurrently(TaskData taskData, String cron) {
		return scheduleRecurrently(taskData.getClass().getSimpleName(), taskData, cron);
	}

	@Override
	public RecurringTaskDetails scheduleRecurrently(String name, TaskData taskData, String cron) {
		String scheduledBy = getScheduledBySystemId();
		String jobId = jobRequestScheduler.scheduleRecurrently(UUID.randomUUID().toString(), cron,
		    new JobRequestAdapter(taskData, scheduledBy));
		RecurringJob job = updateRecurringJobWithName(jobId, name);
		return new JobRunrRecurringTaskDetails(job);
	}

	@Override
	public RecurringTaskDetails scheduleRecurrently(TaskData taskData, String cron, ZoneId zoneId) {
		return scheduleRecurrently(taskData.getClass().getSimpleName(), taskData, cron, zoneId);
	}

	@Override
	public RecurringTaskDetails scheduleRecurrently(String name, TaskData taskData, String cron, ZoneId zoneId) {
		String scheduledBy = getScheduledBySystemId();
		String jobId = jobRequestScheduler.scheduleRecurrently(UUID.randomUUID().toString(), cron, zoneId,
		    new JobRequestAdapter(taskData, scheduledBy));
		RecurringJob job = updateRecurringJobWithName(jobId, name);
		return new JobRunrRecurringTaskDetails(job);
	}

	@Override
	public RecurringTaskDetails scheduleRecurrently(TaskData taskData, Duration interval) {
		return scheduleRecurrently(taskData.getClass().getSimpleName(), taskData, interval);
	}

	@Override
	public RecurringTaskDetails scheduleRecurrently(String name, TaskData taskData, Duration interval) {
		String scheduledBy = getScheduledBySystemId();
		String jobId = jobRequestScheduler.scheduleRecurrently(UUID.randomUUID().toString(), interval,
		    new JobRequestAdapter(taskData, scheduledBy));
		RecurringJob job = updateRecurringJobWithName(jobId, name);
		return new JobRunrRecurringTaskDetails(job);
	}

	private String getScheduledBySystemId() {
		return Context.getAuthenticatedUser().getSystemId();
	}
}
