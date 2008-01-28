package org.openmrs.synchronization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.scheduler.Schedulable;
import org.openmrs.scheduler.TaskConfig;
import org.openmrs.synchronization.ingest.SyncTransmissionResponse;
import org.openmrs.synchronization.server.RemoteServer;

/**
 * Represents scheduled task to perform full data synchronization with a remote server as identified during the task setup.
 *
 */
public class SynchronizationTask implements Schedulable {

	// Logger
	private static Log log = LogFactory.getLog(SynchronizationTask.class);
	
	// Instance of configuration information for task
	private TaskConfig taskConfig;
	private Integer serverId = 0;

	/**
	 * Default Constructor (Uses SchedulerConstants.username and
	 * SchedulerConstants.password
	 */
	public SynchronizationTask() {
		// do nothing for now
	}

	/**
	 * Runs 'full' data synchronization (i.e. both send local changes and receive changes from the remote server as identified 
	 * in the task setup). 
	 * <p> NOTE: Any exception (outside of session open/close) is caughted and reported in the error log thus creating retry
	 * behavior based on the scheduled frequency.
	 */
	public void run() {
		Context.openSession();
		try {
			log.debug("Synchronizing data to a server.");
			if (Context.isAuthenticated() == false && serverId > 0)
				authenticate();
			
			// test to see if sync is enabled before trying to sync
			SyncStatusState syncStatus = SyncUtil.getSyncStatus();
			
			if ( syncStatus.equals(SyncStatusState.ENABLED_CONTINUE_ON_ERROR) || syncStatus.equals(SyncStatusState.ENABLED_STRICT) ) {
			
				RemoteServer server = Context.getSynchronizationService().getRemoteServer(serverId);
				if ( server != null ) {
					SyncTransmissionResponse response = SyncUtilTransmission.doFullSynchronize(server);
					try {
						response.createFile(true, SyncConstants.DIR_JOURNAL);
					} catch ( Exception e ) {
	    				log.error("Unable to create file to store SyncTransmissionResponse: " + response.getFileName(), e);
	    				e.printStackTrace();
					}
				}
			} else {
				log.info("Not going to sync because Syncing is not ENABLED");
			}
		} catch (Exception e) {
			log.error("Scheduler error while trying to synchronize data. Will retry per schedule.", e);
		} finally {
			Context.closeSession();
			log.debug("Synchronization complete.");
		}
	}
	
	/**
	 * Initializes task. Note serverId is in most cases an Id (as stored in sync server table) of parent. As such, parent Id
	 * does not need to be stored separately with the task as it can always be determined from sync server table. 
	 * serverId is stored here as we envision using this feature to also 'export' data to another server -- esentially 
	 * 'shadow' copying data to a separate server for other uses such as reporting.   
	 * 
	 * @param config
	 */
	public void initialize(TaskConfig config) { 
		this.taskConfig = config;
		try {
			this.serverId = Integer.valueOf(this.taskConfig.getProperty(SyncConstants.SCHEDULED_TASK_PROPERTY_SERVER_ID));
        } catch (Exception e) {
        	this.serverId = 0;
        	log.error("Could not find serverId for this sync scheduled task.",e);
        }
	}
	
	private void authenticate() {
		try {
			AdministrationService adminService = Context.getAdministrationService();
			Context.authenticate(adminService.getGlobalProperty("scheduler.username"),
				adminService.getGlobalProperty("scheduler.password"));
			
		} catch (ContextAuthenticationException e) {
			log.error("Error authenticating user", e);
		}
	}	 

}
