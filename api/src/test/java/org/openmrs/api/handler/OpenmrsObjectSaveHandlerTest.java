/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.handler;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.OpenmrsObject;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.annotation.AllowEmptyStrings;
import org.openmrs.annotation.AllowLeadingOrTrailingWhitespace;

/**
 * Tests for {@link OpenmrsObjectSaveHandler}
 */
public class OpenmrsObjectSaveHandlerTest {
	
	/**
	 * @see OpenmrsObjectSaveHandler#handle(OpenmrsObject,User,Date,String)
	 */
	@Test
	public void handle_shouldSetEmptyStringPropertiesToNull() {
		Role role = new Role();
		role.setName("");
		role.setDescription("");
		role.setRole("");
		
		new OpenmrsObjectSaveHandler().handle(role, null, null, null);
		
		Assert.assertNull(role.getName());
		Assert.assertNull(role.getDescription());
		Assert.assertNull(role.getRole());
	}
	
	/**
	 * @see OpenmrsObjectSaveHandler#handle(OpenmrsObject,User,Date,String)
	 */
	@Test
	public void handle_shouldNotSetEmptyStringPropertiesToNullForAllowEmptyStringsAnnotation() {
		SomeClass obj = new SomeClass("");
		new OpenmrsObjectSaveHandler().handle(obj, null, null, null);
		Assert.assertNotNull(obj.getName());
	}
	
	/**
	 * @see OpenmrsObjectSaveHandler#handle(OpenmrsObject,User,Date,String)
	 */
	@Test
	public void handle_shouldNotTrimEmptyStringsForAllowLeadingOrTrailingWhitespaceAnnotation() {
		SomeClass obj = new SomeClass(null, " ");
		new OpenmrsObjectSaveHandler().handle(obj, null, null, null);
		Assert.assertNotNull(obj.getDescription());
	}
	
	/**
	 * @see OpenmrsObjectSaveHandler#handle(OpenmrsObject,User,Date,String)
	 */
	@Test
	public void handle_shouldTrimStringsWithoutAllowLeadingOrTrailingWhitespaceAnnotation() {
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setCode(" code ");
		term.setConceptSource(new ConceptSource(1));
		new OpenmrsObjectSaveHandler().handle(term, null, null, null);
		Assert.assertEquals("code", term.getCode());
	}
	
	/**
	 * @see OpenmrsObjectSaveHandler#handle(OpenmrsObject,User,Date,String)
	 */
	@Test
	public void handle_shouldTrimEmptyStringsForAllowEmptyStringsAnnotation() {
		SomeClass obj = new SomeClass(" name ");
		new OpenmrsObjectSaveHandler().handle(obj, null, null, null);
		Assert.assertEquals("name", obj.getName());
	}
	
	public class SomeClass extends BaseOpenmrsObject {
		
		private Integer id;
		
		private String name;
		
		private String description;
		
		public SomeClass(String name) {
			setName(name);
		}
		
		public SomeClass(String name, String description) {
			setName(name);
			setDescription(description);
		}
		
		public String getName() {
			return name;
		}
		
		@AllowEmptyStrings
		public void setName(String name) {
			this.name = name;
		}
		
		public String getDescription() {
			return description;
		}
		
		@AllowLeadingOrTrailingWhitespace
		public void setDescription(String description) {
			this.description = description;
		}
		
		@Override
		public void setId(Integer id) {
			this.id = id;
		}
		
		@Override
		public Integer getId() {
			return id;
		}
	}
}
