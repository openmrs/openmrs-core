/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIException;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * This is a super class to make a bridge between an OpenMrsObject and is position in the form.
 */
@MappedSuperclass
public abstract class FormRecordable extends BaseChangeableOpenmrsData{
	
	private static final String FORM_NAMESPACE_PATH_SEPARATOR = "^";

	private static final int FORM_NAMESPACE_PATH_MAX_LENGTH = 255;
	
	@Column(name = "form_namespace_path")
	protected String formNamespaceAndPath;


	/**
	 * Gets the namespace for the form field that was used to capture the obs details in the form
	 *
	 * @return the namespace
	 * @since 1.11
	 * <strong>Should</strong> return the namespace for a form field that has no path
	 * <strong>Should</strong> return the correct namespace for a form field with a path
	 * <strong>Should</strong> return null if the namespace is not specified
	 */
	public String getFormFieldNamespace() {
		if (StringUtils.isNotBlank(formNamespaceAndPath)) {
			//Only the path was specified
			if (formNamespaceAndPath.startsWith(FORM_NAMESPACE_PATH_SEPARATOR)) {
				return null;
			}
			return formNamespaceAndPath.substring(0, formNamespaceAndPath.indexOf(FORM_NAMESPACE_PATH_SEPARATOR));
		}

		return formNamespaceAndPath;
	}

	/**
	 * Gets the path for the form field that was used to capture the obs details in the form
	 *
	 * @return the the form field path
	 * @since 1.11
	 * <strong>Should</strong> return the path for a form field that has no namespace
	 * <strong>Should</strong> return the correct path for a form field with a namespace
	 * <strong>Should</strong> return null if the path is not specified
	 */
	public String getFormFieldPath() {
		if (StringUtils.isNotBlank(formNamespaceAndPath)) {
			//Only the namespace was specified
			if (formNamespaceAndPath.endsWith(FORM_NAMESPACE_PATH_SEPARATOR)) {
				return null;
			}
			return formNamespaceAndPath.substring(formNamespaceAndPath.indexOf(FORM_NAMESPACE_PATH_SEPARATOR) + 1);
		}

		return formNamespaceAndPath;
	}

	/**
	 * Sets the namespace and path of the form field that was used to capture the obs details in the
	 * form.<br>
	 * <b>Note:</b> Namespace and formFieldPath together must not exceed 254 characters in length,
	 * form applications can subtract the length of their namespace from 254 to determine the
	 * maximum length they can use for a form field path.
	 *
	 * @param namespace the namespace of the form field
	 * @param formFieldPath the path of the form field
	 * @since 1.11
	 * <strong>Should</strong> set the underlying formNamespaceAndPath in the correct pattern
	 * <strong>Should</strong> reject a namepace containing the separator
	 * <strong>Should</strong> reject a path containing the separator
	 * <strong>Should</strong> reject a namepace and path combination longer than the max length
	 * <strong>Should</strong> not mark the obs as dirty when the value has not been changed
	 * <strong>Should</strong> mark the obs as dirty when the value has been changed
	 * <strong>Should</strong> mark the obs as dirty when the value is changed from a null to a non null value
	 * <strong>Should</strong> mark the obs as dirty when the value is changed from a non null to a null value
	 */
	public void setFormField(String namespace, String formFieldPath) {
		if (namespace == null && formFieldPath == null) {
			markAsDirty(formNamespaceAndPath, null);
			formNamespaceAndPath = null;
			return;
		}

		String nsAndPathTemp = "";
		if (StringUtils.isNotBlank(namespace) && StringUtils.isNotBlank(formFieldPath)) {
			nsAndPathTemp = namespace + FORM_NAMESPACE_PATH_SEPARATOR + formFieldPath;
		} else if (StringUtils.isNotBlank(namespace)) {
			nsAndPathTemp = namespace + FORM_NAMESPACE_PATH_SEPARATOR;
		} else if (StringUtils.isNotBlank(formFieldPath)) {
			nsAndPathTemp = FORM_NAMESPACE_PATH_SEPARATOR + formFieldPath;
		}

		if (nsAndPathTemp.length() > FORM_NAMESPACE_PATH_MAX_LENGTH) {
			throw new APIException("Obs.namespaceAndPathTooLong", (Object[]) null);
		}
		if (StringUtils.countMatches(nsAndPathTemp, FORM_NAMESPACE_PATH_SEPARATOR) > 1) {
			throw new APIException("Obs.namespaceAndPathNotContainSeparator", (Object[]) null);
		}

		markAsDirty(this.formNamespaceAndPath, nsAndPathTemp);
		formNamespaceAndPath = nsAndPathTemp;
	}	
		
	protected abstract void markAsDirty(Object oldValue, Object newValue);
}
