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

import liquibase.util.StringUtils;
import org.junit.jupiter.api.Test;
import org.openmrs.api.APIException;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests all methods that are not getter or setters in the BaseFormRecordableOpenmrsDataImpl java object this test class for BaseFormRecordableOpenmrsDataImpl
 *
 * @see BaseFormRecordableOpenmrsDataImpl
 */
public class BaseFormRecordableOpenmrsDataTest {

	private static final String FORM_NAMESPACE_PATH_SEPARATOR = "^";

	/**
	 * @see BaseFormRecordableOpenmrsDataImpl#setFormField(String,String)
	 */
	@Test
	public void setFormField_shouldSetTheUnderlyingFormNamespaceAndPathInTheCorrectPattern() throws Exception {
		final String ns = "my ns";
		final String path = "my path";
		BaseFormRecordableOpenmrsDataImpl impl = new BaseFormRecordableOpenmrsDataImpl();
		impl.setFormField(ns, path);
		Field formNamespaceAndPathProperty = BaseFormRecordableOpenmrsData.class.getDeclaredField("formNamespaceAndPath");
		formNamespaceAndPathProperty.setAccessible(true);
		assertEquals(ns + FORM_NAMESPACE_PATH_SEPARATOR + path, formNamespaceAndPathProperty.get(impl));
	}

	/**
	 * @see BaseFormRecordableOpenmrsDataImpl#getFormFieldNamespace()
	 */
	@Test
	public void getFormFieldNamespace_shouldReturnNullIfTheNamespaceIsNotSpecified() throws Exception {
		BaseFormRecordableOpenmrsDataImpl impl = new BaseFormRecordableOpenmrsDataImpl();
		impl.setFormField("", "my path");
		assertNull(impl.getFormFieldNamespace());
	}

	/**
	 * @see BaseFormRecordableOpenmrsDataImpl#getFormFieldNamespace()
	 */
	@Test
	public void getFormFieldNamespace_shouldReturnTheCorrectNamespaceForAFormFieldWithAPath() throws Exception {
		final String ns = "my ns";
		final String path = "my path";
		BaseFormRecordableOpenmrsDataImpl impl = new BaseFormRecordableOpenmrsDataImpl();
		impl.setFormField(ns, path);
		assertEquals(ns, impl.getFormFieldNamespace());
	}

	/**
	 * @see BaseFormRecordableOpenmrsDataImpl#getFormFieldNamespace()
	 */
	@Test
	public void getFormFieldNamespace_shouldReturnTheNamespaceForAFormFieldThatHasNoPath() throws Exception {
		final String ns = "my ns";
		BaseFormRecordableOpenmrsDataImpl impl = new BaseFormRecordableOpenmrsDataImpl();
		impl.setFormField(ns, null);
		assertEquals(ns, impl.getFormFieldNamespace());
	}

	/**
	 * @see BaseFormRecordableOpenmrsDataImpl#getFormFieldPath()
	 */
	@Test
	public void getFormFieldPath_shouldReturnNullIfThePathIsNotSpecified() throws Exception {
		BaseFormRecordableOpenmrsDataImpl impl = new BaseFormRecordableOpenmrsDataImpl();
		impl.setFormField("my ns", "");
		assertNull(impl.getFormFieldPath());
	}

	/**
	 * @see BaseFormRecordableOpenmrsDataImpl#getFormFieldPath()
	 */
	@Test
	public void getFormFieldPath_shouldReturnTheCorrectPathForAFormFieldWithANamespace() throws Exception {
		final String ns = "my ns";
		final String path = "my path";
		BaseFormRecordableOpenmrsDataImpl impl = new BaseFormRecordableOpenmrsDataImpl();
		impl.setFormField(ns, path);
		assertEquals(path, impl.getFormFieldPath());
	}

	/**
	 * @see BaseFormRecordableOpenmrsDataImpl#getFormFieldPath()
	 */
	@Test
	public void getFormFieldPath_shouldReturnThePathForAFormFieldThatHasNoNamespace() throws Exception {
		final String path = "my path";
		BaseFormRecordableOpenmrsDataImpl impl = new BaseFormRecordableOpenmrsDataImpl();
		impl.setFormField("", path);
		assertEquals(path, impl.getFormFieldPath());
	}

	/**
	 * @see BaseFormRecordableOpenmrsDataImpl#setFormField(String,String)
	 */
	@Test
	public void setFormField_shouldRejectANamepaceAndPathCombinationLongerThanTheMaxLength() throws Exception {

		final String ns = StringUtils.repeat("x", 255);
		final String path = "";
		BaseFormRecordableOpenmrsDataImpl impl = new BaseFormRecordableOpenmrsDataImpl();
		assertThrows(APIException.class, () -> impl.setFormField(ns, path));
	}

	/**
	 * @see BaseFormRecordableOpenmrsDataImpl#setFormField(String,String)
	 */
	@Test
	public void setFormField_shouldRejectANamepaceContainingTheSeparator() throws Exception {
		final String ns = "my ns" + FORM_NAMESPACE_PATH_SEPARATOR;
		BaseFormRecordableOpenmrsDataImpl impl = new BaseFormRecordableOpenmrsDataImpl();
		assertThrows(APIException.class, () -> impl.setFormField(ns, ""));
	}

	/**
	 * @see BaseFormRecordableOpenmrsDataImpl#setFormField(String,String)
	 */
	@Test
	public void setFormField_shouldRejectAPathContainingTheSeparator() throws Exception {
		final String path = FORM_NAMESPACE_PATH_SEPARATOR + "my path";
		BaseFormRecordableOpenmrsDataImpl impl = new BaseFormRecordableOpenmrsDataImpl();
		assertThrows(APIException.class, () -> impl.setFormField("", path));
	}

	public class BaseFormRecordableOpenmrsDataImpl extends BaseFormRecordableOpenmrsData {

		@Override
		protected void markAsDirty(Object oldValue, Object newValue){
			// Do Nothing
		}

		@Override
		public Integer getId() {
			return 1;
		}

		@Override
		public void setId(Integer id) {

		}
	}
}
