package org.openmrs.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientIdentifierException;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

public class OpenmrsUtil {

	private static Log log = LogFactory.getLog(OpenmrsUtil.class);

	public static int getCheckDigit(String idWithoutCheckdigit)
			throws Exception {

		// allowable characters within identifier
		String validChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVYWXZ_";

		// remove leading or trailing whitespace, convert to uppercase
		idWithoutCheckdigit = idWithoutCheckdigit.trim().toUpperCase();

		// this will be a running total
		int sum = 0;

		// loop through digits from right to left
		for (int i = 0; i < idWithoutCheckdigit.length(); i++) {

			// set ch to "current" character to be processed
			char ch = idWithoutCheckdigit.charAt(idWithoutCheckdigit.length()
					- i - 1);

			// throw exception for invalid characters
			if (validChars.indexOf(ch) == -1)
				throw new Exception("\"" + ch + "\" is an invalid character");

			// our "digit" is calculated using ASCII value - 48
			int digit = (int) ch - 48;

			// weight will be the current digit's contribution to
			// the running total
			int weight;
			if (i % 2 == 0) {

				// for alternating digits starting with the rightmost, we
				// use our formula this is the same as multiplying x 2 and
				// adding digits together for values 0 to 9. Using the
				// following formula allows us to gracefully calculate a
				// weight for non-numeric "digits" as well (from their
				// ASCII value - 48).
				weight = (2 * digit) - (int) (digit / 5) * 9;

			} else {

				// even-positioned digits just contribute their ascii
				// value minus 48
				weight = digit;

			}

			// keep a running total of weights
			sum += weight;

		}

		// avoid sum less than 10 (if characters below "0" allowed,
		// this could happen)
		sum = Math.abs(sum) + 10;

		// check digit is amount needed to reach next number
		// divisible by ten
		return (10 - (sum % 10)) % 10;

	}

	/**
	 * 
	 * @param id
	 * @return true/false whether id has a valid check digit
	 * @throws Exception
	 *			 on invalid characters and invalid id formation
	 */
	public static boolean isValidCheckDigit(String id) throws Exception {

		// Let regular expression take care of this now
		/*
		if (!id.matches("^[A-Za-z0-9_]+-[0-9A-J]$")) {
			throw new Exception("Invalid characters and/or id formation");
		}
		*/
		
		if ( id.indexOf("-") < 0 ) {
			throw new PatientIdentifierException("Cannot find check-digit in identifier");
		}

		String idWithoutCheckDigit = id.substring(0, id.indexOf("-"));

		int computedCheckDigit = getCheckDigit(idWithoutCheckDigit);

		String checkDigit = id.substring(id.indexOf("-") + 1, id.length());
		
		if ( checkDigit.equalsIgnoreCase("A") ) checkDigit = "0";
		if ( checkDigit.equalsIgnoreCase("B") ) checkDigit = "1";
		if ( checkDigit.equalsIgnoreCase("C") ) checkDigit = "2";
		if ( checkDigit.equalsIgnoreCase("D") ) checkDigit = "3";
		if ( checkDigit.equalsIgnoreCase("E") ) checkDigit = "4";
		if ( checkDigit.equalsIgnoreCase("F") ) checkDigit = "5";
		if ( checkDigit.equalsIgnoreCase("G") ) checkDigit = "6";
		if ( checkDigit.equalsIgnoreCase("H") ) checkDigit = "7";
		if ( checkDigit.equalsIgnoreCase("I") ) checkDigit = "8";
		if ( checkDigit.equalsIgnoreCase("J") ) checkDigit = "9";
		
		int givenCheckDigit = Integer.valueOf(checkDigit);

		return (computedCheckDigit == givenCheckDigit);
	}

	/**
	 * Compares origList to newList returning map of differences
	 * 
	 * @param origList
	 * @param newList
	 * @return [List toAdd, List toDelete] with respect to origList
	 */
	public static <E extends Object> Collection<Collection<E>> compareLists(Collection<E> origList,
			Collection<E> newList) {
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

	public static Boolean isInNormalNumericRange(Float value,
			ConceptNumeric concept) {
		if (concept.getHiNormal() == null || concept.getLowNormal() == null)
			return false;
		return (value <= concept.getHiNormal() && value >= concept
				.getLowNormal());
	}

	public static Boolean isInCriticalNumericRange(Float value,
			ConceptNumeric concept) {
		if (concept.getHiCritical() == null || concept.getLowCritical() == null)
			return false;
		return (value <= concept.getHiCritical() && value >= concept
				.getLowCritical());
	}

	public static Boolean isInAbsoluteNumericRange(Float value,
			ConceptNumeric concept) {
		if (concept.getHiAbsolute() == null || concept.getLowAbsolute() == null)
			return false;
		return (value <= concept.getHiAbsolute() && value >= concept
				.getLowAbsolute());
	}

	public static Boolean isValidNumericValue(Float value,
			ConceptNumeric concept) {
		if (concept.getHiAbsolute() == null || concept.getLowAbsolute() == null)
			return true;
		return (value <= concept.getHiAbsolute() && value >= concept
				.getLowAbsolute());
	}

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
	 * Copy file from inputStream onto the outputStream
	 * 
	 * @param inputStream
	 * @param outputStream
	 * @throws IOException
	 */
	public static void copyFile(InputStream inputStream,
			OutputStream outputStream) throws IOException {
		if (inputStream == null || outputStream == null)
			return;
		
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
			if (in != null)  in.close();
			if (out != null) out.close();
		}
		
		outputStream.close();
	}
	
