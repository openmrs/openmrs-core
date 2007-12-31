package org.openmrs.reporting.export;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientSet;
import org.openmrs.util.OpenmrsUtil;

public class DataExportUtil {
	
	/**
	 * 
	 * @param exports
	 */
	public static void generateExports(List<DataExportReportObject> exports) {
		
		Log log = LogFactory.getLog(DataExportUtil.class);
		
		for (DataExportReportObject dataExport : exports) {
			try {
				generateExport(dataExport, null);
			}
			catch (Exception e) {
				log.warn("Error while generating export: " + dataExport, e);
			}
		}
		
	}

	
	/**
	 * Generates a data export file given a data export (columns) and patient set (rows).  
	 * 
	 * @param dataExport
	 * @param patientSet
	 * @param separator
	 * @throws Exception
	 */
	public static void generateExport(DataExportReportObject dataExport, PatientSet patientSet, String separator) throws Exception {
		// Set up functions used in the report ( $!{fn:...} )
		DataExportFunctions functions = new DataExportFunctions();
		functions.setSeparator(separator);
		generateExport(dataExport, patientSet, functions);
	}
	
	/**
	 * 
	 * @param dataExport
	 * @param patientSet
	 * @throws Exception
	 */
	public static void generateExport(DataExportReportObject dataExport, PatientSet patientSet) throws Exception {
		// Set up functions used in the report ( $!{fn:...} )
		DataExportFunctions functions = new DataExportFunctions();
		generateExport(dataExport, patientSet, functions);
	}
	
	/**
	 * 
	 * @param dataExport
	 * @param patientSet (nullable)
	 * @throws Exception
	 */
	public static void generateExport(DataExportReportObject dataExport, PatientSet patientSet, DataExportFunctions functions) throws Exception {
		
		// defining log file here to attempt to reduce memory consumption
		Log log = LogFactory.getLog(DataExportUtil.class);
		
		VelocityEngine velocityEngine = new VelocityEngine();
		
		try {
			velocityEngine.init();
		} catch (Exception e) {
			log.error("Error initializing Velocity engine", e);
		}
		
		File file = getGeneratedFile(dataExport);
		PrintWriter report = new PrintWriter(file);
		
		VelocityContext velocityContext = new VelocityContext();
		
		// Set up list of patients if one wasn't passed into this method
		if (patientSet == null) {
			patientSet = dataExport.generatePatientSet();
			functions.setAllPatients(dataExport.isAllPatients());
		}
		
		// add the error handler
		EventCartridge ec = new EventCartridge();
		ec.addEventHandler(new VelocityExceptionHandler());
		velocityContext.attachEventCartridge(ec);
		
		// Set up velocity utils
		Locale locale = Context.getLocale();
		velocityContext.put("locale", locale);		
		velocityContext.put("fn", functions);
		
		velocityContext.put("patientSet", patientSet);
		
		String template = dataExport.generateTemplate();
		
		if (log.isDebugEnabled())
			log.debug("Template: " + template.substring(0, template.length() < 3500 ? template.length() : 3500) + "...");
		
		try {
			velocityEngine.evaluate(velocityContext, report, DataExportUtil.class.getName(), template);
		}
		catch (Exception e) {
			log.error("Error evaluating data export " + dataExport.getReportObjectId(), e);
			log.error("Template: " + template.substring(0, template.length() < 3500 ? template.length() : 3500) + "...");
			report.print("\n\nError: \n" + e.toString() + "\n Stacktrace: \n");
			e.printStackTrace(report);
		}
		finally {
			report.close();
			velocityContext.remove("fn");
			velocityContext.remove("patientSet");
			velocityContext = null;
			
			// reset the ParserPool to something else now?
			// using this to get to RuntimeInstance.init();
			velocityEngine.init();
			
			velocityEngine = null;
			
			patientSet = null;
			functions.clear();
			functions = null;
			template = null;
			dataExport = null;
			log.debug("Clearing hibernate session");
			Context.clearSession();
			
			// clear out the excess objects
			System.gc();
			System.gc();
		}
		
	}
	
	/**
	 * Returns the path and name of the generated file
	 * 
	 * @param dataExport
	 * @return
	 */
	public static File getGeneratedFile(DataExportReportObject dataExport) {
		File dir = new File(OpenmrsUtil.getApplicationDataDirectory(), "dataExports");
		dir.mkdirs();
		
		String filename = dataExport.getName().replace(" ", "_");
		filename += "_" + Context.getLocale().toString().toLowerCase();
		
		File file = new File(dir, filename);
		
		return file;
	}
	
	/**
	 * Private class used for velocity error masking
	 */
	public static class VelocityExceptionHandler implements MethodExceptionEventHandler {

		private Log log = LogFactory.getLog(this.getClass());
		
		/**
		 * When a user-supplied method throws an exception, the MethodExceptionEventHandler 
		 * is invoked with the Class, method name and thrown Exception. The handler can 
		 * either return a valid Object to be used as the return value of the method call, 
		 * or throw the passed-in or new Exception, which will be wrapped and propogated to 
		 * the user as a MethodInvocationException
		 * 
		 * @see org.apache.velocity.app.event.MethodExceptionEventHandler#methodException(java.lang.Class, java.lang.String, java.lang.Exception)
		 */
		public Object methodException(Class claz, String method, Exception e) throws Exception {
			
			log.debug("Claz: " + claz.getName() + " method: " + method, e);
			
			// if formatting a date (and probably getting an "IllegalArguementException")
			if ("format".equals(method))
				return null;
			
			// keep the default behaviour
			throw e;
		}

	}
	
}
