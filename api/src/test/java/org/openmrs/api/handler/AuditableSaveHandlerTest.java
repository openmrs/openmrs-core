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
package org.openmrs.api.handler;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Auditable;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.User;
import org.openmrs.test.Verifies;

/**
 * Tests for the {@link AuditableSaveHandler} class.
 */
public class AuditableSaveHandlerTest {
	
	/**
	 * @see {@link CreateableSaveHandler#handle(Auditable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should set creator if null", method = "handle(Auditable,User,Date,String)")
	public void handle_shouldSetCreatorIfNull() throws Exception {
		AuditableSaveHandler handler = new AuditableSaveHandler();
		Auditable auditable = new ConceptName(); // a createable with a null creator
		handler.handle(auditable, new User(2), null, null);
		Assert.assertEquals(new User(2), auditable.getCreator());
	}
	
	/**
	 * @see {@link AuditableSaveHandler#handle(Auditable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not set creator if non null", method = "handle(Auditable,User,Date,String)")
	public void handle_shouldNotSetCreatorIfNonNull() throws Exception {
		AuditableSaveHandler handler = new AuditableSaveHandler();
		Auditable auditable = new ConceptName();
		auditable.setCreator(new User(3)); // a createable with a nonnull creator
		handler.handle(auditable, new User(2), null, null);
		Assert.assertEquals(new User(3), auditable.getCreator());
	}
	
	/**
	 * @see {@link AuditableSaveHandler#handle(Auditable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should set dateCreated if null", method = "handle(Auditable,User,Date,String)")
	public void handle_shouldSetDateCreatedIfNull() throws Exception {
		Date d = new Date(new Date().getTime() - 1000); // a time that isn't "now"
		
		AuditableSaveHandler handler = new AuditableSaveHandler();
		Auditable auditable = new ConceptName(); // a createable with a null dateCreated
		handler.handle(auditable, null, d, null);
		Assert.assertEquals(d, auditable.getDateCreated());
	}
	
	/**
	 * @see {@link AuditableSaveHandler#handle(Auditable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not set dateCreated if non null", method = "handle(Auditable,User,Date,String)")
	public void handle_shouldNotSetDateCreatedIfNonNull() throws Exception {
		Date d = new Date(new Date().getTime() - 1000); // a time that isn't "now"
		
		AuditableSaveHandler handler = new AuditableSaveHandler();
		Auditable auditable = new ConceptName(); // a createable with a null dateCreated
		auditable.setDateCreated(d);
		
		handler.handle(auditable, null, new Date(), null);
		Assert.assertEquals(d, auditable.getDateCreated());
	}
	
	/**
	 * @see {@link AuditableSaveHandler#handle(Auditable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should set changedBy if id exists", method = "handle(Auditable,User,Date,String)")
	public void handle_shouldSetChangedByIfIdExists() throws Exception {
		Auditable auditable = new ConceptDatatype(5); // an auditable with a non-null id
		new AuditableSaveHandler().handle(auditable, new User(2), null, null);
		Assert.assertEquals(new User(2), auditable.getChangedBy());
	}
	
	/**
	 * @see {@link AuditableSaveHandler#handle(Auditable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should set dateChanged if id exists", method = "handle(Auditable,User,Date,String)")
	public void handle_shouldSetDateChangedIfIdExists() throws Exception {
		Date d = new Date(new Date().getTime() - 1000); // a time that isn't "now"
		
		Auditable auditable = new ConceptDatatype(5); // an auditable with a non-null id
		new AuditableSaveHandler().handle(auditable, null, d, null);
		Assert.assertEquals(d, auditable.getDateChanged());
	}
	
	/**
	 * @see {@link AuditableSaveHandler#handle(Auditable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not set dateChanged if id doesnt exist", method = "handle(Auditable,User,Date,String)")
	public void handle_shouldNotSetDateChangedIfIdDoesntExist() throws Exception {
		Auditable auditable = new ConceptDatatype(); // an auditable with a null id
		new AuditableSaveHandler().handle(auditable, null, new Date(), null);
		Assert.assertNull(auditable.getDateChanged());
	}
	
	/**
	 * @see {@link AuditableSaveHandler#handle(Auditable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not set changedBy if id doesnt exist", method = "handle(Auditable,User,Date,String)")
	public void handle_shouldNotSetChangedByIfIdDoesntExist() throws Exception {
		Auditable auditable = new ConceptDatatype(); // an auditable with a null id
		new AuditableSaveHandler().handle(auditable, new User(2), null, null);
		Assert.assertNull(auditable.getChangedBy());
	}
	
	/**
	 * A class to help test that if {@link #getId()} throws an {@link UnsupportedOperationException}
	 * , the changedBy and dateChanged get set anyway.
	 * 
	 * @see AuditableSaveHandlerTest#handle_shouldSetChangedByIfIdIsUnsupported()
	 * @see AuditableSaveHandlerTest#handle_shouldSetDateChangedIfIdIsUnsupported()
	 */
	private class AuditableWithUnsupportedId extends BaseOpenmrsMetadata {
		
		public Integer getId() {
			throw new UnsupportedOperationException();
		}
		
		public void setId(Integer id) {
		}
		
	}
	
	/**
	 * @see {@link AuditableSaveHandler#handle(Auditable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should set changedBy if id is unsupported", method = "handle(Auditable,User,Date,String)")
	public void handle_shouldSetChangedByIfIdIsUnsupported() throws Exception {
		AuditableWithUnsupportedId auditable = new AuditableWithUnsupportedId();
		
		new AuditableSaveHandler().handle(auditable, new User(2), null, null);
		Assert.assertEquals(new User(2), auditable.getChangedBy());
	}
	
	/**
	 * @see {@link AuditableSaveHandler#handle(Auditable,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should set dateChanged if id is unsupported", method = "handle(Auditable,User,Date,String)")
	public void handle_shouldSetDateChangedIfIdIsUnsupported() throws Exception {
		AuditableWithUnsupportedId auditable = new AuditableWithUnsupportedId();
		
		Date d = new Date(new Date().getTime() - 1000); // a time that isn't "now"
		
		new AuditableSaveHandler().handle(auditable, null, d, null);
		Assert.assertEquals(d, auditable.getDateChanged());
	}
}