	/**
	 * Look for a file named <code>filename</code> in folder
	 * 
	 * @param folder
	 * @param filename
	 * @return true/false whether filename exists in folder
	 */
	public static boolean folderContains(File folder, String filename) {
		if (folder == null) return false;
		if (!folder.isDirectory()) return false;
		
		for (File f : folder.listFiles()) {
			if (f.getName().equals(filename))
				return true;
		}
		return false;
	}
	
	
	/**
	 * Initialize global settings
	 * Find and load modules
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
					log.fatal("Database name cannot be configured from 'connection.url' ." +
							"Either supply 'connection.database_name' or correct the url", e);
				}
			}
		}
		
		val = p.getProperty("connection.database_business_name", null);
		if (val == null)
			val = OpenmrsConstants.DATABASE_NAME;
		OpenmrsConstants.DATABASE_BUSINESS_NAME = val;
		
	}
	
	
	/**
	 * Takes a String like "size=compact|order=date" and returns a Map<String,String> from the keys to the values.
	 * @param paramList
	 * @return
	 */
	public static Map<String, String> parseParameterList(String paramList) {
		Map<String, String> ret = new HashMap<String, String>();
		if (paramList != null && paramList.length() > 0) {
			String[] args = paramList.split("\\|");
			for (String s : args) {
				int ind = s.indexOf('=');
				if (ind <= 0) {
					throw new IllegalArgumentException("Misformed argument in dynamic page specification string: '" + s + "' is not 'key=value'.");
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
	 * Compares two java.util.Date objects, but handles java.sql.Timestamp (which is not directly comparable to a date)
	 * by dropping its nanosecond value.
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
		if (d1 == null)
			return 1;
		else if (d2 == null)
			return -1;
		else
			return compare(d1, d2);
	}
	
	public static <E extends Comparable<E>> int compareWithNullAsLowest(E c1, E c2) {
		if (c1 == null)
			return -1;
		else if (c2 == null)
			return 1;
		else
			return c1.compareTo(c2);
	}
	
	public static <E extends Comparable<E>> int compareWithNullAsGreatest(E c1, E c2) {
		if (c1 == null)
			return 1;
		else if (c2 == null)
			return -1;
		else
			return c1.compareTo(c2);
	}

	public static Integer ageFromBirthdate(Date birthdate) {
		if (birthdate == null)
			return null;
		
		Calendar today = Calendar.getInstance();
		
		Calendar bday = new GregorianCalendar();
		bday.setTime(birthdate);
		
		int age = today.get(Calendar.YEAR) - bday.get(Calendar.YEAR);
		
		//tricky bit:
		// set birthday calendar to this year
		// if the current date is less that the new 'birthday', subtract a year
		bday.set(Calendar.YEAR, today.get(Calendar.YEAR));
		if (today.before(bday)) {
				age = age -1;
		}

		return age;
	}
	
	/**
	 * Converts a collection to a String with a specified separator between all elements
	 * @param c Collection to be joined
	 * @param separator string to put between all elements
	 * @return a String representing the toString() of all elements in c, separated by separator
	 */
	public static <E extends Object> String join(Collection<E> c, String separator) {
		if (c == null) return "";
		
		StringBuilder ret = new StringBuilder();
		for (Iterator i = c.iterator(); i.hasNext(); ) {
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
		
		for (StringTokenizer st = new StringTokenizer(descriptor, "|"); st.hasMoreTokens(); ) {
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
				} catch (Exception ex) { }
			}
			if (c != null) {
				if (isSet) {
					List<Concept> inSet = cs.getConceptsInSet(c);
					ret.addAll(inSet);
				} else {
					ret.add(c);
				}
			}
		}
		return ret;
	}

	public static List<Concept> delimitedStringToConceptList( String delimitedString, String delimiter, Context context ) {
		List<Concept> ret = null;
		
		if ( delimitedString != null && context != null ) {
			String[] tokens = delimitedString.split(delimiter);
			for ( String token : tokens ) {
				Integer conceptId = null;
				
				try {
					conceptId = new Integer(token);
				} catch (NumberFormatException nfe) {
					conceptId = null;
				}
				
				Concept c = null;
				
				if ( conceptId != null ) {
					c = Context.getConceptService().getConcept(conceptId);
				} else {
					c = Context.getConceptService().getConceptByName(token);
				}
				
				if ( c != null ) {
					if ( ret == null ) ret = new ArrayList<Concept>();
					ret.add(c);
				}
			}
		}
		
		return ret;
	}

	public static Map<String, Concept> delimitedStringToConceptMap( String delimitedString, String delimiter) {
		Map<String,Concept> ret = null;
		
		if ( delimitedString != null) {
			String[] tokens = delimitedString.split(delimiter);
			for ( String token : tokens ) {
				Concept c = OpenmrsUtil.getConceptByIdOrName(token);
				
				if ( c != null ) {
					if ( ret == null ) ret = new HashMap<String, Concept>();
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
		} catch (NumberFormatException nfe) {
			conceptId = null;
		}
		
		if ( conceptId != null ) {
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
		
		for (StringTokenizer st = new StringTokenizer(descriptor, "|"); st.hasMoreTokens(); ) {
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
				} catch (Exception ex) { }
			}
			if (c != null) {
				if (isSet) {
					List<Concept> inSet = cs.getConceptsInSet(c);
					ret.addAll(inSet);
				} else {
					ret.add(c);
				}
			}
		}
		return ret;
	}
	
	public static Date lastSecondOfDay(Date date) {
		if (date == null)
			return null;
		Calendar c = new GregorianCalendar();
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

	public static boolean deleteDirectory(File dir) throws IOException {
		if (!dir.exists() || !dir.isDirectory())
			throw new IOException("Could not delete directory '" + dir.getAbsolutePath()
				+ "' (not a directory)");
		log.debug("Deleting directory " + dir.getAbsolutePath());
		File[] fileList = dir.listFiles();
		for (File f : fileList) {
			if (f.isDirectory())
				deleteDirectory(f);
			boolean success = f.delete();
			log.debug("   deleting " + f.getName() + " : " + (success ? "ok" : "failed"));
		}
		boolean success = dir.delete();
		if (success)
			log.debug("   ...and directory itself");
		else
			log.warn("   ...could not remove directory: " + dir.getAbsolutePath());
		return success;
	}
	
	/**
	 * Utility method to convert local URL to a File object.
	 * @param url an URL
	 * @return file object for given URL or <code>null</code> if URL is not
	 *		 local
	 */
	public static File url2file(final URL url) {
		if (!"file".equalsIgnoreCase(url.getProtocol())) {
			return null;
		}	
		return new File(url.getFile().replaceAll("%20", " "));
	}
	
	/**
     * Opens input stream for given resource. This method behaves differently
     * for different URL types:
     * <ul>
     *   <li>for <b>local files</b> it returns buffered file input stream;</li>
     *   <li>for <b>local JAR files</b> it reads resource content into memory
     *     buffer and returns byte array input stream that wraps those
     *     buffer (this prevents locking JAR file);</li>
     *   <li>for <b>common URL's</b> this method simply opens stream to that URL
     *     using standard URL API.</li>
     * </ul>
     * It is not recommended to use this method for big resources within JAR
     * files.
     * @param url resource URL
     * @return input stream for given resource
     * @throws IOException if any I/O error has occurred
     */
    public static InputStream getResourceInputStream(final URL url)
            throws IOException {
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
            } finally {
                in.close();
            }
        } finally {
            jarFile.close();
        }
    }
    
    /**
     * @return The path to the directory on the file system that will hold miscellaneous
     * 			data about the application (runtime properties, modules, etc)
     */
    public static String getApplicationDataDirectory() {
    	String filepath;
    	
    	if (OpenmrsConstants.OPERATING_SYSTEM_LINUX.equalsIgnoreCase(OpenmrsConstants.OPERATING_SYSTEM))
			filepath = System.getProperty("user.home") + File.separator + ".OpenMRS";
		else
			filepath = System.getProperty("user.home") + File.separator + 
					"Application Data" + File.separator + 
					"OpenMRS";
				
		filepath = filepath + File.separator;
		
		File folder = new File(filepath);
		if (!folder.exists())
			folder.mkdirs();
		
		return filepath;
    }
    
    /**
     * Save the given xml document to the given outfile
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

    	if ( concept != null && list != null ) {
    		for ( Concept c : list ) {
    			if ( c.equals(concept) ) {
    				ret = true;
    				break;
    			}
    		}
    	}
    	
    	return ret;
	}
    
    public static Date fromDateHelper(
    		Date comparisonDate,
    		Integer withinLastDays, Integer withinLastMonths,
    		Integer untilDaysAgo, Integer untilMonthsAgo,
    		Date sinceDate, Date untilDate) {

    	Date ret = null;
		if (withinLastDays != null || withinLastMonths != null) {
			Calendar gc = new GregorianCalendar();
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
    
    public static Date toDateHelper(
    		Date comparisonDate,
    		Integer withinLastDays, Integer withinLastMonths,
    		Integer untilDaysAgo, Integer untilMonthsAgo,
    		Date sinceDate, Date untilDate) {

    	Date ret = null;
		if (untilDaysAgo != null || untilMonthsAgo != null) {
			Calendar gc = new GregorianCalendar();
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

}
