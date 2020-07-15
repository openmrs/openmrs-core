/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

/**
 * Defines the methods to make a bridge between an OpenMrsObject and is position in the form.
 *
 * @since  2.4.0
 */
public interface FormRecordable {
	
	/**
	 * Gets the namespace for the form field that was used to capture the obs details in the form
	 *
	 * @return the namespace
	 * @since 2.4.0
	 * <strong>Should</strong> return the namespace for a form field that has no path
	 * <strong>Should</strong> return the correct namespace for a form field with a path
	 * <strong>Should</strong> return null if the namespace is not specified
	 */
	String getFormFieldNamespace();
	
	/**
	 * Gets the path for the form field that was used to capture the obs details in the form
	 *
	 * @return the the form field path
	 * @since 2.4.0
	 * <strong>Should</strong> return the path for a form field that has no namespace
	 * <strong>Should</strong> return the correct path for a form field with a namespace
	 * <strong>Should</strong> return null if the path is not specified
	 */
	String getFormFieldPath();
	
	/**
	 * Sets the namespace and path of the form field that was used to capture the obs details in the
	 * form.<br>
	 * <b>Note:</b> Namespace and formFieldPath together must not exceed 254 characters in length,
	 * form applications can subtract the length of their namespace from 254 to determine the
	 * maximum length they can use for a form field path.
	 *
	 * @param namespace     the namespace of the form field
	 * @param formFieldPath the path of the form field
	 * @since 2.4.0
	 * <strong>Should</strong> set the underlying formNamespaceAndPath in the correct pattern
	 * <strong>Should</strong> reject a namepace containing the separator
	 * <strong>Should</strong> reject a path containing the separator
	 * <strong>Should</strong> reject a namepace and path combination longer than the max length
	 * <strong>Should</strong> not mark the obs as dirty when the value has not been changed
	 * <strong>Should</strong> mark the obs as dirty when the value has been changed
	 * <strong>Should</strong> mark the obs as dirty when the value is changed from a null to a non null value
	 * <strong>Should</strong> mark the obs as dirty when the value is changed from a non null to a null value
	 */
	void setFormField(String namespace, String formFieldPath);
}
