package org.openmrs.scheduler.tasks;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.reporting.ReportService;
import org.openmrs.reporting.export.DataExportReportObject;
import org.openmrs.reporting.export.DataExportUtil;
import org.openmrs.scheduler.Schedulable;
import org.openmrs.scheduler.TaskConfig;

/**
 *  Generates a data export 
 */
public class GenerateDataExportTask implements Schedulable { 

	// Logger 
	private Log log = LogFactory.getLog( GenerateDataExportTask.class );

	// Instance of configuration information for task
	private TaskConfig taskConfig;
	private String idString = "";
	/**
	 * Initialize task.
	 * 
	 * @param config
	 */
	public void initialize(TaskConfig config) { 
		this.taskConfig = config;
		this.idString = config.getProperty("dataExportIds");
	} 
	/** 
	 *  Process the next form entry in the database and then remove the form entry from the database.
	 */
	public void run() {
		Context.openSession();
		try {
			log.debug("Generating data exports...");
			
			if (Context.isAuthenticated() == false)
				authenticate();
			
			authenticate();
			
			if (idString != null && idString.length() > 0) {
				idString = idString.replace(",", " ");
				
				String[] ids = idString.split(" ");
				
				List<DataExportReportObject> reports = new Vector<DataExportReportObject>();
				ReportService rs = Context.getReportService();
				
				for (String id : ids) {
					if (id != null) {
						id = id.trim();
						if (id.length() > 0) {
							DataExportReportObject report = (DataExportReportObject)rs.getReportObject(Integer.valueOf(id));
							reports.add(report);
						}
					}
				}
				
				DataExportUtil.generateExports(reports);
			}
			
		} catch (Exception e) {
			log.error("Error running generate data export queue task", e);
			throw new APIException("Error running generate data export queue task", e);
		} finally {
			Context.closeSession();
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
