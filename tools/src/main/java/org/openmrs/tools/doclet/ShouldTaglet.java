/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.tools.doclet;

import com.sun.tools.doclets.Taglet;
import com.sun.javadoc.Tag;
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
		StringBuilder result = new StringBuilder("\n<DT><B>").append(HEADER).append("</B></DT>");
		for (int i = 0; i < tags.length; i++) {
			result.append("\n  <DD>Should ").append(tags[i].text()).append("</DD>");
		}
		return result.toString();
	}
}
