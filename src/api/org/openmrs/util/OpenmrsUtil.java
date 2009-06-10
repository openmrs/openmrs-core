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
package org.openmrs.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Drug;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Person;
import org.openmrs.PersonAttributeType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.InvalidCharactersPasswordException;
import org.openmrs.api.PasswordException;
import org.openmrs.api.PatientService;
import org.openmrs.api.ShortPasswordException;
import org.openmrs.api.WeakPasswordException;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortSearchHistory;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.module.ModuleException;
import org.openmrs.patient.IdentifierValidator;
import org.openmrs.propertyeditor.CohortEditor;
import org.openmrs.propertyeditor.ConceptEditor;
import org.openmrs.propertyeditor.DrugEditor;
import org.openmrs.propertyeditor.EncounterTypeEditor;
import org.openmrs.propertyeditor.FormEditor;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.propertyeditor.PersonAttributeTypeEditor;
import org.openmrs.propertyeditor.ProgramEditor;
import org.openmrs.propertyeditor.ProgramWorkflowStateEditor;
import org.openmrs.report.EvaluationContext;
import org.openmrs.reporting.CohortFilter;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.reporting.SearchArgument;
import org.openmrs.xml.OpenmrsCycleStrategy;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.load.Persister;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 * Utility methods used in openmrs
 */
public class OpenmrsUtil {
	
	private static Log log = LogFactory.getLog(OpenmrsUtil.class);
	
	/**
	 * @param idWithoutCheckdigit
	 * @return int - the calculated check digit for the given string
	 * @throws Exception
	 * @deprecated Use {@link PatientService#getIdentifierValidator(String)}
	 * @should get valid check digits
	 */
	public static int getCheckDigit(String idWithoutCheckdigit) throws Exception {
		PatientService ps = Context.getPatientService();
		IdentifierValidator piv = ps.getDefaultIdentifierValidator();
		
		String withCheckDigit = piv.getValidIdentifier(idWithoutCheckdigit);
		char checkDigitChar = withCheckDigit.charAt(withCheckDigit.length() - 1);
		
		if (Character.isDigit(checkDigitChar))
			return Integer.parseInt("" + checkDigitChar);
		else {
			switch (checkDigitChar) {
				case 'A':
				case 'a':
					return 0;
				case 'B':
				case 'b':
					return 1;
				case 'C':
				case 'c':
					return 2;
				case 'D':
				case 'd':
					return 3;
				case 'E':
				case 'e':
					return 4;
				case 'F':
				case 'f':
					return 5;
				case 'G':
				case 'g':
					return 6;
				case 'H':
				case 'h':
					return 7;
				case 'I':
				case 'i':
					return 8;
				case 'J':
				case 'j':
					return 9;
				default:
					return 10;
			}
		}
		
	}
	
	/**
	 * @param id
	 * @return true/false whether id has a valid check digit
	 * @throws Exception on invalid characters and invalid id formation
	 * @deprecated Should be using {@link PatientService#getIdentifierValidator(String)}
	 * @should validate correct check digits
	 * @should not validate invalid check digits
	 * @should throw error if given an invalid character in id
	 */
	public static boolean isValidCheckDigit(String id) throws Exception {
		PatientService ps = Context.getPatientService();
		IdentifierValidator piv = ps.getDefaultIdentifierValidator();
		
		return piv.isValid(id);
	}
	
	/**
	 * Compares origList to newList returning map of differences
	 * 
	 * @param origList
	 * @param newList
	 * @return [List toAdd, List toDelete] with respect to origList
	 */
	public static <E extends Object> Collection<Collection<E>> compareLists(Collection<E> origList, Collection<E> newList) {
		// TODO finish function
		
		Collection<Collection<E>> returnList = new Vector<Collection<E>>();
		
		Collection<E> toAdd = new LinkedList<E>();
		Collection<E> toDel = new LinkedList<E>();
		
		// loop over the new list.
		for (E currentNewListObj : newList) {
			// loop over the original list
			boolean foundInList = false;
			for (E currentOrigListObj : origList) {
				// checking if the current new list object is in the original
				// list
				if (currentNewListObj.equals(currentOrigListObj)) {
					foundInList = true;
					origList.remove(currentOrigListObj);
					break;
				}
			}
			if (!foundInList)
				toAdd.add(currentNewListObj);
			
			// all found new objects were removed from the orig list,
			// leaving only objects needing to be removed
			toDel = origList;
			
		}
		
		returnList.add(toAdd);
		returnList.add(toDel);
		
		return returnList;
	}
	
	public static boolean isStringInArray(String str, String[] arr) {
		boolean retVal = false;
		
		if (str != null && arr != null) {
			for (int i = 0; i < arr.length; i++) {
				if (str.equals(arr[i]))
					retVal = true;
			}
		}
		return retVal;
	}
	
	public static Boolean isInNormalNumericRange(Float value, ConceptNumeric concept) {
		if (concept.getHiNormal() == null || concept.getLowNormal() == null)
			return false;
		return (value <= concept.getHiNormal() && value >= concept.getLowNormal());
	}
	
	public static Boolean isInCriticalNumericRange(Float value, ConceptNumeric concept) {
		if (concept.getHiCritical() == null || concept.getLowCritical() == null)
			return false;
		return (value <= concept.getHiCritical() && value >= concept.getLowCritical());
	}
	
	public static Boolean isInAbsoluteNumericRange(Float value, ConceptNumeric concept) {
		if (concept.getHiAbsolute() == null || concept.getLowAbsolute() == null)
			return false;
		return (value <= concept.getHiAbsolute() && value >= concept.getLowAbsolute());
	}
	
	public static Boolean isValidNumericValue(Float value, ConceptNumeric concept) {
		if (concept.getHiAbsolute() == null || concept.getLowAbsolute() == null)
			return true;
		return (value <= concept.getHiAbsolute() && value >= concept.getLowAbsolute());
	}
	
