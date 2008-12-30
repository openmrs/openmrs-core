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
package org.openmrs.reporting.export;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.openmrs.Cohort;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;
import org.openmrs.util.OpenmrsUtil;

/**
 * Utility methods for use by Data Exports
 */
public class DataExportUtil {
	
	private static HashMap<String, Object> dataExportKeys = new HashMap<String, Object>();
	
	/**
	 * Allows a module or some other service to add things to the available keys in the velocity
	 * context
	 * 
	 * @see #generateExport(DataExportReportObject, Cohort, DataExportFunctions, EvaluationContext)
	 */
	public static void putDataExportKey(String key, Object obj) {
		dataExportKeys.put(key, obj);
	}
	
	/**
	 * Remove the given key from the available data export keys If the key doesn't exist, this will
	 * fail silently
	 * 
	 * @param key key to remove
	 * @see #putDataExportKey(String, Object)
	 * @see #generateExport(DataExportReportObject, Cohort, DataExportFunctions, EvaluationContext)
	 */
	public static void removeDataExportKey(String key) {
		dataExportKeys.remove(key);
	}
	
	/**
	 * Find the data export key previously added or null if not found
	 * 
	 * @param key
	 * @return
	 * @see #putDataExportKey(String, Object)
	 * @see #generateExport(DataExportReportObject, Cohort, DataExportFunctions, EvaluationContext)
	 */
	public static Object getDataExportKey(String key) {
		return dataExportKeys.get(key);
	}
	
	/**
	 * @param exports
	 */
	public static void generateExports(List<DataExportReportObject> exports, EvaluationContext context) {
		
		Log log = LogFactory.getLog(DataExportUtil.class);
		
		for (DataExportReportObject dataExport : exports) {
			try {
				generateExport(dataExport, null, context);
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
	public static void generateExport(DataExportReportObject dataExport, Cohort patientSet, String separator,
	                                  EvaluationContext context) throws Exception {
		// Set up functions used in the report ( $!{fn:...} )
		DataExportFunctions functions = new DataExportFunctions();
		functions.setSeparator(separator);
		generateExport(dataExport, patientSet, functions, context);
	}
	
	/**
	 * @param dataExport
	 * @param patientSet
	 * @throws Exception
	 */
	public static void generateExport(DataExportReportObject dataExport, Cohort patientSet, EvaluationContext context)
	                                                                                                                  throws Exception {
		// Set up functions used in the report ( $!{fn:...} )
		DataExportFunctions functions = new DataExportFunctions();
		generateExport(dataExport, patientSet, functions, context);
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @param dataExport
	 * @param patientSet
	 * @param functions
	 * @param context
	 * @throws Exception
	 */
	public static void generateExport(DataExportReportObject dataExport, Cohort patientSet, DataExportFunctions functions,
	                                  EvaluationContext context) throws Exception {
		
		// defining log file here to attempt to reduce memory consumption
		Log log = LogFactory.getLog(DataExportUtil.class);
		
		VelocityEngine velocityEngine = new VelocityEngine();
		
		try {
			velocityEngine.init();
		}
		catch (Exception e) {
			log.error("Error initializing Velocity engine", e);
		}
		
		File file = getGeneratedFile(dataExport);
		PrintWriter report = new PrintWriter(file);
		
		VelocityContext velocityContext = new VelocityContext();
		
		// Set up list of patients if one wasn't passed into this method
		if (patientSet == null) {
			patientSet = dataExport.generatePatientSet(context);
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
		
		/*
		 * If we have any additional velocity objects that need to 
		 * be added, do so here.
		 */
		if (dataExportKeys != null && dataExportKeys.size() != 0) {
			for (Map.Entry<String, Object> entry : dataExportKeys.entrySet()) {
				velocityContext.put(entry.getKey(), entry.getValue());
			}
		}
		
		velocityContext.put("patientSet", patientSet);
		
		String template = dataExport.generateTemplate();
		
		// check if some deprecated columns are being used in this export
		// warning: hacky.
		if (template.contains("fn.getPatientAttr('Patient', 'tribe')")) {
			throw new APIException(
			        "Unable to generate export: "
			                + dataExport.getName()
			                + " because it contains a reference to an outdated 'tribe' column.  You must install the 'Tribe Module' into OpenMRS to continue to reference tribes in OpenMRS.");
		}
		
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
		 * When a user-supplied method throws an exception, the MethodExceptionEventHandler is
		 * invoked with the Class, method name and thrown Exception. The handler can either return a
		 * valid Object to be used as the return value of the method call, or throw the passed-in or
		 * new Exception, which will be wrapped and propogated to the user as a
		 * MethodInvocationException
		 * 
		 * @see org.apache.velocity.app.event.MethodExceptionEventHandler#methodException(java.lang.Class,
		 *      java.lang.String, java.lang.Exception)
		 */
		@SuppressWarnings("unchecked")
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
