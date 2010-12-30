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
package org.openmrs.tools.doclet;

import com.sun.tools.doclets.Taglet;
import com.sun.javadoc.*;
import java.util.Map;

/**
 * Taglet for @should annotations.
 */
public class ShouldTaglet implements Taglet {
	
	private static final String NAME = "should";
	
	private static final String HEADER = "Expected Behavior:";
	
	/**
	 * Return the name of this custom tag.
	 */
	public String getName() {
		return NAME;
	}
	
	/**
	 * Will return false since <code> @should</code> cannot be used in field documentation.
	 * 
	 * @return false since <code> @should</code> can be used in field documentation and true
	 *         otherwise.
	 */
	public boolean inField() {
		return false;
	}
	
	/**
	 * Will return true since <code> @should</code> can be used in constructor documentation.
	 * 
	 * @return true since <code> @should</code> can be used in constructor documentation and false
	 *         otherwise.
	 */
	public boolean inConstructor() {
		return true;
	}
	
	/**
	 * Will return true since <code> @should</code> can be used in method documentation.
	 * 
	 * @return true since <code> @should</code> can be used in method documentation and false
	 *         otherwise.
	 */
	public boolean inMethod() {
		return true;
	}
	
	/**
	 * Will return true since <code> @should</code> can be used in method documentation.
	 * 
	 * @return true since <code> @should</code> can be used in overview documentation and false
	 *         otherwise.
	 */
	public boolean inOverview() {
		return true;
	}
	
	/**
	 * Will return false since <code> @should</code> cannot be used in package documentation.
	 * 
	 * @return false since <code> @should</code> cannot be used in package documentation and true
	 *         otherwise.
	 */
	public boolean inPackage() {
		return false;
	}
	
	/**
	 * Will return true since <code> @should</code> can be used in type documentation (classes or
	 * interfaces).
	 * 
	 * @return true since <code> @should</code> can be used in type documentation and false
	 *         otherwise.
	 */
	public boolean inType() {
		return true;
	}
	
	/**
	 * Will return false since <code> @should</code> is not an inline tag.
	 * 
	 * @return false since <code> @should</code> is not an inline tag.
	 */
	
	public boolean isInlineTag() {
		return false;
	}
	
	/**
	 * Register this Taglet.
	 * 
	 * @param tagletMap the map to register this tag to.
	 */
	public static void register(Map tagletMap) {
		ShouldTaglet tag = new ShouldTaglet();
		Taglet t = (Taglet) tagletMap.get(tag.getName());
		if (t != null) {
			tagletMap.remove(tag.getName());
		}
		tagletMap.put(tag.getName(), tag);
	}
	
	/**
	 * Given the <code>Tag</code> representation of this custom tag, return its string
	 * representation.
	 * 
	 * @param tag the <code>Tag</code> representation of this custom tag.
	 */
	public String toString(Tag tag) {
		return "\n<DT><B>" + HEADER + "</B></DT>\n  <DD>Should " + tag.text() + "</DD>";
	}
	
	/**
	 * Given an array of <code>Tag</code>s representing this custom tag, return its string
	 * representation.
	 * 
	 * @param tags the array of <code>Tag</code>s representing of this custom tag.
	 */
	public String toString(Tag[] tags) {
		if (tags.length == 0) {
			return null;
		}
		String result = "\n<DT><B>" + HEADER + "</B></DT>";
		for (int i = 0; i < tags.length; i++) {
			result += "\n  <DD>Should " + tags[i].text() + "</DD>";
		}
		return result;
	}
}