	/**
	 * Return a string representation of the given file
	 * 
	 * @param file
	 * @return String file contents
	 * @throws IOException
	 */
	public static String getFileAsString(File file) throws IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}
	
	/**
	 * Return a byte array representation of the given file
	 * 
	 * @param file
	 * @return byte[] file contents
	 * @throws IOException
	 */
	public static byte[] getFileAsBytes(File file) throws IOException {
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			byte[] b = new byte[fileInputStream.available()];
			fileInputStream.read(b);
			fileInputStream.close();
			return b;
		}
		catch (Exception e) {
			log.error("Unable to get file as byte array", e);
		}
		
		return null;
	}
	
	/**
	 * Copy file from inputStream onto the outputStream inputStream is not closed in this method
	 * outputStream /is/ closed at completion of this method
	 * 
	 * @param inputStream Stream to copy from
	 * @param outputStream Stream/location to copy to
	 * @throws IOException thrown if an error occurs during read/write
	 */
	public static void copyFile(InputStream inputStream, OutputStream outputStream) throws IOException {
		if (inputStream == null || outputStream == null) {
			if (outputStream != null) {
				try {
					outputStream.close();
				}
				catch (Exception e) { /* pass */}
			}
			
			return;
		}
		
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new BufferedInputStream(inputStream);
			out = new BufferedOutputStream(outputStream);
			while (true) {
				int data = in.read();
				if (data == -1) {
					break;
				}
				out.write(data);
			}
		}
		finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			try {
				outputStream.close();
			}
			catch (Exception e) { /* pass */}
		}
		
	}
	
	/**
	 * Look for a file named <code>filename</code> in folder
	 * 
	 * @param folder
	 * @param filename
	 * @return true/false whether filename exists in folder
	 */
	public static boolean folderContains(File folder, String filename) {
		if (folder == null)
			return false;
		if (!folder.isDirectory())
			return false;
		
		for (File f : folder.listFiles()) {
			if (f.getName().equals(filename))
				return true;
		}
		return false;
	}
	
	/**
	 * Initialize global settings Find and load modules
	 * 
	 * @param p properties from runtime configuration
	 */
	public static void startup(Properties p) {
		
		// Override global OpenMRS constants if specified by the user
		
		// Allow for "demo" mode where patient data is obscured
		String val = p.getProperty("obscure_patients", null);
		if (val != null && "true".equalsIgnoreCase(val))
			OpenmrsConstants.OBSCURE_PATIENTS = true;
		
		val = p.getProperty("obscure_patients.family_name", null);
		if (val != null)
			OpenmrsConstants.OBSCURE_PATIENTS_FAMILY_NAME = val;
		
		val = p.getProperty("obscure_patients.given_name", null);
		if (val != null)
			OpenmrsConstants.OBSCURE_PATIENTS_GIVEN_NAME = val;
		
		val = p.getProperty("obscure_patients.middle_name", null);
		if (val != null)
			OpenmrsConstants.OBSCURE_PATIENTS_MIDDLE_NAME = val;
		
		// Override the default "openmrs" database name
		val = p.getProperty("connection.database_name", null);
		if (val == null) {
			// the database name wasn't supplied explicitly, guess it 
			//   from the connection string
			val = p.getProperty("connection.url", null);
			
			if (val != null) {
				try {
					int endIndex = val.lastIndexOf("?");
					if (endIndex == -1)
						endIndex = val.length();
					int startIndex = val.lastIndexOf("/", endIndex);
					val = val.substring(startIndex + 1, endIndex);
					OpenmrsConstants.DATABASE_NAME = val;
				}
				catch (Exception e) {
					log.fatal("Database name cannot be configured from 'connection.url' ."
					        + "Either supply 'connection.database_name' or correct the url", e);
				}
			}
		}
		
		// set the business database name
		val = p.getProperty("connection.database_business_name", null);
		if (val == null)
			val = OpenmrsConstants.DATABASE_NAME;
		OpenmrsConstants.DATABASE_BUSINESS_NAME = val;
		
		// set the application data directory
		val = p.getProperty(OpenmrsConstants.APPLICATION_DATA_DIRECTORY_RUNTIME_PROPERTY, null);
		if (val != null)
			OpenmrsConstants.APPLICATION_DATA_DIRECTORY = val;
		
		// set global log level
		applyLogLevels();
		
	}
	
	/**
	 * Set the org.openmrs log4j logger's level if global property log.level.openmrs (
	 * OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL ) exists. Valid values for global property are
	 * trace, debug, info, warn, error or fatal.
	 */
	public static void applyLogLevels() {
		AdministrationService adminService = Context.getAdministrationService();
		String logLevel = adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL);
		String logClass = OpenmrsConstants.LOG_CLASS_DEFAULT;
		
		// potentially have different levels here.  only doing org.openmrs right now
		applyLogLevel(logClass, logLevel);
	}
	
	/**
	 * Set the log4j log level for class <code>logClass</code> to <code>logLevel</code>.
	 * 
	 * @param logClass optional string giving the class level to change. Defaults to
	 *            OpenmrsConstants.LOG_CLASS_DEFAULT . Should be something like org.openmrs.___
	 * @param logLevel one of OpenmrsConstants.LOG_LEVEL_*
	 */
	public static void applyLogLevel(String logClass, String logLevel) {
		
		if (logLevel != null) {
			
			// the default log level is org.openmrs
			if (logClass == null || "".equals(logClass))
				logClass = OpenmrsConstants.LOG_CLASS_DEFAULT;
			
			Logger logger = Logger.getLogger(logClass);
			
			logLevel = logLevel.toLowerCase();
			if (OpenmrsConstants.LOG_LEVEL_TRACE.equals(logLevel)) {
				logger.setLevel(Level.TRACE);
			} else if (OpenmrsConstants.LOG_LEVEL_DEBUG.equals(logLevel)) {
				logger.setLevel(Level.DEBUG);
			} else if (OpenmrsConstants.LOG_LEVEL_INFO.equals(logLevel)) {
				logger.setLevel(Level.INFO);
			} else if (OpenmrsConstants.LOG_LEVEL_WARN.equals(logLevel)) {
				logger.setLevel(Level.WARN);
			} else if (OpenmrsConstants.LOG_LEVEL_ERROR.equals(logLevel)) {
				logger.setLevel(Level.ERROR);
			} else if (OpenmrsConstants.LOG_LEVEL_FATAL.equals(logLevel)) {
				logger.setLevel(Level.FATAL);
			} else {
				log.warn("Global property " + logLevel + " is invalid. "
				        + "Valid values are trace, debug, info, warn, error or fatal");
			}
		}
	}
	
	/**
	 * Takes a String like "size=compact|order=date" and returns a Map<String,String> from the keys
	 * to the values.
	 * 
	 * @param paramList <code>String</code> with a list of parameters
	 * @return Map<String, String> of the parameters passed
	 */
	public static Map<String, String> parseParameterList(String paramList) {
		Map<String, String> ret = new HashMap<String, String>();
		if (paramList != null && paramList.length() > 0) {
			String[] args = paramList.split("\\|");
			for (String s : args) {
				int ind = s.indexOf('=');
				if (ind <= 0) {
					throw new IllegalArgumentException("Misformed argument in dynamic page specification string: '" + s
					        + "' is not 'key=value'.");
				}
				String name = s.substring(0, ind);
				String value = s.substring(ind + 1);
				ret.put(name, value);
			}
		}
		return ret;
	}
	
	public static <Arg1, Arg2 extends Arg1> boolean nullSafeEquals(Arg1 d1, Arg2 d2) {
		if (d1 == null)
			return d2 == null;
		else if (d2 == null)
			return false;
		else
			return d1.equals(d2);
	}
	
	/**
	 * Compares two java.util.Date objects, but handles java.sql.Timestamp (which is not directly
	 * comparable to a date) by dropping its nanosecond value.
	 */
	public static int compare(Date d1, Date d2) {
		if (d1 instanceof Timestamp && d2 instanceof Timestamp) {
			return d1.compareTo(d2);
		}
		if (d1 instanceof Timestamp)
			d1 = new Date(((Timestamp) d1).getTime());
		if (d2 instanceof Timestamp)
			d2 = new Date(((Timestamp) d2).getTime());
		return d1.compareTo(d2);
	}
	
	/**
	 * Compares two Date/Timestamp objects, treating null as the earliest possible date.
	 */
	public static int compareWithNullAsEarliest(Date d1, Date d2) {
		if (d1 == null && d2 == null)
			return 0;
		if (d1 == null)
			return -1;
		else if (d2 == null)
			return 1;
		else
			return compare(d1, d2);
	}
	
	/**
	 * Compares two Date/Timestamp objects, treating null as the earliest possible date.
	 */
	public static int compareWithNullAsLatest(Date d1, Date d2) {
		if (d1 == null && d2 == null)
			return 0;
		if (d1 == null)
			return 1;
		else if (d2 == null)
			return -1;
		else
			return compare(d1, d2);
	}
	
	public static <E extends Comparable<E>> int compareWithNullAsLowest(E c1, E c2) {
		if (c1 == null && c2 == null)
			return 0;
		if (c1 == null)
			return -1;
		else if (c2 == null)
			return 1;
		else
			return c1.compareTo(c2);
	}
	
	public static <E extends Comparable<E>> int compareWithNullAsGreatest(E c1, E c2) {
		if (c1 == null && c2 == null)
			return 0;
		if (c1 == null)
			return 1;
		else if (c2 == null)
			return -1;
		else
			return c1.compareTo(c2);
	}
	
	/**
	 * @deprecated this method is not currently used within OpenMRS and is a duplicate of
	 *             {@link Person#getAge(Date)}
	 */
	public static Integer ageFromBirthdate(Date birthdate) {
		if (birthdate == null)
			return null;
		
		Calendar today = Calendar.getInstance();
		
		Calendar bday = Calendar.getInstance();
		bday.setTime(birthdate);
		
		int age = today.get(Calendar.YEAR) - bday.get(Calendar.YEAR);
		
		//Adjust age when today's date is before the person's birthday
		int todaysMonth = today.get(Calendar.MONTH);
		int bdayMonth = bday.get(Calendar.MONTH);
		int todaysDay = today.get(Calendar.DAY_OF_MONTH);
		int bdayDay = bday.get(Calendar.DAY_OF_MONTH);
		
		if (todaysMonth < bdayMonth) {
			age--;
		} else if (todaysMonth == bdayMonth && todaysDay < bdayDay) {
			// we're only comparing on month and day, not minutes, etc
			age--;
		}
		
		return age;
	}
	
	/**
	 * Converts a collection to a String with a specified separator between all elements
	 * 
	 * @param c Collection to be joined
	 * @param separator string to put between all elements
	 * @return a String representing the toString() of all elements in c, separated by separator
	 */
	public static <E extends Object> String join(Collection<E> c, String separator) {
		if (c == null)
			return "";
		
		StringBuilder ret = new StringBuilder();
		for (Iterator<E> i = c.iterator(); i.hasNext();) {
			ret.append(i.next());
			if (i.hasNext())
				ret.append(separator);
		}
		return ret.toString();
	}
	
	public static Set<Concept> conceptSetHelper(String descriptor) {
		Set<Concept> ret = new HashSet<Concept>();
		if (descriptor == null || descriptor.length() == 0)
			return ret;
		ConceptService cs = Context.getConceptService();
		
		for (StringTokenizer st = new StringTokenizer(descriptor, "|"); st.hasMoreTokens();) {
			String s = st.nextToken().trim();
			boolean isSet = s.startsWith("set:");
			if (isSet)
				s = s.substring(4).trim();
			Concept c = null;
			if (s.startsWith("name:")) {
				String name = s.substring(5).trim();
				c = cs.getConceptByName(name);
			} else {
				try {
					c = cs.getConcept(Integer.valueOf(s.trim()));
				}
				catch (Exception ex) {}
			}
			if (c != null) {
				if (isSet) {
					List<Concept> inSet = cs.getConceptsByConceptSet(c);
					ret.addAll(inSet);
				} else {
					ret.add(c);
				}
			}
		}
		return ret;
	}
	
	public static List<Concept> delimitedStringToConceptList(String delimitedString, String delimiter, Context context) {
		List<Concept> ret = null;
		
		if (delimitedString != null && context != null) {
			String[] tokens = delimitedString.split(delimiter);
			for (String token : tokens) {
				Integer conceptId = null;
				
				try {
					conceptId = new Integer(token);
				}
				catch (NumberFormatException nfe) {
					conceptId = null;
				}
				
				Concept c = null;
				
				if (conceptId != null) {
					c = Context.getConceptService().getConcept(conceptId);
				} else {
					c = Context.getConceptService().getConceptByName(token);
				}
				
				if (c != null) {
					if (ret == null)
						ret = new ArrayList<Concept>();
					ret.add(c);
				}
			}
		}
		
		return ret;
	}
	
	public static Map<String, Concept> delimitedStringToConceptMap(String delimitedString, String delimiter) {
		Map<String, Concept> ret = null;
		
		if (delimitedString != null) {
			String[] tokens = delimitedString.split(delimiter);
			for (String token : tokens) {
				Concept c = OpenmrsUtil.getConceptByIdOrName(token);
				
				if (c != null) {
					if (ret == null)
						ret = new HashMap<String, Concept>();
					ret.put(token, c);
				}
			}
		}
		
		return ret;
	}
	
	// DEPRECATED: This method should now be replaced with ConceptService.getConceptByIdOrName()
	public static Concept getConceptByIdOrName(String idOrName) {
		Concept c = null;
		Integer conceptId = null;
		
		try {
			conceptId = new Integer(idOrName);
		}
		catch (NumberFormatException nfe) {
			conceptId = null;
		}
		
		if (conceptId != null) {
			c = Context.getConceptService().getConcept(conceptId);
		} else {
			c = Context.getConceptService().getConceptByName(idOrName);
		}
		
		return c;
	}
	
	// TODO: properly handle duplicates
	public static List<Concept> conceptListHelper(String descriptor) {
		List<Concept> ret = new ArrayList<Concept>();
		if (descriptor == null || descriptor.length() == 0)
			return ret;
		ConceptService cs = Context.getConceptService();
		
		for (StringTokenizer st = new StringTokenizer(descriptor, "|"); st.hasMoreTokens();) {
			String s = st.nextToken().trim();
			boolean isSet = s.startsWith("set:");
			if (isSet)
				s = s.substring(4).trim();
			Concept c = null;
			if (s.startsWith("name:")) {
				String name = s.substring(5).trim();
				c = cs.getConceptByName(name);
			} else {
				try {
					c = cs.getConcept(Integer.valueOf(s.trim()));
				}
				catch (Exception ex) {}
			}
			if (c != null) {
				if (isSet) {
					List<Concept> inSet = cs.getConceptsByConceptSet(c);
					ret.addAll(inSet);
				} else {
					ret.add(c);
				}
			}
		}
		return ret;
	}
	
	/**
	 * Return a date that is the same day as the passed in date, but the hours and seconds are the
	 * latest possible for that day.
	 * 
	 * @param date date to adjust
	 * @return a date that is the last possible time in the day
	 */
	public static Date lastSecondOfDay(Date date) {
		if (date == null)
			return null;
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		// TODO: figure out the right way to do this (or at least set milliseconds to zero)
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.add(Calendar.DAY_OF_MONTH, 1);
		c.add(Calendar.SECOND, -1);
		return c.getTime();
	}
	
	public static Date safeDate(Date d1) {
		return new Date(d1.getTime());
	}
	
	/**
	 * Recursively deletes files in the given <code>dir</code> folder
	 * 
	 * @param dir File directory to delete
	 * @return true/false whether the delete was completed successfully
	 * @throws IOException if <code>dir</code> is not a directory
	 */
	public static boolean deleteDirectory(File dir) throws IOException {
		if (!dir.exists() || !dir.isDirectory())
			throw new IOException("Could not delete directory '" + dir.getAbsolutePath() + "' (not a directory)");
		
		if (log.isDebugEnabled())
			log.debug("Deleting directory " + dir.getAbsolutePath());
		
		File[] fileList = dir.listFiles();
		for (File f : fileList) {
			if (f.isDirectory())
				deleteDirectory(f);
			boolean success = f.delete();
			
			if (log.isDebugEnabled())
				log.debug("   deleting " + f.getName() + " : " + (success ? "ok" : "failed"));
			
			if (!success)
				f.deleteOnExit();
		}
		
		boolean success = dir.delete();
		
		if (!success) {
			log.warn("   ...could not remove directory: " + dir.getAbsolutePath());
			dir.deleteOnExit();
		}
		
		if (success && log.isDebugEnabled())
			log.debug("   ...and directory itself");
		
		return success;
	}
	
	/**
	 * Utility method to convert local URL to a File object.
	 * 
	 * @param url an URL
	 * @return file object for given URL or <code>null</code> if URL is not local
	 * @should return null given null parameter
	 */
	public static File url2file(final URL url) {
		if (url == null || !"file".equalsIgnoreCase(url.getProtocol())) {
			return null;
		}
		return new File(url.getFile().replaceAll("%20", " "));
	}
	
	/**
	 * Opens input stream for given resource. This method behaves differently for different URL
	 * types:
	 * <ul>
	 * <li>for <b>local files</b> it returns buffered file input stream;</li>
	 * <li>for <b>local JAR files</b> it reads resource content into memory buffer and returns byte
	 * array input stream that wraps those buffer (this prevents locking JAR file);</li>
	 * <li>for <b>common URL's</b> this method simply opens stream to that URL using standard URL
	 * API.</li>
	 * </ul>
	 * It is not recommended to use this method for big resources within JAR files.
	 * 
	 * @param url resource URL
	 * @return input stream for given resource
	 * @throws IOException if any I/O error has occurred
	 */
	public static InputStream getResourceInputStream(final URL url) throws IOException {
		File file = url2file(url);
		if (file != null) {
			return new BufferedInputStream(new FileInputStream(file));
		}
		if (!"jar".equalsIgnoreCase(url.getProtocol())) {
			return url.openStream();
		}
		String urlStr = url.toExternalForm();
		if (urlStr.endsWith("!/")) {
			//JAR URL points to a root entry
			throw new FileNotFoundException(url.toExternalForm());
		}
		int p = urlStr.indexOf("!/");
		if (p == -1) {
			throw new MalformedURLException(url.toExternalForm());
		}
		String path = urlStr.substring(p + 2);
		file = url2file(new URL(urlStr.substring(4, p)));
		if (file == null) {// non-local JAR file URL
			return url.openStream();
		}
		JarFile jarFile = new JarFile(file);
		try {
			ZipEntry entry = jarFile.getEntry(path);
			if (entry == null) {
				throw new FileNotFoundException(url.toExternalForm());
			}
			InputStream in = jarFile.getInputStream(entry);
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				copyFile(in, out);
				return new ByteArrayInputStream(out.toByteArray());
			}
			finally {
				in.close();
			}
		}
		finally {
			jarFile.close();
		}
	}
	
	/**
	 * @return The path to the directory on the file system that will hold miscellaneous data about
	 *         the application (runtime properties, modules, etc)
	 */
	public static String getApplicationDataDirectory() {
		
		String filepath = null;
		
		if (OpenmrsConstants.APPLICATION_DATA_DIRECTORY != null) {
			filepath = OpenmrsConstants.APPLICATION_DATA_DIRECTORY;
		} else {
			if (OpenmrsConstants.UNIX_BASED_OPERATING_SYSTEM)
				filepath = System.getProperty("user.home") + File.separator + ".OpenMRS";
			else
				filepath = System.getProperty("user.home") + File.separator + "Application Data" + File.separator
				        + "OpenMRS";
			
			filepath = filepath + File.separator;
		}
		
		File folder = new File(filepath);
		if (!folder.exists())
			folder.mkdirs();
		
		return filepath;
	}
	
	/**
	 * Find the given folderName in the application data directory. Or, treat folderName like an
	 * absolute url to a directory
	 * 
	 * @param folderName
	 * @return folder capable of storing information
	 */
	public static File getDirectoryInApplicationDataDirectory(String folderName) throws APIException {
		//  try to load the repository folder straight away.
		File folder = new File(folderName);
		
		// if the property wasn't a full path already, assume it was intended to be a folder in the 
		// application directory
		if (!folder.isAbsolute()) {
			folder = new File(OpenmrsUtil.getApplicationDataDirectory(), folderName);
		}
		
		// now create the directory folder if it doesn't exist
		if (!folder.exists()) {
			log.warn("'" + folder.getAbsolutePath() + "' doesn't exist.  Creating directories now.");
			folder.mkdirs();
		}
		
		if (!folder.isDirectory())
			throw new APIException("'" + folder.getAbsolutePath() + "' should be a directory but it is not");
		
		return folder;
	}
	
	/**
	 * Save the given xml document to the given outfile
	 * 
	 * @param doc Document to be saved
	 * @param outFile file pointer to the location the xml file is to be saved to
	 */
	public static void saveDocument(Document doc, File outFile) {
		OutputStream outStream = null;
		try {
			outStream = new FileOutputStream(outFile);
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			
			DocumentType doctype = doc.getDoctype();
			if (doctype != null) {
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
			}
			
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(outStream);
			transformer.transform(source, result);
		}
		catch (TransformerException e) {
			throw new ModuleException("Error while saving dwrmodulexml back to dwr-modules.xml", e);
		}
		catch (FileNotFoundException e) {
			throw new ModuleException("/WEB-INF/dwr-modules.xml file doesn't exist.", e);
		}
		finally {
			try {
				if (outStream != null)
					outStream.close();
			}
			catch (Exception e) {
				log.warn("Unable to close outstream", e);
			}
		}
	}
	
	public static List<Integer> delimitedStringToIntegerList(String delimitedString, String delimiter) {
		List<Integer> ret = new ArrayList<Integer>();
		String[] tokens = delimitedString.split(delimiter);
		for (String token : tokens) {
			token = token.trim();
			if (token.length() == 0)
				continue;
			else
				ret.add(Integer.valueOf(token));
		}
		return ret;
	}
	
	public static boolean isConceptInList(Concept concept, List<Concept> list) {
		boolean ret = false;
		
		if (concept != null && list != null) {
			for (Concept c : list) {
				if (c.equals(concept)) {
					ret = true;
					break;
				}
			}
		}
		
		return ret;
	}
	
	public static Date fromDateHelper(Date comparisonDate, Integer withinLastDays, Integer withinLastMonths,
	                                  Integer untilDaysAgo, Integer untilMonthsAgo, Date sinceDate, Date untilDate) {
		
		Date ret = null;
		if (withinLastDays != null || withinLastMonths != null) {
			Calendar gc = Calendar.getInstance();
			gc.setTime(comparisonDate != null ? comparisonDate : new Date());
			if (withinLastDays != null)
				gc.add(Calendar.DAY_OF_MONTH, -withinLastDays);
			if (withinLastMonths != null)
				gc.add(Calendar.MONTH, -withinLastMonths);
			ret = gc.getTime();
		}
		if (sinceDate != null && (ret == null || sinceDate.after(ret)))
			ret = sinceDate;
		return ret;
	}
	
	public static Date toDateHelper(Date comparisonDate, Integer withinLastDays, Integer withinLastMonths,
	                                Integer untilDaysAgo, Integer untilMonthsAgo, Date sinceDate, Date untilDate) {
		
		Date ret = null;
		if (untilDaysAgo != null || untilMonthsAgo != null) {
			Calendar gc = Calendar.getInstance();
			gc.setTime(comparisonDate != null ? comparisonDate : new Date());
			if (untilDaysAgo != null)
				gc.add(Calendar.DAY_OF_MONTH, -untilDaysAgo);
			if (untilMonthsAgo != null)
				gc.add(Calendar.MONTH, -untilMonthsAgo);
			ret = gc.getTime();
		}
		if (untilDate != null && (ret == null || untilDate.before(ret)))
			ret = untilDate;
		return ret;
	}
	
	/**
	 * @param collection
	 * @param elements
	 * @return Whether _collection_ contains any of _elements_
	 */
	public static <T> boolean containsAny(Collection<T> collection, Collection<T> elements) {
		for (T obj : elements) {
			if (collection.contains(obj))
				return true;
		}
		return false;
	}
	
	/**
	 * Allows easy manipulation of a Map<?, Set>
	 */
	public static <K, V> void addToSetMap(Map<K, Set<V>> map, K key, V obj) {
		Set<V> set = map.get(key);
		if (set == null) {
			set = new HashSet<V>();
			map.put(key, set);
		}
		set.add(obj);
	}
	
	public static <K, V> void addToListMap(Map<K, List<V>> map, K key, V obj) {
		List<V> list = map.get(key);
		if (list == null) {
			list = new ArrayList<V>();
			map.put(key, list);
		}
		list.add(obj);
	}
	
	/**
	 * Get the current user's date format Will look similar to "mm-dd-yyyy". Depends on user's
	 * locale.
	 * 
	 * @return a simple date format
	 */
	public static SimpleDateFormat getDateFormat() {
		String localeKey = Context.getLocale().toString().toLowerCase();
		
		// get the actual pattern from the constants
		String pattern = OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(localeKey);
		
		// default to the "first" locale pattern
		if (pattern == null)
			pattern = (String) OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().values().toArray()[0];
		
		return new SimpleDateFormat(pattern, Context.getLocale());
	}
	
	/**
	 * @deprecated see reportingcompatibility module
	 */
	@Deprecated
	public static PatientFilter toPatientFilter(PatientSearch search, CohortSearchHistory history) {
		return toPatientFilter(search, history, null);
	}
	
	/**
	 * Takes a String (e.g. a user-entered one) and parses it into an object of the specified class
	 * 
	 * @param string
	 * @param clazz
	 * @return Object of type <code>clazz</code> with the data from <code>string</code>
	 */
	@SuppressWarnings("unchecked")
	public static Object parse(String string, Class clazz) {
		try {
			// If there's a valueOf(String) method, just use that (will cover at least String, Integer, Double, Boolean) 
			Method valueOfMethod = null;
			try {
				valueOfMethod = clazz.getMethod("valueOf", String.class);
			}
			catch (NoSuchMethodException ex) {}
			if (valueOfMethod != null) {
				return valueOfMethod.invoke(null, string);
			} else if (clazz.isEnum()) {
				// Special-case for enum types
				List<Enum> constants = Arrays.asList((Enum[]) clazz.getEnumConstants());
				for (Enum e : constants)
					if (e.toString().equals(string))
						return e;
				throw new IllegalArgumentException(string + " is not a legal value of enum class " + clazz);
			} else if (String.class.equals(clazz)) {
				return string;
			} else if (Location.class.equals(clazz)) {
				try {
					Integer.parseInt(string);
					LocationEditor ed = new LocationEditor();
					ed.setAsText(string);
					return ed.getValue();
				}
				catch (NumberFormatException ex) {
					return Context.getLocationService().getLocation(string);
				}
			} else if (Concept.class.equals(clazz)) {
				ConceptEditor ed = new ConceptEditor();
				ed.setAsText(string);
				return ed.getValue();
			} else if (Program.class.equals(clazz)) {
				ProgramEditor ed = new ProgramEditor();
				ed.setAsText(string);
				return ed.getValue();
			} else if (ProgramWorkflowState.class.equals(clazz)) {
				ProgramWorkflowStateEditor ed = new ProgramWorkflowStateEditor();
				ed.setAsText(string);
				return ed.getValue();
			} else if (EncounterType.class.equals(clazz)) {
				EncounterTypeEditor ed = new EncounterTypeEditor();
				ed.setAsText(string);
				return ed.getValue();
			} else if (Form.class.equals(clazz)) {
				FormEditor ed = new FormEditor();
				ed.setAsText(string);
				return ed.getValue();
			} else if (Drug.class.equals(clazz)) {
				DrugEditor ed = new DrugEditor();
				ed.setAsText(string);
				return ed.getValue();
			} else if (PersonAttributeType.class.equals(clazz)) {
				PersonAttributeTypeEditor ed = new PersonAttributeTypeEditor();
				ed.setAsText(string);
				return ed.getValue();
			} else if (Cohort.class.equals(clazz)) {
				CohortEditor ed = new CohortEditor();
				ed.setAsText(string);
				return ed.getValue();
			} else if (Date.class.equals(clazz)) {
				// TODO: this uses the date format from the current session, which could cause problems if the user changes it after searching. 
				DateFormat df = new SimpleDateFormat(OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(
				    Context.getLocale().toString().toLowerCase()), Context.getLocale());
				CustomDateEditor ed = new CustomDateEditor(df, true, 10);
				ed.setAsText(string);
				return ed.getValue();
			} else if (Object.class.equals(clazz)) {
				// TODO: Decide whether this is a hack. Currently setting Object arguments with a String
				return string;
			} else {
				throw new IllegalArgumentException("Don't know how to handle class: " + clazz);
			}
		}
		catch (Exception ex) {
			log.error("error converting \"" + string + "\" to " + clazz, ex);
			throw new IllegalArgumentException(ex);
		}
	}
	
	/**
	 * Uses reflection to translate a PatientSearch into a PatientFilter
	 * 
	 * @deprecated see reportingcompatibility module
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public static PatientFilter toPatientFilter(PatientSearch search, CohortSearchHistory history,
	                                            EvaluationContext evalContext) {
		if (search.isSavedSearchReference()) {
			PatientSearch ps = ((PatientSearchReportObject) Context.getReportObjectService().getReportObject(
			    search.getSavedSearchId())).getPatientSearch();
			return toPatientFilter(ps, history, evalContext);
		} else if (search.isSavedFilterReference()) {
			return Context.getReportObjectService().getPatientFilterById(search.getSavedFilterId());
		} else if (search.isSavedCohortReference()) {
			Cohort c = Context.getCohortService().getCohort(search.getSavedCohortId());
			// to prevent lazy loading exceptions, cache the member ids here
			if (c != null)
				c.getMemberIds().size();
			return new CohortFilter(c);
		} else if (search.isComposition()) {
			if (history == null && search.requiresHistory())
				throw new IllegalArgumentException("You can't evaluate this search without a history");
			else
				return search.cloneCompositionAsFilter(history, evalContext);
		} else {
			Class clz = search.getFilterClass();
			if (clz == null)
				throw new IllegalArgumentException("search must be saved, composition, or must have a class specified");
			log.debug("About to instantiate " + clz);
			PatientFilter pf = null;
			try {
				pf = (PatientFilter) clz.newInstance();
			}
			catch (Exception ex) {
				log.error("Couldn't instantiate a " + search.getFilterClass(), ex);
				return null;
			}
			Class[] stringSingleton = { String.class };
			if (search.getArguments() != null) {
				for (SearchArgument sa : search.getArguments()) {
					if (log.isDebugEnabled())
						log.debug("Looking at (" + sa.getPropertyClass() + ") " + sa.getName() + " -> " + sa.getValue());
					PropertyDescriptor pd = null;
					try {
						pd = new PropertyDescriptor(sa.getName(), clz);
					}
					catch (IntrospectionException ex) {
						log.error("Error while examining property " + sa.getName(), ex);
						continue;
					}
					Class<?> realPropertyType = pd.getPropertyType();
					
					// instantiate the value of the search argument
					String valueAsString = sa.getValue();
					String testForExpression = search.getArgumentValue(sa.getName());
					if (testForExpression != null) {
						valueAsString = testForExpression;
						log.debug("Setting " + sa.getName() + " to: " + valueAsString);
						if (evalContext != null && EvaluationContext.isExpression(valueAsString)) {
							Object evaluated = evalContext.evaluateExpression(testForExpression);
							if (evaluated != null) {
								if (evaluated instanceof Date)
									valueAsString = Context.getDateFormat().format((Date) evaluated);
								else
									valueAsString = evaluated.toString();
							}
							log.debug("Evaluated " + sa.getName() + " to: " + valueAsString);
						}
					}
					
					Object value = null;
					Class<?> valueClass = sa.getPropertyClass();
					try {
						// If there's a valueOf(String) method, just use that (will cover at least String, Integer, Double, Boolean) 
						Method valueOfMethod = null;
						try {
							valueOfMethod = valueClass.getMethod("valueOf", stringSingleton);
						}
						catch (NoSuchMethodException ex) {}
						if (valueOfMethod != null) {
							Object[] holder = { valueAsString };
							value = valueOfMethod.invoke(pf, holder);
						} else if (realPropertyType.isEnum()) {
							// Special-case for enum types
							List<Enum> constants = Arrays.asList((Enum[]) realPropertyType.getEnumConstants());
							for (Enum e : constants) {
								if (e.toString().equals(valueAsString)) {
									value = e;
									break;
								}
							}
						} else if (String.class.equals(valueClass)) {
							value = valueAsString;
						} else if (Location.class.equals(valueClass)) {
							LocationEditor ed = new LocationEditor();
							ed.setAsText(valueAsString);
							value = ed.getValue();
						} else if (Concept.class.equals(valueClass)) {
							ConceptEditor ed = new ConceptEditor();
							ed.setAsText(valueAsString);
							value = ed.getValue();
						} else if (Program.class.equals(valueClass)) {
							ProgramEditor ed = new ProgramEditor();
							ed.setAsText(valueAsString);
							value = ed.getValue();
						} else if (ProgramWorkflowState.class.equals(valueClass)) {
							ProgramWorkflowStateEditor ed = new ProgramWorkflowStateEditor();
							ed.setAsText(valueAsString);
							value = ed.getValue();
						} else if (EncounterType.class.equals(valueClass)) {
							EncounterTypeEditor ed = new EncounterTypeEditor();
							ed.setAsText(valueAsString);
							value = ed.getValue();
						} else if (Form.class.equals(valueClass)) {
							FormEditor ed = new FormEditor();
							ed.setAsText(valueAsString);
							value = ed.getValue();
						} else if (Drug.class.equals(valueClass)) {
							DrugEditor ed = new DrugEditor();
							ed.setAsText(valueAsString);
							value = ed.getValue();
						} else if (PersonAttributeType.class.equals(valueClass)) {
							PersonAttributeTypeEditor ed = new PersonAttributeTypeEditor();
							ed.setAsText(valueAsString);
							value = ed.getValue();
						} else if (Cohort.class.equals(valueClass)) {
							CohortEditor ed = new CohortEditor();
							ed.setAsText(valueAsString);
							value = ed.getValue();
						} else if (Date.class.equals(valueClass)) {
							// TODO: this uses the date format from the current session, which could cause problems if the user changes it after searching. 
							DateFormat df = Context.getDateFormat(); // new SimpleDateFormat(OpenmrsConstants.OPENMRS_LOCALE_DATE_PATTERNS().get(Context.getLocale().toString().toLowerCase()), Context.getLocale());
							CustomDateEditor ed = new CustomDateEditor(df, true, 10);
							ed.setAsText(valueAsString);
							value = ed.getValue();
						} else if (LogicCriteria.class.equals(valueClass)) {
							value = Context.getLogicService().parseString(valueAsString);
						} else {
							// TODO: Decide whether this is a hack. Currently setting Object arguments with a String
							value = valueAsString;
						}
					}
					catch (Exception ex) {
						log.error("error converting \"" + valueAsString + "\" to " + valueClass, ex);
						continue;
					}
					
					if (value != null) {
						
						if (realPropertyType.isAssignableFrom(valueClass)) {
							log.debug("setting value of " + sa.getName() + " to " + value);
							try {
								pd.getWriteMethod().invoke(pf, value);
							}
							catch (Exception ex) {
								log.error(
								    "Error setting value of " + sa.getName() + " to " + sa.getValue() + " -> " + value, ex);
								continue;
							}
						} else if (Collection.class.isAssignableFrom(realPropertyType)) {
							log.debug(sa.getName() + " is a Collection property");
							// if realPropertyType is a collection, add this value to it (possibly after instantiating)
							try {
								Collection collection = (Collection) pd.getReadMethod().invoke(pf, (Object[]) null);
								if (collection == null) {
									// we need to instantiate this collection. I'm going with the following rules, which should be rethought:
									//	 SortedSet -> TreeSet
									//	 Set -> HashSet
									//   Otherwise -> ArrayList
									if (SortedSet.class.isAssignableFrom(realPropertyType)) {
										collection = new TreeSet();
										log.debug("instantiated a TreeSet");
										pd.getWriteMethod().invoke(pf, collection);
									} else if (Set.class.isAssignableFrom(realPropertyType)) {
										collection = new HashSet();
										log.debug("instantiated a HashSet");
										pd.getWriteMethod().invoke(pf, collection);
									} else {
										collection = new ArrayList();
										log.debug("instantiated an ArrayList");
										pd.getWriteMethod().invoke(pf, collection);
									}
								}
								collection.add(value);
							}
							catch (Exception ex) {
								log.error("Error instantiating collection for property " + sa.getName() + " whose class is "
								        + realPropertyType, ex);
								continue;
							}
						} else {
							log.error(pf.getClass() + " . " + sa.getName() + " should be " + realPropertyType
							        + " but is given as " + valueClass);
						}
					}
				}
			}
			log.debug("Returning " + pf);
			return pf;
		}
	}
	
	/**
	 * Loops over the collection to check to see if the given object is in that collection. This
	 * method <i>only</i> uses the .equals() method for comparison. This should be used in the
	 * patient/person objects on their collections. Their collections are SortedSets which use the
	 * compareTo method for equality as well. The compareTo method is currently optimized for
	 * sorting, not for equality. A null <code>obj</code> will return false
	 * 
	 * @param objects collection to loop over
	 * @param obj Object to look for in the <code>objects</code>
	 * @return true/false whether the given object is found
	 * @should use equals method for comparison instead of compareTo given List collection
	 * @should use equals method for comparison instead of compareTo given SortedSet collection
	 */
	public static boolean collectionContains(Collection<?> objects, Object obj) {
		if (obj == null || objects == null)
			return false;
		
		for (Object o : objects) {
			if (o != null && o.equals(obj))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Get a serializer that will do the common type of serialization and deserialization. Cycles of
	 * objects are taken into account
	 * 
	 * @return Serializer to do the (de)serialization
	 * @deprecated - Use OpenmrsSerializer from
	 *             Context.getSerializationService.getDefaultSerializer() Note, this uses a
	 *             different Serialization mechanism, so you may need to use this for conversion
	 */
	@Deprecated
	public static Serializer getSerializer() {
		return new Persister(new OpenmrsCycleStrategy());
	}
	
	/**
	 * Get a short serializer that will only do the very basic serialization necessary. This is
	 * controlled by the objects that are being serialized via the @Replace methods
	 * 
	 * @return Serializer to do the short (de)serialization
	 * @see OpenmrsConstants#SHORT_SERIALIZATION
	 * @deprecated - Use OpenmrsSerializer from
	 *             Context.getSerializationService.getDefaultSerializer() Note, this uses a
	 *             different Serialization mechanism, so you may need to use this for conversion
	 */
	@Deprecated
	public static Serializer getShortSerializer() {
		return new Persister(new OpenmrsCycleStrategy(true));
	}
	
	/**
	 * True/false whether the current serialization is supposed to be a short serialization. A
	 * shortened serialization This should be called from methods marked with the @Replace notation
	 * that take in a single <code>Map</code> parameter.
	 * 
	 * @param sessionMap current serialization session
	 * @return true/false whether or not to do the shortened serialization
	 * @deprecated - use SerializationService and OpenmrsSerializer implementation for Serialization
	 */
	@Deprecated
	public static boolean isShortSerialization(Map<?, ?> sessionMap) {
		return sessionMap.containsKey(OpenmrsConstants.SHORT_SERIALIZATION);
	}
	
	/**
	 * Gets an out File object. If date is not provided, the current timestamp is used. If user is
	 * not provided, the user id is not put into the filename. Assumes dir is already created
	 * 
	 * @param dir directory to make the random filename in
	 * @param date optional Date object used for the name
	 * @param user optional User creating this file object
	 * @return file new file that is able to be written to
	 */
	public static File getOutFile(File dir, Date date, User user) {
		
		File outFile;
		do {
			// format to print date in filenmae
			DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-HHmm-ssSSS");
			
			// use current date if none provided
			if (date == null)
				date = new Date();
			
			StringBuilder filename = new StringBuilder();
			
			// the start of the filename is the time so we can do some sorting
			filename.append(dateFormat.format(date));
			
			// insert the user id if they provided it
			if (user != null) {
				filename.append("-");
				filename.append(user.getUserId());
				filename.append("-");
			}
			
			// the end of the filename is a randome number between 0 and 10000
			filename.append((int) (Math.random() * 10000));
			filename.append(".xml");
			
			outFile = new File(dir, filename.toString());
			
			// set to null to avoid very minimal possiblity of an infinite loop
			date = null;
			
		} while (outFile.exists());
		
		return outFile;
	}
	
	/**
	 * Creates a relatively acceptable unique string of the give size
	 * 
	 * @return unique string
	 */
	public static String generateUid(Integer size) {
		StringBuffer sb = new StringBuffer(size);
		for (int i = 0; i < size; i++) {
			int ch = (int) (Math.random() * 62);
			if (ch < 10) // 0-9
				sb.append(ch);
			else if (ch < 36) // a-z
				sb.append((char) (ch - 10 + 'a'));
			else
				sb.append((char) (ch - 36 + 'A'));
		}
		return sb.toString();
	}
	
	/**
	 * Creates a uid of length 20
	 * 
	 * @see #generateUid(Integer)
	 */
	public static String generateUid() {
		return generateUid(20);
	}
	
	/**
	 * Post the given map of variables to the given url string
	 * 
	 * @param urlString valid http url to post data to
	 * @param dataToPost Map<String, String> of key value pairs to post to urlString
	 * @return response from urlString after posting
	 */
	public static String postToUrl(String urlString, Map<String, String> dataToPost) {
		OutputStreamWriter wr = null;
		BufferedReader rd = null;
		String response = "";
		StringBuffer data = null;
		
		try {
			// Construct data
			for (Map.Entry<String, String> entry : dataToPost.entrySet()) {
				
				// skip over invalid post variables
				if (entry.getKey() == null || entry.getValue() == null)
					continue;
				
				// create the string buffer if this is the first variable
				if (data == null)
					data = new StringBuffer();
				else
					data.append("&"); // only append this if its _not_ the first datum
					
				// finally, setup the actual post string
				data.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
				data.append("=");
				data.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			}
			
			// Send the data
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Length", String.valueOf(data.length()));
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data.toString());
			wr.flush();
			wr.close();
			
			// Get the response
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				response = response + line + "\n";
			}
			
		}
		catch (Exception e) {
			log.warn("Exception while posting to : " + urlString, e);
			log.warn("Reponse from server was: " + response);
		}
		finally {
			if (wr != null)
				try {
					wr.close();
				}
				catch (Exception e) { /* pass */}
			if (rd != null)
				try {
					rd.close();
				}
				catch (Exception e) { /* pass */}
		}
		
		return response;
	}
	
	/**
	 * Convenience method to replace Properties.store(), which isn't UTF-8 compliant
	 * 
	 * @param properties
	 * @param file
	 * @param comment
	 */
	public static void storeProperties(Properties properties, File file, String comment) {
		OutputStream outStream = null;
		try {
			outStream = new FileOutputStream(file, true);
			storeProperties(properties, outStream, comment);
		}
		catch (IOException ex) {
			log.error("Unable to create file " + file.getAbsolutePath() + " in storeProperties routine.");
		}
		finally {
			try {
				if (outStream != null)
					outStream.close();
			}
			catch (IOException ioe){
				//pass
			}
		}
	}
	
	/**
	 * Convenience method to replace Properties.store(), which isn't UTF-8 compliant
	 * 
	 * @param properties
	 * @param file
	 * @param comment (which appears in comments in properties file)
	 */
	public static void storeProperties(Properties properties, OutputStream outStream, String comment) {
		try {
			OutputStreamWriter osw = new OutputStreamWriter(new BufferedOutputStream(outStream), "UTF-8");
			Writer out = new BufferedWriter(osw);
			if (comment != null)
				out.write("\n#" + comment + "\n");
			out.write("#" + new Date() + "\n");
			for (Map.Entry<Object, Object> e : properties.entrySet()) {
				out.write(e.getKey() + "=" + e.getValue() + "\n");
			}
			out.write("\n");
			out.flush();
			out.close();
		}
		catch (FileNotFoundException fnfe) {
			log.error("target file not found" + fnfe);
		}
		catch (UnsupportedEncodingException ex) { //pass
			log.error("unsupported encoding error hit" + ex);
		}
		catch (IOException ioex) {
			log.error("IO exception encountered trying to append to properties file" + ioex);
		}
		
	}
	
	/**
	 * This method is a replacement for Properties.load(InputStream) so that we can load in utf-8
	 * characters. Currently the load method expects the inputStream to point to a latin1 encoded
	 * file.
	 * 
	 * @param props the properties object to write into
	 * @param input the input stream to read from
	 */
	public static void loadProperties(Properties props, InputStream input) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
			while (reader.ready()) {
				String line = reader.readLine();
				if (line.length() > 0 && line.charAt(0) != '#') {
					int pos = line.indexOf("=");
					if (pos > 0) {
						String keyString = line.substring(0, pos);
						String valueString = line.substring(pos + 1);
						if (keyString != null && keyString.length() > 0) {
							props.put(keyString, fixPropertiesValueString(valueString));
						}
					}
				}
			}
			reader.close();
		}
		catch (UnsupportedEncodingException uee) {
			log.error("Unsupported encoding used in properties file " + uee);
		}
		catch (IOException ioe) {
			log.error("Unable to read properties from properties file " + ioe);
		}
	}
	
	/**
	 * By default java will escape colons and equal signs when writing properites files. <br/>
	 * <br/>
	 * This method turns escaped colons into colons and escaped equal signs into just equal signs.
	 * 
	 * @param value the value portion of a properties file to fix
	 * @return the value with escaped characters fixed
	 */
	private static String fixPropertiesValueString(String value) {
		String returnString = value.replace("\n", "");
		returnString = returnString.replace("\\:", ":");
		returnString = returnString.replace("\\=", "=");
		
		return returnString;
	}
	
	/**
	 * Utility to check the validity of a password for a certain {@link User}. Valid password are
	 * string with:
	 * <ul>
	 * <li>8 character minimum length
	 * <li>have at least one digit
	 * <li>have at least one upper case character
	 * <li>not equal to {@link User}'s username or system id
	 * </ul>
	 * The regular expression currently used is "^(?=.*?[0-9])(?=.*?[A-Z])[\\w]*$".
	 * 
	 * @param username user name of the user with password to validated
	 * @param password string that will be validated
	 * @param systemId system id of the user with password to be validated
	 * @throws PasswordException
	 * @since 1.5
	 * @should fail with short password
	 * @should fail with digit only password
	 * @should fail with char only password
	 * @should fail without upper case char password
	 * @should fail with password equals to user name
	 * @should fail with password equals to system id
	 */
	public static void validatePassword(String username, String password, String systemId) throws PasswordException {
		if (password.length() < 8)
			throw new ShortPasswordException();
		Pattern pattern = Pattern.compile("^(?=.*?[0-9])(?=.*?[A-Z])[\\w]*$");
		Matcher matcher = pattern.matcher(password);
		if (!matcher.matches())
			throw new InvalidCharactersPasswordException();
		if (password.equals(username) || password.equals(systemId))
			throw new WeakPasswordException();
	}
}
