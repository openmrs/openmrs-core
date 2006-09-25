package org.openmrs.web;

public class WebUtil {
	
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
	
}
