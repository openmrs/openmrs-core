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
	
}
