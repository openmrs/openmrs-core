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
	 * Process the next form entry in the database and then remove the form
	 * entry from the database.
	 */
	public void run() {
		Context.openSession();
		try {
			log.debug("Synchronizing data to parent ... ");
			if (Context.isAuthenticated() == false && serverId > 0)
				authenticate();
			RemoteServer server = Context.getSynchronizationService().getRemoteServer(serverId);
			if ( server != null ) {
				SyncTransmissionResponse response = SyncUtilTransmission.doFullSynchronize(server);
				try {
					response.CreateFile(true, SyncConstants.DIR_JOURNAL);
				} catch ( Exception e ) {
    				log.error("Unable to create file to store SyncTransmissionResponse: " + response.getFileName());
    				e.printStackTrace();
				}
			}
		} catch (Exception e) {
			log.error("Scheduler error while trying to synchronize data. Will retry per schedule.", e);
		} finally {
			Context.closeSession();
		}
	}
	
	/**
	 * Initialize task.
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
