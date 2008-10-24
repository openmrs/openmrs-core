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
package org.openmrs;

import org.junit.Assert;
import org.junit.Test;

/**
 * This class tests the all of the {@link Form} non-trivial object methods.
 * 
 * @see Form
 */
public class FormTest {
	
	/**
	 * Make sure the Form(Integer) constructor sets the formId
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldSetFormIdFromConstructor() throws Exception {
		Form form = new Form(123);
		Assert.assertEquals(123, form.getFormId().intValue());
	}
	
	/**
	 * Makes sure that two different form objects that have the same 
	 * form id are considered equal 
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldHaveEqualFormObjectsByFormId() throws Exception {
		Form form1 = new Form(1);
		// another form with the same form id
		Form form2 = new Form(1);
		
		Assert.assertTrue(form1.equals(form2));
	}
	
	/**
	 * Makes sure that two different form objects that have different
	 * form ids are considered unequal
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotHaveEqualFormObjectsByFormId() throws Exception {
		Form form1 = new Form(1);
		// another form with a different form id
		Form form2 = new Form(2);
		
		Assert.assertFalse(form1.equals(form2));
	}
	
	/**
	 * Makes sure that two different form objects that have the same 
	 * form id are considered equal (checks for NPEs)
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldHaveEqualFormObjectsWithNoFormId() throws Exception {
		// an form object with no form id
		Form form = new Form();
		
		Assert.assertTrue(form.equals(form));
	}
	
	/**
	 * Makes sure that two different form objects are unequal when
	 * one of them doesn't have an form id defined (checks for NPEs)
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotHaveEqualFormObjectsWhenOneHasNullFormId() throws Exception {
		Form formWithId = new Form(1);
		// another form that doesn't have an form id
		Form formWithoutId = new Form();
		
		Assert.assertFalse(formWithId.equals(formWithoutId));
		
		// now test the reverse
		Assert.assertFalse(formWithoutId.equals(formWithId));
		
		Form anotherFormWithoutId = new Form();
		// now test with both not having an id
		Assert.assertFalse(formWithoutId.equals(anotherFormWithoutId));
	}
	
	/**
	 * Make sure we can call {@link Form#hashCode()} with all null
	 * attributes on form and still get a hashcode
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetHashCodeWithNullAttributes() throws Exception {
		new Form().hashCode();
	}
		
}
