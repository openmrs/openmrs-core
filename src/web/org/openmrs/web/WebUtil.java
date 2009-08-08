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
	 * Strips out the path from a string if "C:\documents\file.doc", will return "file.doc" if
	 * "file.doc", will return "file.doc" if "/home/file.doc" will return "file.doc"
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
