package org.openmrs.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptNumeric;

public class Helper {

	private static Log log = LogFactory.getLog(Helper.class);

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
	 *             on invalid characters and invalid id formation
	 */
	public static boolean isValidCheckDigit(String id) throws Exception {

		if (!id.matches("^[A-Za-z0-9_]+-[0-9]$")) {
			throw new Exception("Invalid characters and/or id formation");
		}

		String idWithoutCheckDigit = id.substring(0, id.indexOf("-"));

		int computedCheckDigit = getCheckDigit(idWithoutCheckDigit);

		int givenCheckDigit = Integer.valueOf(id.substring(id.indexOf("-") + 1,
				id.length()));

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
		byte[] c = new byte[1];
		while (inputStream.read(c) != -1)
			outputStream.write(c);
		outputStream.close();
	}
	
	/**
	 * Initialize global settings
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
		if (val == null)
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
				String[] thisArg = s.split("=");
				if (thisArg.length == 2) {
					ret.put(thisArg[0], thisArg[1]);
				} else {
					throw new IllegalArgumentException("Misformed argument in dynamic page specification string: '" + s + "' is not 'key=value'.");
				}
			}
		}
		return ret;
	}
}
