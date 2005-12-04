package org.openmrs.reporting;

import java.util.List;

/**
 * Formats a data item for display in some format
 * @author djazayeri
 */
public interface OutputFormatter {
	
	/**
	 * What form is the output of the format() method?
	 */
	public String getMimeType();

	/**
	 * Generally speaking, can this formatter format an input of a specific class?
	 * @param inputClass	the Class the user would like to format
	 * @return	true if this Formatter is capable of formatting inputClass
	 */
	public boolean canFormat(Class inputClass);
	
	/**
	 * Formats an input into some human-readable output format (e.g. HTML, CSV, PDF, plaintext)
	 */
	public Object format(Object input);
	
}
