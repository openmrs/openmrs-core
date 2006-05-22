package org.openmrs.web;

public class WebUtil {
	
	public static String escapeHTML(String s) {
		
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		
		return s;
	}


}
