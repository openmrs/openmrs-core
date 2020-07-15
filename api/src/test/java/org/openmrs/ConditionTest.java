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
 * This class tests all methods that are not getter or setters in the Condition java object TODO: finish
 * this test class for Condition
 * 
 * @see Condition
 */
public class ConditionTest {
		
	private static final String FORM_NAMESPACE_PATH_SEPARATOR = "^";
	
	/**
	 * @see Condition#setFormField(String,String)
	 */
	@Test
	public void setFormField_shouldSetTheUnderlyingFormNamespaceAndPathInTheCorrectPattern() throws Exception {
		final String ns = "my ns";
		final String path = "my path";
		Condition condition = new Condition();
		condition.setFormField(ns, path);
		Field formNamespaceAndPathProperty = BaseFormRecordableOpenmrsData.class.getDeclaredField("formNamespaceAndPath");
		formNamespaceAndPathProperty.setAccessible(true);
		assertEquals(ns + FORM_NAMESPACE_PATH_SEPARATOR + path, formNamespaceAndPathProperty.get(condition));
	}
	
	/**
	 * @see Condition#getFormFieldNamespace()
	 */
	@Test
	public void getFormFieldNamespace_shouldReturnNullIfTheNamespaceIsNotSpecified() throws Exception {
		Condition condition = new Condition();
		condition.setFormField("", "my path");
		assertNull(condition.getFormFieldNamespace());
	}
	
	/**
	 * @see Condition#getFormFieldNamespace()
	 */
	@Test
	public void getFormFieldNamespace_shouldReturnTheCorrectNamespaceForAFormFieldWithAPath() throws Exception {
		final String ns = "my ns";
		final String path = "my path";
		Condition condition = new Condition();
		condition.setFormField(ns, path);
		assertEquals(ns, condition.getFormFieldNamespace());
	}
	
	/**
	 * @see Condition#getFormFieldNamespace()
	 */
	@Test
	public void getFormFieldNamespace_shouldReturnTheNamespaceForAFormFieldThatHasNoPath() throws Exception {
		final String ns = "my ns";
		Condition condition = new Condition();
		condition.setFormField(ns, null);
		assertEquals(ns, condition.getFormFieldNamespace());
	}
	
	/**
	 * @see Condition#getFormFieldPath()
	 */
	@Test
	public void getFormFieldPath_shouldReturnNullIfThePathIsNotSpecified() throws Exception {
		Condition condition = new Condition();
		condition.setFormField("my ns", "");
		assertNull(condition.getFormFieldPath());
	}
	
	/**
	 * @see Condition#getFormFieldPath()
	 */
	@Test
	public void getFormFieldPath_shouldReturnTheCorrectPathForAFormFieldWithANamespace() throws Exception {
		final String ns = "my ns";
		final String path = "my path";
		Condition condition = new Condition();
		condition.setFormField(ns, path);
		assertEquals(path, condition.getFormFieldPath());
	}
	
	/**
	 * @see Condition#getFormFieldPath()
	 */
	@Test
	public void getFormFieldPath_shouldReturnThePathForAFormFieldThatHasNoNamespace() throws Exception {
		final String path = "my path";
		Condition condition = new Condition();
		condition.setFormField("", path);
		assertEquals(path, condition.getFormFieldPath());
	}
	
	/**
	 * @see Condition#setFormField(String,String)
	 */
	@Test
	public void setFormField_shouldRejectANamepaceAndPathCombinationLongerThanTheMaxLength() throws Exception {
		
		final String ns = StringUtils.repeat("x", 255);
		final String path = "";
		Condition condition = new Condition();
		assertThrows(APIException.class, () -> condition.setFormField(ns, path));
	}
	
	/**
	 * @see Condition#setFormField(String,String)
	 */
	@Test
	public void setFormField_shouldRejectANamepaceContainingTheSeparator() throws Exception {
		final String ns = "my ns" + FORM_NAMESPACE_PATH_SEPARATOR;
		Condition condition = new Condition();
		assertThrows(APIException.class, () -> condition.setFormField(ns, ""));
	}
	
	/**
	 * @see Condition#setFormField(String,String)
	 */
	@Test
	public void setFormField_shouldRejectAPathContainingTheSeparator() throws Exception {
		final String path = FORM_NAMESPACE_PATH_SEPARATOR + "my path";
		Condition condition = new Condition();
		assertThrows(APIException.class, () -> condition.setFormField("", path));
	}
}
