package org.openmrs.reporting.export;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.openmrs.BaseTest;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientSet;
import org.openmrs.reporting.ReportService;


public class DataExportTest extends BaseTest {
	
	private Log log = LogFactory.getLog(this.getClass());

	public void testClass() throws Exception {
		
		startup();
		
		try {
			Velocity.init();
		} catch (Exception e) {
			log.error("Error initializing Velocity engine", e);
		}
		
		Context.authenticate("admin", "test");
		
		ReportService rs = Context.getReportService();
		DataExportReportObject dataExport = (DataExportReportObject)rs.getReportObject(4);
		
		VelocityContext velocityContext = new VelocityContext();
		Writer report = new StringWriter();
		
		// Set up velocity utils
		Locale locale = Context.getLocale();
		velocityContext.put("locale", locale);
		
		// Set up functions used in the report ( $!{fn:...} )
		DataExportFunctions functions = new DataExportFunctions();
		velocityContext.put("fn", functions);
		
		report.append("Report: " + dataExport.getName() + "\n\n");
		
		// Set up list of patients
		PatientSet patientSet = dataExport.generatePatientSet();
		velocityContext.put("patientSet", patientSet);
		
		String template = dataExport.generateTemplate();
		
		try {
			Velocity.evaluate(velocityContext, report, this.getClass().getName(), template);
		}
		catch (Exception e) {
			log.error("Error evaluating data export " + dataExport.getReportObjectId(), e);
			report.append("\n\nError: \n" + e.toString());
			throw e;
		}
		
		report.append("\ntemplate: " + template);
		log.error("report: " + report);
		
		shutdown();
	}
	
}