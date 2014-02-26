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
