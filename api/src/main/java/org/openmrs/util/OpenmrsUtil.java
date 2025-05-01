/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import javax.activation.MimetypesFileTypeMap;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptReferenceRange;
import org.openmrs.Drug;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.PersonAttributeType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.User;
import org.openmrs.annotation.AddOnStartup;
import org.openmrs.annotation.HasAddOnStartupPrivileges;
import org.openmrs.annotation.Logging;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.InvalidCharactersPasswordException;
import org.openmrs.api.PasswordException;
import org.openmrs.api.ShortPasswordException;
import org.openmrs.api.WeakPasswordException;
import org.openmrs.api.context.Context;
import org.openmrs.logging.OpenmrsLoggingUtil;
import org.openmrs.module.ModuleException;
import org.openmrs.module.ModuleFactory;
import org.openmrs.propertyeditor.CohortEditor;
import org.openmrs.propertyeditor.ConceptEditor;
import org.openmrs.propertyeditor.DrugEditor;
import org.openmrs.propertyeditor.EncounterTypeEditor;
import org.openmrs.propertyeditor.FormEditor;
import org.openmrs.propertyeditor.LocationEditor;
import org.openmrs.propertyeditor.PersonAttributeTypeEditor;
import org.openmrs.propertyeditor.ProgramEditor;
import org.openmrs.propertyeditor.ProgramWorkflowStateEditor;
import org.openmrs.validator.ObsValidator;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.NoSuchMessageException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 * Utility methods used in openmrs
 */
public class OpenmrsUtil {
	private OpenmrsUtil() {
	}
	
	private static volatile MimetypesFileTypeMap mimetypesFileTypeMap = null;
	
	private static org.slf4j.Logger log = LoggerFactory.getLogger(OpenmrsUtil.class);
	
	private static Map<Locale, SimpleDateFormat> dateFormatCache = new HashMap<>();
	
	private static Map<Locale, SimpleDateFormat> timeFormatCache = new HashMap<>();
	
	/**
	 * Compares origList to newList returning map of differences
	 * 
	 * @param origList
	 * @param newList
	 * @return [List toAdd, List toDelete] with respect to origList
	 */
	public static <E> Collection<Collection<E>> compareLists(Collection<E> origList, Collection<E> newList) {	
		Collection<Collection<E>> returnList = new ArrayList<>();
		
		Collection<E> toAdd = new LinkedList<>();
		Collection<E> toDel = new LinkedList<>();
		
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
			if (!foundInList) {
				toAdd.add(currentNewListObj);
			}
			
			// all found new objects were removed from the orig list,
			// leaving only objects needing to be removed
			toDel = origList;
			
		}
		
		returnList.add(toAdd);
		returnList.add(toDel);
		
