package org.openmrs.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WebUtil {
	
	private static Log log = LogFactory.getLog(WebUtil.class);
	
	public static String escapeHTML(String s) {
		
		if (s == null)
			return "";
		
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		
		return s;
	}

	public static String escapeQuotes(String s) {
		
		if (s == null)
			return "";
		
		s = s.replace("\"", "\\\"");
		
		return s;
	}
	
	public static String escapeNewlines(String s) {
		if (s == null)
			return "";
		
		s = s.replace("\n", "\\n");
		
		return s;
	}

	public static String escapeQuotesAndNewlines(String s) {
		if (s == null)
			return "";
		
		s = s.replace("\"", "\\\"");
		s = s.replace("\n", "\\n");
		
		return s;
	}
	
	/**
	 * Strips out the path from a string
	 * if "C:\documents\file.doc", will return "file.doc"
	 * if "file.doc", will return "file.doc"
	 * if "/home/file.doc" will return "file.doc"
	 * 
	 * @param filename
	 * @return filename stripped down
	 */
	public static String stripFilename(String filename) {
		if (log.isDebugEnabled())
			log.debug("Stripping filename from: " + filename);
		
		// for unix based filesystems
		int index = filename.lastIndexOf("/");
		if (index != -1)
			filename = filename.substring(index + 1);
		
		// for windows based filesystems
		index = filename.lastIndexOf("\\");
		if (index != -1)
			filename = filename.substring(index + 1);
		
		if (log.isDebugEnabled())
			log.debug("Returning stripped down filename: " + filename);
		
		return filename;
	}
	
}
