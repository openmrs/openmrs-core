/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.customdatatype;

/**
 * A metadata item describing how a type of custom value is stored. (For example a GlobalProperty is its own
 * {@link CustomValueDescriptor}, and a VisitAttributeTypes is the descriptor for VisitAttribute.)
 * @since 1.9
 */
public interface CustomValueDescriptor {
	
	/**
	 * Return the name of a class that implements {@link CustomDatatype}.
	 * @return the datatype used to store this custom value
	 * @see CustomDatatype
	 */
	String getDatatypeClassname();
	
	/**
	 * May be null.
	 * @return the configuration to be passed to the datatype. For example if the datatype is RegexValidatedString the datatypeConfig would be the regular expression
	 * @see CustomDatatype#setConfiguration(String)
	 */
	String getDatatypeConfig();
	
	/**
	 * May be null.
	 * @return the fully-qualified classname of the preferred {@link CustomDatatypeHandler} chosen by the
	 * system administrator for this attribute type.
	 */
	String getPreferredHandlerClassname();
	
	/**
	 * May be null.
	 * @return the configuration to be passed to the handler for a datatype. For example if the
	 * datatype is "regex-validated-string", the handlerConfig would be the regular expression.
	 * @see CustomDatatypeHandler#setHandlerConfiguration(String)
	 */
	String getHandlerConfig();
	
}
