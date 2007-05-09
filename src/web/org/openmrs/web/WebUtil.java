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
	 * Potentially strips out the path from a string
	 * if "C:\documents\file.doc", will return "file.doc"
	 * if "file.doc", will return "file.doc"
	 * if "/home/file.doc" will return "file.doc"
	 * 
	 * @param filename
	 * @return filename stripped down
	 */
	public static String stripFilename(String filename) {
		if (filename.indexOf("/") != -1) {
			filename = filename.substring(filename.lastIndexOf("/"));
		}
	
		if (filename.indexOf("\\") != -1) {
			filename = filename.substring(filename.lastIndexOf("\\"));
		}
		
		return filename;
	}
	
}
