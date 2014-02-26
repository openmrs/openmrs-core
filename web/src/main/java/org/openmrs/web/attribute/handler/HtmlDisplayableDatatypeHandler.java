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
package org.openmrs.web.attribute.handler;

import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeHandler;

/**
 * A CustomDatatypeHandler that is capable of producing HTML output for display
 */
public interface HtmlDisplayableDatatypeHandler<T> extends CustomDatatypeHandler<CustomDatatype<T>, T> {
	
	/**
	 * Renders an HTML-formatted summary view of the custom value, that does not take a lot of space. (This is
	 * subjective, but generally means < 100 characters in length, or an image of < 200 pixels in each dimension.)
	 * This method should return quickly, e.g. in case we're rendering thousands of custom values in a table.
	 * 
	 * @param datatype
	 * @param valueReference
	 * @return an HTML-formatted summary view of valueReference
	 */
	CustomDatatype.Summary toHtmlSummary(CustomDatatype<T> datatype, String valueReference);
	
	/**
	 * Renders the full view of a custom value, as HTML.
	 * 
	 * @param datatype
	 * @param valueReference
	 * @return an HTML-formatted full view of valueReference
	 */
	String toHtml(CustomDatatype<T> datatype, String valueReference);
	
}
