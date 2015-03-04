/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
