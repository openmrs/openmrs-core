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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import liquibase.util.StringUtil;
import org.junit.jupiter.api.Test;
import org.openmrs.api.APIException;

import java.lang.reflect.Field;


/**
 * This class tests all methods that are not getter or setters in the BaseFormRecordableOpenmrsDataImpl java object this test class for BaseFormRecordableOpenmrsDataImpl
 *
 * @see BaseFormRecordableOpenmrsData
 */
public class BaseFormRecordableOpenmrsDataTest {

	private static final String FORM_NAMESPACE_PATH_SEPARATOR = "^";

	/**
	 * @see BaseFormRecordableOpenmrsData#setFormField(String,String)
	 */
	@Test
	public void setFormField_shouldSetTheUnderlyingFormNamespaceAndPathInTheCorrectPattern() throws Exception {
		final String ns = "my ns";
		final String path = "my path";
		Obs impl = new Obs();
		impl.setFormField(ns, path);
		Field formNamespaceAndPathProperty = BaseFormRecordableOpenmrsData.class.getDeclaredField("formNamespaceAndPath");
		formNamespaceAndPathProperty.setAccessible(true);
		assertEquals(ns + FORM_NAMESPACE_PATH_SEPARATOR + path, formNamespaceAndPathProperty.get(impl));
	}

	/**
	 * @see BaseFormRecordableOpenmrsData#getFormFieldNamespace()
	 */
	@Test
	public void getFormFieldNamespace_shouldReturnNullIfTheNamespaceIsNotSpecified() throws Exception {
		Obs impl = new Obs();
		impl.setFormField("", "my path");
		assertNull(impl.getFormFieldNamespace());
	}

	/**
	 * @see BaseFormRecordableOpenmrsData#getFormFieldNamespace()
	 */
	@Test
	public void getFormFieldNamespace_shouldReturnTheCorrectNamespaceForAFormFieldWithAPath() throws Exception {
		final String ns = "my ns";
		final String path = "my path";
		Obs impl = new Obs();
		impl.setFormField(ns, path);
		assertEquals(ns, impl.getFormFieldNamespace());
	}

	/**
	 * @see BaseFormRecordableOpenmrsData#getFormFieldNamespace()
	 */
	@Test
	public void getFormFieldNamespace_shouldReturnTheNamespaceForAFormFieldThatHasNoPath() throws Exception {
		final String ns = "my ns";
		Obs impl = new Obs();
		impl.setFormField(ns, null);
		assertEquals(ns, impl.getFormFieldNamespace());
	}

	/**
	 * @see BaseFormRecordableOpenmrsData#getFormFieldPath()
	 */
	@Test
	public void getFormFieldPath_shouldReturnNullIfThePathIsNotSpecified() throws Exception {
		Obs impl = new Obs();
		impl.setFormField("my ns", "");
		assertNull(impl.getFormFieldPath());
	}

	/**
	 * @see BaseFormRecordableOpenmrsData#getFormFieldPath()
	 */
	@Test
	public void getFormFieldPath_shouldReturnTheCorrectPathForAFormFieldWithANamespace() throws Exception {
		final String ns = "my ns";
		final String path = "my path";
		Obs impl = new Obs();
		impl.setFormField(ns, path);
		assertEquals(path, impl.getFormFieldPath());
	}

	/**
	 * @see BaseFormRecordableOpenmrsData#getFormFieldPath()
	 */
	@Test
	public void getFormFieldPath_shouldReturnThePathForAFormFieldThatHasNoNamespace() throws Exception {
		final String path = "my path";
		Obs impl = new Obs();
		impl.setFormField("", path);
		assertEquals(path, impl.getFormFieldPath());
	}

	/**
	 * @see BaseFormRecordableOpenmrsData#setFormField(String,String)
	 */
	@Test
	public void setFormField_shouldRejectANamepaceAndPathCombinationLongerThanTheMaxLength() throws Exception {
		final String ns = StringUtil.repeat("x", 255);
		final String path = "";
		
		Obs impl = new Obs();
		assertThrows(APIException.class, () -> impl.setFormField(ns, path));
	}

	/**
	 * @see BaseFormRecordableOpenmrsData#setFormField(String,String)
	 */
	@Test
	public void setFormField_shouldRejectANamepaceContainingTheSeparator() throws Exception {
		final String ns = "my ns" + FORM_NAMESPACE_PATH_SEPARATOR;
		Obs impl = new Obs();
		assertThrows(APIException.class, () -> impl.setFormField(ns, ""));
	}

	/**
	 * @see BaseFormRecordableOpenmrsData#setFormField(String,String)
	 */
	@Test
	public void setFormField_shouldRejectAPathContainingTheSeparator() throws Exception {
		final String path = FORM_NAMESPACE_PATH_SEPARATOR + "my path";
		Obs impl = new Obs();
		assertThrows(APIException.class, () -> impl.setFormField("", path));
	}
}