		return returnList;
	}
	
	public static boolean isStringInArray(String str, String[] arr) {
		if (str != null && arr != null) {
			for (String anArr : arr) {
				if (str.equals(anArr)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static Boolean isInNormalNumericRange(Float value, ConceptNumeric concept) {
		if (concept.getHiNormal() == null || concept.getLowNormal() == null) {
			return false;
		}
		return (value <= concept.getHiNormal() && value >= concept.getLowNormal());
	}
	
	public static Boolean isInCriticalNumericRange(Float value, ConceptNumeric concept) {
		if (concept.getHiCritical() == null || concept.getLowCritical() == null) {
			return false;
		}
		return (value <= concept.getHiCritical() && value >= concept.getLowCritical());
	}
	
	public static Boolean isInAbsoluteNumericRange(Float value, ConceptNumeric concept) {
		if (concept.getHiAbsolute() == null || concept.getLowAbsolute() == null) {
			return false;
		}
		return (value <= concept.getHiAbsolute() && value >= concept.getLowAbsolute());
	}
	
	public static Boolean isValidNumericValue(Float value, ConceptNumeric concept) {
		if (concept.getHiAbsolute() == null || concept.getLowAbsolute() == null) {
			return true;
		}
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
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
		char[] buf = new char[1024];
		int numRead;
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
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			byte[] b = new byte[fileInputStream.available()];
			fileInputStream.read(b);
			return b;
		}
		catch (Exception e) {
			log.error("Unable to get file as byte array", e);
		}
		finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				}
				catch (IOException io) {
					log.warn("Couldn't close fileInputStream: " + io);
				}
			}
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
	 * <strong>Should</strong> not copy the outputstream if outputstream is null
	 * <strong>Should</strong> not copy the outputstream if inputstream is null
	 * <strong>Should</strong> copy inputstream to outputstream and close the outputstream
	 */
	public static void copyFile(InputStream inputStream, OutputStream outputStream) throws IOException {
		if (inputStream == null || outputStream == null) {
			if (outputStream != null) {
				IOUtils.closeQuietly(outputStream);
			}
			return;
		}
		
		try {
			IOUtils.copy(inputStream, outputStream);
		}
		finally {
			IOUtils.closeQuietly(outputStream);
		}
	}
	
	/**
	 * Get mime type of the given file
	 *
	 * @param file
	 * @return mime type
	 */
	public static String getFileMimeType(File file) {
		if (mimetypesFileTypeMap == null) {
			synchronized (OpenmrsUtil.class) {
				mimetypesFileTypeMap = new MimetypesFileTypeMap();
			}
		}
		return mimetypesFileTypeMap.getContentType(file);
	}
	
	/**
	 * Look for a file named <code>filename</code> in folder
	 * 
	 * @param folder
	 * @param filename
	 * @return true/false whether filename exists in folder
	 */
	public static boolean folderContains(File folder, String filename) {
		if (folder == null) {
			return false;
		}
		if (!folder.isDirectory()) {
			return false;
		}
		File[] files = folder.listFiles();
		if (files == null) {
			return false;
		}
		
		for (File f : files) {
			if (f.getName().equals(filename)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * These are the privileges that are required by OpenMRS. This looks for privileges marked as
	 * {@link AddOnStartup} to know which privs, upon startup or loading of a module, to insert into
	 * the database if they do not exist already. These privileges are not allowed to be deleted.
	 * They are marked as 'locked' in the administration screens.
	 * 
	 * @return privileges core to the system
	 * @see PrivilegeConstants
	 * @see Context#checkCoreDataset()
	 */
	public static Map<String, String> getCorePrivileges() {
		Map<String, String> corePrivileges = new HashMap<>();
		
		// TODO getCorePrivileges() is called so so many times that getClassesWithAnnotation() better do some catching.
		Set<Class<?>> classes = OpenmrsClassScanner.getInstance().getClassesWithAnnotation(HasAddOnStartupPrivileges.class);
		
		for (Class cls : classes) {
			Field[] flds = cls.getDeclaredFields();
			for (Field fld : flds) {
				String fieldValue = null;
				
				AddOnStartup privilegeAnnotation = fld.getAnnotation(AddOnStartup.class);
				if (null == privilegeAnnotation) {
					continue;
				}
				if (!privilegeAnnotation.core()) {
					continue;
				}
				
				try {
					fieldValue = (String) fld.get(null);
				}
				catch (IllegalAccessException e) {
					log.error("Field is inaccessible.", e);
				}
				corePrivileges.put(fieldValue, privilegeAnnotation.description());
			}
		}
		
		// always add the module core privileges back on
		for (org.openmrs.Privilege privilege : ModuleFactory.getPrivileges()) {
			corePrivileges.put(privilege.getPrivilege(), privilege.getDescription());
		}
		
		return corePrivileges;
	}
	
	/**
	 * All roles returned by this method are inserted into the database if they do not exist
	 * already. These roles are also forbidden to be deleted from the administration screens.
	 * 
	 * @return roles that are core to the system
	 */
	public static Map<String, String> getCoreRoles() {
		Map<String, String> roles = new HashMap<>();
		
		Field[] flds = RoleConstants.class.getDeclaredFields();
		for (Field fld : flds) {
			String fieldValue = null;
			
			AddOnStartup roleAnnotation = fld.getAnnotation(AddOnStartup.class);
			if (null == roleAnnotation) {
				continue;
			}
			if (!roleAnnotation.core()) {
				continue;
			}
			
			try {
				fieldValue = (String) fld.get(null);
			}
			catch (IllegalAccessException e) {
				log.error("Field is inaccessible.", e);
			}
			roles.put(fieldValue, roleAnnotation.description());
		}
		
		return roles;
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
		if (val != null && "true".equalsIgnoreCase(val)) {
			OpenmrsConstants.OBSCURE_PATIENTS = true;
		}
		
		val = p.getProperty("obscure_patients.family_name", null);
		if (val != null) {
			OpenmrsConstants.OBSCURE_PATIENTS_FAMILY_NAME = val;
		}
		
		val = p.getProperty("obscure_patients.given_name", null);
		if (val != null) {
			OpenmrsConstants.OBSCURE_PATIENTS_GIVEN_NAME = val;
		}
		
		val = p.getProperty("obscure_patients.middle_name", null);
		if (val != null) {
			OpenmrsConstants.OBSCURE_PATIENTS_MIDDLE_NAME = val;
		}
		
		// Override the default "openmrs" database name
		val = p.getProperty("connection.database_name", null);
		if (val == null) {
			// the database name wasn't supplied explicitly, guess it
			// from the connection string
			val = p.getProperty("connection.url", null);
			
			if (val != null) {
				try {
					int endIndex = val.lastIndexOf("?");
					if (endIndex == -1) {
						endIndex = val.length();
					}
					int startIndex = val.lastIndexOf("/", endIndex);
					val = val.substring(startIndex + 1, endIndex);
					OpenmrsConstants.DATABASE_NAME = val;
				}
				catch (Exception e) {
					log.error(MarkerFactory.getMarker("FATAL"), "Database name cannot be configured from 'connection.url' ."
					        + "Either supply 'connection.database_name' or correct the url",
					    e);
				}
			}
		}
		
		// set the business database name
		val = p.getProperty("connection.database_business_name", null);
		if (val == null) {
			val = OpenmrsConstants.DATABASE_NAME;
		}
		OpenmrsConstants.DATABASE_BUSINESS_NAME = val;
	}
	
	/**
	 * Gets the in-memory log appender. This method needed to be added as it is much more difficult to
	 * get a specific appender in the Log4J2 architecture. This method is called in places where we need
	 * to display logging message.
	 *
	 * @since 2.4.0
	 * @deprecated As of 2.4.4, 2.5.1, and 2.6.0; replaced by {@link OpenmrsLoggingUtil#getMemoryAppender()} instead
	 */
	@Deprecated
	public static MemoryAppender getMemoryAppender() {
		return new MemoryAppender(OpenmrsLoggingUtil.getMemoryAppender());
	}
	
	/**
	 * Set the org.openmrs log4j logger's level if global property log.level.openmrs (
	 * OpenmrsConstants.GLOBAL_PROPERTY_LOG_LEVEL ) exists. Valid values for global property are
	 * trace, debug, info, warn, error or fatal.
	 * 
	 * @deprecated As of 2.4.4, 2.5.1, and 2.6.0; replaced by {@link OpenmrsLoggingUtil#applyLogLevels()}
	 */
	@Logging(ignore = true)
	@Deprecated
	public static void applyLogLevels() {
		OpenmrsLoggingUtil.applyLogLevels();
	}
	
	/**
	 * Setup root level log appenders.
	 *
	 * @since 1.9.2
	 * @deprecated As of 2.4.4, 2.5.1, and 2.6.0; replaced by {@link OpenmrsLoggingUtil#reloadLoggingConfiguration()}
	 */
	@Deprecated
	public static void setupLogAppenders() {
		OpenmrsLoggingUtil.reloadLoggingConfiguration();
	}
	
	/**
	 * Set the log4j log level for class <code>logClass</code> to <code>logLevel</code>.
	 * 
	 * @param logClass optional string giving the class level to change. Defaults to
	 *            OpenmrsConstants.LOG_CLASS_DEFAULT . Should be something like org.openmrs.___
	 * @param logLevel one of OpenmrsConstants.LOG_LEVEL_*
	 *                 
	 * @deprecated As of 2.4.4, 2.5.1, and 2.6.0; replaced by {@link OpenmrsLoggingUtil#applyLogLevel(String, String)}
	 */
	@Deprecated
	public static void applyLogLevel(String logClass, String logLevel) {
		OpenmrsLoggingUtil.applyLogLevel(logClass, logLevel);
	}
	
	/**
	 * Takes a String like "size=compact|order=date" and returns a Map&lt;String,String&gt; from the
	 * keys to the values.
	 * 
	 * @param paramList <code>String</code> with a list of parameters
	 * @return Map&lt;String, String&gt; of the parameters passed
	 */
	public static Map<String, String> parseParameterList(String paramList) {
		Map<String, String> ret = new HashMap<>();
		if (paramList != null && paramList.length() > 0) {
			String[] args = paramList.split("\\|");
			for (String s : args) {
				int ind = s.indexOf('=');
				if (ind <= 0) {
					throw new IllegalArgumentException(
					        "Misformed argument in dynamic page specification string: '" + s + "' is not 'key=value'.");
				}
				String name = s.substring(0, ind);
				String value = s.substring(ind + 1);
				ret.put(name, value);
			}
		}
		return ret;
	}
	
	public static <Arg1, Arg2 extends Arg1> boolean nullSafeEquals(Arg1 d1, Arg2 d2) {
		if (d1 == null) {
			return d2 == null;
		} else if (d2 == null) {
			return false;
		}
		return (d1 instanceof Date && d2 instanceof Date) ? compare((Date) d1, (Date) d2) == 0 : d1.equals(d2);
	}
	
	/**
	 * Compares two java.util.Date objects, but handles java.sql.Timestamp (which is not directly
	 * comparable to a date) by dropping its nanosecond value.
	 */
	public static int compare(Date d1, Date d2) {
		if (d1 instanceof Timestamp && d2 instanceof Timestamp) {
			return d1.compareTo(d2);
		}
		if (d1 instanceof Timestamp) {
			d1 = new Date(d1.getTime());
		}
		if (d2 instanceof Timestamp) {
			d2 = new Date(d2.getTime());
		}
		return d1.compareTo(d2);
	}
	
	/**
	 * Compares two Date/Timestamp objects, treating null as the earliest possible date.
	 */
	public static int compareWithNullAsEarliest(Date d1, Date d2) {
		if (d1 == null && d2 == null) {
			return 0;
		}
		if (d1 == null) {
			return -1;
		} else if (d2 == null) {
			return 1;
		} else {
			return compare(d1, d2);
		}
	}
	
	/**
	 * Compares two Date/Timestamp objects, treating null as the earliest possible date.
	 */
	public static int compareWithNullAsLatest(Date d1, Date d2) {
		if (d1 == null && d2 == null) {
			return 0;
		}
		if (d1 == null) {
			return 1;
		} else if (d2 == null) {
			return -1;
		} else {
			return compare(d1, d2);
		}
	}
	
	public static <E extends Comparable<E>> int compareWithNullAsLowest(E c1, E c2) {
		if (c1 == null && c2 == null) {
			return 0;
		}
		if (c1 == null) {
			return -1;
		} else if (c2 == null) {
			return 1;
		} else {
			return c1.compareTo(c2);
		}
	}
	
	public static <E extends Comparable<E>> int compareWithNullAsGreatest(E c1, E c2) {
		if (c1 == null && c2 == null) {
			return 0;
		}
		if (c1 == null) {
			return 1;
		} else if (c2 == null) {
			return -1;
		} else {
			return c1.compareTo(c2);
		}
	}
	
	/**
	 * Converts a collection to a String with a specified separator between all elements
	 * 
	 * @param c Collection to be joined
	 * @param separator string to put between all elements
	 * @return a String representing the toString() of all elements in c, separated by separator
	 * @deprecated as of 2.2 use Java's {@link String#join} or Apache Commons StringUtils.join for iterables which do not extend {@link CharSequence}
	 */
	@Deprecated
	public static <E> String join(Collection<E> c, String separator) {
		if (c == null) {
			return "";
		}
		
		StringBuilder ret = new StringBuilder();
		for (Iterator<E> i = c.iterator(); i.hasNext();) {
			ret.append(i.next());
			if (i.hasNext()) {
				ret.append(separator);
			}
		}
		return ret.toString();
	}
	
	public static Set<Concept> conceptSetHelper(String descriptor) {
		Set<Concept> ret = new HashSet<>();
		if (descriptor == null || descriptor.length() == 0) {
			return ret;
		}
		ConceptService cs = Context.getConceptService();
		
		for (StringTokenizer st = new StringTokenizer(descriptor, "|"); st.hasMoreTokens();) {
			String s = st.nextToken().trim();
			boolean isSet = s.startsWith("set:");
			if (isSet) {
				s = s.substring(4).trim();
			}
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
	 * Parses and loads a delimited list of concept ids or names
	 * 
	 * @param delimitedString the delimited list of concept ids or names
	 * @param delimiter the delimiter, e.g. ","
	 * @return the list of concepts
	 * @since 1.10, 1.9.2, 1.8.5
	 */
	public static List<Concept> delimitedStringToConceptList(String delimitedString, String delimiter) {
		List<Concept> ret = null;
		
		if (delimitedString != null) {
			String[] tokens = delimitedString.split(delimiter);
			for (String token : tokens) {
				Integer conceptId;
				
				try {
					conceptId = Integer.valueOf(token);
				}
				catch (NumberFormatException nfe) {
					conceptId = null;
				}
				
				Concept c;
				
				if (conceptId != null) {
					c = Context.getConceptService().getConcept(conceptId);
				} else {
					c = Context.getConceptService().getConceptByName(token);
				}
				
				if (c != null) {
					if (ret == null) {
						ret = new ArrayList<>();
					}
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
				Concept c = Context.getConceptService().getConcept(token);
				
				if (c != null) {
					if (ret == null) {
						ret = new HashMap<>();
					}
					ret.put(token, c);
				}
			}
		}
		
		return ret;
	}

	public static List<Concept> conceptListHelper(String descriptor) {
		Set<Concept> ret = new LinkedHashSet<>();
		if (descriptor == null || descriptor.length() == 0) {
			return Collections.emptyList();
		}
		ConceptService cs = Context.getConceptService();
		
		for (StringTokenizer st = new StringTokenizer(descriptor, "|"); st.hasMoreTokens();) {
			String s = st.nextToken().trim();
			boolean isSet = s.startsWith("set:");
			if (isSet) {
				s = s.substring(4).trim();
			}
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
		return new ArrayList<>(ret);
	}
	
	/**
	 * Gets the date having the last millisecond of a given day. Meaning that the hours, seconds,
	 * and milliseconds are the latest possible for that day.
	 * 
	 * @param day the day.
	 * @return the date with the last millisecond of the day.
	 */
	public static Date getLastMomentOfDay(Date day) {
		Calendar calender = Calendar.getInstance();
		calender.setTime(day);
		calender.set(Calendar.HOUR_OF_DAY, 23);
		calender.set(Calendar.MINUTE, 59);
		calender.set(Calendar.SECOND, 59);
		calender.set(Calendar.MILLISECOND, 999);
		
		return calender.getTime();
	}
	
	/**
	 * Return a date that is the same day as the passed in date, but the hours and seconds are the
	 * earliest possible for that day.
	 * 
	 * @param date date to adjust
	 * @return a date that is the first possible time in the day
	 * @since 1.9
	 */
	public static Date firstSecondOfDay(Date date) {
		if (date == null) {
			return null;
		}
		
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
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
		if (!dir.exists() || !dir.isDirectory()) {
			throw new IOException("Could not delete directory '" + dir.getAbsolutePath() + "' (not a directory)");
		}
		
		log.debug("Deleting directory {}", dir.getAbsolutePath());
		
		File[] fileList = dir.listFiles();
		if (fileList == null) {
			return false;
		}
		for (File f : fileList) {
			if (f.isDirectory()) {
				deleteDirectory(f);
			}
			boolean success = f.delete();
			
			if (log.isDebugEnabled()) {
				log.debug("   deleting " + f.getName() + " : " + (success ? "ok" : "failed"));
			}
			
			if (!success) {
				f.deleteOnExit();
			}
		}
		
		boolean success = dir.delete();
		
		if (!success) {
			log.warn("   ...could not remove directory: " + dir.getAbsolutePath());
			dir.deleteOnExit();
		}
		
		if (success && log.isDebugEnabled()) {
			log.debug("   ...and directory itself");
		}
		
		return success;
	}
	
	/**
	 * Utility method to convert local URL to a File object.
	 * 
	 * @param url an URL
	 * @return file object for given URL or <code>null</code> if URL is not local
	 * <strong>Should</strong> return null given null parameter
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
			// JAR URL points to a root entry
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
		try (JarFile jarFile = new JarFile(file)) {
			ZipEntry entry = jarFile.getEntry(path);
			if (entry == null) {
				throw new FileNotFoundException(url.toExternalForm());
			}
			try (InputStream in = jarFile.getInputStream(entry)) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				copyFile(in, out);
				return new ByteArrayInputStream(out.toByteArray());
			}
		}
	}
	
	/**
	 * <pre>
	 * Returns the application data directory. Searches for the value first 
	 * in the "OPENMRS_APPLICATION_DATA_DIRECTORY" system property and "application_data_directory" runtime property, then in the servlet
	 * init parameter "application.data.directory." If not found, returns:
	 * a) "{user.home}/.OpenMRS" on UNIX-based systems
	 * b) "{user.home}\Application Data\OpenMRS" on Windows
	 * 
	 * </pre>
	 * 
	 * @return The path to the directory on the file system that will hold miscellaneous data about
	 *         the application (runtime properties, modules, etc)
	 */
	public static String getApplicationDataDirectory() {
		return getApplicationDataDirectoryAsFile().toString();
	}
	
	public static File getApplicationDataDirectoryAsFile() {
		String filepath = null;
		final String openmrsDir = "OpenMRS";
		
		String systemProperty = System.getProperty(OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY);
		//System and runtime property take precedence
		if (StringUtils.isNotBlank(systemProperty)) {
			filepath = systemProperty;
		} else {
			String runtimeProperty = Context.getRuntimeProperties()
				.getProperty(OpenmrsConstants.APPLICATION_DATA_DIRECTORY_RUNTIME_PROPERTY, null);
			if (StringUtils.isNotBlank(runtimeProperty)) {
				filepath = runtimeProperty;
			}
		}
		
		if (filepath == null) {
			if (OpenmrsConstants.UNIX_BASED_OPERATING_SYSTEM) {
				filepath = Paths.get(System.getProperty("user.home"), "." + openmrsDir).toString();
				if (!canWrite(new File(filepath))) {
					log.warn("Unable to write to users home dir, fallback to: "
						+ OpenmrsConstants.APPLICATION_DATA_DIRECTORY_FALLBACK_UNIX);
					filepath = Paths.get(OpenmrsConstants.APPLICATION_DATA_DIRECTORY_FALLBACK_UNIX, openmrsDir).toString();
				}
			} else {
				filepath = Paths.get(System.getProperty("user.home"), "Application Data", "OpenMRS").toString();
				if (!new File(filepath).exists()) {
					filepath = Paths.get(System.getenv("appdata"), "OpenMRS").toString();
				}
				if (!canWrite(new File(filepath))) {
					log.warn("Unable to write to users home dir, fallback to: "
						+ OpenmrsConstants.APPLICATION_DATA_DIRECTORY_FALLBACK_WIN);
					filepath = OpenmrsConstants.APPLICATION_DATA_DIRECTORY_FALLBACK_WIN + File.separator + openmrsDir;
				}
			}
			
			filepath = filepath + File.separator;
		}
		
		File folder = new File(filepath);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		
		return folder;
	}
	
	/**
	 * Can be used to override default application data directory.
	 * <p>
	 * Note that it will not override application data directory provided as a system property.
	 * 
	 * @param path
	 * @since 1.11
	 */
	public static void setApplicationDataDirectory(String path) {
		if (StringUtils.isBlank(path)) {
			System.clearProperty(OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY);
		} else {
			System.setProperty(OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY, path);
		}
	}
	
	/**
	 * Checks if we can write to a given folder.
	 * 
	 * @param folder the directory to check.
	 * @return true if we can write to it, else false.
	 */
	private static boolean canWrite(File folder) {
		try {
			//We need to first create the folder if it does not exist, 
			//else File.canWrite() will return false even when we
			//have the necessary permissions.
			if (!folder.exists()) {
				folder.mkdirs();
			}
			
			return folder.canWrite();
		}
		catch (SecurityException ex) {
			//all we wanted to know is whether we have permissions
		}
		
		return false;
	}
	
	/**
	 * Returns the location of the OpenMRS log file.
	 * <p/>
	 * <strong>Warning:</strong> as of 2.4.4, 2.5.1, and 2.6.0 which allows configuration via a configuration file, the
	 * result of this call can return null if either the file appender uses a name other than
	 * {@link OpenmrsConstants#LOG_OPENMRS_FILE_APPENDER} or if the appender with that name is not one of the default file
	 * appending types.
	 * 
	 * @return the path to the OpenMRS log file
	 * @since 1.9.2
	 * @deprecated As of 2.4.4, 2.5.1, and 2.6.0; replaced by {@link OpenmrsLoggingUtil#getOpenmrsLogLocation()}
	 */
	@Deprecated
	public static String getOpenmrsLogLocation() {
		return OpenmrsLoggingUtil.getOpenmrsLogLocation();
	}
	
	/**
	 * Checks whether the current JVM version is at least Java 8.
	 * 
	 * @throws APIException if the current JVM version is earlier than Java 8
	 */
	public static void validateJavaVersion() {
		// check whether the current JVM version is at least Java 8
		if (System.getProperty("java.version").matches("1\\.[0-7]\\.(.*)")) {
			throw new APIException(
				"OpenMRS " + OpenmrsConstants.OPENMRS_VERSION_SHORT + " requires Java 8 and above, but is running under " + 
					System.getProperty("java.version"));
		}
	}
	
	/**
	 * Find the given folderName in the application data directory. Or, treat folderName like an
	 * absolute url to a directory
	 * 
	 * @param folderName
	 * @return folder capable of storing information
	 */
	public static File getDirectoryInApplicationDataDirectory(String folderName) throws APIException {
		// try to load the repository folder straight away.
		File folder = new File(folderName);
		
		// if the property wasn't a full path already, assume it was intended to
		// be a folder in the
		// application directory
		if (!folder.isAbsolute()) {
			folder = new File(getApplicationDataDirectoryAsFile(), folderName);
		}
		
		// now create the directory folder if it doesn't exist
		if (!folder.exists()) {
			log.warn("'" + folder.getAbsolutePath() + "' doesn't exist.  Creating directories now.");
			folder.mkdirs();
		}
		
		if (!folder.isDirectory()) {
			throw new APIException("should.be.directory", new Object[] { folder.getAbsolutePath() });
		}
		
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
			throw new ModuleException(outFile.getAbsolutePath() + " file doesn't exist.", e);
		}
		finally {
			try {
				if (outStream != null) {
					outStream.close();
				}
			}
			catch (Exception e) {
				log.warn("Unable to close outstream", e);
			}
		}
	}
	
	public static List<Integer> delimitedStringToIntegerList(String delimitedString, String delimiter) {
		List<Integer> ret = new ArrayList<>();
		String[] tokens = delimitedString.split(delimiter);
		for (String token : tokens) {
			token = token.trim();
			if (token.length() != 0) {
				ret.add(Integer.valueOf(token));
			}
		}
		return ret;
	}
	
	/**
	 * Tests if the given String starts with any of the specified prefixes
	 * 
	 * @param str the string to test
	 * @param prefixes an array of prefixes to test against
	 * @return true if the String starts with any of the specified prefixes, otherwise false.
	 */
	public static boolean stringStartsWith(String str, String[] prefixes) {
		for (String prefix : prefixes) {
			if (StringUtils.startsWith(str, prefix)) {
				return true;
			}
		}
		
		return false;
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
			if (withinLastDays != null) {
				gc.add(Calendar.DAY_OF_MONTH, -withinLastDays);
			}
			if (withinLastMonths != null) {
				gc.add(Calendar.MONTH, -withinLastMonths);
			}
			ret = gc.getTime();
		}
		if (sinceDate != null && (ret == null || sinceDate.after(ret))) {
			ret = sinceDate;
		}
		return ret;
	}
	
	public static Date toDateHelper(Date comparisonDate, Integer withinLastDays, Integer withinLastMonths,
	        Integer untilDaysAgo, Integer untilMonthsAgo, Date sinceDate, Date untilDate) {
		
		Date ret = null;
		if (untilDaysAgo != null || untilMonthsAgo != null) {
			Calendar gc = Calendar.getInstance();
			gc.setTime(comparisonDate != null ? comparisonDate : new Date());
			if (untilDaysAgo != null) {
				gc.add(Calendar.DAY_OF_MONTH, -untilDaysAgo);
			}
			if (untilMonthsAgo != null) {
				gc.add(Calendar.MONTH, -untilMonthsAgo);
			}
			ret = gc.getTime();
		}
		if (untilDate != null && (ret == null || untilDate.before(ret))) {
			ret = untilDate;
		}
		return ret;
	}
	
	/**
	 * @param collection
	 * @param elements
	 * @return Whether _collection_ contains any of _elements_
	 */
	public static <T> boolean containsAny(Collection<T> collection, Collection<T> elements) {
		for (T obj : elements) {
			if (collection.contains(obj)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Allows easy manipulation of a Map&lt;?, Set&gt;
	 */
	public static <K, V> void addToSetMap(Map<K, Set<V>> map, K key, V obj) {
		Set<V> set = map.computeIfAbsent(key, k -> new HashSet<>());
		set.add(obj);
	}
	
	public static <K, V> void addToListMap(Map<K, List<V>> map, K key, V obj) {
		List<V> list = map.computeIfAbsent(key, k -> new ArrayList<>());
		list.add(obj);
	}
	
	/**
	 * Get the current user's date format Will look similar to "mm-dd-yyyy". Depends on user's
	 * locale.
	 * 
	 * @return a simple date format
	 * <strong>Should</strong> return a pattern with four y characters in it
	 * <strong>Should</strong> not allow the returned SimpleDateFormat to be modified
	 * @since 1.5
	 */
	public static SimpleDateFormat getDateFormat(Locale locale) {
		if (dateFormatCache.containsKey(locale)) {
			return (SimpleDateFormat) dateFormatCache.get(locale).clone();
		}
		
		// note that we are using the custom OpenmrsDateFormat class here which prevents erroneous parsing of 2-digit years
		SimpleDateFormat sdf = new OpenmrsDateFormat((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, locale),
		        locale);
		String pattern = sdf.toPattern();
		
		if (!pattern.contains("yyyy")) {
			// otherwise, change the pattern to be a four digit year
			String regex = "yy";
			if (!pattern.contains("yy")) {
				//Java 11 has dd/MM/y instead of dd/MM/yy
				regex = "y";
			}
			pattern = pattern.replaceFirst(regex, "yyyy");
			sdf.applyPattern(pattern);
		}
		if (!pattern.contains("MM")) {
			// change the pattern to be a two digit month
			pattern = pattern.replaceFirst("M", "MM");
			sdf.applyPattern(pattern);
		}
		if (!pattern.contains("dd")) {
			// change the pattern to be a two digit day
			pattern = pattern.replaceFirst("d", "dd");
			sdf.applyPattern(pattern);
		}
		
		dateFormatCache.put(locale, sdf);
		
		return (SimpleDateFormat) sdf.clone();
	}
	
	/**
	 * Get the current user's time format Will look similar to "hh:mm a". Depends on user's locale.
	 * 
	 * @return a simple time format
	 * <strong>Should</strong> return a pattern with two h characters in it
	 * <strong>Should</strong> not allow the returned SimpleDateFormat to be modified
	 * @since 1.9
	 */
	public static SimpleDateFormat getTimeFormat(Locale locale) {
		if (timeFormatCache.containsKey(locale)) {
			return (SimpleDateFormat) timeFormatCache.get(locale).clone();
		}
		
		SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.SHORT, locale);
		String pattern = sdf.toPattern();
		
		if (!(pattern.contains("hh") || pattern.contains("HH"))) {
			// otherwise, change the pattern to be a two digit hour
			pattern = pattern.replaceFirst("h", "hh").replaceFirst("H", "HH");
			sdf.applyPattern(pattern);
		}
		
		timeFormatCache.put(locale, sdf);
		
		return (SimpleDateFormat) sdf.clone();
	}
	
	/**
	 * Get the current user's datetime format Will look similar to "mm-dd-yyyy hh:mm a". Depends on
	 * user's locale.
	 * 
	 * @return a simple date format
	 * <strong>Should</strong> return a pattern with four y characters and two h characters in it
	 * <strong>Should</strong> not allow the returned SimpleDateFormat to be modified
	 * @since 1.9
	 */
	public static SimpleDateFormat getDateTimeFormat(Locale locale) {
		SimpleDateFormat dateFormat;
		SimpleDateFormat timeFormat;
		
		dateFormat = getDateFormat(locale);
		timeFormat = getTimeFormat(locale);
		
		String pattern = dateFormat.toPattern() + " " + timeFormat.toPattern();
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern(pattern);
		return sdf;
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
			// If there's a valueOf(String) method, just use that (will cover at
			// least String, Integer, Double, Boolean)
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
				for (Enum e : constants) {
					if (e.toString().equals(string)) {
						return e;
					}
				}
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
				// TODO: this uses the date format from the current session,
				// which could cause problems if the user changes it after
				// searching.
				CustomDateEditor ed = new CustomDateEditor(Context.getDateFormat(), true, 10);
				ed.setAsText(string);
				return ed.getValue();
			} else if (Object.class.equals(clazz)) {
				// TODO: Decide whether this is a hack. Currently setting Object
				// arguments with a String
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
	 * Loops over the collection to check to see if the given object is in that collection. This
	 * method <i>only</i> uses the .equals() method for comparison. This should be used in the
	 * patient/person objects on their collections. Their collections are SortedSets which use the
	 * compareTo method for equality as well. The compareTo method is currently optimized for
	 * sorting, not for equality. A null <code>obj</code> will return false
	 * 
	 * @param objects collection to loop over
	 * @param obj Object to look for in the <code>objects</code>
	 * @return true/false whether the given object is found
	 * <strong>Should</strong> use equals method for comparison instead of compareTo given List collection
	 * <strong>Should</strong> use equals method for comparison instead of compareTo given SortedSet collection
	 */
	public static boolean collectionContains(Collection<?> objects, Object obj) {
		if (obj == null || objects == null) {
			return false;
		}
		
		for (Object o : objects) {
			if (o != null && o.equals(obj)) {
				return true;
			}
		}
		
		return false;
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
		Random gen = new Random();
		File outFile;
		do {
			// format to print date in filename
			DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-HHmm-ssSSS");
			
			// use current date if none provided
			if (date == null) {
				date = new Date();
			}
			
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
			filename.append(gen.nextInt() * 10000);
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
		Random gen = new Random();
		StringBuilder sb = new StringBuilder(size);
		for (int i = 0; i < size; i++) {
			int ch = gen.nextInt() * 62;
			if (ch < 10) {
				// 0-9
				sb.append(ch);
			} else if (ch < 36) {
				// a-z
				sb.append((char) (ch - 10 + 'a'));
			} else {
				sb.append((char) (ch - 36 + 'A'));
			}
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
	 * Convenience method to replace Properties.store(), which isn't UTF-8 compliant <br>
	 * NOTE: In Java 6, you will be able to pass the load() and store() methods a UTF-8
	 * Reader/Writer object as an argument, making this method unnecessary.
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
				if (outStream != null) {
					outStream.close();
				}
			}
			catch (IOException ioe) {
				// pass
			}
		}
	}
	
	/**
	 * Convenience method to replace Properties.store(), which isn't UTF-8 compliant NOTE: In Java
	 * 6, you will be able to pass the load() and store() methods a UTF-8 Reader/Writer object as an
	 * argument.
	 * 
	 * @param properties
	 * @param outStream
	 * @param comment (which appears in comments in properties file)
	 */
	public static void storeProperties(Properties properties, OutputStream outStream, String comment) {
		try {
			Charset utf8 = StandardCharsets.UTF_8;
			properties.store(new OutputStreamWriter(outStream, utf8), comment);
		}
		catch (FileNotFoundException fnfe) {
			log.error("target file not found" + fnfe);
		}
		catch (UnsupportedEncodingException ex) { // pass
			log.error("unsupported encoding error hit" + ex);
		}
		catch (IOException ioex) {
			log.error("IO exception encountered trying to append to properties file" + ioex);
		}
		
	}
	
	/**
	 * This method is a replacement for Properties.load(InputStream) so that we can load in utf-8
	 * characters. Currently the load method expects the inputStream to point to a latin1 encoded
	 * file. <br>
	 * NOTE: In Java 6, you will be able to pass the load() and store() methods a UTF-8
	 * Reader/Writer object as an argument, making this method unnecessary.
	 * 
	 * @param props the properties object to write into
	 * @param inputStream the input stream to read from
	 */
	public static void loadProperties(Properties props, InputStream inputStream) {
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
			props.load(reader);
		}
		catch (FileNotFoundException fnfe) {
			log.error("Unable to find properties file" + fnfe);
		}
		catch (UnsupportedEncodingException uee) {
			log.error("Unsupported encoding used in properties file" + uee);
		}
		catch (IOException ioe) {
			log.error("Unable to read properties from properties file" + ioe);
		}
		finally {
			try {
				if (reader != null) {
					reader.close();
				}
			}
			catch (IOException ioe) {
				log.error("Unable to close properties file " + ioe);
			}
		}
	}
	
	/**
	 * Convenience method used to load properties from the given file.
	 * 
	 * @param props the properties object to be loaded into
	 * @param propertyFile the properties file to read
	 */
	public static void loadProperties(Properties props, File propertyFile) {
		try {
			loadProperties(props, new FileInputStream(propertyFile));
		}
		catch (FileNotFoundException fnfe) {
			log.error("Unable to find properties file" + fnfe);
		}
	}
	
	/**
	 * Utility method for getting the translation for the passed code
	 * 
	 * @param code the message key to lookup
	 * @param args the replacement values for the translation string
	 * @return the message, or if not found, the code
	 */
	public static String getMessage(String code, Object... args) {
		Locale l = Context.getLocale();
		try {
			String translation = Context.getMessageSourceService().getMessage(code, args, l);
			if (translation != null) {
				return translation;
			}
		}
		catch (NoSuchMessageException e) {
			log.warn("Message code <" + code + "> not found for locale " + l);
		}
		catch (APIException apiEx) {
			// in case the services aren't set up yet
			log.debug("Unable to get code: " + code, apiEx);
			return code;
		}
		return code;
	}
	
	/**
	 * Utility to check the validity of a password for a certain {@link User}. Passwords must be
	 * non-null. Their required strength is configured via global properties:
	 * <table summary="Configuration props">
	 * <tr>
	 * <th>Description</th>
	 * <th>Property</th>
	 * <th>Default Value</th>
	 * </tr>
	 * <tr>
	 * <th>Require that it not match the {@link User}'s username or system id
	 * <th>{@link OpenmrsConstants#GP_PASSWORD_CANNOT_MATCH_USERNAME_OR_SYSTEMID}</th>
	 * <th>true</th>
	 * </tr>
	 * <tr>
	 * <th>Require a minimum length
	 * <th>{@link OpenmrsConstants#GP_PASSWORD_MINIMUM_LENGTH}</th>
	 * <th>8</th>
	 * </tr>
	 * <tr>
	 * <th>Require both an upper and lower case character
	 * <th>{@link OpenmrsConstants#GP_PASSWORD_REQUIRES_UPPER_AND_LOWER_CASE}</th>
	 * <th>true</th>
	 * </tr>
	 * <tr>
	 * <th>Require at least one numeric character
	 * <th>{@link OpenmrsConstants#GP_PASSWORD_REQUIRES_DIGIT}</th>
	 * <th>true</th>
	 * </tr>
	 * <tr>
	 * <th>Require at least one non-numeric character
	 * <th>{@link OpenmrsConstants#GP_PASSWORD_REQUIRES_NON_DIGIT}</th>
	 * <th>true</th>
	 * </tr>
	 * <tr>
	 * <th>Require a match on the specified regular expression
	 * <th>{@link OpenmrsConstants#GP_PASSWORD_CUSTOM_REGEX}</th>
	 * <th>null</th>
	 * </tr>
	 * </table>
	 * 
	 * @param username user name of the user with password to validated
	 * @param password string that will be validated
	 * @param systemId system id of the user with password to be validated
	 * @throws PasswordException
	 * @since 1.5
	 * <strong>Should</strong> fail with short password by default
	 * <strong>Should</strong> fail with short password if not allowed
	 * <strong>Should</strong> pass with short password if allowed
	 * <strong>Should</strong> fail with digit only password by default
	 * <strong>Should</strong> fail with digit only password if not allowed
	 * <strong>Should</strong> pass with digit only password if allowed
	 * <strong>Should</strong> fail with char only password by default
	 * <strong>Should</strong> fail with char only password if not allowed
	 * <strong>Should</strong> pass with char only password if allowed
	 * <strong>Should</strong> fail without both upper and lower case password by default
	 * <strong>Should</strong> fail without both upper and lower case password if not allowed
	 * <strong>Should</strong> pass without both upper and lower case password if allowed
	 * <strong>Should</strong> fail with password equals to user name by default
	 * <strong>Should</strong> fail with password equals to user name if not allowed
	 * <strong>Should</strong> pass with password equals to user name if allowed
	 * <strong>Should</strong> fail with password equals to system id by default
	 * <strong>Should</strong> fail with password equals to system id if not allowed
	 * <strong>Should</strong> pass with password equals to system id if allowed
	 * <strong>Should</strong> fail with password not matching configured regex
	 * <strong>Should</strong> pass with password matching configured regex
	 * <strong>Should</strong> allow password to contain non alphanumeric characters
	 * <strong>Should</strong> allow password to contain white spaces
	 * <strong>Should</strong> still work without an open session
	 */
	public static void validatePassword(String username, String password, String systemId) throws PasswordException {
		
		// default values for all of the global properties
		String userGp = "true";
		String lengthGp = "8";
		String caseGp = "true";
		String digitGp = "true";
		String nonDigitGp = "true";
		String regexGp = null;
		AdministrationService svc = null;
		
		try {
			svc = Context.getAdministrationService();
		}
		catch (APIException apiEx) {
			// if a service isn't available, fail quietly and just do the
			// defaults
			log.debug("Unable to get global properties", apiEx);
		}
		
		if (svc != null && Context.isSessionOpen()) {
			// (the session won't be open here to allow for the unit test to
			// fake not having the admin service available)
			userGp = svc.getGlobalProperty(OpenmrsConstants.GP_PASSWORD_CANNOT_MATCH_USERNAME_OR_SYSTEMID, userGp);
			lengthGp = svc.getGlobalProperty(OpenmrsConstants.GP_PASSWORD_MINIMUM_LENGTH, lengthGp);
			caseGp = svc.getGlobalProperty(OpenmrsConstants.GP_PASSWORD_REQUIRES_UPPER_AND_LOWER_CASE, caseGp);
			digitGp = svc.getGlobalProperty(OpenmrsConstants.GP_PASSWORD_REQUIRES_DIGIT, digitGp);
			nonDigitGp = svc.getGlobalProperty(OpenmrsConstants.GP_PASSWORD_REQUIRES_NON_DIGIT, nonDigitGp);
			regexGp = svc.getGlobalProperty(OpenmrsConstants.GP_PASSWORD_CUSTOM_REGEX, regexGp);
		}
		
		if (password == null) {
			throw new WeakPasswordException();
		}
		
		if ("true".equals(userGp) && (password.equals(username) || password.equals(systemId))) {
			throw new WeakPasswordException();
		}
		
		if (StringUtils.isNotEmpty(lengthGp)) {
			try {
				int minLength = Integer.parseInt(lengthGp);
				if (password.length() < minLength) {
					throw new ShortPasswordException(getMessage("error.password.length", lengthGp));
				}
			}
			catch (NumberFormatException nfe) {
				log.warn(
				    "Error in global property <" + OpenmrsConstants.GP_PASSWORD_MINIMUM_LENGTH + "> must be an Integer");
			}
		}
		
		if ("true".equals(caseGp) && !containsUpperAndLowerCase(password)) {
			throw new InvalidCharactersPasswordException(getMessage("error.password.requireMixedCase"));
		}
		
		if ("true".equals(digitGp) && !containsDigit(password)) {
			throw new InvalidCharactersPasswordException(getMessage("error.password.requireNumber"));
		}
		
		if ("true".equals(nonDigitGp) && containsOnlyDigits(password)) {
			throw new InvalidCharactersPasswordException(getMessage("error.password.requireLetter"));
		}
		
		if (StringUtils.isNotEmpty(regexGp)) {
			try {
				Pattern pattern = Pattern.compile(regexGp);
				Matcher matcher = pattern.matcher(password);
				if (!matcher.matches()) {
					throw new InvalidCharactersPasswordException(getMessage("error.password.different"));
				}
			}
			catch (PatternSyntaxException pse) {
				log.warn("Invalid regex of " + regexGp + " defined in global property <"
				        + OpenmrsConstants.GP_PASSWORD_CUSTOM_REGEX + ">.");
			}
		}
	}
	
	/**
	 * @param test the string to test
	 * @return true if the passed string contains both upper and lower case characters
	 * <strong>Should</strong> return true if string contains upper and lower case
	 * <strong>Should</strong> return false if string does not contain lower case characters
	 * <strong>Should</strong> return false if string does not contain upper case characters
	 */
	public static boolean containsUpperAndLowerCase(String test) {
		if (test != null) {
			Pattern pattern = Pattern.compile("^(?=.*?[A-Z])(?=.*?[a-z])[\\w|\\W]*$");
			Matcher matcher = pattern.matcher(test);
			return matcher.matches();
		}
		return false;
	}
	
	/**
	 * @param test the string to test
	 * @return true if the passed string contains only numeric characters
	 * <strong>Should</strong> return true if string contains only digits
	 * <strong>Should</strong> return false if string contains any non-digits
	 */
	public static boolean containsOnlyDigits(String test) {
		if (test != null) {
			for (char c : test.toCharArray()) {
				if (!Character.isDigit(c)) {
					return false;
				}
			}
		}
		return StringUtils.isNotEmpty(test);
	}
	
	/**
	 * @param test the string to test
	 * @return true if the passed string contains any numeric characters
	 * <strong>Should</strong> return true if string contains any digits
	 * <strong>Should</strong> return false if string contains no digits
	 */
	public static boolean containsDigit(String test) {
		if (test != null) {
			for (char c : test.toCharArray()) {
				if (Character.isDigit(c)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * A null-safe and exception safe way to close an inputstream or an outputstream
	 * 
	 * @param closableStream an InputStream or OutputStream to close
	 */
	public static void closeStream(Closeable closableStream) {
		if (closableStream != null) {
			try {
				closableStream.close();
			}
			catch (IOException io) {
				log.trace("Error occurred while closing stream", io);
			}
		}
	}
	
	/**
	 * Convert a stack trace into a shortened version for easier viewing and data storage, excluding
	 * those lines we are least concerned with; should average about 60% reduction in stack trace
	 * length
	 * 
	 * @param stackTrace original stack trace from an error
	 * @return shortened stack trace
	 * <strong>Should</strong> return null if stackTrace is null
	 * <strong>Should</strong> remove springframework and reflection related lines
	 * @since 1.7
	 */
	public static String shortenedStackTrace(String stackTrace) {
		if (stackTrace == null) {
			return null;
		}
		
		List<String> results = new ArrayList<>();
		final Pattern exclude = Pattern.compile("(org.springframework.|java.lang.reflect.Method.invoke|sun.reflect.)");
		boolean found = false;
		
		for (String line : stackTrace.split("\n")) {
			Matcher m = exclude.matcher(line);
			if (m.find()) {
				found = true;
			} else {
				if (found) {
					found = false;
					results.add("\tat [ignored] ...");
				}
				results.add(line);
			}
		}
		
		return StringUtils.join(results, "\n");
	}
	
	/**
	 * <pre>
	 * Finds and loads the runtime properties file for a specific OpenMRS application.
	 * Searches for the file in this order:
	 * 1) {current directory}/{applicationname}_runtime.properties
	 * 2) an environment variable called "{APPLICATIONNAME}_RUNTIME_PROPERTIES_FILE"
	 * 3) {openmrs_app_dir}/{applicationName}_runtime.properties   // openmrs_app_dir is typically {user_home}/.OpenMRS
	 * </pre>
	 * 
	 * @see #getApplicationDataDirectory()
	 * @param applicationName (defaults to "openmrs") the name of the running OpenMRS application,
	 *            e.g. if you have deployed OpenMRS as a web application you would give the deployed
	 *            context path here
	 * @return runtime properties, or null if none can be found
	 * @since 1.8
	 */
	public static Properties getRuntimeProperties(String applicationName) {
		if (applicationName == null) {
			applicationName = "openmrs";
		}
		String pathName;
		pathName = getRuntimePropertiesFilePathName(applicationName);
		FileInputStream propertyStream = null;
		try {
			if (pathName != null) {
				propertyStream = new FileInputStream(pathName);
			}
		}
		catch (FileNotFoundException e) {
			log.warn("Unable to find a runtime properties file at " + new File(pathName).getAbsolutePath());
		}
		
		try {
			if (propertyStream == null) {
				throw new IOException("Could not find a runtime properties file named " + pathName
				        + " in the OpenMRS application data directory, or the current directory");
			}
			
			Properties props = new Properties();
			OpenmrsUtil.loadProperties(props, propertyStream);
			propertyStream.close();
			log.info("Using runtime properties file: " + pathName);
			return props;
		}
		catch (Exception ex) {
			log.info("Got an error while attempting to load the runtime properties", ex);
			log.warn(
			    "Unable to find a runtime properties file. Initial setup is needed. View the webapp to run the setup wizard.");
			return null;
		}
	}
	
	/**
	 * Checks whether the system is running in test mode
	 * 
	 * @return boolean
	 */
	
	public static boolean isTestMode() {
		return "true".equalsIgnoreCase(System.getProperty("FUNCTIONAL_TEST_MODE"));
	}
	
	/**
	 * Gets the full path and name of the runtime properties file.
	 * 
	 * @param applicationName (defaults to "openmrs") the name of the running OpenMRS application,
	 *            e.g. if you have deployed OpenMRS as a web application you would give the deployed
	 *            context path here
	 * @return runtime properties file path and name, or null if none can be found
	 * @since 1.9
	 */
	public static String getRuntimePropertiesFilePathName(String applicationName) {
		if (applicationName == null) {
			applicationName = "openmrs";
		}
		
		String defaultFileName = applicationName + "-runtime.properties";
		String fileNameInTestMode = getRuntimePropertiesFileNameInTestMode();
		
		// first look in the current directory (that java was started from)
		String pathName = fileNameInTestMode != null ? fileNameInTestMode : defaultFileName;
		log.debug("Attempting to look for properties file in current directory: " + pathName);
		if (new File(pathName).exists()) {
			return pathName;
		} else {
			log.warn("Unable to find a runtime properties file at " + new File(pathName).getAbsolutePath());
		}
		
		// next look from environment variable
		String envVarName = applicationName.toUpperCase() + "_RUNTIME_PROPERTIES_FILE";
		String envFileName = System.getenv(envVarName);
		if (envFileName != null) {
			log.debug("Atempting to look for runtime properties from: " + pathName);
			if (new File(envFileName).exists()) {
				return envFileName;
			} else {
				log.warn("Unable to find properties file with path: " + pathName + ". (derived from environment variable "
				        + envVarName + ")");
			}
		} else {
			log.info("Couldn't find an environment variable named " + envVarName);
			if (log.isDebugEnabled()) {
				log.debug("Available environment variables are named: " + System.getenv().keySet());
			}
		}
		
		// next look in the OpenMRS application data directory
		File file = new File(getApplicationDataDirectory(), pathName);
		pathName = file.getAbsolutePath();
		log.debug("Attempting to look for property file from: " + pathName);
		if (file.exists()) {
			return pathName;
		} else {
			log.warn("Unable to find properties file: " + pathName);
		}
		
		return null;
	}
	
	public static String getRuntimePropertiesFileNameInTestMode() {
		String filename = null;
		if (isTestMode()) {
			log.info("In functional testing mode. Ignoring the existing runtime properties file");
			filename = getOpenMRSVersionInTestMode() + "-test-runtime.properties";
		}
		return filename;
	}
	
	/**
	 * Gets OpenMRS version name under test mode.
	 * 
	 * @return String openmrs version number
	 */
	public static String getOpenMRSVersionInTestMode() {
		return System.getProperty("OPENMRS_VERSION", "openmrs");
	}
	
	/**
	 * Performs a case insensitive Comparison of two strings taking care of null values
	 * 
	 * @param s1 the string to compare
	 * @param s2 the string to compare
	 * @return true if strings are equal (ignoring case)
	 * <strong>Should</strong> return false if only one of the strings is null
	 * <strong>Should</strong> be case insensitive
	 * @since 1.8
	 */
	public static boolean nullSafeEqualsIgnoreCase(String s1, String s2) {
		if (s1 == null) {
			return s2 == null;
		} else if (s2 == null) {
			return false;
		}
		
		return s1.equalsIgnoreCase(s2);
	}
	
	/**
	 * This method converts the given Long value to an Integer. If the Long value will not fit in an
	 * Integer an exception is thrown
	 * 
	 * @param longValue the value to convert
	 * @return the long value in integer form.
	 * @throws IllegalArgumentException if the long value does not fit into an integer
	 */
	public static Integer convertToInteger(Long longValue) {
		if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(longValue + " cannot be cast to Integer without changing its value.");
		}
		return longValue.intValue();
	}
	
	/**
	 * Checks if the passed in date's day of the year is the one that comes immediately before that
	 * of the current date
	 * 
	 * @param date the date to check
	 * @since 1.9
	 * @return true if the date comes immediately before the current date otherwise false
	 */
	public static boolean isYesterday(Date date) {
		if (date == null) {
			return false;
		}
		
		Calendar c1 = Calendar.getInstance();
		c1.add(Calendar.DAY_OF_YEAR, -1); // yesterday
		
		Calendar c2 = Calendar.getInstance();
		c2.setTime(date);
		
		return (c1.get(Calendar.ERA) == c2.get(Calendar.ERA) && c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
		        && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR));
	}
	
	/**
	 * Get declared field names of a class
	 * 
	 * @param clazz
	 * @return
	 */
	public static Set<String> getDeclaredFields(Class<?> clazz) {
		return Arrays.stream(clazz.getDeclaredFields()).map(Field::getName).collect(Collectors.toSet());
	}

	/**
	 * This method checks if a given value is a valid numeric value for the person/patient in subject 
	 * given the concept. It checks if a given value is within the valid reference range.
	 *
	 * @param value The value to check
	 * @param obs The observation to be verified
	 * @return Error message containing expected range if there was a range mismatch, else returns empty string.
	 * 
	 * @since 2.7.0
	 */
	public static String isValidNumericValue(Float value, Obs obs) {
		ConceptReferenceRange conceptReferenceRange = Context.getConceptService().getConceptReferenceRange(obs.getPerson(), obs.getConcept());
		if (conceptReferenceRange == null) {
			return "";
		}

		if ((conceptReferenceRange.getHiAbsolute() != null && conceptReferenceRange.getHiAbsolute() < value) ||
			(conceptReferenceRange.getLowAbsolute() != null && conceptReferenceRange.getLowAbsolute() > value)) {
			return String.format("Expected value between %s and %s", conceptReferenceRange.getLowAbsolute(), conceptReferenceRange.getHiAbsolute());
		} else {
			return "";
		}
	}
	
}
